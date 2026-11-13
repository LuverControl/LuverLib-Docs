package frc.robot.luverlib.limelight;

import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.util.sendable.SendableRegistry;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Atualização automática de uma ou várias câmeras pelo CommandScheduler.
 * O projeto fornece giroscópio e destino das medições; não há dependência de swerve, motores ou IDs.
 * O consumidor recebe cada medição nova uma vez, em ordem de captura dentro do ciclo.
 */
public final class VisaoLimelight extends SubsystemBase implements AutoCloseable {
  private final List<Limelight> cameras;
  private final Supplier<OrientacaoRobo> fornecedorOrientacao;
  private final Consumer<MedicaoVisao> consumidor;
  private List<MedicaoVisao> ultimasMedicoes = List.of();
  private boolean fechada;

  /** Alvos/IA ou leitura manual: sem fornecer gyro, MegaTag2 fica rejeitado por orientação ausente. */
  public VisaoLimelight(Limelight... cameras) {
    this(null, null, cameras);
  }

  public VisaoLimelight(Supplier<OrientacaoRobo> fornecedorOrientacao,
      Consumer<MedicaoVisao> consumidor, Limelight... cameras) {
    super("Visão Limelight");
    try {
      this.cameras = List.copyOf(Arrays.asList(Objects.requireNonNull(cameras).clone()));
      if (this.cameras.isEmpty()) throw new IllegalArgumentException("Informe ao menos uma câmera.");
      Set<String> nomes = new HashSet<>();
      for (Limelight camera : this.cameras) {
        if (!nomes.add(camera.obterNome())) throw new IllegalArgumentException("Nome de câmera repetido.");
      }
      this.fornecedorOrientacao = fornecedorOrientacao;
      this.consumidor = consumidor;
    } catch (RuntimeException erro) {
      CommandScheduler.getInstance().unregisterSubsystem(this);
      SendableRegistry.remove(this);
      throw erro;
    }
  }

  @Override
  public void periodic() {
    if (fechada) return;
    if (fornecedorOrientacao != null) {
      OrientacaoRobo orientacao = Objects.requireNonNull(fornecedorOrientacao.get(),
          "O fornecedor de orientação retornou null.");
      Set<NetworkTableInstance> redes = new HashSet<>();
      for (Limelight camera : cameras) {
        camera.enviarOrientacaoSemFlush(orientacao);
        redes.add(camera.rede());
      }
      for (NetworkTableInstance rede : redes) rede.flush();
    }
    List<MedicaoVisao> novas = new ArrayList<>();
    for (Limelight camera : cameras) novas.addAll(camera.atualizar());
    novas.sort(Comparator.comparingDouble(MedicaoVisao::instanteCapturaSegundos)
        .thenComparing(MedicaoVisao::camera));
    ultimasMedicoes = List.copyOf(novas);
    if (consumidor != null) ultimasMedicoes.forEach(consumidor);
  }

  public List<MedicaoVisao> obterMedicoesUltimoCiclo() { return ultimasMedicoes; }

  /** Interrompe a atualização automática. As câmeras continuam pertencendo ao projeto. */
  @Override
  public void close() {
    if (fechada) return;
    fechada = true;
    ultimasMedicoes = List.of();
    CommandScheduler.getInstance().unregisterSubsystem(this);
    SendableRegistry.remove(this);
  }
}
