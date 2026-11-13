package frc.robot.luverlib.advantagescope;

import frc.robot.Constants;

/**
 * Monta configurações com os ajustes de publicação e gravação de Constants.AdvantageScope.
 */
public final class VariaveisAdvantageScope {
  public static final String RAIZ_TABELA = Constants.AdvantageScope.RAIZ_TABELA;
  public static final double INTERVALO_SEGUNDOS = Constants.AdvantageScope.INTERVALO_SEGUNDOS;
  public static final boolean ATUALIZACAO_AUTOMATICA =
      Constants.AdvantageScope.ATUALIZACAO_AUTOMATICA;
  public static final boolean GRAVAR_JOYSTICKS = Constants.AdvantageScope.GRAVAR_JOYSTICKS;

  public static ConfiguracaoAdvantageScope configuracaoPadrao() {
    return new ConfiguracaoAdvantageScope(
        RAIZ_TABELA, INTERVALO_SEGUNDOS, ATUALIZACAO_AUTOMATICA);
  }

  private VariaveisAdvantageScope() {}
}
