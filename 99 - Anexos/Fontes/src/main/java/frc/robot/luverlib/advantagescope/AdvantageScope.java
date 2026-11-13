package frc.robot.luverlib.advantagescope;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.PubSubOption;
import edu.wpi.first.networktables.Publisher;
import edu.wpi.first.util.WPIUtilJNI;
import edu.wpi.first.util.struct.Struct;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj2.command.Subsystem;
import frc.robot.luverlib.motores.neo.MotorNeo;
import frc.robot.luverlib.motores.kraken.MotorKraken;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Saída NT4 reconhecida pelo AdvantageScope, sem alterar o ciclo TimedRobot ou exigir AdvantageKit.
 * Registre os fornecedores uma vez. O CommandScheduler existente atualiza as leituras, inclusive
 * desabilitado. Em projetos sem scheduler, desative o automático e chame atualizar no ciclo do robô.
 *
 * <p>Geometria: metros, radianos, origem azul fixa; velocidades do chassi relativas ao robô.
 * Criação, registro e encerramento devem acontecer na thread principal, fora de periodic/callbacks.
 */
public final class AdvantageScope implements Subsystem, AutoCloseable {
  private final NetworkTableInstance rede;
  private final NetworkTable tabela;
  private final ConfiguracaoAdvantageScope configuracao;
  private final PubSubOption[] opcoes;
  private final LongSupplier relogio;
  private final Map<String, CanalAdvantageScope<?>> canais = new LinkedHashMap<>();
  private long proximaAtualizacao;
  private boolean fechado;

  public AdvantageScope() {
    this(NetworkTableInstance.getDefault(), VariaveisAdvantageScope.configuracaoPadrao());
  }

  public AdvantageScope(ConfiguracaoAdvantageScope configuracao) {
    this(NetworkTableInstance.getDefault(), configuracao);
  }

  /** A instância NT é emprestada: close não a encerra nem muda seu servidor/endereço. */
  public AdvantageScope(NetworkTableInstance rede, ConfiguracaoAdvantageScope configuracao) {
    this(rede, configuracao, WPIUtilJNI::now);
  }

  AdvantageScope(NetworkTableInstance rede, ConfiguracaoAdvantageScope configuracao,
      LongSupplier relogio) {
    this.rede = Objects.requireNonNull(rede);
    this.configuracao = Objects.requireNonNull(configuracao);
    this.relogio = Objects.requireNonNull(relogio);
    tabela = rede.getTable(configuracao.raizTabela());
    opcoes = new PubSubOption[] {
      PubSubOption.periodic(configuracao.intervaloSegundos()),
      PubSubOption.sendAll(true), PubSubOption.keepDuplicates(true)
    };
    if (configuracao.atualizacaoAutomatica()) {
      CommandScheduler.getInstance().registerSubsystem(this);
    }
  }

  /** Números com unidade explícita reconhecida pelo gráfico, por exemplo V, A, m, rad/s ou rpm. */
  public CanalAdvantageScope<Double> registrarNumero(
      String nome, String unidade, DoubleSupplier fonte) {
    Objects.requireNonNull(fonte);
    Objects.requireNonNull(unidade);
    if (unidade.isBlank() || unidade.indexOf('"') >= 0 || unidade.indexOf('\\') >= 0
        || unidade.codePoints().anyMatch(Character::isISOControl)) {
      throw new IllegalArgumentException("Informe uma unidade simples, como V, A, m ou rad/s.");
    }
    conferirNome(nome);
    var publicador = tabela.getDoubleTopic(nome).publish(opcoes);
    publicador.getTopic().setProperty("unit", "\"" + unidade + "\"");
    var canal = adicionar(nome, publicador, publicador::set, Double::isFinite);
    canal.definirFonte(fonte::getAsDouble);
    return canal;
  }

  public CanalAdvantageScope<Boolean> registrarBooleano(String nome, BooleanSupplier fonte) {
    Objects.requireNonNull(fonte);
    conferirNome(nome);
    var publicador = tabela.getBooleanTopic(nome).publish(opcoes);
    CanalAdvantageScope<Boolean> canal = adicionar(nome, publicador, publicador::set, valor -> true);
    canal.definirFonte(fonte::getAsBoolean);
    return canal;
  }

  public CanalAdvantageScope<String> registrarTexto(String nome, Supplier<String> fonte) {
    Objects.requireNonNull(fonte);
    conferirNome(nome);
    var publicador = tabela.getStringTopic(nome).publish(opcoes);
    CanalAdvantageScope<String> canal = adicionar(nome, publicador, publicador::set, valor -> true);
    canal.definirFonte(fonte);
    return canal;
  }

  /** Canal de eventos, como pose atual/alvo de um callback do PathPlanner. */
  public <T> CanalAdvantageScope<T> criarEstrutura(String nome, Struct<T> tipo) {
    Objects.requireNonNull(tipo);
    conferirNome(nome);
    var publicador = tabela.getStructTopic(nome, tipo).publish(opcoes);
    return adicionar(nome, publicador, publicador::set, AdvantageScope::estruturaValida);
  }

