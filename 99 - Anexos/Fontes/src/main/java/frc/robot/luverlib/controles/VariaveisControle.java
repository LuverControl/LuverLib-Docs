package frc.robot.luverlib.controles;

import frc.robot.Constants;

/**
 * Referências de compatibilidade; edite portas e perfis em Constants.Controle
 * e pilotagem em Constants.Tracao.
 */
public final class VariaveisControle {
  // Posição USB do controle na Driver Station: de 0 a 5.
  public static final int PORTA_PILOTO = Constants.Controle.PORTA_PILOTO;
  public static final int PORTA_OPERADOR = Constants.Controle.PORTA_OPERADOR;

  // Zona morta: 0.08 ignora os 8% centrais e ajuda a evitar movimento com o controle solto.
  // Expoente: 1.0 é linear; 2.0 permite ajustes mais suaves perto do centro.
  // Limite de saída: 1.0 libera 100%; 0.5 limita a saída deste eixo a 50%.
  // Inversão: false mantém o sentido; true troca positivo por negativo.

  public static final PerfilEixo ANALOGICO_ESQUERDO_X = Constants.Controle.ANALOGICO_ESQUERDO_X;

  public static final PerfilEixo ANALOGICO_ESQUERDO_Y = Constants.Controle.ANALOGICO_ESQUERDO_Y;

  public static final PerfilEixo ANALOGICO_DIREITO_X = Constants.Controle.ANALOGICO_DIREITO_X;

  public static final PerfilEixo ANALOGICO_DIREITO_Y = Constants.Controle.ANALOGICO_DIREITO_Y;

  // Gatilhos variam de 0 a 1. O evento ativa quando o valor PASSA deste limite.
  // Exemplo: 0.5 exige apertar mais da metade do curso. Não altera a leitura analógica.
  public static final double LIMIAR_GATILHOS = Constants.Controle.LIMIAR_GATILHOS;

  /** Referências da resposta de pilotagem definida em Constants.Tracao. */
  public static final class Tracao {
    public static final double ESCALA_FRENTE = Constants.Tracao.ESCALA_FRENTE;
    public static final double ESCALA_GIRO = Constants.Tracao.ESCALA_GIRO;
    // Corte simples, sem reescalonar a leitura, como no código original do DualFire.
    public static final double ZONA_MORTA = Constants.Tracao.ZONA_MORTA;

    private Tracao() {}
  }

  private VariaveisControle() {}
}
