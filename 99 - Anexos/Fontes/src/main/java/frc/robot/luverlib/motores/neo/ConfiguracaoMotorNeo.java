package frc.robot.luverlib.motores.neo;

/**
 * Configuração imutável de um NEO. Os valores padrão vêm de Constants.Neo via VariaveisMotor.
 *
 * @param limiteCorrenteAmperes limite inteligente de corrente, maior que zero
 * @param invertido troca o sentido de rotação
 * @param frearAoParar usa freio elétrico em vez de giro livre
 * @param tensaoNominalVolts referência da compensação de tensão, maior que zero e até 24 V
 * @param rampaMalhaAbertaSegundos tempo para atingir saída completa; zero desativa a rampa
 * @param rampaMalhaFechadaSegundos rampa para controle fechado; zero desativa a rampa
 * @param rotacoesMotorPorRotacaoSaida redução: voltas do motor para uma volta do mecanismo
 * @param limiteSaida limite dos comandos de MotorNeo, de 0 a 1
 * @param gravarConfiguracao salva os parâmetros na memória do SPARK MAX na inicialização
 * @param compensarTensao habilita a compensação; false mantém a resposta percentual sem compensação
 */
public record ConfiguracaoMotorNeo(
    int limiteCorrenteAmperes,
    boolean invertido,
    boolean frearAoParar,
    double tensaoNominalVolts,
    double rampaMalhaAbertaSegundos,
    double rampaMalhaFechadaSegundos,
    double rotacoesMotorPorRotacaoSaida,
    double limiteSaida,
    boolean gravarConfiguracao,
    boolean compensarTensao) {

  /** Mantém os construtores existentes com a compensação habilitada, como antes. */
  public ConfiguracaoMotorNeo(int limiteCorrenteAmperes, boolean invertido, boolean frearAoParar,
      double tensaoNominalVolts, double rampaMalhaAbertaSegundos,
      double rampaMalhaFechadaSegundos, double rotacoesMotorPorRotacaoSaida,
      double limiteSaida, boolean gravarConfiguracao) {
    this(limiteCorrenteAmperes, invertido, frearAoParar, tensaoNominalVolts,
        rampaMalhaAbertaSegundos, rampaMalhaFechadaSegundos, rotacoesMotorPorRotacaoSaida,
        limiteSaida, gravarConfiguracao, true);
  }

  public ConfiguracaoMotorNeo {
    if (limiteCorrenteAmperes <= 0) {
      throw new IllegalArgumentException("O limite de corrente deve ser maior que zero.");
    }
    validarParametroRev(tensaoNominalVolts, "Tensão nominal", false);
    if (tensaoNominalVolts > 24.0) {
      throw new IllegalArgumentException("A tensão nominal deve ser de até 24 V.");
    }
    validarParametroRev(rampaMalhaAbertaSegundos, "Rampa de malha aberta", true);
    validarParametroRev(rampaMalhaFechadaSegundos, "Rampa de malha fechada", true);
    if (!Double.isFinite(rotacoesMotorPorRotacaoSaida) || rotacoesMotorPorRotacaoSaida <= 0.0) {
      throw new IllegalArgumentException("A redução deve ser finita e maior que zero.");
    }
    validarParametroRev(1.0 / rotacoesMotorPorRotacaoSaida, "Fator do encoder", false);
    if (!Double.isFinite(limiteSaida) || limiteSaida < 0.0 || limiteSaida > 1.0) {
      throw new IllegalArgumentException("O limite de saída deve ser finito, de 0 a 1.");
    }
  }

  /** Altera a corrente só nesta cópia; não modifica o padrão ou outro motor. */
  public ConfiguracaoMotorNeo comLimiteCorrente(int amperes) {
    return new ConfiguracaoMotorNeo(amperes, invertido, frearAoParar, tensaoNominalVolts,
        rampaMalhaAbertaSegundos, rampaMalhaFechadaSegundos, rotacoesMotorPorRotacaoSaida,
        limiteSaida, gravarConfiguracao, compensarTensao);
  }

  public ConfiguracaoMotorNeo comInversao(boolean inverter) {
    return new ConfiguracaoMotorNeo(limiteCorrenteAmperes, inverter, frearAoParar, tensaoNominalVolts,
        rampaMalhaAbertaSegundos, rampaMalhaFechadaSegundos, rotacoesMotorPorRotacaoSaida,
        limiteSaida, gravarConfiguracao, compensarTensao);
  }

  public ConfiguracaoMotorNeo comLimiteSaida(double limite) {
    return new ConfiguracaoMotorNeo(limiteCorrenteAmperes, invertido, frearAoParar, tensaoNominalVolts,
        rampaMalhaAbertaSegundos, rampaMalhaFechadaSegundos, rotacoesMotorPorRotacaoSaida,
        limite, gravarConfiguracao, compensarTensao);
  }

  public ConfiguracaoMotorNeo comCompensacaoTensao(boolean habilitada) {
    return new ConfiguracaoMotorNeo(limiteCorrenteAmperes, invertido, frearAoParar, tensaoNominalVolts,
        rampaMalhaAbertaSegundos, rampaMalhaFechadaSegundos, rotacoesMotorPorRotacaoSaida,
        limiteSaida, gravarConfiguracao, habilitada);
  }

  /** Conversão de rotações/RPM do motor para rotações/RPM da saída do mecanismo. */
  public double fatorEncoder() {
    return 1.0 / rotacoesMotorPorRotacaoSaida;
  }

  private static void validarParametroRev(double valor, String nome, boolean permiteZero) {
    // A REVLib transmite esses parâmetros como float; evita infinito ou zero por conversão.
    float convertido = (float) valor;
    if (!Double.isFinite(valor) || valor < 0.0 || !Float.isFinite(convertido)
        || (!permiteZero && convertido == 0.0f) || (valor > 0.0 && convertido == 0.0f)) {
      throw new IllegalArgumentException(nome + " deve ser finito, "
          + (permiteZero ? "maior ou igual a zero" : "maior que zero")
          + " e representável pela REVLib.");
    }
  }
}