  /** Arrays nativos: caminhos, alvos de visão ou componentes móveis de um modelo 3D. */
  public <T> CanalAdvantageScope<T[]> criarEstruturas(String nome, Struct<T> tipo) {
    return criarEstruturas(nome, tipo,
        valores -> Arrays.stream(valores).allMatch(AdvantageScope::estruturaValida));
  }

  private <T> CanalAdvantageScope<T[]> criarEstruturas(
      String nome, Struct<T> tipo, Predicate<T[]> validar) {
    Objects.requireNonNull(tipo);
    conferirNome(nome);
    var publicador = tabela.getStructArrayTopic(nome, tipo).publish(opcoes);
    return adicionar(nome, publicador, publicador::set, validar);
  }

  public <T> CanalAdvantageScope<T> registrarEstrutura(
      String nome, Struct<T> tipo, Supplier<T> fonte) {
    Objects.requireNonNull(fonte);
    var canal = criarEstrutura(nome, tipo);
    canal.definirFonte(fonte);
    return canal;
  }

  public <T> CanalAdvantageScope<T[]> registrarEstruturas(
      String nome, Struct<T> tipo, Supplier<T[]> fonte) {
    Objects.requireNonNull(fonte);
    var canal = criarEstruturas(nome, tipo);
    canal.definirFonte(fonte);
    return canal;
  }

  public CanalAdvantageScope<Pose2d> registrarPose2d(String nome, Supplier<Pose2d> fonte) {
    return registrarEstrutura(nome, Pose2d.struct, fonte);
  }

  public CanalAdvantageScope<Pose3d> registrarPose3d(String nome, Supplier<Pose3d> fonte) {
    return registrarEstrutura(nome, Pose3d.struct, fonte);
  }

  /** Array vazio remove o caminho/alvos anteriores da visualização; null indica falha de leitura. */
  public CanalAdvantageScope<Pose2d[]> registrarPoses2d(String nome, Supplier<Pose2d[]> fonte) {
    return registrarEstruturas(nome, Pose2d.struct, fonte);
  }

  public CanalAdvantageScope<Pose3d[]> registrarPoses3d(String nome, Supplier<Pose3d[]> fonte) {
    return registrarEstruturas(nome, Pose3d.struct, fonte);
  }

  public CanalAdvantageScope<Rotation2d> registrarRotacao(String nome, Supplier<Rotation2d> fonte) {
    return registrarEstrutura(nome, Rotation2d.struct, fonte);
  }

  /** Metros/s e radianos/s, no referencial do robô. Publique medido e desejado em nomes distintos. */
  public CanalAdvantageScope<ChassisSpeeds> registrarVelocidades(
      String nome, Supplier<ChassisSpeeds> fonte) {
    return registrarEstrutura(nome, ChassisSpeeds.struct, fonte);
  }

  /** Quatro módulos: frente esquerda, frente direita, trás esquerda, trás direita (FL/FR/BL/BR). */
  public CanalAdvantageScope<SwerveModuleState[]> registrarSwerve(
      String nome, Supplier<SwerveModuleState[]> fonte) {
    Objects.requireNonNull(fonte);
    var canal = criarEstruturas(nome, SwerveModuleState.struct,
        estados -> (estados.length == 0 || estados.length == 4)
            && Arrays.stream(estados).allMatch(AdvantageScope::estruturaValida));
    canal.definirFonte(fonte);
    return canal;
  }

  /** Só lê o motor. Encerre este painel antes de fechar os dispositivos registrados. */
  public void registrarMotorNeo(String nome, MotorNeo motor) {
    Objects.requireNonNull(motor);
    conferirGrupo(nome, "Posicao", "Velocidade", "Corrente", "Temperatura", "TensaoAplicada");
    registrarNumero(nome + "/Posicao", "rotations", motor::obterPosicaoRotacoes);
    registrarNumero(nome + "/Velocidade", "rpm", motor::obterVelocidadeRPM);
    registrarNumero(nome + "/Corrente", "A", motor::obterCorrenteAmperes);
    registrarNumero(nome + "/Temperatura", "celsius", motor::obterTemperaturaCelsius);
    registrarNumero(nome + "/TensaoAplicada", "V", motor::obterTensaoAplicadaVolts);
  }

