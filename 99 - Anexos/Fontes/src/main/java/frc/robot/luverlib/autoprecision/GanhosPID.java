package frc.robot.luverlib.autoprecision;

/** Ganhos na unidade do controlador escolhido; ganhos SPARK usam a base de tempo nativa em ms. */
public record GanhosPID(double p, double i, double d) {
  public GanhosPID {
    for (double ganho : new double[] {p, i, d}) {
      if (!Double.isFinite(ganho) || ganho < 0 || !Float.isFinite((float) ganho)
          || ganho > 0 && (float) ganho == 0) {
        throw new IllegalArgumentException("Ganhos PID devem ser finitos, não negativos e caber em float.");
      }
    }
  }

  public boolean dentroDe(GanhosPID maximos) {
    return p <= maximos.p && i <= maximos.i && d <= maximos.d;
  }
}
