package frc.robot.luverlib.motores.kraken;

import com.ctre.phoenix6.sim.TalonFXSimState;

/** Modelo físico informado pelo projeto; seleciona também a simulação correspondente da CTRE. */
public enum ModeloKraken {
  X60(TalonFXSimState.MotorType.KrakenX60),
  X44(TalonFXSimState.MotorType.KrakenX44);

  private final TalonFXSimState.MotorType tipoSimulacao;

  ModeloKraken(TalonFXSimState.MotorType tipoSimulacao) {
    this.tipoSimulacao = tipoSimulacao;
  }

  TalonFXSimState.MotorType tipoSimulacao() {
    return tipoSimulacao;
  }
}
