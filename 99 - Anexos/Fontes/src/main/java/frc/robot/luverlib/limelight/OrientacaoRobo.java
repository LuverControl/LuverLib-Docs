package frc.robot.luverlib.limelight;

import edu.wpi.first.math.geometry.Rotation2d;

/** Orientação na origem azul: yaw positivo anti-horário. Ângulos em graus, taxas em graus/s. */
public record OrientacaoRobo(double yawGraus, double velocidadeYawGrausSegundo,
    double pitchGraus, double velocidadePitchGrausSegundo,
    double rollGraus, double velocidadeRollGrausSegundo) {
  public OrientacaoRobo {
    for (double valor : new double[] {yawGraus, velocidadeYawGrausSegundo, pitchGraus,
        velocidadePitchGrausSegundo, rollGraus, velocidadeRollGrausSegundo}) {
      if (!Double.isFinite(valor)) {
        throw new IllegalArgumentException("A orientação deve conter apenas valores finitos.");
      }
    }
  }

  public static OrientacaoRobo plana(Rotation2d rotacao, double velocidadeRadianosSegundo) {
    return new OrientacaoRobo(rotacao.getDegrees(), Math.toDegrees(velocidadeRadianosSegundo),
        0, 0, 0, 0);
  }

  double[] paraArray() {
    return new double[] {yawGraus, velocidadeYawGrausSegundo, pitchGraus,
        velocidadePitchGrausSegundo, rollGraus, velocidadeRollGrausSegundo};
  }
}
