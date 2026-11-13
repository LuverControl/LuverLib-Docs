package guia;

import edu.wpi.first.math.geometry.Pose2d;
import java.util.function.Supplier;
import frc.robot.luverlib.advantagescope.AdvantageScope;
import frc.robot.luverlib.motores.neo.MotorNeo;

public final class LogsExemplo implements AutoCloseable {
  private final AdvantageScope dados = new AdvantageScope();

  public LogsExemplo(MotorNeo motorExistente, Supplier<Pose2d> poseReal) {
    try {
      dados.registrarMotorNeo("Mecanismo", motorExistente);
      dados.registrarNumero("Mecanismo/RPM", "rpm", motorExistente::obterVelocidadeRPM);
      dados.registrarPose2d("Robo/Pose", poseReal);
      dados.registrarEstadoRobo();
    } catch (RuntimeException | Error falha) {
      try { dados.close(); } catch (RuntimeException | Error limpeza) { falha.addSuppressed(limpeza); }
      throw falha;
    }
  }

  @Override public void close() {
    dados.close();
  }
}
