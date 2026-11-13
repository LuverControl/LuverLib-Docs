package frc.robot.luverlib.telemetria;

import edu.wpi.first.util.sendable.Sendable;
import edu.wpi.first.util.sendable.SendableBuilder;
import edu.wpi.first.util.sendable.SendableRegistry;
import frc.robot.luverlib.limelight.Limelight;
import java.util.Objects;

/** Diagnóstico somente leitura; a coleta é feita por VisaoLimelight ou atualizar(). */
public final class TelemetriaLimelight implements Sendable, AutoCloseable {
  private final Limelight camera;

  public TelemetriaLimelight(Limelight camera) {
    this.camera = Objects.requireNonNull(camera);
  }

  @Override
  public void initSendable(SendableBuilder painel) {
    painel.setSmartDashboardType("LuverLib/Limelight");
    painel.addStringProperty("Câmera", camera::obterNome, null);
    painel.addBooleanProperty("Conectada", camera::estaConectada, null);
    painel.addBooleanProperty("Tem alvo", () -> camera.obterAlvo().isPresent(), null);
    painel.addDoubleProperty("TX (graus)", () -> camera.obterAlvo().map(a -> a.txGraus()).orElse(0.0), null);
    painel.addDoubleProperty("TY (graus)", () -> camera.obterAlvo().map(a -> a.tyGraus()).orElse(0.0), null);
    painel.addDoubleProperty("Área do alvo (%)",
        () -> camera.obterAlvo().map(a -> a.areaPercentual()).orElse(0.0), null);
    painel.addDoubleProperty("Pipeline ativo", camera::obterPipelineAtivo, null);
    painel.addStringProperty("Classe detector", camera::obterClasseDetectorPrimaria, null);
    painel.addDoubleProperty("Detecções neurais", () -> camera.obterDeteccoesNeurais().size(), null);
    painel.addBooleanProperty("Pacote neural válido", camera::pacoteNeuralValido, null);
    painel.addBooleanProperty("Pose válida",
        () -> camera.obterUltimaAvaliacaoPose().medicao().isPresent(), null);
    painel.addStringProperty("Estado da pose", () -> camera.obterUltimaAvaliacaoPose().motivo().name(), null);
    painel.addDoubleProperty("Medições aceitas", camera::obterQuantidadeAceitas, null);
    painel.addDoubleProperty("Medições rejeitadas", camera::obterQuantidadeRejeitadas, null);
    painel.addDoubleArrayProperty("Pose azul [x,y,graus]", () -> camera.obterUltimaAvaliacaoPose()
        .medicao().map(m -> new double[] {m.pose().getX(), m.pose().getY(),
            m.pose2d().getRotation().getDegrees()}).orElse(new double[0]), null);
    painel.addDoubleProperty("Latência pose (ms)", () -> camera.obterUltimaAvaliacaoPose().medicao()
        .map(m -> m.latenciaSegundos() * 1000).orElse(-1.0), null);
    painel.addDoubleProperty("Temperatura CPU (°C)", () -> camera.obterHardware()
        .map(v -> v.get(0)).orElse(-1.0), null);
    painel.addDoubleProperty("FPS", () -> camera.obterHardware().map(v -> v.get(3)).orElse(-1.0), null);
  }

  @Override
  public void close() { SendableRegistry.remove(this); }
}
