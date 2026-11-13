package frc.robot.luverlib.limelight;

/** Montagem no referencial Limelight: frente/direita/cima em metros; roll/pitch/yaw em graus. */
public record PosicaoCamera(double frenteMetros, double direitaMetros, double cimaMetros,
    double rollGraus, double pitchGraus, double yawGraus) {
  public PosicaoCamera {
    for (double valor : new double[] {frenteMetros, direitaMetros, cimaMetros,
        rollGraus, pitchGraus, yawGraus}) {
      if (!Double.isFinite(valor)) {
        throw new IllegalArgumentException("A montagem deve conter apenas valores finitos.");
      }
    }
  }

  double[] paraArray() {
    return new double[] {frenteMetros, direitaMetros, cimaMetros, rollGraus, pitchGraus, yawGraus};
  }
}
