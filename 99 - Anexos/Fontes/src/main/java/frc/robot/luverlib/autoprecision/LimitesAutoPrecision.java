package frc.robot.luverlib.autoprecision;

/**
 * Limites do mecanismo definidos pelo projeto. Velocidade/aceleração máximas definem o perfil;
 * velocidadeMaximaMedida é o corte físico independente, em unidades de posição por segundo.
 */
public record LimitesAutoPrecision(
    double posicaoMinima, double posicaoMaxima, double saidaMaxima,
    double velocidadeMaxima, double aceleracaoMaxima, double correnteMaxima,
    double temperaturaMaxima, double deslocamentoMaximoEnsaio, double velocidadeMaximaMedida) {
  /** Compatibilidade: neste construtor, a velocidade do perfil também é o corte de velocidade medida. */
  public LimitesAutoPrecision(double posicaoMinima, double posicaoMaxima, double saidaMaxima,
      double velocidadeMaxima, double aceleracaoMaxima, double correnteMaxima,
      double temperaturaMaxima, double deslocamentoMaximoEnsaio) {
    this(posicaoMinima, posicaoMaxima, saidaMaxima, velocidadeMaxima, aceleracaoMaxima,
        correnteMaxima, temperaturaMaxima, deslocamentoMaximoEnsaio, velocidadeMaxima);
  }
  public LimitesAutoPrecision {
    finito(posicaoMinima);
    finito(posicaoMaxima);
    if (posicaoMinima >= posicaoMaxima) {
      throw new IllegalArgumentException("A posição mínima deve ser menor que a máxima.");
    }
    for (double valor : new double[] {saidaMaxima, velocidadeMaxima, aceleracaoMaxima,
        correnteMaxima, temperaturaMaxima, deslocamentoMaximoEnsaio, velocidadeMaximaMedida}) {
      positivo(valor);
    }
    if (saidaMaxima > 1 || deslocamentoMaximoEnsaio > posicaoMaxima - posicaoMinima
        || velocidadeMaximaMedida < velocidadeMaxima) {
      throw new IllegalArgumentException("Saída até 1, deslocamento dentro da faixa e corte de "
          + "velocidade medida maior ou igual à velocidade do perfil.");
    }
  }

  static void finito(double valor) {
    if (!Double.isFinite(valor) || !Float.isFinite((float) valor)) {
      throw new IllegalArgumentException("O valor deve ser finito e representável pelo controlador.");
    }
  }

  static void positivo(double valor) {
    finito(valor);
    if (valor <= 0 || (float) valor == 0) {
      throw new IllegalArgumentException("O valor deve ser maior que zero.");
    }
  }
}
