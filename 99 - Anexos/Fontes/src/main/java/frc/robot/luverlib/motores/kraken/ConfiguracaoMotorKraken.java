package frc.robot.luverlib.motores.kraken;

import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.signals.FeedbackSensorSourceValue;
import com.ctre.phoenix6.signals.InvertedValue;
import com.ctre.phoenix6.signals.NeutralModeValue;
import java.util.Objects;

/**
 * Ajustes imutáveis do Kraken X60/X44 com Talon FX integrado. Corrente de alimentação é da bateria;
 * corrente de estator é do motor. Os dois limitadores ficam habilitados.
 * Posição/velocidade representam a saída do mecanismo após a redução, em rotações e RPM.
 */
public record ConfiguracaoMotorKraken(
    ModeloKraken modelo,
    double limiteCorrenteAlimentacaoAmperes,
    double limiteCorrenteEstatorAmperes,
    boolean invertido,
    boolean frearAoParar,
    double tensaoMaximaVolts,
    double rampaMalhaAbertaSegundos,
    double rampaMalhaFechadaSegundos,
    double rotacoesMotorPorRotacaoSaida,
    double limiteSaida) {

  public ConfiguracaoMotorKraken {
    Objects.requireNonNull(modelo, "Informe X60 ou X44.");
    positivoAte(limiteCorrenteAlimentacaoAmperes, 800, "Corrente de alimentação");
    positivoAte(limiteCorrenteEstatorAmperes, 800, "Corrente de estator");
    positivoAte(tensaoMaximaVolts, 16, "Tensão máxima");
    intervalo(rampaMalhaAbertaSegundos, 0, 1, "Rampa de malha aberta");
    intervalo(rampaMalhaFechadaSegundos, 0, 1, "Rampa de malha fechada");
    positivoAte(rotacoesMotorPorRotacaoSaida, 1000, "Redução");
    if (!Double.isFinite(1.0 / rotacoesMotorPorRotacaoSaida)) {
      throw new IllegalArgumentException("A redução é pequena demais para uma conversão finita.");
    }
    intervalo(limiteSaida, 0, 1, "Limite de saída");
  }

  public ConfiguracaoMotorKraken comCorrentes(double alimentacaoAmperes, double estatorAmperes) {
    return new ConfiguracaoMotorKraken(modelo, alimentacaoAmperes, estatorAmperes,
        invertido, frearAoParar, tensaoMaximaVolts, rampaMalhaAbertaSegundos,
        rampaMalhaFechadaSegundos, rotacoesMotorPorRotacaoSaida, limiteSaida);
  }

  public ConfiguracaoMotorKraken comInversao(boolean inverter) {
    return new ConfiguracaoMotorKraken(modelo, limiteCorrenteAlimentacaoAmperes,
        limiteCorrenteEstatorAmperes, inverter, frearAoParar, tensaoMaximaVolts,
        rampaMalhaAbertaSegundos, rampaMalhaFechadaSegundos, rotacoesMotorPorRotacaoSaida, limiteSaida);
  }

  public ConfiguracaoMotorKraken comFreio(boolean frear) {
    return new ConfiguracaoMotorKraken(modelo, limiteCorrenteAlimentacaoAmperes,
        limiteCorrenteEstatorAmperes, invertido, frear, tensaoMaximaVolts,
        rampaMalhaAbertaSegundos, rampaMalhaFechadaSegundos, rotacoesMotorPorRotacaoSaida, limiteSaida);
  }

  public ConfiguracaoMotorKraken comReducao(double reducao) {
    return new ConfiguracaoMotorKraken(modelo, limiteCorrenteAlimentacaoAmperes,
        limiteCorrenteEstatorAmperes, invertido, frearAoParar, tensaoMaximaVolts,
        rampaMalhaAbertaSegundos, rampaMalhaFechadaSegundos, reducao, limiteSaida);
  }

  public ConfiguracaoMotorKraken comLimiteSaida(double limite) {
    return new ConfiguracaoMotorKraken(modelo, limiteCorrenteAlimentacaoAmperes,
        limiteCorrenteEstatorAmperes, invertido, frearAoParar, tensaoMaximaVolts,
        rampaMalhaAbertaSegundos, rampaMalhaFechadaSegundos, rotacoesMotorPorRotacaoSaida, limite);
  }

  public ConfiguracaoMotorKraken comRampas(double abertaSegundos, double fechadaSegundos) {
    return new ConfiguracaoMotorKraken(modelo, limiteCorrenteAlimentacaoAmperes,
        limiteCorrenteEstatorAmperes, invertido, frearAoParar, tensaoMaximaVolts,
        abertaSegundos, fechadaSegundos, rotacoesMotorPorRotacaoSaida, limiteSaida);
  }

  /** Cria uma configuração nova; não compartilha objetos mutáveis do Phoenix entre motores. */
  TalonFXConfiguration criarConfiguracaoPhoenix() {
    var phoenix = new TalonFXConfiguration();
    phoenix.CurrentLimits.SupplyCurrentLimitEnable = true;
    phoenix.CurrentLimits.SupplyCurrentLimit = limiteCorrenteAlimentacaoAmperes;
    // Usa um teto constante explícito, sem herdar a redução temporizada padrão do Phoenix.
    phoenix.CurrentLimits.SupplyCurrentLowerTime = 0;
    phoenix.CurrentLimits.StatorCurrentLimitEnable = true;
    phoenix.CurrentLimits.StatorCurrentLimit = limiteCorrenteEstatorAmperes;
    phoenix.MotorOutput.Inverted = invertido
        ? InvertedValue.Clockwise_Positive : InvertedValue.CounterClockwise_Positive;
    phoenix.MotorOutput.NeutralMode = frearAoParar ? NeutralModeValue.Brake : NeutralModeValue.Coast;
    phoenix.MotorOutput.PeakForwardDutyCycle = limiteSaida;
    phoenix.MotorOutput.PeakReverseDutyCycle = -limiteSaida;
    phoenix.Voltage.PeakForwardVoltage = tensaoMaximaVolts * limiteSaida;
    phoenix.Voltage.PeakReverseVoltage = -tensaoMaximaVolts * limiteSaida;
    phoenix.OpenLoopRamps.DutyCycleOpenLoopRampPeriod = rampaMalhaAbertaSegundos;
    phoenix.OpenLoopRamps.VoltageOpenLoopRampPeriod = rampaMalhaAbertaSegundos;
    phoenix.ClosedLoopRamps.DutyCycleClosedLoopRampPeriod = rampaMalhaFechadaSegundos;
    phoenix.ClosedLoopRamps.VoltageClosedLoopRampPeriod = rampaMalhaFechadaSegundos;
    phoenix.Feedback.FeedbackSensorSource = FeedbackSensorSourceValue.RotorSensor;
    phoenix.Feedback.SensorToMechanismRatio = rotacoesMotorPorRotacaoSaida;
    return phoenix;
  }

  private static void positivoAte(double valor, double maximo, String nome) {
    intervalo(valor, 0, maximo, nome);
    if (valor == 0) {
      throw new IllegalArgumentException(nome + " deve ser maior que zero.");
    }
  }

  private static void intervalo(double valor, double minimo, double maximo, String nome) {
    if (!Double.isFinite(valor) || valor < minimo || valor > maximo) {
      throw new IllegalArgumentException(nome + " deve ser finito, de " + minimo + " a " + maximo + ".");
    }
  }
}
