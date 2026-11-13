package frc.robot.luverlib.limelight;

import edu.wpi.first.math.Matrix;
import edu.wpi.first.math.VecBuilder;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.numbers.N1;
import edu.wpi.first.math.numbers.N3;
import java.util.List;
import java.util.Objects;

/** Medição aceita, na origem azul, pronta para addVisionMeasurement com confiança por quadro. */
public record MedicaoVisao(String camera, MetodoPose metodo, Pose3d pose,
    double instanteCapturaSegundos, double latenciaSegundos, double distanciaMediaTagsMetros,
    List<MarcadorAprilTag> tags, double desvioLinearMetros, double desvioAngularRadianos) {
  public MedicaoVisao {
    Objects.requireNonNull(camera);
    Objects.requireNonNull(metodo);
    Objects.requireNonNull(pose);
    tags = List.copyOf(tags);
  }

  public Pose2d pose2d() { return pose.toPose2d(); }

  /** Nova matriz a cada chamada; MegaTag2 ignora yaw por ele já depender do giroscópio. */
  public Matrix<N3, N1> desviosPadrao() {
    return VecBuilder.fill(desvioLinearMetros, desvioLinearMetros, desvioAngularRadianos);
  }
}
