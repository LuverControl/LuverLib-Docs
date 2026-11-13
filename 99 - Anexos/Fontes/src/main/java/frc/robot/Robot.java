package frc.robot;

import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj2.command.CommandScheduler;

/** Ciclo do robô WPILib. Este exemplo permite conferir os controles em simulação. */
public final class Robot extends TimedRobot {
  private final RobotContainer configuracao = new RobotContainer();

  @Override
  public void robotPeriodic() {
    CommandScheduler.getInstance().run();
  }

  @Override
  public void disabledInit() {
    CommandScheduler.getInstance().cancelAll();
    configuracao.pararVibracao();
  }
}
