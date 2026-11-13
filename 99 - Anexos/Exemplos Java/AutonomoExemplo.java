package guia;

import com.pathplanner.lib.config.RobotConfig;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import frc.robot.luverlib.pathplanner.ConexaoPathPlanner;
import frc.robot.luverlib.pathplanner.PathPlannerLuver;
import frc.robot.luverlib.pathplanner.SeletorAutonomo;

public final class AutonomoExemplo implements AutoCloseable {
  private final SeletorAutonomo seletor;
  private Command autonomoAtual;

  public AutonomoExemplo(ConexaoPathPlanner conexaoReal, RobotConfig roboReal,
      String nomeEvento, Command evento, String nomeAutoPadrao) {
    var caminhos = new PathPlannerLuver(conexaoReal, roboReal);
    caminhos.registrarComando(nomeEvento, evento);
    seletor = caminhos.criarSeletor("Autonomo", nomeAutoPadrao);
  }

  public void iniciarAutonomo() {
    autonomoAtual = seletor.obterSelecionado();
    if (autonomoAtual != null) CommandScheduler.getInstance().schedule(autonomoAtual);
  }

  public void entrarEmTeleop() {
    if (autonomoAtual != null) CommandScheduler.getInstance().cancel(autonomoAtual);
  }

  @Override public void close() {
    try { entrarEmTeleop(); } finally { seletor.close(); }
  }
}
