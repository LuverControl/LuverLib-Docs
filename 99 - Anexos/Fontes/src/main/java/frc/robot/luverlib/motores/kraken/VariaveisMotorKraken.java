package frc.robot.luverlib.motores.kraken;

import frc.robot.Constants;

/**
 * Repassa os ajustes de Constants.Kraken para as configurações dos motores.
 */
public final class VariaveisMotorKraken {
  public static final ModeloKraken MODELO_PADRAO = Constants.Kraken.MODELO_PADRAO;
  public static final double LIMITE_ALIMENTACAO_AMPERES =
      Constants.Kraken.LIMITE_ALIMENTACAO_AMPERES;
  public static final double LIMITE_ESTATOR_AMPERES = Constants.Kraken.LIMITE_ESTATOR_AMPERES;
  public static final boolean INVERTER_SENTIDO = Constants.Kraken.INVERTER_SENTIDO;
  public static final boolean FREAR_AO_PARAR = Constants.Kraken.FREAR_AO_PARAR;
  public static final double TENSAO_MAXIMA_VOLTS = Constants.Kraken.TENSAO_MAXIMA_VOLTS;
  public static final double RAMPA_MALHA_ABERTA_SEGUNDOS =
      Constants.Kraken.RAMPA_MALHA_ABERTA_SEGUNDOS;
  public static final double RAMPA_MALHA_FECHADA_SEGUNDOS =
      Constants.Kraken.RAMPA_MALHA_FECHADA_SEGUNDOS;
  public static final double ROTACOES_MOTOR_POR_ROTACAO_SAIDA =
      Constants.Kraken.ROTACOES_MOTOR_POR_ROTACAO_SAIDA;
  public static final double LIMITE_SAIDA = Constants.Kraken.LIMITE_SAIDA;

  // Operações de configuração são bloqueantes e só acontecem durante a inicialização.
  public static final double TIMEOUT_CONFIGURACAO_SEGUNDOS =
      Constants.Kraken.TIMEOUT_CONFIGURACAO_SEGUNDOS;
  public static final int TENTATIVAS_CONFIGURACAO = Constants.Kraken.TENTATIVAS_CONFIGURACAO;
  public static final double FREQUENCIA_ENCODER_HZ = Constants.Kraken.FREQUENCIA_ENCODER_HZ;
  public static final double FREQUENCIA_TELEMETRIA_HZ = Constants.Kraken.FREQUENCIA_TELEMETRIA_HZ;

  public static ConfiguracaoMotorKraken configuracaoPadrao() {
    return configuracaoPadrao(MODELO_PADRAO);
  }

  public static ConfiguracaoMotorKraken configuracaoPadrao(ModeloKraken modelo) {
    return new ConfiguracaoMotorKraken(modelo, LIMITE_ALIMENTACAO_AMPERES,
        LIMITE_ESTATOR_AMPERES, INVERTER_SENTIDO, FREAR_AO_PARAR, TENSAO_MAXIMA_VOLTS,
        RAMPA_MALHA_ABERTA_SEGUNDOS, RAMPA_MALHA_FECHADA_SEGUNDOS,
        ROTACOES_MOTOR_POR_ROTACAO_SAIDA, LIMITE_SAIDA);
  }

  private VariaveisMotorKraken() {}
}
