package frc.robot.luverlib.limelight;

import frc.robot.Constants;
import java.util.Optional;

/**
 * Monta a política de visão com os ajustes de Constants.Limelight.
 * Configure nomes, pipelines e geometria no projeto e limites do campo com comLimitesCampo.
 */
public final class VariaveisLimelight {
  public static final MetodoPose METODO_POSE = Constants.Limelight.METODO_POSE;
  public static final double IDADE_MAXIMA_QUADRO_SEGUNDOS =
      Constants.Limelight.IDADE_MAXIMA_QUADRO_SEGUNDOS;
  public static final double IDADE_MAXIMA_ORIENTACAO_SEGUNDOS =
      Constants.Limelight.IDADE_MAXIMA_ORIENTACAO_SEGUNDOS;
  public static final double GIRO_MAXIMO_GRAUS_SEGUNDO =
      Constants.Limelight.GIRO_MAXIMO_GRAUS_SEGUNDO;
  public static final double AMBIGUIDADE_MAXIMA_TAG_UNICA =
      Constants.Limelight.AMBIGUIDADE_MAXIMA_TAG_UNICA;
  public static final double ERRO_Z_MAXIMO_METROS = Constants.Limelight.ERRO_Z_MAXIMO_METROS;
  public static final double DISTANCIA_MAXIMA_TAGS_METROS =
      Constants.Limelight.DISTANCIA_MAXIMA_TAGS_METROS;
  // Baselines também funcionam como piso para evitar confiança excessiva a curta distância.
  public static final double DESVIO_LINEAR_BASE_METROS =
      Constants.Limelight.DESVIO_LINEAR_BASE_METROS;
  public static final double DESVIO_ANGULAR_BASE_RADIANOS =
      Constants.Limelight.DESVIO_ANGULAR_BASE_RADIANOS;
  public static final double FATOR_CONFIANCA = Constants.Limelight.FATOR_CONFIANCA;
  public static final double FATOR_LINEAR_MEGATAG2 = Constants.Limelight.FATOR_LINEAR_MEGATAG2;

  public static ConfiguracaoLimelight configuracaoPadrao() {
    return new ConfiguracaoLimelight(METODO_POSE, IDADE_MAXIMA_QUADRO_SEGUNDOS,
        IDADE_MAXIMA_ORIENTACAO_SEGUNDOS, GIRO_MAXIMO_GRAUS_SEGUNDO,
        AMBIGUIDADE_MAXIMA_TAG_UNICA, ERRO_Z_MAXIMO_METROS, DISTANCIA_MAXIMA_TAGS_METROS,
        DESVIO_LINEAR_BASE_METROS, DESVIO_ANGULAR_BASE_RADIANOS,
        FATOR_CONFIANCA, FATOR_LINEAR_MEGATAG2, Optional.empty());
  }

  private VariaveisLimelight() {}
}
