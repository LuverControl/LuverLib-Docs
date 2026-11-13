package frc.robot.luverlib.controles;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.GenericHID.RumbleType;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.event.EventLoop;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj2.command.button.Trigger;
import java.util.EnumMap;
import java.util.Objects;

/**
 * Leitura do controle Xbox com ajustes de Constants.Controle e eventos para comandos WPILib.
 *
 * <p>Crie uma instância no RobotContainer. Sem controle conectado, os eixos retornam zero e os
 * eventos ficam inativos. Configure os botões uma vez, fora do laço periódico do robô.
 */
public final class ControleXbox {
  /** Os quatro eixos dos analógicos. Os gatilhos têm leituras próprias de 0 a 1. */
  public enum Analogico {
    ESQUERDO_X(XboxController.Axis.kLeftX.value),
    ESQUERDO_Y(XboxController.Axis.kLeftY.value),
    DIREITO_X(XboxController.Axis.kRightX.value),
    DIREITO_Y(XboxController.Axis.kRightY.value);

    private final int indice;

    Analogico(int indice) {
      this.indice = indice;
    }
  }

  public enum Botao {
    A(XboxController.Button.kA.value),
    B(XboxController.Button.kB.value),
    X(XboxController.Button.kX.value),
    Y(XboxController.Button.kY.value),
    LB(XboxController.Button.kLeftBumper.value),
    RB(XboxController.Button.kRightBumper.value),
    VOLTAR(XboxController.Button.kBack.value),
    INICIAR(XboxController.Button.kStart.value),
    CLIQUE_ESQUERDO(XboxController.Button.kLeftStick.value),
    CLIQUE_DIREITO(XboxController.Button.kRightStick.value);

    private final int indice;

    Botao(int indice) {
      this.indice = indice;
    }
  }

  /** Direções exatas do D-pad; CENTRO representa o direcional solto. */
  public enum Direcao {
    CIMA(0), CIMA_DIREITA(45), DIREITA(90), BAIXO_DIREITA(135),
    BAIXO(180), BAIXO_ESQUERDA(225), ESQUERDA(270), CIMA_ESQUERDA(315), CENTRO(-1);

    private final int angulo;

    Direcao(int angulo) {
      this.angulo = angulo;
    }
  }

  private final XboxController dispositivo;
  private final EventLoop eventos;
  private final EnumMap<Analogico, PerfilEixo> perfis = new EnumMap<>(Analogico.class);
  private final EnumMap<Botao, Trigger> botoes = new EnumMap<>(Botao.class);
  private final EnumMap<Direcao, Trigger> direcoes = new EnumMap<>(Direcao.class);

  /** Usa os ajustes de Constants.Controle e o agendador padrão da WPILib. */
  public ControleXbox(int porta) {
    this(porta, CommandScheduler.getInstance().getDefaultButtonLoop());
  }

  /** Versão com laço próprio, útil para simulação. O chamador deve executar eventos.poll(). */
  public ControleXbox(int porta, EventLoop eventos) {
    if (porta < 0 || porta >= DriverStation.kJoystickPorts) {
      throw new IllegalArgumentException("A porta USB do controle deve estar entre 0 e 5.");
    }
    this.eventos = Objects.requireNonNull(eventos, "O laço de eventos é obrigatório.");
    validarFracao(VariaveisControle.LIMIAR_GATILHOS, "Limiar dos gatilhos");
    dispositivo = new XboxController(porta);
    perfis.put(Analogico.ESQUERDO_X, VariaveisControle.ANALOGICO_ESQUERDO_X);
    perfis.put(Analogico.ESQUERDO_Y, VariaveisControle.ANALOGICO_ESQUERDO_Y);
    perfis.put(Analogico.DIREITO_X, VariaveisControle.ANALOGICO_DIREITO_X);
    perfis.put(Analogico.DIREITO_Y, VariaveisControle.ANALOGICO_DIREITO_Y);
    for (Botao botao : Botao.values()) {
      botoes.put(botao, new Trigger(eventos,
          () -> estaConectado() && dispositivo.getRawButton(botao.indice)));
    }
    for (Direcao direcao : Direcao.values()) {
      direcoes.put(direcao, new Trigger(eventos,
          () -> estaConectado() && dispositivo.getPOV() == direcao.angulo));
    }
  }

  /** Sobrescreve o ajuste apenas nesta instância, por exemplo para um segundo operador. */
  public void definirPerfil(Analogico analogico, PerfilEixo perfil) {
    perfis.put(Objects.requireNonNull(analogico, "O analógico é obrigatório."),
        Objects.requireNonNull(perfil, "O perfil é obrigatório."));
  }

