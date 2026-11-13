package guia;

import frc.robot.luverlib.motores.neo.MotorNeo;
import frc.robot.luverlib.motores.neo.VariaveisMotor;
import frc.robot.luverlib.motores.kraken.ModeloKraken;
import frc.robot.luverlib.motores.kraken.MotorKraken;
import frc.robot.luverlib.motores.kraken.VariaveisMotorKraken;

public final class MotoresExemplo {
  private MotoresExemplo() {}

  // Os IDs recebidos devem ser declarados em Constants pelo projeto.
  public static MotorNeo criarNeo(int idCanDeConstants) {
    return new MotorNeo(idCanDeConstants, VariaveisMotor.configuracaoPadrao());
  }

  public static MotorNeo criarSeguidor(int idCanDeConstants, MotorNeo lider) {
    return new MotorNeo(idCanDeConstants,
        VariaveisMotor.Tracao.configuracaoSeguidor(), lider);
  }

  public static MotorKraken criarKraken(int idCanDeConstants, ModeloKraken modelo) {
    return new MotorKraken(idCanDeConstants,
        VariaveisMotorKraken.configuracaoPadrao(modelo));
  }
}
