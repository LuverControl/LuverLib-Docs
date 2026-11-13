package frc.robot.luverlib.limelight;

/** Dimensões do campo informadas pelo projeto, em metros; não ficam presas a uma temporada. */
public record LimitesCampo(double comprimentoMetros, double larguraMetros, double margemMetros) {
  public LimitesCampo {
    if (!Double.isFinite(comprimentoMetros) || comprimentoMetros <= 0
        || !Double.isFinite(larguraMetros) || larguraMetros <= 0
        || !Double.isFinite(margemMetros) || margemMetros < 0) {
      throw new IllegalArgumentException("Dimensões devem ser positivas e margem não negativa.");
    }
  }

  public boolean contem(double x, double y) {
    return Double.isFinite(x) && Double.isFinite(y) && x >= -margemMetros && y >= -margemMetros
        && x <= comprimentoMetros + margemMetros && y <= larguraMetros + margemMetros;
  }
}
