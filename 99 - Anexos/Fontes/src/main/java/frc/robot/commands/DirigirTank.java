package frc.robot.commands;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Subsystem;
import edu.wpi.first.wpilibj.DriverStation;
import frc.robot.luverlib.controles.ControleXbox;
import frc.robot.Constants.Tracao;
import frc.robot.subsystems.ChassiTank;
import java.util.Objects;
import java.util.function.BiConsumer;

/** Comando do ROBÔ: pilotagem original do DualFire, usando o ControleXbox da LuverLib nova. */
public final class DirigirTank extends Command {
  private final ControleXbox controle;
  private final BiConsumer<Double, Double> mover;
  private final Runnable parar;

  public DirigirTank(ChassiTank chassi, ControleXbox controle) {
    this(controle, chassi::mover, chassi::parar, chassi);
  }

  DirigirTank(ControleXbox controle, BiConsumer<Double, Double> mover, Runnable parar,
      Subsystem chassi) {
    this.controle = Objects.requireNonNull(controle);
    this.mover = Objects.requireNonNull(mover);
    this.parar = Objects.requireNonNull(parar);
    addRequirements(Objects.requireNonNull(chassi));
  }

  @Override
  public void execute() {
    if (!DriverStation.isTeleopEnabled() || !controle.estaConectado()) { parar.run(); return; }
    double esquerdo = cortarZonaMorta(controle.valorGatilhoEsquerdo());
    double direito = cortarZonaMorta(controle.valorGatilhoDireito());
    // Leitura bruta deliberada: preserva o corte do DualFire, sem a curva reescalonada de PerfilEixo.
    double analogico = cortarZonaMorta(controle.obterDispositivo().getLeftX());
    mover.accept((direito - esquerdo) * Tracao.ESCALA_FRENTE, analogico * Tracao.ESCALA_GIRO);
  }

  private static double cortarZonaMorta(double valor) {
    return Double.isFinite(valor) && Math.abs(valor) > Tracao.ZONA_MORTA ? valor : 0;
  }

  @Override
  public void end(boolean interrompido) { parar.run(); }

  @Override
  public boolean isFinished() { return false; }
}
