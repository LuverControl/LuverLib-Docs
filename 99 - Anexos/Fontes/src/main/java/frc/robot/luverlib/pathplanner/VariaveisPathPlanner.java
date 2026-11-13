package frc.robot.luverlib.pathplanner;

import com.pathplanner.lib.config.PIDConstants;
import com.pathplanner.lib.path.PathConstraints;
import frc.robot.Constants;

/**
 * Monta configurações com os PIDs e limites de Constants.PathPlanner.
 * Massa, inércia, rodas, redução e geometria são configuradas no aplicativo ou no RobotConfig.
 */
public final class VariaveisPathPlanner {
  // PID de posição X/Y e de orientação, usado no swerve. Diferencial usa o controlador LTV.
  public static final PIDConstants PID_TRANSLACAO = Constants.PathPlanner.PID_TRANSLACAO;
  public static final PIDConstants PID_ROTACAO = Constants.PathPlanner.PID_ROTACAO;

  // Limites do PATHFINDING. Os caminhos desenhados usam os limites salvos no aplicativo.
  public static final double VELOCIDADE_MAXIMA_METROS_POR_SEGUNDO =
      Constants.PathPlanner.VELOCIDADE_MAXIMA_METROS_POR_SEGUNDO;
  public static final double ACELERACAO_MAXIMA_METROS_POR_SEGUNDO_QUADRADO =
      Constants.PathPlanner.ACELERACAO_MAXIMA_METROS_POR_SEGUNDO_QUADRADO;
  public static final double VELOCIDADE_ANGULAR_MAXIMA_GRAUS_POR_SEGUNDO =
      Constants.PathPlanner.VELOCIDADE_ANGULAR_MAXIMA_GRAUS_POR_SEGUNDO;
  public static final double ACELERACAO_ANGULAR_MAXIMA_GRAUS_POR_SEGUNDO_QUADRADO =
      Constants.PathPlanner.ACELERACAO_ANGULAR_MAXIMA_GRAUS_POR_SEGUNDO_QUADRADO;
  public static final double TENSAO_NOMINAL_VOLTS = Constants.PathPlanner.TENSAO_NOMINAL_VOLTS;

  // Deve corresponder ao período real de atualização do drivetrain.
  public static final double PERIODO_CONTROLE_SEGUNDOS =
      Constants.PathPlanner.PERIODO_CONTROLE_SEGUNDOS;

  public static ConfiguracaoPathPlanner configuracaoPadrao() {
    return new ConfiguracaoPathPlanner(PID_TRANSLACAO, PID_ROTACAO,
        new PathConstraints(VELOCIDADE_MAXIMA_METROS_POR_SEGUNDO,
            ACELERACAO_MAXIMA_METROS_POR_SEGUNDO_QUADRADO,
            Math.toRadians(VELOCIDADE_ANGULAR_MAXIMA_GRAUS_POR_SEGUNDO),
            Math.toRadians(ACELERACAO_ANGULAR_MAXIMA_GRAUS_POR_SEGUNDO_QUADRADO),
            TENSAO_NOMINAL_VOLTS), PERIODO_CONTROLE_SEGUNDOS);
  }

  private VariaveisPathPlanner() {}
}
