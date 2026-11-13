package frc.robot.subsystems;

import com.ctre.phoenix6.hardware.Pigeon2;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.util.sendable.SendableRegistry;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants.Tracao;
import frc.robot.luverlib.motores.neo.MotorNeo;
import frc.robot.luverlib.motores.neo.VariaveisMotor;
import java.util.ArrayList;
import java.util.List;

/** Tração do DualFire: IDs e ajustes centralizados em Constants.Tracao. */
public final class ChassiTank extends SubsystemBase implements AutoCloseable {
  private final MotorNeo liderEsquerdo;
  private final MotorNeo liderDireito;
  private final MotorNeo seguidorEsquerdo;
  private final MotorNeo seguidorDireito;
  private final Pigeon2 giroscopio;
  private final List<AutoCloseable> dispositivos = new ArrayList<>();
  private boolean fechado;

  public ChassiTank() {
    this(Tracao.LIDER_ESQUERDO, Tracao.SEGUIDOR_ESQUERDO,
        Tracao.LIDER_DIREITO, Tracao.SEGUIDOR_DIREITO, Tracao.PIGEON);
  }

  /** Permite usar IDs separados na simulação, sem mudar os IDs reais das Constants. */
  ChassiTank(int idEsquerdo, int idSeguidorEsquerdo,
      int idDireito, int idSeguidorDireito, int idPigeon) {
    try {
      liderEsquerdo = registrar(new MotorNeo(idEsquerdo,
          VariaveisMotor.Tracao.configuracaoLiderEsquerdo()));
      liderDireito = registrar(new MotorNeo(idDireito,
          VariaveisMotor.Tracao.configuracaoLiderDireito()));
      var configuracaoSeguidores = VariaveisMotor.Tracao.configuracaoSeguidor();
      seguidorEsquerdo = registrar(new MotorNeo(idSeguidorEsquerdo, configuracaoSeguidores, liderEsquerdo));
      seguidorDireito = registrar(new MotorNeo(idSeguidorDireito, configuracaoSeguidores, liderDireito));
      giroscopio = registrar(new Pigeon2(idPigeon));
    } catch (RuntimeException | Error falha) {
      CommandScheduler.getInstance().unregisterSubsystem(this);
      SendableRegistry.remove(this);
      try { fecharDispositivos(); } catch (RuntimeException erro) { falha.addSuppressed(erro); }
      throw falha;
    }
  }

  /** Mesma mistura do DualFire: esquerda = frente + giro; direita = frente - giro. */
  public void mover(double frenteTras, double giro) {
    garantirAberto();
    double esquerda = frenteTras + giro;
    double direita = frenteTras - giro;
    if (!Double.isFinite(frenteTras) || !Double.isFinite(giro)
        || !Double.isFinite(esquerda) || !Double.isFinite(direita)) {
      parar();
      throw new IllegalArgumentException("Comando de tração inválido. Chassi parado.");
    }
    liderEsquerdo.definirPotencia(esquerda);
    liderDireito.definirPotencia(direita);
  }

  public Rotation2d obterRotacao() {
    garantirAberto();
    return Rotation2d.fromDegrees(giroscopio.getYaw().getValueAsDouble());
  }

  public void parar() {
    if (fechado) return;
    try { liderEsquerdo.parar(); } finally { liderDireito.parar(); }
  }

  MotorNeo obterLiderEsquerdo() { return liderEsquerdo; }
  MotorNeo obterLiderDireito() { return liderDireito; }
  MotorNeo obterSeguidorEsquerdo() { return seguidorEsquerdo; }
  MotorNeo obterSeguidorDireito() { return seguidorDireito; }

  private <T extends AutoCloseable> T registrar(T dispositivo) {
    dispositivos.add(dispositivo);
    return dispositivo;
  }

  private void garantirAberto() {
    if (fechado) throw new IllegalStateException("Chassi tank fechado.");
  }

  @Override
  public void close() {
    if (fechado) return;
    try {
      var comando = getCurrentCommand();
      if (comando != null) comando.cancel();
      parar();
    } finally {
      fechado = true;
      CommandScheduler.getInstance().unregisterSubsystem(this);
      SendableRegistry.remove(this);
      fecharDispositivos();
    }
  }

  private void fecharDispositivos() {
    RuntimeException erro = null;
    for (AutoCloseable dispositivo : dispositivos) {
      try { dispositivo.close(); }
      catch (Exception e) {
        if (erro == null) erro = new IllegalStateException("Falha ao encerrar a tração.", e);
        else erro.addSuppressed(e);
      }
    }
    dispositivos.clear();
    if (erro != null) throw erro;
  }
}