  public double valorAnalogico(Analogico analogico) {
    Objects.requireNonNull(analogico, "O analógico é obrigatório.");
    return estaConectado()
        ? perfis.get(analogico).aplicar(dispositivo.getRawAxis(analogico.indice)) : 0.0;
  }

  public double analogicoEsquerdoX() { return valorAnalogico(Analogico.ESQUERDO_X); }
  public double analogicoEsquerdoY() { return valorAnalogico(Analogico.ESQUERDO_Y); }
  public double analogicoDireitoX() { return valorAnalogico(Analogico.DIREITO_X); }
  public double analogicoDireitoY() { return valorAnalogico(Analogico.DIREITO_Y); }

  /** Leitura analógica do gatilho, de 0 a 1, sem curva nem zona morta de analógicos. */
  public double valorGatilhoEsquerdo() {
    return estaConectado() ? limitarGatilho(dispositivo.getLeftTriggerAxis()) : 0.0;
  }

  public double valorGatilhoDireito() {
    return estaConectado() ? limitarGatilho(dispositivo.getRightTriggerAxis()) : 0.0;
  }

  /** Retorna um Trigger WPILib, que permite onTrue, onFalse, whileTrue e combinações. */
  public Trigger botao(Botao botao) {
    return botoes.get(Objects.requireNonNull(botao, "O botão é obrigatório."));
  }

  public Trigger botaoA() { return botao(Botao.A); }
  public Trigger botaoB() { return botao(Botao.B); }
  public Trigger botaoX() { return botao(Botao.X); }
  public Trigger botaoY() { return botao(Botao.Y); }
  public Trigger botaoLB() { return botao(Botao.LB); }
  public Trigger botaoRB() { return botao(Botao.RB); }
  public Trigger botaoVoltar() { return botao(Botao.VOLTAR); }
  public Trigger botaoIniciar() { return botao(Botao.INICIAR); }
  public Trigger cliqueAnalogicoEsquerdo() { return botao(Botao.CLIQUE_ESQUERDO); }
  public Trigger cliqueAnalogicoDireito() { return botao(Botao.CLIQUE_DIREITO); }

  public Trigger gatilhoEsquerdo() { return gatilhoEsquerdo(VariaveisControle.LIMIAR_GATILHOS); }
  public Trigger gatilhoDireito() { return gatilhoDireito(VariaveisControle.LIMIAR_GATILHOS); }

  /** Ativa quando a leitura passa do limiar; a comparação é estritamente maior. */
  public Trigger gatilhoEsquerdo(double limiar) {
    validarFracao(limiar, "Limiar do gatilho");
    return new Trigger(eventos, () -> valorGatilhoEsquerdo() > limiar);
  }

  public Trigger gatilhoDireito(double limiar) {
    validarFracao(limiar, "Limiar do gatilho");
    return new Trigger(eventos, () -> valorGatilhoDireito() > limiar);
  }

  public Trigger direcional(Direcao direcao) {
    return direcoes.get(Objects.requireNonNull(direcao, "A direção é obrigatória."));
  }

  public Trigger direcionalCima() { return direcional(Direcao.CIMA); }
  public Trigger direcionalDireita() { return direcional(Direcao.DIREITA); }
  public Trigger direcionalBaixo() { return direcional(Direcao.BAIXO); }
  public Trigger direcionalEsquerda() { return direcional(Direcao.ESQUERDA); }

  public boolean estaConectado() {
    return dispositivo.isConnected();
  }

  /** Intensidades de vibração esquerda e direita, de 0 a 1. Use pararVibracao ao encerrar. */
  public void definirVibracao(double esquerda, double direita) {
    validarFracao(esquerda, "Vibração esquerda");
    validarFracao(direita, "Vibração direita");
    dispositivo.setRumble(RumbleType.kLeftRumble, estaConectado() ? esquerda : 0.0);
    dispositivo.setRumble(RumbleType.kRightRumble, estaConectado() ? direita : 0.0);
  }

  public void pararVibracao() {
    definirVibracao(0.0, 0.0);
  }

  /** Acesso bruto para simulação e uso avançado; ignora os ajustes desta classe. */
  public XboxController obterDispositivo() {
    return dispositivo;
  }

  private static double limitarGatilho(double valor) {
    return Double.isFinite(valor) ? Math.max(0.0, Math.min(valor, 1.0)) : 0.0;
  }

  private static void validarFracao(double valor, String nome) {
    if (!Double.isFinite(valor) || valor < 0.0 || valor > 1.0) {
      throw new IllegalArgumentException(nome + " deve ser finito, de 0 a 1.");
    }
  }
}
