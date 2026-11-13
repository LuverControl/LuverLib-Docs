package frc.robot.luverlib.pathplanner;

import com.pathplanner.lib.auto.AutoBuilder;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Seletor com autônomos pré-carregados; arquivos inválidos são informados e ficam fora do painel. */
public final class SeletorAutonomo implements AutoCloseable {
  private final SendableChooser<Command> seletor;
  private final Map<String, String> falhas = new LinkedHashMap<>();
  private final Command parada;

  SeletorAutonomo(PathPlannerLuver biblioteca, String nomePainel, String autonomoPadrao) {
    ArquivosPathPlanner.nomeTexto(nomePainel);
    Objects.requireNonNull(autonomoPadrao, "O nome padrão não pode ser nulo; use vazio para parar.");
    List<String> nomes = AutoBuilder.getAllAutoNames().stream().sorted().toList();
    if (!autonomoPadrao.isEmpty() && !nomes.contains(autonomoPadrao)) {
      throw new IllegalArgumentException("Autônomo padrão não encontrado: " + autonomoPadrao);
    }
    parada = biblioteca.criarComandoParar();
    seletor = new SendableChooser<>();
    try {
      carregarOpcoes(biblioteca, nomes, autonomoPadrao);
      SmartDashboard.putData(nomePainel, seletor);
    } catch (RuntimeException | Error falha) {
      seletor.close();
      throw falha;
    }
  }

  private void carregarOpcoes(PathPlannerLuver biblioteca, List<String> nomes,
      String autonomoPadrao) {
    String opcaoParada = "Sem autônomo";
    while (nomes.contains(opcaoParada)) {
      opcaoParada = "(" + opcaoParada + ")";
    }
    seletor.setDefaultOption(opcaoParada, parada);
    for (String nome : nomes) {
      try {
        Command comando = biblioteca.carregarAutonomo(nome);
        if (nome.equals(autonomoPadrao)) {
          seletor.setDefaultOption(nome, comando);
        } else {
          seletor.addOption(nome, comando);
        }
      } catch (RuntimeException falha) {
        falhas.put(nome, Objects.toString(falha.getMessage(), falha.getClass().getSimpleName()));
        DriverStation.reportError("LuverLib: autônomo indisponível '" + nome + "': "
            + falha.getMessage(), false);
      }
    }
  }

  /** Devolve o comando pronto. O Robot agenda em autonomousInit e cancela em teleopInit. */
  public Command obterSelecionado() {
    Command selecionado = seletor.getSelected();
    return selecionado == null ? parada : selecionado;
  }

  public Map<String, String> obterFalhasCarregamento() {
    return Map.copyOf(falhas);
  }

  public SendableChooser<Command> obterSeletor() {
    return seletor;
  }

  @Override
  public void close() {
    seletor.close();
  }
}
