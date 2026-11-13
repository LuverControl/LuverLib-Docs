package guia;

import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.Constants;
import frc.robot.luverlib.controles.ControleXbox;

public final class ControleExemplo {
  private final ControleXbox piloto =
      new ControleXbox(Constants.Controle.PORTA_PILOTO);

  public void associarBotaoA(Command comandoDoMecanismo) {
    piloto.botaoA().whileTrue(comandoDoMecanismo);
  }

  public double lerGatilhoDireito() {
    return piloto.valorGatilhoDireito();
  }

  public void pararVibracao() {
    piloto.pararVibracao();
  }
}
