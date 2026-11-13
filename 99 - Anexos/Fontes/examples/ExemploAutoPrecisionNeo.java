package exemplo;

import com.revrobotics.PersistMode;
import com.revrobotics.REVLibError;
import com.revrobotics.ResetMode;
import com.revrobotics.spark.ClosedLoopSlot;
import com.revrobotics.spark.FeedbackSensor;
import com.revrobotics.spark.SparkBase.ControlType;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.config.SparkMaxConfig;
import edu.wpi.first.math.trajectory.TrapezoidProfile;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Subsystem;
import frc.robot.luverlib.autoprecision.AutoPrecision;
import frc.robot.luverlib.autoprecision.ConfiguracaoAutoPrecision;
import frc.robot.luverlib.autoprecision.ControleNeoAutoPrecision;
import frc.robot.luverlib.autoprecision.GanhosPID;
import frc.robot.luverlib.motores.neo.MotorNeo;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * Integra um MotorNeo que já pertence ao mecanismo. Não cria motor, zero físico ou limites.
 * A configuração recebida deve ser a configuração real, revisada pelo responsável pelo robô.
 * As posições usam rotações da saída conforme o fator do MotorNeo; velocidade usa rotações/s
 * e aceleração rotações/s². O texto config.unidade() deve identificar essa unidade corretamente.
 *
 * <p>Crie uma instância em robotInit/configuração do mecanismo. Associe criarComando() a
 * whileTrue de um botão local; o AutoPrecision exige Test habilitado e autorização local.
 * O CommandScheduler precisa continuar rodando no robotPeriodic.
 *
 * <p>Depois de revisar o resultado, com o robô desabilitado:
 * <pre>{@code
 * exemplo.aplicarGanhosAprovados(ganhosRevisados, false); // somente RAM
 * var perfil = exemplo.criarPerfilDaOperacao();
 * var referencia = new TrapezoidProfile.State(
 *     motor.obterPosicaoRotacoes(), motor.obterVelocidadeRPM() / 60.0);
 * // No execute de um comando que exige o MESMO Subsystem do mecanismo:
 * referencia = perfil.calculate(dtSegundos, referencia,
 *     new TrapezoidProfile.State(alvoEmRotacoesDaSaida, 0));
 * exemplo.aplicarReferenciaPerfilada(referencia);
 * // No end desse comando, chame motor.parar().
 * }</pre>
 * O comando de operação deve manter as verificações locais de corrente, temperatura,
 * velocidade medida e posição do mecanismo, inclusive durante interrupções/disabled.
 * Use o período real entre ciclos e os mesmos fatores, slot, feedforward, rampas e perfil
 * usados na avaliação; esses parâmetros também influenciam os ganhos aprovados.
 */
public final class ExemploAutoPrecisionNeo implements AutoCloseable {
  private final MotorNeo motor;
  private final SparkMax spark;
  private final ClosedLoopSlot slot;
  private final ConfiguracaoAutoPrecision config;
  private final AutoPrecision afinador;
  private boolean ganhosAprovadosAplicados;
  private boolean fechado;

  public ExemploAutoPrecisionNeo(String nome, MotorNeo motorExistente, Subsystem mecanismo,
      ClosedLoopSlot slotExplicito, ConfiguracaoAutoPrecision configuracaoReal,
      BooleanSupplier autorizacaoLocal) {
    motor = Objects.requireNonNull(motorExistente);
    spark = motor.obterControlador();
    slot = Objects.requireNonNull(slotExplicito);
    config = Objects.requireNonNull(configuracaoReal);
    afinador = new AutoPrecision(nome, Objects.requireNonNull(mecanismo),
        new ControleNeoAutoPrecision(motor, slot), config,
        Objects.requireNonNull(autorizacaoLocal));
  }

  public Command criarComando() {
    exigirAberto();
    return afinador.criarComando();
  }

