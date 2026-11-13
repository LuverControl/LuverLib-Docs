
package frc.robot;

import com.pathplanner.lib.config.PIDConstants;
import frc.robot.luverlib.controles.PerfilEixo;
import frc.robot.luverlib.limelight.MetodoPose;
import frc.robot.luverlib.motores.kraken.ModeloKraken;

/**
 * Ponto central para IDs de qualquer motor/dispositivo, PIDs e parâmetros de ajuste rápido.
 * Após alterar, compile e execute novamente. Os valores iniciais devem corresponder ao mecanismo.
 * Qualquer conteúdo além de identificação e ajustes exige autorização prévia do dono.
 */
public final class Constants {
  /** Identificação e ajustes da tração DualFire. */
  public static final class Tracao {
    // IDs CAN do DualFire: dianteiros são líderes, traseiros são seguidores.
    public static final int LIDER_ESQUERDO = 4;
    public static final int SEGUIDOR_ESQUERDO = 2;
    public static final int LIDER_DIREITO = 1;
    public static final int SEGUIDOR_DIREITO = 3;
    public static final int PIGEON = 20;

    public static final boolean INVERTER_ESQUERDA = false;
    public static final boolean INVERTER_DIREITA = true;
    public static final boolean INVERTER_SEGUIDORES = false;
    public static final int CORRENTE_LIDERES_AMPERES = 40;
    public static final boolean FREAR_LIDERES = true;
    public static final int CORRENTE_SEGUIDORES_AMPERES = 80;
    public static final boolean FREAR_SEGUIDORES = false;

    public static final double TENSAO_NOMINAL_VOLTS = 12.0;
    public static final double RAMPA_MALHA_ABERTA_SEGUNDOS = 0.0;
    public static final double RAMPA_MALHA_FECHADA_SEGUNDOS = 0.0;
    public static final double ROTACOES_MOTOR_POR_ROTACAO_SAIDA = 1.0;
    public static final double LIMITE_POTENCIA = 1.0;
    public static final boolean COMPENSAR_TENSAO = false;
    public static final boolean GRAVAR_CONFIGURACAO = true;

    public static final double ESCALA_FRENTE = 0.4;
    public static final double ESCALA_GIRO = 0.2;
    // Corte simples do DualFire, sem reescalonar a leitura.
    public static final double ZONA_MORTA = 0.05;

    private Tracao() {}
  }

  /** Ajustes iniciais dos NEO com SPARK MAX; IDs ficam no grupo de cada mecanismo. */
  public static final class Neo {
    public static final int LIMITE_CORRENTE_AMPERES = 40;
    public static final boolean INVERTER_SENTIDO = false;
    // Freio elétrico não sustenta posição como um PID.
    public static final boolean FREAR_AO_PARAR = true;
    public static final double TENSAO_NOMINAL_VOLTS = 12.0;
    // Tempo de zero até saída completa; zero desativa a rampa.
    public static final double RAMPA_MALHA_ABERTA_SEGUNDOS = 0.2;
    public static final double RAMPA_MALHA_FECHADA_SEGUNDOS = 0.0;
    // 1.0 = ligação direta; 10.0 = dez voltas do motor por volta do mecanismo.
    public static final double ROTACOES_MOTOR_POR_ROTACAO_SAIDA = 1.0;
    public static final double LIMITE_SAIDA = 1.0;
    // Aplicada uma vez na inicialização, fora do laço periódico.
    public static final boolean GRAVAR_CONFIGURACAO = true;

    private Neo() {}
  }

  /** Ajustes iniciais dos Kraken com Talon FX. */
  public static final class Kraken {
    public static final ModeloKraken MODELO_PADRAO = ModeloKraken.X60;
    public static final double LIMITE_ALIMENTACAO_AMPERES = 40;
    public static final double LIMITE_ESTATOR_AMPERES = 60;
    public static final boolean INVERTER_SENTIDO = false;
    public static final boolean FREAR_AO_PARAR = true;
    public static final double TENSAO_MAXIMA_VOLTS = 12;
    public static final double RAMPA_MALHA_ABERTA_SEGUNDOS = 0.2;
    public static final double RAMPA_MALHA_FECHADA_SEGUNDOS = 0;
    public static final double ROTACOES_MOTOR_POR_ROTACAO_SAIDA = 1;
    public static final double LIMITE_SAIDA = 1;
    public static final double TIMEOUT_CONFIGURACAO_SEGUNDOS = 0.25;
    public static final int TENTATIVAS_CONFIGURACAO = 3;
    public static final double FREQUENCIA_ENCODER_HZ = 50;
    public static final double FREQUENCIA_TELEMETRIA_HZ = 10;

