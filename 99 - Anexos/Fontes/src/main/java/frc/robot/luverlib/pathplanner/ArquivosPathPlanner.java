package frc.robot.luverlib.pathplanner;

import com.pathplanner.lib.auto.NamedCommands;
import com.pathplanner.lib.path.PathPlannerPath;
import edu.wpi.first.wpilibj.Filesystem;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/** Confere os arquivos antes dos carregadores que substituem falhas por comandos vazios. */
final class ArquivosPathPlanner {
  static void validarAutonomo(String nome, boolean holonomico) {
    JSONObject json = ler("autos", nome, ".auto");
    validarVersao(json);
    if (Boolean.TRUE.equals(json.get("choreoAuto"))) {
      throw new IllegalArgumentException("Este módulo carrega autos PathPlanner; importe caminhos "
          + "Choreo pela API específica de Choreo antes de compor seus comandos.");
    }
    List<PathPlannerPath> caminhos = new ArrayList<>();
    validarComando(json.get("command"), false, caminhos);
    if (Boolean.TRUE.equals(json.get("resetOdom")) && caminhos.isEmpty()) {
      throw new IllegalArgumentException("Um auto com resetOdom precisa de um caminho inicial.");
    }
    if (holonomico && !caminhos.isEmpty() && caminhos.get(0).getIdealStartingState() == null) {
      throw new IllegalArgumentException("Defina o estado inicial do primeiro caminho no aplicativo.");
    }
  }

  static PathPlannerPath carregarCaminho(String nome) {
    JSONObject json = ler("paths", nome, ".path");
    validarVersao(json);
    Object marcadores = json.get("eventMarkers");
    if (marcadores instanceof JSONArray eventos) {
      for (Object item : eventos) {
        if (!(item instanceof JSONObject evento)) {
          throw new IllegalArgumentException("Marcador de evento inválido em " + nome);
        }
        if (evento.get("command") != null) {
          validarComando(evento.get("command"), true, new ArrayList<>());
        }
      }
    }
    try {
      return PathPlannerPath.fromPathFile(nome);
    } catch (IOException | ParseException | RuntimeException falha) {
      throw new IllegalArgumentException("Não foi possível carregar o caminho " + nome, falha);
    }
  }

  private static void validarComando(Object objeto, boolean emEvento,
      List<PathPlannerPath> caminhos) {
    if (!(objeto instanceof JSONObject comando)
        || !(comando.get("data") instanceof JSONObject dados)
        || !(comando.get("type") instanceof String tipo)) {
      throw new IllegalArgumentException("Estrutura de comando PathPlanner inválida.");
    }
    switch (tipo) {
      case "named" -> {
        String nome = nomeTexto(dados.get("name"));
        if (!NamedCommands.hasCommand(nome)) {
          throw new IllegalArgumentException("Registre o comando nomeado antes de carregar: " + nome);
        }
      }
      case "path" -> {
        if (emEvento) {
          throw new IllegalArgumentException("Um marcador não pode iniciar outro caminho.");
        }
        caminhos.add(carregarCaminho(nomeTexto(dados.get("pathName"))));
      }
      case "wait" -> {
        Object tempo = dados.get("waitTime");
        if (tempo instanceof JSONObject expressao) {
          tempo = expressao.get("val");
        }
        if (!(tempo instanceof Number numero) || !Double.isFinite(numero.doubleValue())
            || numero.doubleValue() < 0.0) {
          throw new IllegalArgumentException("O tempo de espera deve ser finito e não negativo.");
        }
      }
      case "sequential", "parallel", "race", "deadline" -> {
        if (!(dados.get("commands") instanceof JSONArray filhos)) {
          throw new IllegalArgumentException("O grupo de comandos precisa de uma lista.");
        }
        for (Object filho : filhos) {
          validarComando(filho, emEvento, caminhos);
        }
      }
      default -> throw new IllegalArgumentException("Tipo de comando desconhecido: " + tipo);
    }
  }

  private static JSONObject ler(String pasta, String nome, String extensao) {
    validarNomeArquivo(nome);
    Path arquivo = Filesystem.getDeployDirectory().toPath()
        .resolve("pathplanner").resolve(pasta).resolve(nome + extensao);
    try {
      Object json = new JSONParser().parse(Files.readString(arquivo, StandardCharsets.UTF_8));
      if (json instanceof JSONObject objeto) {
        return objeto;
      }
      throw new IllegalArgumentException("O JSON precisa conter um objeto: " + arquivo);
    } catch (IOException | ParseException falha) {
      throw new IllegalArgumentException("Confira o arquivo PathPlanner: " + arquivo, falha);
    }
  }

  private static void validarVersao(JSONObject json) {
    // PathPlannerLib 2026.1.2 usa formato 2025.X; não é o ano da WPILib.
    if (!(json.get("version") instanceof String versao) || !versao.startsWith("2025.")) {
      throw new IllegalArgumentException("Reexporte no PathPlanner compatível: formato esperado 2025.X.");
    }
  }

  static void validarNomeArquivo(String nome) {
    nomeTexto(nome);
    if (nome.contains("/") || nome.contains("\\") || nome.equals(".") || nome.equals("..")
        || nome.endsWith(".auto") || nome.endsWith(".path")) {
      throw new IllegalArgumentException("Informe o nome do arquivo, sem pasta ou extensão: " + nome);
    }
  }

  static String nomeTexto(Object valor) {
    if (!(valor instanceof String nome) || nome.isBlank() || nome.indexOf('\0') >= 0) {
      throw new IllegalArgumentException("O nome não pode estar vazio ou conter caractere nulo.");
    }
    return nome;
  }

  private ArquivosPathPlanner() {}
}
