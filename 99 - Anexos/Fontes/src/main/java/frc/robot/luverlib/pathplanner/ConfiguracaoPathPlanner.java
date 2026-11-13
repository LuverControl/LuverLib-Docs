package frc.robot.luverlib.pathplanner;

import com.pathplanner.lib.config.PIDConstants;
import com.pathplanner.lib.path.PathConstraints;
import java.util.Objects;

/** Ajustes independentes por projeto. As unidades dos limites são metros, segundos e radianos. */
public record ConfiguracaoPathPlanner(
    PIDConstants pidTranslacao, PIDConstants pidRotacao,
    PathConstraints limitesPathfinding, double periodoControleSegundos) {

  public ConfiguracaoPathPlanner {
    validarPid(pidTranslacao);
    validarPid(pidRotacao);
    Objects.requireNonNull(limitesPathfinding, "Os limites do pathfinding são obrigatórios.");
    exigirPositivo(limitesPathfinding.maxVelocityMPS(), "Velocidade máxima");
    exigirPositivo(limitesPathfinding.maxAccelerationMPSSq(), "Aceleração máxima");
    exigirPositivo(limitesPathfinding.maxAngularVelocityRadPerSec(), "Velocidade angular máxima");
    exigirPositivo(limitesPathfinding.maxAngularAccelerationRadPerSecSq(),
        "Aceleração angular máxima");
    exigirPositivo(limitesPathfinding.nominalVoltageVolts(), "Tensão nominal");
    if (limitesPathfinding.unlimited() || limitesPathfinding.nominalVoltageVolts() > 24.0) {
      throw new IllegalArgumentException("Use limites definidos e tensão nominal de até 24 V.");
    }
    exigirPositivo(periodoControleSegundos, "Período de controle");
  }

  private static void validarPid(PIDConstants pid) {
    Objects.requireNonNull(pid, "Os ganhos PID são obrigatórios.");
    for (double ganho : new double[] {pid.kP, pid.kI, pid.kD}) {
      if (!Double.isFinite(ganho) || ganho < 0.0) {
        throw new IllegalArgumentException("Os ganhos PID devem ser finitos e não negativos.");
      }
    }
    // A WPILib permite infinito positivo para desativar a restrição da zona integral.
    if (Double.isNaN(pid.iZone) || pid.iZone < 0.0) {
      throw new IllegalArgumentException("A zona integral deve ser não negativa.");
    }
  }

  static void exigirPositivo(double valor, String nome) {
    if (!Double.isFinite(valor) || valor <= 0.0) {
      throw new IllegalArgumentException(nome + " deve ser finito e maior que zero.");
    }
  }
}
