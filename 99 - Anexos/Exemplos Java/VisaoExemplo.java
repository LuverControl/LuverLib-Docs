package guia;

import java.util.function.Consumer;
import java.util.function.Supplier;
import frc.robot.luverlib.limelight.AvaliacaoPose;
import frc.robot.luverlib.limelight.Limelight;
import frc.robot.luverlib.limelight.LimitesCampo;
import frc.robot.luverlib.limelight.MedicaoVisao;
import frc.robot.luverlib.limelight.OrientacaoRobo;
import frc.robot.luverlib.limelight.VariaveisLimelight;
import frc.robot.luverlib.limelight.VisaoLimelight;

public final class VisaoExemplo implements AutoCloseable {
  private final Limelight camera;
  private final VisaoLimelight visao;

  public VisaoExemplo(String nomeDaCamera, LimitesCampo campo,
      Supplier<OrientacaoRobo> orientacaoReal,
      Consumer<MedicaoVisao> receberNoEstimador) {
    var ajustes = VariaveisLimelight.configuracaoPadrao().comLimitesCampo(campo);
    camera = new Limelight(nomeDaCamera, ajustes);
    try {
      visao = new VisaoLimelight(orientacaoReal, receberNoEstimador, camera);
    } catch (RuntimeException | Error falha) {
      try { camera.close(); } catch (RuntimeException | Error limpeza) { falha.addSuppressed(limpeza); }
      throw falha;
    }
  }

  public AvaliacaoPose diagnostico() {
    return camera.obterUltimaAvaliacaoPose();
  }

  @Override public void close() {
    try { visao.close(); } finally { camera.close(); }
  }
}
