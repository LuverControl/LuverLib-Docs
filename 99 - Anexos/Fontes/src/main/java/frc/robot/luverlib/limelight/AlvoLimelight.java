package frc.robot.luverlib.limelight;

import java.util.OptionalDouble;

/** Dados do mesmo pacote t2d; área em porcentagem e horário de captura já compensado. */
public record AlvoLimelight(double txGraus, double tyGraus, double areaPercentual,
    int idAprilTag, int classeDetector, int classeClassificador, double instanteCapturaSegundos) {

  /** Distância horizontal; inclinação positiva para cima. Sem solução geométrica retorna vazio. */
  public OptionalDouble estimarDistanciaHorizontalMetros(
      double alturaCameraMetros, double alturaAlvoMetros, double inclinacaoParaCimaGraus) {
    if (!Double.isFinite(alturaCameraMetros) || !Double.isFinite(alturaAlvoMetros)
        || !Double.isFinite(inclinacaoParaCimaGraus)) {
      throw new IllegalArgumentException("A geometria deve ser finita.");
    }
    double angulo = inclinacaoParaCimaGraus + tyGraus;
    if (!Double.isFinite(angulo) || Math.abs(angulo) >= 90) return OptionalDouble.empty();
    double tangente = Math.tan(Math.toRadians(angulo));
    if (Math.abs(tangente) < 1e-6) return OptionalDouble.empty();
    double distancia = (alturaAlvoMetros - alturaCameraMetros) / tangente;
    return Double.isFinite(distancia) && distancia > 0
        ? OptionalDouble.of(distancia) : OptionalDouble.empty();
  }
}
