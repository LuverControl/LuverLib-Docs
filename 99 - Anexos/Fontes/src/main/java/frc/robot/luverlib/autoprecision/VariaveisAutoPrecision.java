package frc.robot.luverlib.autoprecision;

import frc.robot.Constants;

/**
 * Temporização recebida de Constants.AutoPrecision. Ganhos e limites físicos pertencem a cada mecanismo.
 */
public final class VariaveisAutoPrecision {
  public static final double PERIODO_SEGUNDOS = Constants.AutoPrecision.PERIODO_SEGUNDOS;
  public static final double TIMEOUT_HEARTBEAT_SEGUNDOS =
      Constants.AutoPrecision.TIMEOUT_HEARTBEAT_SEGUNDOS;
  // Identificador interno do contrato de comunicação; não é parâmetro de ajuste do robô.
  public static final int VERSAO_PROTOCOLO = 1;
  private VariaveisAutoPrecision() {}
}
