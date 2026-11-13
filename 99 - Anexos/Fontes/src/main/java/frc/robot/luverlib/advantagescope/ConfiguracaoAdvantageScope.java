package frc.robot.luverlib.advantagescope;

/** Ajustes da publicação; nomes, sensores e dimensões do robô continuam no projeto. */
public record ConfiguracaoAdvantageScope(
    String raizTabela, double intervaloSegundos, boolean atualizacaoAutomatica) {
  public ConfiguracaoAdvantageScope {
    validarCaminho(raizTabela);
    double micros = intervaloSegundos * 1_000_000.0;
    if (!Double.isFinite(micros) || micros < 1.0 || micros >= Long.MAX_VALUE) {
      throw new IllegalArgumentException("Informe um intervalo finito de pelo menos 1 microssegundo.");
    }
  }

  public ConfiguracaoAdvantageScope comRaiz(String raiz) {
    return new ConfiguracaoAdvantageScope(raiz, intervaloSegundos, atualizacaoAutomatica);
  }

  public ConfiguracaoAdvantageScope comAtualizacaoAutomatica(boolean automatica) {
    return new ConfiguracaoAdvantageScope(raizTabela, intervaloSegundos, automatica);
  }

  static void validarCaminho(String caminho) {
    if (caminho == null || caminho.isBlank() || caminho.startsWith("/")
        || caminho.endsWith("/") || caminho.contains("\\")
        || caminho.codePoints().anyMatch(Character::isISOControl)) {
      throw new IllegalArgumentException("Use um caminho relativo, preenchido e sem barras nas pontas.");
    }
    for (String parte : caminho.split("/", -1)) {
      if (parte.isBlank() || parte.equals(".") || parte.equals("..")) {
        throw new IllegalArgumentException("O caminho não pode conter segmentos vazios, . ou ...");
      }
    }
  }
}
