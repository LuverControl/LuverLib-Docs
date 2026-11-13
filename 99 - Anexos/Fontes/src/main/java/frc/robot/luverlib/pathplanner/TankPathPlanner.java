package frc.robot.luverlib.pathplanner;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.controller.SimpleMotorFeedforward;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.DifferentialDriveKinematics;
import edu.wpi.first.math.kinematics.DifferentialDriveWheelSpeeds;
import edu.wpi.first.wpilibj2.command.Subsystem;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

/**
 * Adapta o controle tank usado na DualFire: RPM, cinemática diferencial e feedforward em tensão.
 *
 * <p>Não cria motores, giroscópio nem estimador de pose. A odometria permanece no projeto.
 * O modelo kS/kV equivale ao SimpleMotorFeedforward sem aceleração usado no código antigo.
 */
public final class TankPathPlanner {
  /** Tensões solicitadas ao lado esquerdo e direito, respeitando o limite configurado. */
  public record Tensoes(double esquerdaVolts, double direitaVolts) {}

  private final ConfiguracaoTankPathPlanner ajustes;
  private final DifferentialDriveKinematics cinematica;
  private final SimpleMotorFeedforward feedforward;

  public TankPathPlanner(ConfiguracaoTankPathPlanner ajustes) {
    this.ajustes = Objects.requireNonNull(ajustes, "A calibração do tank é obrigatória.");
    cinematica = new DifferentialDriveKinematics(ajustes.larguraEntreRodasMetros());
    feedforward = new SimpleMotorFeedforward(ajustes.ksVolts(),
        ajustes.kvVoltsPorMetroPorSegundo());
  }

  /** Converte a leitura RPM nos dois lados para m/s, com o sinal definido pelo projeto. */
  public DifferentialDriveWheelSpeeds obterVelocidadesRodas(double rpmEsquerda, double rpmDireita) {
    exigirFinito(rpmEsquerda);
    exigirFinito(rpmDireita);
    double fator = ajustes.metrosPorRotacaoEncoder() / 60.0;
    double esquerda = rpmEsquerda * fator * (ajustes.inverterEncoderEsquerdo() ? -1 : 1);
    double direita = rpmDireita * fator * (ajustes.inverterEncoderDireito() ? -1 : 1);
    exigirFinito(esquerda);
    exigirFinito(direita);
    return new DifferentialDriveWheelSpeeds(esquerda, direita);
  }

  public ChassisSpeeds obterVelocidadesRelativasRobo(double rpmEsquerda, double rpmDireita) {
    ChassisSpeeds velocidades = cinematica.toChassisSpeeds(
        obterVelocidadesRodas(rpmEsquerda, rpmDireita));
    exigirFinito(velocidades.vxMetersPerSecond);
    exigirFinito(velocidades.omegaRadiansPerSecond);
    return velocidades;
  }

  /** Converte a saída do LTV em tensões, dessaturando os lados juntos para preservar a curva. */
  public Tensoes calcularTensoes(ChassisSpeeds velocidades) {
    Objects.requireNonNull(velocidades, "A velocidade do tank é obrigatória.");
    exigirFinito(velocidades.vxMetersPerSecond);
    exigirFinito(velocidades.vyMetersPerSecond);
    exigirFinito(velocidades.omegaRadiansPerSecond);
    if (Math.abs(velocidades.vyMetersPerSecond) > 1e-9) {
      throw new IllegalArgumentException("Tank não admite velocidade lateral: use configuração diferencial.");
    }
    var rodas = cinematica.toWheelSpeeds(velocidades);
    exigirFinito(rodas.leftMetersPerSecond);
    exigirFinito(rodas.rightMetersPerSecond);
    rodas.desaturate(ajustes.velocidadeMaximaRodaMetrosPorSegundo());
    // kA é zero. A API discreta atual reproduz kS * sinal(v) + kV * v sem API removida.
    double esquerda = feedforward.calculateWithVelocities(
        rodas.leftMetersPerSecond, rodas.leftMetersPerSecond);
    double direita = feedforward.calculateWithVelocities(
        rodas.rightMetersPerSecond, rodas.rightMetersPerSecond);
    exigirFinito(esquerda);
    exigirFinito(direita);
    double limite = ajustes.tensaoMaximaVolts();
    return new Tensoes(MathUtil.clamp(esquerda, -limite, limite),
        MathUtil.clamp(direita, -limite, limite));
  }

  /**
   * Liga o adaptador à fachada. As RPM e tensões são dos lados esquerdo/direito.
   *
   * <p>Este modo usa o feedforward kS/kV calibrado pelo projeto, como a DualFire; não soma o
   * DriveFeedforwards fornecido pelo PathPlanner, que contém forças/correntes, não tensões kS/kV.
   * O projeto aplica as tensões aos líderes; configura seguidores e inversão elétrica separadamente.
   */
  public ConexaoPathPlanner criarConexao(Subsystem subsistema, Supplier<Pose2d> obterPose,
      Consumer<Pose2d> redefinirPose, DoubleSupplier rpmEsquerda, DoubleSupplier rpmDireita,
      BiConsumer<Double, Double> aplicarTensoes, Runnable parar) {
    Objects.requireNonNull(rpmEsquerda, "O fornecedor de RPM esquerda é obrigatório.");
    Objects.requireNonNull(rpmDireita, "O fornecedor de RPM direita é obrigatório.");
    Objects.requireNonNull(aplicarTensoes, "A aplicação das tensões é obrigatória.");
    Objects.requireNonNull(parar, "A parada real do drivetrain é obrigatória.");
    return new ConexaoPathPlanner(subsistema, obterPose, redefinirPose,
        () -> obterVelocidadesRelativasRobo(rpmEsquerda.getAsDouble(), rpmDireita.getAsDouble()),
        (velocidades, feedforwards) -> {
          try {
            Tensoes tensoes = calcularTensoes(velocidades);
            aplicarTensoes.accept(tensoes.esquerdaVolts(), tensoes.direitaVolts());
          } catch (RuntimeException falha) {
            parar.run();
            throw falha;
          }
        }, parar);
  }

  public ConfiguracaoTankPathPlanner obterConfiguracao() {
    return ajustes;
  }

  private static void exigirFinito(double valor) {
    if (!Double.isFinite(valor)) {
      throw new IllegalArgumentException("A leitura ou saída do tank deve ser finita.");
    }
  }
}
