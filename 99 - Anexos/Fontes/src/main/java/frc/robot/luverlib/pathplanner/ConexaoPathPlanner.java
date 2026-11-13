package frc.robot.luverlib.pathplanner;

import com.pathplanner.lib.util.DriveFeedforwards;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj2.command.Subsystem;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Liga a biblioteca ao drivetrain do projeto, sem criar motores, sensores ou odometria.
 *
 * @param subsistema drivetrain que os comandos devem reservar no agendador WPILib
 * @param obterPoseCampo pose com origem global no lado azul, inclusive na aliança vermelha
 * @param redefinirPoseCampo redefine a mesma odometria/estimador usado pelo drivetrain e pela visão
 * @param obterVelocidadesRelativasRobo velocidades medidas no referencial do ROBÔ, não do campo
 * @param aplicarSaida recebe velocidades relativas ao ROBÔ e feedforwards; ordem swerve:
 *     frente esquerda, frente direita, traseira esquerda, traseira direita; diferencial: esquerda,
 *     direita. Se otimizar/inverter um módulo, ajuste também o sinal de seu feedforward.
 * @param parar ação que realmente zera a saída do drivetrain ao terminar ou cancelar um comando
 */
public record ConexaoPathPlanner(
    Subsystem subsistema, Supplier<Pose2d> obterPoseCampo, Consumer<Pose2d> redefinirPoseCampo,
    Supplier<ChassisSpeeds> obterVelocidadesRelativasRobo,
    BiConsumer<ChassisSpeeds, DriveFeedforwards> aplicarSaida, Runnable parar) {

  public ConexaoPathPlanner {
    Objects.requireNonNull(subsistema, "O subsistema de movimento é obrigatório.");
    Objects.requireNonNull(obterPoseCampo, "A leitura da pose é obrigatória.");
    Objects.requireNonNull(redefinirPoseCampo, "A redefinição da pose é obrigatória.");
    Objects.requireNonNull(obterVelocidadesRelativasRobo, "A leitura de velocidades é obrigatória.");
    Objects.requireNonNull(aplicarSaida, "A saída do drivetrain é obrigatória.");
    Objects.requireNonNull(parar, "A ação de parar o drivetrain é obrigatória.");
  }
}
