package frc.robot.luverlib.pathplanner;

import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.auto.NamedCommands;
import com.pathplanner.lib.commands.FollowPathCommand;
import com.pathplanner.lib.commands.PathfindingCommand;
import com.pathplanner.lib.config.RobotConfig;
import com.pathplanner.lib.controllers.PPHolonomicDriveController;
import com.pathplanner.lib.controllers.PPLTVController;
import com.pathplanner.lib.controllers.PathFollowingController;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BooleanSupplier;

/**
 * Integração PathPlanner para swerve ou diferencial. Crie uma vez, após criar o drivetrain.
 *
 * <p>Ordem: configurar, registrar TODOS os comandos nomeados, carregar os autos/caminhos e criar
 * o seletor na inicialização. Não lê arquivos dentro de autonomousInit nem agenda movimento sozinho.
 * AutoBuilder é global: uma segunda configuração é recusada, inclusive por outra biblioteca.
 */
public final class PathPlannerLuver {
  private final ConexaoPathPlanner conexao;
  private final ConfiguracaoPathPlanner ajustes;
  private final boolean holonomico;
  private final Map<String, Command> autonomos = new LinkedHashMap<>();
  private boolean carregamentoIniciado;

  public PathPlannerLuver(ConexaoPathPlanner conexao, RobotConfig robo) {
    this(conexao, robo, VariaveisPathPlanner.configuracaoPadrao(),
        PathPlannerLuver::estaNaAliancaVermelha);
  }

  /** O fornecedor de aliança é consultado na execução, sem congelar a aliança na inicialização. */
  public PathPlannerLuver(ConexaoPathPlanner conexao, RobotConfig robo,
      ConfiguracaoPathPlanner ajustes, BooleanSupplier inverterParaVermelho) {
    this.conexao = Objects.requireNonNull(conexao, "A conexão com o drivetrain é obrigatória.");
    this.ajustes = Objects.requireNonNull(ajustes, "Os ajustes são obrigatórios.");
    Objects.requireNonNull(inverterParaVermelho, "O fornecedor de aliança é obrigatório.");
    validarRobo(robo);
    if (AutoBuilder.isConfigured()) {
      throw new IllegalStateException("AutoBuilder já configurado. Use uma única instância no robô.");
    }
    holonomico = robo.isHolonomic;
    PathFollowingController controlador = holonomico
        ? new PPHolonomicDriveController(ajustes.pidTranslacao(), ajustes.pidRotacao(),
            ajustes.periodoControleSegundos())
        : new PPLTVController(ajustes.periodoControleSegundos());
    AutoBuilder.configure(this::lerPose, this::redefinirPose, this::lerVelocidades,
        conexao.aplicarSaida(), controlador, robo, inverterParaVermelho, conexao.subsistema());
  }

  /** Lê src/main/deploy/pathplanner/settings.json. Não inventa geometria se o arquivo falhar. */
  public static RobotConfig carregarConfiguracaoDoProjeto() {
    try {
      RobotConfig robo = RobotConfig.fromGUISettings();
      validarRobo(robo);
      return robo;
    } catch (Exception falha) {
      throw new IllegalStateException("Configure o robô no PathPlanner e exporte "
          + "src/main/deploy/pathplanner/settings.json.", falha);
    }
  }

  public static boolean estaNaAliancaVermelha() {
    return DriverStation.getAlliance().map(alianca -> alianca == Alliance.Red).orElse(false);
  }

  /** O nome precisa coincidir com o aplicativo. Não sobrescreve registros de outro módulo. */
  public void registrarComando(String nome, Command comando) {
    ArquivosPathPlanner.nomeTexto(nome);
    Objects.requireNonNull(comando, "O comando é obrigatório.");
    if (carregamentoIniciado) {
      throw new IllegalStateException("Registre todos os eventos antes de carregar autos/caminhos.");
    }
    if (NamedCommands.hasCommand(nome)) {
      throw new IllegalArgumentException("Comando nomeado já registrado: " + nome);
    }
    NamedCommands.registerCommand(nome, comando);
  }

  /** Pré-carrega e guarda o auto. Chamadas seguintes devolvem o mesmo comando pronto. */
  public Command carregarAutonomo(String nome) {
    ArquivosPathPlanner.validarNomeArquivo(nome);
    if (autonomos.containsKey(nome)) {
      return autonomos.get(nome);
    }
    ArquivosPathPlanner.validarAutonomo(nome, holonomico);
    carregamentoIniciado = true;
    Command comando = finalizarComParada(AutoBuilder.buildAuto(nome), nome);
    autonomos.put(nome, comando);
    return comando;
  }

  /** Segue a partir da pose atual; NÃO teletransporta/redefine a odometria para o início. */
  public Command criarComandoCaminho(String nome) {
    var caminho = ArquivosPathPlanner.carregarCaminho(nome);
    carregamentoIniciado = true;
    return finalizarComParada(AutoBuilder.followPath(caminho), "Seguir " + nome);
  }

