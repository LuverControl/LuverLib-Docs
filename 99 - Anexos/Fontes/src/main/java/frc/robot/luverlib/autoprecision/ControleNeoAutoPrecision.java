package frc.robot.luverlib.autoprecision;

import com.revrobotics.PersistMode;
import com.revrobotics.REVLibError;
import com.revrobotics.ResetMode;
import com.revrobotics.spark.ClosedLoopSlot;
import com.revrobotics.spark.FeedbackSensor;
import com.revrobotics.spark.SparkBase.ControlType;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.config.SparkMaxConfig;
import frc.robot.luverlib.motores.neo.MotorNeo;
import java.util.Objects;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

/**
 * PID de posição interno do SPARK MAX. Slot escolhido explicitamente, sem gravação em flash.
 * Mantém os fatores do MotorNeo; não zera encoder nem decide o zero físico do mecanismo.
 * Durante o ensaio, suspende a compensação de tensão para limitar o duty real declarado.
 * Ao finalizar, restaura sua habilitação e tensão nominal, além dos parâmetros temporários.
 */
public final class ControleNeoAutoPrecision implements ControleAutoPrecision {
  private final MotorNeo motor;
  private final SparkMax spark;
  private final ClosedLoopSlot slot;
  private ConfiguracaoAutoPrecision configuracao;
  private SparkMaxConfig restauracao;

  public ControleNeoAutoPrecision(MotorNeo motor, ClosedLoopSlot slot) {
    this.motor = Objects.requireNonNull(motor);
    spark = motor.obterControlador();
    this.slot = Objects.requireNonNull(slot);
  }

  @Override public String identificador() {
    return "sparkmax-posicao-duty-sem-compensacao-slot" + slot.value;
  }

  @Override
  public Medicao medir() {
    return new Medicao(ler(motor::obterPosicaoRotacoes), ler(motor::obterVelocidadeRPM) / 60.0,
        ler(spark::getAppliedOutput), ler(motor::obterCorrenteAmperes),
        ler(motor::obterTemperaturaCelsius));
  }

  @Override
  public void iniciar(ConfiguracaoAutoPrecision config) {
    if (restauracao != null) {
      throw new IllegalStateException("O backend NEO já está em uso.");
    }
    parar();
    if (lerParametro(spark::isFollower)) {
      throw new IllegalStateException("AutoPrecision requer o motor líder; não use um SPARK seguidor.");
    }
    var leitura = spark.configAccessor.closedLoop;
    if (lerParametro(leitura::getFeedbackSensor) != FeedbackSensor.kPrimaryEncoder
        || lerParametro(leitura::getPositionWrappingEnabled)) {
      throw new IllegalStateException("AutoPrecision requer encoder primário e posição sem wrapping.");
    }
    double fator = motor.obterConfiguracao().fatorEncoder();
    if (Math.abs(lerParametro(spark.configAccessor.encoder::getPositionConversionFactor) - fator) > 1e-6
        || Math.abs(lerParametro(spark.configAccessor.encoder::getVelocityConversionFactor) - fator) > 1e-6
        || config.limites().saidaMaxima() > motor.obterConfiguracao().limiteSaida()) {
      throw new IllegalArgumentException("Preserve os fatores do MotorNeo e seu limite de saída.");
    }
    SparkMaxConfig anterior = new SparkMaxConfig();
    anterior.closedLoop.pid(lerParametro(() -> leitura.getP(slot)),
        lerParametro(() -> leitura.getI(slot)), lerParametro(() -> leitura.getD(slot)), slot)
        .iZone(lerParametro(() -> leitura.getIZone(slot)), slot)
        .iMaxAccum(lerParametro(() -> leitura.getMaxIAccumulation(slot)), slot)
        .outputRange(lerParametro(() -> leitura.getMinOutput(slot)),
            lerParametro(() -> leitura.getMaxOutput(slot)), slot)
        .allowedClosedLoopError(lerParametro(() -> leitura.getAllowedClosedLoopError(slot)), slot);
    var soft = spark.configAccessor.softLimit;
    double limiteFrente = lerParametro(soft::getForwardSoftLimit);
    double limiteRe = lerParametro(soft::getReverseSoftLimit);
    boolean frenteAtivo = lerParametro(soft::getForwardSoftLimitEnabled);
    boolean reAtivo = lerParametro(soft::getReverseSoftLimitEnabled);
    anterior.softLimit.forwardSoftLimit(limiteFrente).reverseSoftLimit(limiteRe)
        .forwardSoftLimitEnabled(frenteAtivo).reverseSoftLimitEnabled(reAtivo);
    boolean compensacaoAtiva = lerParametro(spark.configAccessor::getVoltageCompensationEnabled);
    anterior.voltageCompensation(lerParametro(spark.configAccessor::getVoltageCompensation));
    if (!compensacaoAtiva) anterior.disableVoltageCompensation();
    restauracao = anterior;
    configuracao = config;
    SparkMaxConfig limites = new SparkMaxConfig();
    // Nunca amplia um limite de hardware que já esteja habilitado no projeto.
    double maximo = (float) config.limites().posicaoMaxima();
    double minimo = (float) config.limites().posicaoMinima();
    if (frenteAtivo && maximo > limiteFrente || reAtivo && minimo < limiteRe || minimo >= maximo) {
      throw new IllegalArgumentException("Os limites de ensaio devem caber nos soft limits existentes.");
    }
    limites.softLimit.forwardSoftLimit(maximo).reverseSoftLimit(minimo)
        .forwardSoftLimitEnabled(true).reverseSoftLimitEnabled(true);
    // A REV aplica a compensação após o limite do slot: a 9 V, 0,3 * 12/9 viraria 0,4 duty.
    limites.disableVoltageCompensation();
    configurar(limites);
    aplicarGanhos(config.ganhosIniciais());
  }

