package frc.robot.luverlib.limelight;

/** Métricas da tag embutidas no próprio pacote de pose; sem combinar tópicos de quadros distintos. */
public record MarcadorAprilTag(int id, double txGraus, double tyGraus, double areaPercentual,
    double distanciaCameraMetros, double distanciaRoboMetros, double ambiguidade) {}
