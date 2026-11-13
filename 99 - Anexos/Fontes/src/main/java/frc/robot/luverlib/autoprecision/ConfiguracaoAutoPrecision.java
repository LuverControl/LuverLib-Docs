package frc.robot.luverlib.autoprecision;

import java.util.Objects;

/** Configuração por mecanismo. O supervisor só pode escolher ganhos/alvos dentro destes limites. */
public record ConfiguracaoAutoPrecision(
    String unidade, LimitesAutoPrecision limites, GanhosPID ganhosIniciais, GanhosPID ganhosMaximos,
    double toleranciaPosicao, double toleranciaVelocidade, double tempoEstavelSegundos,
    double duracaoMaximaEnsaioSegundos, double periodoSegundos, double timeoutHeartbeatSegundos,
    double zonaIntegral, double acumuladorIntegralMaximo) {
  public ConfiguracaoAutoPrecision {
    Objects.requireNonNull(unidade);
    Objects.requireNonNull(limites);
    Objects.requireNonNull(ganhosIniciais);
    Objects.requireNonNull(ganhosMaximos);
    if (unidade.isBlank() || !ganhosIniciais.dentroDe(ganhosMaximos)) {
      throw new IllegalArgumentException("Informe unidade e ganhos iniciais dentro dos limites.");
    }
    for (double valor : new double[] {toleranciaPosicao, toleranciaVelocidade, tempoEstavelSegundos,
        duracaoMaximaEnsaioSegundos, periodoSegundos, timeoutHeartbeatSegundos, zonaIntegral,
        acumuladorIntegralMaximo}) {
      LimitesAutoPrecision.positivo(valor);
    }
    if (periodoSegundos < 0.005 || periodoSegundos > 0.1
        || timeoutHeartbeatSegundos < 3 * periodoSegundos || timeoutHeartbeatSegundos > 2
        || duracaoMaximaEnsaioSegundos > 30 || tempoEstavelSegundos >= duracaoMaximaEnsaioSegundos
        || toleranciaPosicao * 2 >= limites.posicaoMaxima() - limites.posicaoMinima()) {
      throw new IllegalArgumentException("Temporização ou tolerância incompatível com os limites.");
    }
  }
}
