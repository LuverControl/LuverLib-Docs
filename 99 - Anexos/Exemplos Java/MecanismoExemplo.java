package guia;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.luverlib.motores.neo.ConfiguracaoMotorNeo;
import frc.robot.luverlib.motores.neo.MotorNeo;
import frc.robot.luverlib.telemetria.Telemetria;
import frc.robot.luverlib.telemetria.TelemetriaMotorNeo;

public final class MecanismoExemplo extends SubsystemBase implements AutoCloseable {
  private final MotorNeo motor;
  private final TelemetriaMotorNeo painel;

  public MecanismoExemplo(int idDefinidoEmConstants, ConfiguracaoMotorNeo ajustes) {
    motor = new MotorNeo(idDefinidoEmConstants, ajustes);
    try {
      painel = Telemetria.registrarMotor("Mecanismo", motor);
    } catch (RuntimeException | Error falha) {
      try { motor.close(); } catch (RuntimeException | Error limpeza) { falha.addSuppressed(limpeza); }
      throw falha;
    }
  }

  public Command criarComandoManual(double potenciaDefinidaEmConstants) {
    return runEnd(() -> motor.definirPotencia(potenciaDefinidaEmConstants), motor::parar)
        .onlyWhile(DriverStation::isTeleopEnabled);
  }

  @Override public void close() {
    try { painel.close(); } finally { motor.close(); }
  }
}