    private Kraken() {}
  }

  public static final class Controle {
    // Posição USB na Driver Station, de 0 a 5.
    public static final int PORTA_PILOTO = 0;
    public static final int PORTA_OPERADOR = 1;
    // PerfilEixo: zona morta, expoente, limite de saída e inversão.
    // Expoente 1.0 é linear; 2.0 suaviza perto do centro; limite 1.0 libera 100%.
    public static final PerfilEixo ANALOGICO_ESQUERDO_X = new PerfilEixo(0.08, 1.0, 1.0, false);
    // Na WPILib, empurrar para frente dá Y negativo; true inverte o sentido.
    public static final PerfilEixo ANALOGICO_ESQUERDO_Y = new PerfilEixo(0.08, 1.0, 1.0, false);
    public static final PerfilEixo ANALOGICO_DIREITO_X = new PerfilEixo(0.08, 1.0, 1.0, false);
    public static final PerfilEixo ANALOGICO_DIREITO_Y = new PerfilEixo(0.08, 1.0, 1.0, false);
    // Evento ativa quando o gatilho passa deste limite; não muda a leitura analógica.
    public static final double LIMIAR_GATILHOS = 0.5;

    private Controle() {}
  }

  public static final class PathPlanner {
    // PID de posição X/Y e orientação do swerve; o diferencial usa LTV.
    public static final PIDConstants PID_TRANSLACAO = new PIDConstants(5.0, 0.0, 0.0);
    public static final PIDConstants PID_ROTACAO = new PIDConstants(5.0, 0.0, 0.0);
    // Limites de pathfinding; caminhos desenhados usam os limites salvos no aplicativo.
    public static final double VELOCIDADE_MAXIMA_METROS_POR_SEGUNDO = 2.0;
    public static final double ACELERACAO_MAXIMA_METROS_POR_SEGUNDO_QUADRADO = 2.0;
    public static final double VELOCIDADE_ANGULAR_MAXIMA_GRAUS_POR_SEGUNDO = 360.0;
    public static final double ACELERACAO_ANGULAR_MAXIMA_GRAUS_POR_SEGUNDO_QUADRADO = 540.0;
    public static final double TENSAO_NOMINAL_VOLTS = 12.0;
    // Deve corresponder ao período real de atualização do drivetrain.
    public static final double PERIODO_CONTROLE_SEGUNDOS = 0.02;

    private PathPlanner() {}
  }

  /** Pontos de partida para calibrar a qualidade da visão. */
  public static final class Limelight {
    public static final MetodoPose METODO_POSE = MetodoPose.MEGATAG_2;
    public static final double IDADE_MAXIMA_QUADRO_SEGUNDOS = 0.25;
    public static final double IDADE_MAXIMA_ORIENTACAO_SEGUNDOS = 0.10;
    public static final double GIRO_MAXIMO_GRAUS_SEGUNDO = 360.0;
    public static final double AMBIGUIDADE_MAXIMA_TAG_UNICA = 0.30;
    public static final double ERRO_Z_MAXIMO_METROS = 0.75;
    public static final double DISTANCIA_MAXIMA_TAGS_METROS = 8.0;
    // Pisos para evitar confiança excessiva a curta distância.
    public static final double DESVIO_LINEAR_BASE_METROS = 0.02;
    public static final double DESVIO_ANGULAR_BASE_RADIANOS = 0.06;
    public static final double FATOR_CONFIANCA = 1.0;
    public static final double FATOR_LINEAR_MEGATAG2 = 0.5;

    private Limelight() {}
  }

  public static final class AdvantageScope {
    public static final String RAIZ_TABELA = "LuverLib";
    public static final double INTERVALO_SEGUNDOS = 0.02;
    public static final boolean ATUALIZACAO_AUTOMATICA = true;
    public static final boolean GRAVAR_JOYSTICKS = true;

    private AdvantageScope() {}
  }

  public static final class AutoPrecision {
    public static final double PERIODO_SEGUNDOS = 0.02;
    public static final double TIMEOUT_HEARTBEAT_SEGUNDOS = 0.5;

    private AutoPrecision() {}
  }

  private Constants() {}
}