  @Override
  public void aplicarGanhos(GanhosPID ganhos) {
    if (configuracao == null || !ganhos.dentroDe(configuracao.ganhosMaximos())) {
      throw new IllegalArgumentException("Ganhos fora da configuração ativa do mecanismo.");
    }
    parar();
    SparkMaxConfig ajuste = new SparkMaxConfig();
    double limite = configuracao.limites().saidaMaxima();
    ajuste.closedLoop.pid(ganhos.p(), ganhos.i(), ganhos.d(), slot)
        .outputRange(-limite, limite, slot).iZone(configuracao.zonaIntegral(), slot)
        .iMaxAccum(configuracao.acumuladorIntegralMaximo(), slot).allowedClosedLoopError(0, slot);
    configurar(ajuste);
    exigir(spark.getClosedLoopController().setIAccum(0));
  }

  @Override
  public void aplicarReferencia(double posicao) {
    try {
      LimitesAutoPrecision.finito(posicao);
      if (configuracao == null || posicao <= configuracao.limites().posicaoMinima()
          || posicao >= configuracao.limites().posicaoMaxima()) {
        throw new IllegalArgumentException("Referência fora da faixa ativa.");
      }
      exigir(spark.getClosedLoopController().setSetpoint(posicao, ControlType.kPosition, slot));
    } catch (RuntimeException falha) {
      parar();
      throw falha;
    }
  }

  @Override public void parar() { motor.parar(); }

  @Override
  public void finalizar() {
    parar();
    if (restauracao != null) {
      configurar(restauracao);
      restauracao = null;
      configuracao = null;
    }
  }

  private void configurar(SparkMaxConfig config) {
    exigir(spark.configure(config, ResetMode.kNoResetSafeParameters, PersistMode.kNoPersistParameters));
  }

  private double ler(DoubleSupplier fonte) {
    double valor = fonte.getAsDouble();
    exigir(spark.getLastError());
    return valor;
  }

  private <T> T lerParametro(Supplier<T> fonte) {
    T valor = fonte.get();
    exigir(spark.getLastError());
    return valor;
  }

  private static void exigir(REVLibError resultado) {
    if (resultado != REVLibError.kOk) {
      throw new IllegalStateException("Falha do SPARK MAX no AutoPrecision: " + resultado);
    }
  }
}
