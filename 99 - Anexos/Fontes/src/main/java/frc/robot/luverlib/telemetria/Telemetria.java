package frc.robot.luverlib.telemetria;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.luverlib.controles.ControleXbox;
import frc.robot.luverlib.limelight.Limelight;
import frc.robot.luverlib.motores.neo.MotorNeo;
import frc.robot.luverlib.motores.kraken.MotorKraken;
import java.util.Objects;

/**
 * Registra a telemetria uma vez, na criação do robô ou do subsistema.
 *
 * <p>Exemplo: {@code Telemetria.registrarControle("Xbox", piloto)}. O TimedRobot da WPILib
 * atualiza as leituras automaticamente, sem chamadas periódicas no RobotContainer.
 * Nomes e dispositivos são informados pelo projeto. Todos os campos são somente leitura.
 */
public final class Telemetria {
  public static TelemetriaXbox registrarControle(String nome, ControleXbox controle) {
    validarNome(nome);
    TelemetriaXbox dados = new TelemetriaXbox(controle);
    SmartDashboard.putData(nome, dados);
    return dados;
  }

  /** Registre depois de criar o motor. Encerre a telemetria antes de fechar o MotorNeo. */
  public static TelemetriaMotorNeo registrarMotor(String nome, MotorNeo motor) {
    validarNome(nome);
    TelemetriaMotorNeo dados = new TelemetriaMotorNeo(motor);
    SmartDashboard.putData(nome, dados);
    return dados;
  }

  /** Kraken X60/X44; nome e dispositivo pertencem ao projeto. */
  public static TelemetriaMotorKraken registrarMotorKraken(String nome, MotorKraken motor) {
    validarNome(nome);
    var dados = new TelemetriaMotorKraken(motor);
    SmartDashboard.putData(nome, dados);
    return dados;
  }

  public static TelemetriaLimelight registrarLimelight(String nome, Limelight camera) {
    validarNome(nome);
    TelemetriaLimelight dados = new TelemetriaLimelight(camera);
    SmartDashboard.putData(nome, dados);
    return dados;
  }

  private static void validarNome(String nome) {
    Objects.requireNonNull(nome, "O nome da telemetria é obrigatório.");
    if (nome.isBlank()) {
      throw new IllegalArgumentException("O nome da telemetria não pode estar vazio.");
    }
  }

  private Telemetria() {}
}
