package frc.robot.luverlib.telemetria;

import edu.wpi.first.util.sendable.Sendable;
import edu.wpi.first.util.sendable.SendableBuilder;
import edu.wpi.first.util.sendable.SendableRegistry;
import frc.robot.luverlib.motores.kraken.MotorKraken;
import java.util.Objects;
import java.util.function.DoubleSupplier;

/** Leituras do Kraken X60/X44. Não aceita alterações de configuração ou movimento pelo painel. */
public final class TelemetriaMotorKraken implements Sendable, AutoCloseable {
  private final MotorKraken motor;

  public TelemetriaMotorKraken(MotorKraken motor) {
    this.motor = Objects.requireNonNull(motor);
  }

  @Override
  public void initSendable(SendableBuilder painel) {
    painel.setSmartDashboardType("LuverLib/Kraken");
    painel.addStringProperty("Modelo", () -> motor.obterConfiguracao().modelo().name(), null);
    painel.addBooleanProperty("Conectado", motor::estaConectado, null);
    painel.addDoubleProperty("Posição (rotações)", () -> ler(motor::obterPosicaoRotacoes), null);
    painel.addDoubleProperty("Velocidade (RPM)", () -> ler(motor::obterVelocidadeRPM), null);
    painel.addDoubleProperty("Corrente alimentação (A)",
        () -> ler(motor::obterCorrenteAlimentacaoAmperes), null);
    painel.addDoubleProperty("Corrente estator (A)", () -> ler(motor::obterCorrenteEstatorAmperes), null);
    painel.addDoubleProperty("Temperatura (°C)", () -> ler(motor::obterTemperaturaCelsius), null);
    painel.addDoubleProperty("Tensão aplicada (V)", () -> ler(motor::obterTensaoAplicadaVolts), null);
    painel.addDoubleProperty("Tensão alimentação (V)", () -> ler(motor::obterTensaoAlimentacaoVolts), null);
    painel.addDoubleProperty("Limite alimentação (A)",
        () -> motor.obterConfiguracao().limiteCorrenteAlimentacaoAmperes(), null);
    painel.addDoubleProperty("Limite estator (A)",
        () -> motor.obterConfiguracao().limiteCorrenteEstatorAmperes(), null);
  }

  private static double ler(DoubleSupplier fonte) {
    try {
      return fonte.getAsDouble();
    } catch (IllegalStateException indisponivel) {
      return Double.NaN; // Sem leitura válida; não fabrica posição/corrente zero.
    }
  }

  @Override
  public void close() { SendableRegistry.remove(this); }
}
