package frc.robot.luverlib.limelight;

/** As poses usam sempre a origem azul da WPILib, inclusive quando a aliança é vermelha. */
public enum MetodoPose {
  MEGATAG_1("botpose_wpiblue"),
  MEGATAG_2("botpose_orb_wpiblue");

  private final String topico;

  MetodoPose(String topico) { this.topico = topico; }

  public String topico() { return topico; }
}
