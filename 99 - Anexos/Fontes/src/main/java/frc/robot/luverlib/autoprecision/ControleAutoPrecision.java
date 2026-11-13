package frc.robot.luverlib.autoprecision;

/** Backend local do controle. Velocidade em unidades de posição por segundo; saída em duty cycle. */
public interface ControleAutoPrecision {
  record Medicao(double posicao, double velocidade, double saida, double corrente, double temperatura) {
    public boolean finita() {
      return Double.isFinite(posicao) && Double.isFinite(velocidade) && Double.isFinite(saida)
          && Double.isFinite(corrente) && Double.isFinite(temperatura);
    }
  }

  String identificador();
  Medicao medir();
  void iniciar(ConfiguracaoAutoPrecision configuracao);
  void aplicarGanhos(GanhosPID ganhos);
  void aplicarReferencia(double posicao);
  void parar();
  /** Para e restaura apenas os parâmetros temporários sob responsabilidade deste backend. */
  void finalizar();
}