  /** Kraken X60/X44: correntes de alimentação e estator separadas, com unidades explícitas. */
  public void registrarMotorKraken(String nome, MotorKraken motor) {
    Objects.requireNonNull(motor);
    conferirGrupo(nome, "Posicao", "Velocidade", "CorrenteAlimentacao", "CorrenteEstator",
        "Temperatura", "TensaoAplicada", "TensaoAlimentacao", "Conectado", "Modelo");
    registrarNumero(nome + "/Posicao", "rotations", motor::obterPosicaoRotacoes);
    registrarNumero(nome + "/Velocidade", "rpm", motor::obterVelocidadeRPM);
    registrarNumero(nome + "/CorrenteAlimentacao", "A", motor::obterCorrenteAlimentacaoAmperes);
    registrarNumero(nome + "/CorrenteEstator", "A", motor::obterCorrenteEstatorAmperes);
    registrarNumero(nome + "/Temperatura", "celsius", motor::obterTemperaturaCelsius);
    registrarNumero(nome + "/TensaoAplicada", "V", motor::obterTensaoAplicadaVolts);
    registrarNumero(nome + "/TensaoAlimentacao", "V", motor::obterTensaoAlimentacaoVolts);
    registrarBooleano(nome + "/Conectado", motor::estaConectado);
    registrarTexto(nome + "/Modelo", () -> motor.obterConfiguracao().modelo().name());
  }

  /** Estado necessário para relacionar falhas, quedas de bateria e períodos da partida. */
  public void registrarEstadoRobo() {
    conferirGrupo("Robo", "Bateria", "Brownout", "Habilitado", "Autonomo", "ConectadoDS");
    registrarNumero("Robo/Bateria", "V", RobotController::getBatteryVoltage);
    registrarBooleano("Robo/Brownout", RobotController::isBrownedOut);
    registrarBooleano("Robo/Habilitado", DriverStation::isEnabled);
    registrarBooleano("Robo/Autonomo", DriverStation::isAutonomous);
    registrarBooleano("Robo/ConectadoDS", DriverStation::isDSAttached);
  }

  /** Contrato WPILib; o robô existente já executa CommandScheduler.run em robotPeriodic. */
  @Override
  public void periodic() {
    atualizar();
  }

  /** Cada fornecedor é lido uma vez por amostragem; falha em um canal não interrompe os demais. */
  public void atualizar() {
    if (fechado) {
      return;
    }
    long agora = relogio.getAsLong();
    if (agora < proximaAtualizacao) {
      return;
    }
    proximaAtualizacao = agora + (long) (configuracao.intervaloSegundos() * 1_000_000.0);
    for (var canal : canais.values()) {
      canal.atualizar(agora);
    }
  }

  public String obterRaizTabela() {
    return tabela.getPath();
  }

  /** Libera só os publicadores deste painel; não fecha NT, motores ou gravações globais. */
  @Override
  public void close() {
    if (!fechado) {
      fechado = true;
      if (configuracao.atualizacaoAutomatica()) {
        CommandScheduler.getInstance().unregisterSubsystem(this);
      }
      canais.values().forEach(CanalAdvantageScope::encerrar);
      canais.clear();
    }
  }

  private <T> CanalAdvantageScope<T> adicionar(String nome, Publisher publicador,
      CanalAdvantageScope.Publicacao<T> publicacao, Predicate<T> validar) {
    var canal = new CanalAdvantageScope<>(publicador, publicacao, validar,
        tabela.getSubTable("Diagnostico").getSubTable(nome), opcoes, relogio);
    canais.put(nome, canal);
    return canal;
  }

  private void conferirNome(String nome) {
    if (fechado) {
      throw new IllegalStateException("Este painel AdvantageScope já foi encerrado.");
    }
    ConfiguracaoAdvantageScope.validarCaminho(nome);
    if (nome.equals("Diagnostico") || nome.startsWith("Diagnostico/")) {
      throw new IllegalArgumentException("Diagnostico é reservado à qualidade dos canais.");
    }
    if (canais.containsKey(nome) || rede.getTopic(tabela.getPath() + "/" + nome).exists()) {
      throw new IllegalArgumentException("Já existe uma publicação em " + nome + ".");
    }
  }

  private void conferirGrupo(String nome, String... campos) {
    ConfiguracaoAdvantageScope.validarCaminho(nome);
    for (String campo : campos) {
      conferirNome(nome + "/" + campo);
    }
  }

  private static boolean estruturaValida(Object valor) {
    if (valor == null) {
      return false;
    }
    if (valor instanceof Pose2d pose) {
      return finitos(pose.getX(), pose.getY(), pose.getRotation().getRadians());
    }
    if (valor instanceof Pose3d pose) {
      var q = pose.getRotation().getQuaternion();
      return finitos(pose.getX(), pose.getY(), pose.getZ(), q.getW(), q.getX(), q.getY(), q.getZ());
    }
    if (valor instanceof Rotation2d rotacao) {
      return Double.isFinite(rotacao.getRadians());
    }
    if (valor instanceof ChassisSpeeds velocidades) {
      return finitos(velocidades.vxMetersPerSecond,
          velocidades.vyMetersPerSecond, velocidades.omegaRadiansPerSecond);
    }
    if (valor instanceof SwerveModuleState modulo) {
      return modulo.angle != null && finitos(modulo.speedMetersPerSecond, modulo.angle.getRadians());
    }
    return true; // Outros structs WPILib/customizados: o chamador valida os dados de domínio.
  }

  private static boolean finitos(double... valores) {
    return Arrays.stream(valores).allMatch(Double::isFinite);
  }
}