  /** Calcula uma rota até o início do caminho e depois segue seus marcadores e limites. */
  public Command criarComandoIrAteCaminho(String nome) {
    var caminho = ArquivosPathPlanner.carregarCaminho(nome);
    carregamentoIniciado = true;
    return finalizarComParada(AutoBuilder.pathfindThenFollowPath(caminho,
        ajustes.limitesPathfinding()), "Ir até caminho " + nome);
  }

  /** Destino ABSOLUTO no campo com origem azul: nunca aplica transformação por aliança. */
  public Command criarComandoIrAtePose(Pose2d alvoCampo) {
    validarPose(alvoCampo);
    return finalizarComParada(AutoBuilder.pathfindToPose(alvoCampo,
        ajustes.limitesPathfinding(), 0.0), "Ir até pose do campo");
  }

  /** Destino desenhado para azul; transforma para vermelho quando o comando inicia. */
  public Command criarComandoIrAtePoseDaAlianca(Pose2d alvoAzul) {
    validarPose(alvoAzul);
    return finalizarComParada(AutoBuilder.pathfindToPoseFlipped(alvoAzul,
        ajustes.limitesPathfinding(), 0.0), "Ir até pose da aliança");
  }

  /** Redefine a odometria para uma pose azul, transformada pela aliança durante a execução. */
  public Command criarComandoRedefinirPose(Pose2d poseAzul) {
    validarPose(poseAzul);
    Command comando = AutoBuilder.resetOdom(poseAzul);
    comando.addRequirements(conexao.subsistema());
    return comando.withName("Redefinir pose inicial");
  }

  public SeletorAutonomo criarSeletor(String nomePainel) {
    return criarSeletor(nomePainel, "");
  }

  /** Pré-carrega os autos exportados. Sem padrão explícito, o padrão é permanecer parado. */
  public SeletorAutonomo criarSeletor(String nomePainel, String autonomoPadrao) {
    return new SeletorAutonomo(this, nomePainel, autonomoPadrao);
  }

  /** Opcional: agende no robotInit. Usa saídas fictícias e não reserva nem move seu drivetrain. */
  public Command criarComandoAquecimento() {
    return Commands.sequence(FollowPathCommand.warmupCommand(), PathfindingCommand.warmupCommand())
        .withName("Aquecer PathPlanner");
  }

  public Command criarComandoParar() {
    return Commands.runOnce(conexao.parar(), conexao.subsistema()).withName("Sem autônomo");
  }

  public ConfiguracaoPathPlanner obterConfiguracao() {
    return ajustes;
  }

  private Command finalizarComParada(Command comando, String nome) {
    // Reserva o drivetrain até em autos compostos apenas por esperas/eventos.
    comando.addRequirements(conexao.subsistema());
    return comando.finallyDo(interrompido -> conexao.parar().run()).withName(nome);
  }

  private Pose2d lerPose() {
    try {
      Pose2d pose = conexao.obterPoseCampo().get();
      validarPose(pose);
      return pose;
    } catch (RuntimeException falha) {
      conexao.parar().run();
      throw falha;
    }
  }

  private void redefinirPose(Pose2d pose) {
    validarPose(pose);
    conexao.redefinirPoseCampo().accept(pose);
  }

  private ChassisSpeeds lerVelocidades() {
    try {
      ChassisSpeeds velocidades = Objects.requireNonNull(conexao.obterVelocidadesRelativasRobo().get(),
          "O drivetrain devolveu velocidades nulas.");
      if (!Double.isFinite(velocidades.vxMetersPerSecond)
          || !Double.isFinite(velocidades.vyMetersPerSecond)
          || !Double.isFinite(velocidades.omegaRadiansPerSecond)) {
        throw new IllegalStateException("O drivetrain devolveu velocidades inválidas.");
      }
      return velocidades;
    } catch (RuntimeException falha) {
      conexao.parar().run();
      throw falha;
    }
  }

  private static void validarPose(Pose2d pose) {
    Objects.requireNonNull(pose, "A pose é obrigatória.");
    if (!Double.isFinite(pose.getX()) || !Double.isFinite(pose.getY())
        || !Double.isFinite(pose.getRotation().getRadians())) {
      throw new IllegalArgumentException("A pose deve conter apenas valores finitos.");
    }
  }

  private static void validarRobo(RobotConfig robo) {
    Objects.requireNonNull(robo, "A configuração física do robô é obrigatória.");
    ConfiguracaoPathPlanner.exigirPositivo(robo.massKG, "Massa do robô");
    ConfiguracaoPathPlanner.exigirPositivo(robo.MOI, "Momento de inércia");
    Objects.requireNonNull(robo.moduleConfig, "A configuração das rodas é obrigatória.");
    ConfiguracaoPathPlanner.exigirPositivo(robo.moduleConfig.wheelRadiusMeters, "Raio da roda");
    ConfiguracaoPathPlanner.exigirPositivo(robo.moduleConfig.maxDriveVelocityMPS,
        "Velocidade física máxima");
    ConfiguracaoPathPlanner.exigirPositivo(robo.moduleConfig.wheelCOF, "Coeficiente de atrito");
    ConfiguracaoPathPlanner.exigirPositivo(robo.moduleConfig.driveCurrentLimit, "Limite de corrente");
  }
}
