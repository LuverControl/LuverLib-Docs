package frc.robot.luverlib.motores.neo;

import frc.robot.Constants;

/**
 * Repassa os ajustes de Constants.Neo e Constants.Tracao para as configurações NEO.
 */
public final class VariaveisMotor {
  // Limite inteligente de corrente do NEO, em amperes. Deve ser maior que zero.
  public static final int LIMITE_CORRENTE_AMPERES = Constants.Neo.LIMITE_CORRENTE_AMPERES;

  // true troca o sentido de rotação do motor.
  public static final boolean INVERTER_SENTIDO = Constants.Neo.INVERTER_SENTIDO;

  // true usa freio elétrico ao parar; false deixa o motor girar livremente.
  // Freio elétrico não é controle de posição: não sustenta um braço ou elevador sozinho.
  public static final boolean FREAR_AO_PARAR = Constants.Neo.FREAR_AO_PARAR;

  // Tensão de referência da compensação, em volts.
  public static final double TENSAO_NOMINAL_VOLTS = Constants.Neo.TENSAO_NOMINAL_VOLTS;

  // Tempo de zero até a saída completa, em segundos. Zero desativa a rampa.
  // Malha aberta: comandos de potência/tensão. Malha fechada: futuros comandos com PID.
  public static final double RAMPA_MALHA_ABERTA_SEGUNDOS = Constants.Neo.RAMPA_MALHA_ABERTA_SEGUNDOS;
  public static final double RAMPA_MALHA_FECHADA_SEGUNDOS =
      Constants.Neo.RAMPA_MALHA_FECHADA_SEGUNDOS;

  // 1.0 = ligação direta; 10.0 = dez voltas do motor para uma volta do mecanismo.
  // As leituras passam a ser rotações e RPM do MECANISMO, depois dessa redução.
  public static final double ROTACOES_MOTOR_POR_ROTACAO_SAIDA =
      Constants.Neo.ROTACOES_MOTOR_POR_ROTACAO_SAIDA;

  // Limite dos comandos desta classe: 1.0 = 100%; 0.5 = até 50% da saída nominal.
  public static final double LIMITE_SAIDA = Constants.Neo.LIMITE_SAIDA;

  // Salva os ajustes no SPARK MAX durante a inicialização, inclusive para um reinício de energia.
  // A configuração é aplicada uma vez ao criar MotorNeo, nunca no laço periódico.
  public static final boolean GRAVAR_CONFIGURACAO = Constants.Neo.GRAVAR_CONFIGURACAO;

  /** Monta uma configuração independente com os valores acima. */
  public static ConfiguracaoMotorNeo configuracaoPadrao() {
    return new ConfiguracaoMotorNeo(
        LIMITE_CORRENTE_AMPERES,
        INVERTER_SENTIDO,
        FREAR_AO_PARAR,
        TENSAO_NOMINAL_VOLTS,
        RAMPA_MALHA_ABERTA_SEGUNDOS,
        RAMPA_MALHA_FECHADA_SEGUNDOS,
        ROTACOES_MOTOR_POR_ROTACAO_SAIDA,
        LIMITE_SAIDA,
        GRAVAR_CONFIGURACAO);
  }

  /** Referências dos ajustes da tração centralizados em Constants.Tracao. */
  public static final class Tracao {
    public static final boolean INVERTER_ESQUERDA = Constants.Tracao.INVERTER_ESQUERDA;
    public static final boolean INVERTER_DIREITA = Constants.Tracao.INVERTER_DIREITA;
    public static final boolean INVERTER_SEGUIDORES = Constants.Tracao.INVERTER_SEGUIDORES;
    public static final int CORRENTE_LIDERES_AMPERES = Constants.Tracao.CORRENTE_LIDERES_AMPERES;
    public static final boolean FREAR_LIDERES = Constants.Tracao.FREAR_LIDERES;
    public static final int CORRENTE_SEGUIDORES_AMPERES =
        Constants.Tracao.CORRENTE_SEGUIDORES_AMPERES;
    public static final boolean FREAR_SEGUIDORES = Constants.Tracao.FREAR_SEGUIDORES;

    public static final double TENSAO_NOMINAL_VOLTS = Constants.Tracao.TENSAO_NOMINAL_VOLTS;
    public static final double RAMPA_MALHA_ABERTA_SEGUNDOS =
        Constants.Tracao.RAMPA_MALHA_ABERTA_SEGUNDOS;
    public static final double RAMPA_MALHA_FECHADA_SEGUNDOS =
        Constants.Tracao.RAMPA_MALHA_FECHADA_SEGUNDOS;
    public static final double ROTACOES_MOTOR_POR_ROTACAO_SAIDA =
        Constants.Tracao.ROTACOES_MOTOR_POR_ROTACAO_SAIDA;
    public static final double LIMITE_POTENCIA = Constants.Tracao.LIMITE_POTENCIA;
    public static final boolean COMPENSAR_TENSAO = Constants.Tracao.COMPENSAR_TENSAO;
    public static final boolean GRAVAR_CONFIGURACAO = Constants.Tracao.GRAVAR_CONFIGURACAO;

    public static ConfiguracaoMotorNeo configuracaoLiderEsquerdo() {
      return configuracao(CORRENTE_LIDERES_AMPERES, INVERTER_ESQUERDA, FREAR_LIDERES);
    }

    public static ConfiguracaoMotorNeo configuracaoLiderDireito() {
      return configuracao(CORRENTE_LIDERES_AMPERES, INVERTER_DIREITA, FREAR_LIDERES);
    }

    public static ConfiguracaoMotorNeo configuracaoSeguidor() {
      return configuracao(CORRENTE_SEGUIDORES_AMPERES, INVERTER_SEGUIDORES, FREAR_SEGUIDORES);
    }

    private static ConfiguracaoMotorNeo configuracao(int corrente, boolean invertido, boolean frear) {
      return new ConfiguracaoMotorNeo(corrente, invertido, frear, TENSAO_NOMINAL_VOLTS,
          RAMPA_MALHA_ABERTA_SEGUNDOS, RAMPA_MALHA_FECHADA_SEGUNDOS,
          ROTACOES_MOTOR_POR_ROTACAO_SAIDA, LIMITE_POTENCIA,
          GRAVAR_CONFIGURACAO, COMPENSAR_TENSAO);
    }

    private Tracao() {}
  }

  private VariaveisMotor() {}
}
