package frc.robot.luverlib.telemetria;

import edu.wpi.first.util.sendable.Sendable;
import edu.wpi.first.util.sendable.SendableBuilder;
import edu.wpi.first.util.sendable.SendableRegistry;
import frc.robot.luverlib.motores.neo.MotorNeo;
import java.util.Objects;

/** Leituras e ajustes do NEO/SPARK MAX. Não oferece comandos de movimento pelo painel. */
public final class TelemetriaMotorNeo implements Sendable, AutoCloseable {
  private final MotorNeo motor;

  public TelemetriaMotorNeo(MotorNeo motor) {
    this.motor = Objects.requireNonNull(motor, "O motor é obrigatório.");
  }

  @Override
  public void initSendable(SendableBuilder painel) {
    painel.setSmartDashboardType("LuverLib/NEO");
    painel.addDoubleProperty("Posição (rotações)", motor::obterPosicaoRotacoes, null);
    painel.addDoubleProperty("Velocidade (RPM)", motor::obterVelocidadeRPM, null);
    painel.addDoubleProperty("Corrente (A)", motor::obterCorrenteAmperes, null);
    painel.addDoubleProperty("Temperatura (°C)", motor::obterTemperaturaCelsius, null);
    painel.addDoubleProperty("Tensão aplicada (V)", motor::obterTensaoAplicadaVolts, null);
    painel.addDoubleProperty("ID CAN", () -> motor.obterControlador().getDeviceId(), null);
    painel.addDoubleProperty("Limite de corrente (A)",
        () -> motor.obterConfiguracao().limiteCorrenteAmperes(), null);
    painel.addDoubleProperty("Limite de saída (%)",
        () -> motor.obterConfiguracao().limiteSaida() * 100.0, null);
    painel.addBooleanProperty("Invertido", () -> motor.obterConfiguracao().invertido(), null);
    painel.addBooleanProperty("Frear ao parar", () -> motor.obterConfiguracao().frearAoParar(), null);
  }

  /** Encerre antes de fechar o motor. A publicação não é dona do MotorNeo. */
  @Override
  public void close() {
    SendableRegistry.remove(this);
  }
}
