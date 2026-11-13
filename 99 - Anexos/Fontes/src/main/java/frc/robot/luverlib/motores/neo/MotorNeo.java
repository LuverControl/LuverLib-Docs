package frc.robot.luverlib.motores.neo;

import com.revrobotics.PersistMode;
import com.revrobotics.REVLibError;
import com.revrobotics.RelativeEncoder;
import com.revrobotics.ResetMode;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;
import com.revrobotics.spark.config.SparkMaxConfig;
import java.util.Objects;

/**
 * NEO brushless com SPARK MAX. Crie uma vez no subsistema, fora do laço periódico.
 *
 * <p>Uso: {@code new MotorNeo(idCan)}, com o ID recebido do código do robô. Para ajustes próprios
 * de um motor, passe uma ConfiguracaoMotorNeo no segundo argumento. Não troca o ID do dispositivo:
 * o número informado precisa corresponder ao configurado no SPARK MAX.
 */
public final class MotorNeo implements AutoCloseable {
  private final SparkMax controlador;
  private final RelativeEncoder encoder;
  private final ConfiguracaoMotorNeo configuracao;
  private boolean fechado;

  public MotorNeo(int idCan) {
    this(idCan, VariaveisMotor.configuracaoPadrao());
  }

  /** Aplica os parâmetros uma vez. Uma falha impede a criação e libera o controlador. */
  public MotorNeo(int idCan, ConfiguracaoMotorNeo configuracao) {
    this(idCan, configuracao, null);
  }

  /**
   * Cria um seguidor nativo do líder informado; null mantém um motor independente.
   *
   * <p>No seguidor, {@code configuracao.invertido()} inverte o sentido em relação ao líder.
   * O {@code limiteSaida} limita somente comandos enviados pelos métodos deste wrapper;
   * a saída recebida pelo vínculo nativo deve ser limitada no líder.
   */
  public MotorNeo(int idCan, ConfiguracaoMotorNeo configuracao, MotorNeo lider) {
    if (idCan < 1 || idCan > 62) {
      throw new IllegalArgumentException("Use um ID CAN do SPARK MAX entre 1 e 62.");
    }
    this.configuracao = Objects.requireNonNull(configuracao, "A configuração é obrigatória.");
    SparkMax controladorLider = lider == null ? null : lider.obterControlador();
    if (controladorLider != null && controladorLider.getDeviceId() == idCan) {
      throw new IllegalArgumentException("Líder e seguidor precisam de IDs CAN diferentes.");
    }
    SparkMax novo = new SparkMax(idCan, MotorType.kBrushless);
    try {
      novo.stopMotor();
      // Registra também o encoder na simulação REV antes de aplicar seus parâmetros.
      RelativeEncoder novoEncoder = novo.getEncoder();
      PersistMode persistencia = configuracao.gravarConfiguracao()
          ? PersistMode.kPersistParameters : PersistMode.kNoPersistParameters;
      SparkMaxConfig rev = criarConfiguracaoRev(configuracao);
      if (controladorLider != null) rev.follow(controladorLider, configuracao.invertido());
      REVLibError resultado = novo.configure(rev, ResetMode.kResetSafeParameters, persistencia);
      exigirSucesso(resultado, "configurar o SPARK MAX " + idCan);
      if (controladorLider == null) novo.stopMotor();
      controlador = novo;
      encoder = novoEncoder;
    } catch (RuntimeException | Error falha) {
      try {
        novo.stopMotor();
      } finally {
        novo.close();
      }
      throw falha;
    }
  }

  /** Comando de potência de -1 a 1, saturado no limite de saída configurado. */
  public void definirPotencia(double potencia) {
    garantirAberto();
    validarComando(potencia);
    controlador.set(limitar(potencia, configuracao.limiteSaida()));
  }

  /** Comando em volts, limitado à tensão nominal multiplicada pelo limite de saída. */
  public void definirTensao(double volts) {
    garantirAberto();
    validarComando(volts);
    double limiteVolts = configuracao.tensaoNominalVolts() * configuracao.limiteSaida();
    controlador.setVoltage(limitar(volts, limiteVolts));
  }

  public void parar() {
    garantirAberto();
    controlador.stopMotor();
  }

  /** Posição da saída do mecanismo em rotações, considerando a redução configurada. */
  public double obterPosicaoRotacoes() {
    garantirAberto();
    return encoder.getPosition();
  }

  /** Velocidade da saída do mecanismo em rotações por minuto, considerando a redução. */
  public double obterVelocidadeRPM() {
    garantirAberto();
    return encoder.getVelocity();
  }

  /** Altera a referência de posição do encoder; não move o motor. */
  public void definirPosicaoEncoder(double rotacoes) {
    garantirAberto();
    validarComando(rotacoes);
    // O valor enviado é convertido para float dentro da REVLib.
    if (!Float.isFinite((float) rotacoes)) {
      throw new IllegalArgumentException("A posição deve ser representável pela REVLib.");
    }
    exigirSucesso(encoder.setPosition(rotacoes), "alterar a referência do encoder");
  }

  public void zerarEncoder() {
    definirPosicaoEncoder(0.0);
  }

  public double obterCorrenteAmperes() {
    garantirAberto();
    return controlador.getOutputCurrent();
  }

  public double obterTemperaturaCelsius() {
    garantirAberto();
    return controlador.getMotorTemperature();
  }

  public double obterTensaoAplicadaVolts() {
    garantirAberto();
    return controlador.getAppliedOutput() * controlador.getBusVoltage();
  }

  public ConfiguracaoMotorNeo obterConfiguracao() {
    return configuracao;
  }

  /** Acesso avançado/simulação. Comandos diretos ignoram o limite de saída desta classe. */
  public SparkMax obterControlador() {
    garantirAberto();
    return controlador;
  }

  /** Contrato AutoCloseable do Java: para o motor e libera o controlador; pode repetir. */
  @Override
  public void close() {
    if (!fechado) {
      fechado = true;
      try {
        controlador.stopMotor();
      } finally {
        controlador.close();
      }
    }
  }

  private static SparkMaxConfig criarConfiguracaoRev(ConfiguracaoMotorNeo ajustes) {
    SparkMaxConfig rev = new SparkMaxConfig();
    rev.smartCurrentLimit(ajustes.limiteCorrenteAmperes())
        .inverted(ajustes.invertido())
        .idleMode(ajustes.frearAoParar() ? IdleMode.kBrake : IdleMode.kCoast)
        .openLoopRampRate(ajustes.rampaMalhaAbertaSegundos())
        .closedLoopRampRate(ajustes.rampaMalhaFechadaSegundos());
    if (ajustes.compensarTensao()) rev.voltageCompensation(ajustes.tensaoNominalVolts());
    else rev.disableVoltageCompensation();
    rev.encoder.positionConversionFactor(ajustes.fatorEncoder())
        .velocityConversionFactor(ajustes.fatorEncoder());
    return rev;
  }

  private void garantirAberto() {
    if (fechado) {
      throw new IllegalStateException("Este MotorNeo já foi fechado.");
    }
  }

  private void validarComando(double valor) {
    if (!Double.isFinite(valor)) {
      controlador.stopMotor();
      throw new IllegalArgumentException("O comando deve ser finito. O motor foi parado.");
    }
  }

  private static double limitar(double valor, double limite) {
    return Math.max(-limite, Math.min(valor, limite));
  }

  private static void exigirSucesso(REVLibError resultado, String operacao) {
    if (resultado != REVLibError.kOk) {
      throw new IllegalStateException("Falha ao " + operacao + ": " + resultado);
    }
  }
}
