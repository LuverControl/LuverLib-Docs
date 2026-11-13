package frc.robot.luverlib.pathplanner;

/**
 * Calibração do tank definida pelo projeto, como os ajustes usados na DualFire.
 *
 * @param larguraEntreRodasMetros distância entre os lados do drivetrain
 * @param metrosPorRotacaoEncoder distância percorrida por uma rotação da leitura do encoder;
 *     para RPM bruta do motor: diâmetro da roda * PI / redução. Se o encoder já tiver conversão,
 *     use o fator correspondente à leitura convertida, sem aplicar a redução duas vezes.
 * @param velocidadeMaximaRodaMetrosPorSegundo limite de velocidade dos dois lados
 * @param ksVolts compensação de atrito do feedforward
 * @param kvVoltsPorMetroPorSegundo ganho de velocidade do feedforward
 * @param tensaoMaximaVolts limite de tensão solicitado ao controlador
 * @param inverterEncoderEsquerdo corrige o sinal da RPM esquerda para frente positiva
 * @param inverterEncoderDireito corrige o sinal da RPM direita para frente positiva;
 *     independente da inversão elétrica do motor
 */
public record ConfiguracaoTankPathPlanner(
    double larguraEntreRodasMetros, double metrosPorRotacaoEncoder,
    double velocidadeMaximaRodaMetrosPorSegundo, double ksVolts,
    double kvVoltsPorMetroPorSegundo, double tensaoMaximaVolts,
    boolean inverterEncoderEsquerdo, boolean inverterEncoderDireito) {

  public ConfiguracaoTankPathPlanner {
    ConfiguracaoPathPlanner.exigirPositivo(larguraEntreRodasMetros, "Largura entre rodas");
    ConfiguracaoPathPlanner.exigirPositivo(metrosPorRotacaoEncoder, "Metros por rotação do encoder");
    ConfiguracaoPathPlanner.exigirPositivo(velocidadeMaximaRodaMetrosPorSegundo,
        "Velocidade máxima da roda");
    ConfiguracaoPathPlanner.exigirPositivo(kvVoltsPorMetroPorSegundo, "Ganho kV");
    ConfiguracaoPathPlanner.exigirPositivo(tensaoMaximaVolts, "Tensão máxima do tank");
    if (tensaoMaximaVolts > 24 || !Double.isFinite(ksVolts) || ksVolts < 0
        || ksVolts >= tensaoMaximaVolts) {
      throw new IllegalArgumentException("Use tensão até 24 V e kS entre zero e menor que a tensão.");
    }
  }
}