  /**
   * Aplica apenas ganhos revisados. Executar uma vez, desabilitado, fora do ensaio.
   * O argumento persistirEmFlash precisa ser true explicitamente para gravar a configuração.
   * A persistência REV é do controlador: também pode salvar outros parâmetros atuais dele.
   * A compensação fica desabilitada na operação para preservar o modo duty avaliado.
   */
  public void aplicarGanhosAprovados(GanhosPID ganhosRevisados, boolean persistirEmFlash) {
    exigirAberto();
    if (!DriverStation.isDisabled() || afinador.criarComando().isScheduled()) {
      throw new IllegalStateException("Aplique os ganhos desabilitado, com o ensaio encerrado.");
    }
    Objects.requireNonNull(ganhosRevisados);
    if (!ganhosRevisados.dentroDe(config.ganhosMaximos())
        || config.limites().saidaMaxima() > motor.obterConfiguracao().limiteSaida()) {
      throw new IllegalArgumentException("Ganhos/saída acima do permitido pelo mecanismo.");
    }
    motor.parar();
    ganhosAprovadosAplicados = false;
    if (lerParametro(spark::isFollower)
        || lerParametro(spark.configAccessor.closedLoop::getFeedbackSensor)
            != FeedbackSensor.kPrimaryEncoder
        || lerParametro(spark.configAccessor.closedLoop::getPositionWrappingEnabled)) {
      throw new IllegalStateException("Requer controlador independente e encoder primário sem wrapping.");
    }
    double fator = motor.obterConfiguracao().fatorEncoder();
    if (Math.abs(lerParametro(spark.configAccessor.encoder::getPositionConversionFactor) - fator) > 1e-6
        || Math.abs(lerParametro(spark.configAccessor.encoder::getVelocityConversionFactor) - fator) > 1e-6) {
      throw new IllegalStateException("Os fatores do encoder precisam corresponder ao ensaio.");
    }
    var soft = spark.configAccessor.softLimit;
    double frente = lerParametro(soft::getForwardSoftLimit);
    double re = lerParametro(soft::getReverseSoftLimit);
    boolean frenteAtiva = lerParametro(soft::getForwardSoftLimitEnabled);
    boolean reAtiva = lerParametro(soft::getReverseSoftLimitEnabled);
    double minimo = (float) config.limites().posicaoMinima();
    double maximo = (float) config.limites().posicaoMaxima();
    if (minimo >= maximo || frenteAtiva && maximo > frente || reAtiva && minimo < re) {
      throw new IllegalArgumentException("A configuração aprovada ampliaria os soft limits existentes.");
    }
    SparkMaxConfig ajuste = new SparkMaxConfig();
    ajuste.disableVoltageCompensation();
    ajuste.softLimit.forwardSoftLimit(maximo).reverseSoftLimit(minimo)
        .forwardSoftLimitEnabled(true).reverseSoftLimitEnabled(true);
    double limiteDuty = config.limites().saidaMaxima();
    ajuste.closedLoop.pid(ganhosRevisados.p(), ganhosRevisados.i(), ganhosRevisados.d(), slot)
        .outputRange(-limiteDuty, limiteDuty, slot)
        .iZone(config.zonaIntegral(), slot).iMaxAccum(config.acumuladorIntegralMaximo(), slot)
        .allowedClosedLoopError(0, slot);
    exigir(spark.configure(ajuste, ResetMode.kNoResetSafeParameters,
        persistirEmFlash ? PersistMode.kPersistParameters : PersistMode.kNoPersistParameters));
    exigir(spark.getClosedLoopController().setIAccum(0));
    ganhosAprovadosAplicados = true;
  }

  /** Mesmo perfil de velocidade/aceleração do ensaio; não equivale a MAXMotion do SPARK. */
  public TrapezoidProfile criarPerfilDaOperacao() {
    exigirAberto();
    return new TrapezoidProfile(new TrapezoidProfile.Constraints(
        config.limites().velocidadeMaxima(), config.limites().aceleracaoMaxima()));
  }

  /** Envie o estado calculado pelo perfil a cada ciclo do comando que possui o mecanismo. */
  public void aplicarReferenciaPerfilada(TrapezoidProfile.State referencia) {
    try {
      exigirAberto();
      Objects.requireNonNull(referencia);
      if (!ganhosAprovadosAplicados || afinador.criarComando().isScheduled()
          || !Double.isFinite(referencia.position) || !Double.isFinite(referencia.velocity)
          || referencia.position <= config.limites().posicaoMinima()
          || referencia.position >= config.limites().posicaoMaxima()) {
        throw new IllegalStateException("Ganhos não aplicados, ensaio ativo ou referência inválida.");
      }
      exigir(spark.getClosedLoopController().setSetpoint(referencia.position, ControlType.kPosition, slot));
    } catch (RuntimeException falha) {
      motor.parar();
      throw falha;
    }
  }

  private <T> T lerParametro(Supplier<T> origem) {
    T valor = origem.get();
    exigir(spark.getLastError());
    return valor;
  }

  private static void exigir(REVLibError resultado) {
    if (resultado != REVLibError.kOk) {
      throw new IllegalStateException("Falha REV ao aplicar a operação aprovada: " + resultado);
    }
  }

  private void exigirAberto() {
    if (fechado) throw new IllegalStateException("Exemplo AutoPrecision encerrado.");
  }

  /** Libera o afinador e restaura ensaio eventualmente ativo. O MotorNeo pertence ao mecanismo. */
  @Override
  public void close() {
    if (fechado) return;
    fechado = true;
    afinador.close();
  }
}
