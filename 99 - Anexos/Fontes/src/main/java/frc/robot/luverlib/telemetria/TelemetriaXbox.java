package frc.robot.luverlib.telemetria;

import edu.wpi.first.util.sendable.Sendable;
import edu.wpi.first.util.sendable.SendableBuilder;
import edu.wpi.first.util.sendable.SendableRegistry;
import frc.robot.luverlib.controles.ControleXbox;
import frc.robot.luverlib.controles.ControleXbox.Botao;
import java.util.Objects;

/** Leituras do Xbox para o painel. Os ajustes dos eixos ficam em Constants.Controle. */
public final class TelemetriaXbox implements Sendable, AutoCloseable {
  private final ControleXbox controle;

  public TelemetriaXbox(ControleXbox controle) {
    this.controle = Objects.requireNonNull(controle, "O controle é obrigatório.");
  }

  /** Contrato Sendable da WPILib: associa os nomes do painel às leituras do dispositivo. */
  @Override
  public void initSendable(SendableBuilder painel) {
    painel.setSmartDashboardType("LuverLib/Xbox");
    painel.addBooleanProperty("Conectado", controle::estaConectado, null);
    painel.addDoubleProperty("Analógico esquerdo X", controle::analogicoEsquerdoX, null);
    painel.addDoubleProperty("Analógico esquerdo Y", controle::analogicoEsquerdoY, null);
    painel.addDoubleProperty("Analógico direito X", controle::analogicoDireitoX, null);
    painel.addDoubleProperty("Analógico direito Y", controle::analogicoDireitoY, null);
    painel.addDoubleProperty("Gatilho esquerdo", controle::valorGatilhoEsquerdo, null);
    painel.addDoubleProperty("Gatilho direito", controle::valorGatilhoDireito, null);
    painel.addBooleanProperty("Gatilho esquerdo acionado", controle.gatilhoEsquerdo(), null);
    painel.addBooleanProperty("Gatilho direito acionado", controle.gatilhoDireito(), null);
    painel.addDoubleProperty("Direcional (graus)",
        () -> controle.estaConectado() ? controle.obterDispositivo().getPOV() : -1, null);
    for (Botao botao : Botao.values()) {
      painel.addBooleanProperty("Botão " + botao.name().replace('_', ' '), controle.botao(botao), null);
    }
  }

  /** Encerra a publicação; não altera nem desliga o controle. */
  @Override
  public void close() {
    SendableRegistry.remove(this);
  }
}
