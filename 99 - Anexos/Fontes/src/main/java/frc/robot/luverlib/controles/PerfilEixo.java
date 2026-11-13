package frc.robot.luverlib.controles;

/**
 * Ajustes de um eixo analógico, aplicados nesta ordem: zona morta, curva, limite e inversão.
 *
 * @param zonaMorta valores até esse tamanho viram zero; deve estar entre 0 e menor que 1
 * @param expoente 1 mantém a resposta linear; 2 suaviza os movimentos perto do centro
 * @param limiteSaida maior valor absoluto permitido na saída; de 0 a 1
 * @param invertido true troca o sinal do eixo
 */
public record PerfilEixo(
    double zonaMorta, double expoente, double limiteSaida, boolean invertido) {

  public PerfilEixo {
    if (!Double.isFinite(zonaMorta) || zonaMorta < 0.0 || zonaMorta >= 1.0) {
      throw new IllegalArgumentException("A zona morta deve ser finita, de 0 a menor que 1.");
    }
    if (!Double.isFinite(expoente) || expoente < 1.0) {
      throw new IllegalArgumentException("O expoente deve ser finito e maior ou igual a 1.");
    }
    if (!Double.isFinite(limiteSaida) || limiteSaida < 0.0 || limiteSaida > 1.0) {
      throw new IllegalArgumentException("O limite de saída deve ser finito, de 0 a 1.");
    }
  }

  /** Calcula a saída. Mantém o curso completo após a zona morta e zera entradas inválidas. */
  public double aplicar(double entrada) {
    if (!Double.isFinite(entrada)) {
      return 0.0;
    }
    double tamanho = Math.min(Math.abs(entrada), 1.0);
    if (tamanho <= zonaMorta) {
      return 0.0;
    }
    double normalizado = (tamanho - zonaMorta) / (1.0 - zonaMorta);
    double saida = Math.copySign(Math.pow(normalizado, expoente) * limiteSaida, entrada);
    return invertido ? -saida : saida;
  }
}
