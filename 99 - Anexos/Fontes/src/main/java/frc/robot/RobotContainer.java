package frc.robot;

import frc.robot.luverlib.controles.ControleXbox;
import frc.robot.luverlib.telemetria.Telemetria;
import frc.robot.luverlib.telemetria.TelemetriaXbox;
import frc.robot.commands.DirigirTank;
import frc.robot.subsystems.ChassiTank;
import java.util.Objects;

/** Cria os subsistemas e controles do robô e associa seus comandos, como no DualFire. */
public final class RobotContainer implements AutoCloseable {
  private final ChassiTank chassi;
  private final ControleXbox piloto = new ControleXbox(Constants.Controle.PORTA_PILOTO);
  private final TelemetriaXbox telemetriaPiloto;
  private final boolean proprietarioDoChassi;

  public RobotContainer() {
    this(new ChassiTank(), true);
  }

  /** Permite testar a composição com um chassi simulado; quem o fornece continua seu proprietário. */
  public RobotContainer(ChassiTank chassi) {
    this(chassi, false);
  }

  private RobotContainer(ChassiTank chassi, boolean proprietarioDoChassi) {
    this.chassi = Objects.requireNonNull(chassi);
    this.proprietarioDoChassi = proprietarioDoChassi;
    telemetriaPiloto = Telemetria.registrarControle("Xbox", piloto);
    configurarComandos();
  }

  private void configurarComandos() {
    chassi.setDefaultCommand(new DirigirTank(chassi, piloto));
  }

  public ChassiTank obterChassi() { return chassi; }

  public void pararVibracao() {
    piloto.pararVibracao();
  }

  public void pararTudo() {
    chassi.parar();
    piloto.pararVibracao();
  }

  @Override
  public void close() {
    try { pararTudo(); }
    finally {
      telemetriaPiloto.close();
      if (proprietarioDoChassi) chassi.close();
    }
  }
}
