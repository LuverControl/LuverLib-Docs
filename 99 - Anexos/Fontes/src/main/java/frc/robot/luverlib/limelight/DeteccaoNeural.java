package frc.robot.luverlib.limelight;

import edu.wpi.first.math.geometry.Translation2d;
import java.util.List;

/**
 * Detecção bruta do modelo neural/Hailo: classe, ângulos, área e quatro cantos.
 * rawdetections não fornece confiança; o limiar de confiança é configurado no pipeline da câmera.
 */
public record DeteccaoNeural(int classe, double txGraus, double tyGraus,
    double areaPercentual, List<Translation2d> cantosPixels) {
  public DeteccaoNeural {
    cantosPixels = List.copyOf(cantosPixels);
    if (cantosPixels.size() != 4) {
      throw new IllegalArgumentException("Uma detecção deve conter quatro cantos.");
    }
  }
}
