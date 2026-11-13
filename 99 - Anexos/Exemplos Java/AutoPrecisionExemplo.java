package guia;

import com.revrobotics.spark.ClosedLoopSlot;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj2.command.Subsystem;
import edu.wpi.first.wpilibj2.command.button.Trigger;
import java.util.Objects;
import frc.robot.luverlib.autoprecision.AutoPrecision;
import frc.robot.luverlib.autoprecision.ConfiguracaoAutoPrecision;
import frc.robot.luverlib.autoprecision.ControleNeoAutoPrecision;
import frc.robot.luverlib.motores.neo.MotorNeo;

public final class AutoPrecisionExemplo implements AutoCloseable {
  private final AutoPrecision afinador;

  public AutoPrecisionExemplo(String nome, Subsystem mecanismoReal,
      MotorNeo motorExistente, ClosedLoopSlot slotDeConstants,
      ConfiguracaoAutoPrecision configuracaoReal, Trigger autorizacaoLocal) {
    Objects.requireNonNull(autorizacaoLocal, "A autorização local é obrigatória.");
    afinador = new AutoPrecision(nome, mecanismoReal,
        new ControleNeoAutoPrecision(motorExistente, slotDeConstants),
        configuracaoReal, autorizacaoLocal::getAsBoolean);
    try {
      autorizacaoLocal.and(DriverStation::isTestEnabled)
          .whileTrue(afinador.criarComando());
    } catch (RuntimeException | Error falha) {
      try { afinador.close(); } catch (RuntimeException | Error limpeza) { falha.addSuppressed(limpeza); }
      throw falha;
    }
  }

  public AutoPrecision.Estado estado() {
    return afinador.obterEstado();
  }

  @Override public void close() {
    afinador.close();
  }
}
