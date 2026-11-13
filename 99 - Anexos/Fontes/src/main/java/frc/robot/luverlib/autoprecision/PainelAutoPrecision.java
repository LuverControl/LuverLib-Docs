package frc.robot.luverlib.autoprecision;

import edu.wpi.first.networktables.DoubleArrayPublisher;
import edu.wpi.first.networktables.DoublePublisher;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.PubSubOption;
import edu.wpi.first.networktables.StringArrayPublisher;
import edu.wpi.first.networktables.StringArraySubscriber;
import edu.wpi.first.networktables.StringPublisher;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Transporte NT4: pedidos e amostras atômicos; campos escalares legíveis no SmartDashboard. */
final class PainelAutoPrecision implements AutoCloseable {
  final StringArraySubscriber pedidos;
  final StringArraySubscriber heartbeat;
  private final List<AutoCloseable> recursos = new ArrayList<>();
  private final NetworkTable tabela;
  private final Map<String, DoublePublisher> numeros = new LinkedHashMap<>();
  private final StringPublisher sessao;
  private final StringPublisher cliente;
  private final StringPublisher estado;
  private final StringPublisher mensagem;
  private final DoubleArrayPublisher amostras;
  private final DoubleArrayPublisher resultado;
  private final DoubleArrayPublisher melhor;
  private final StringArrayPublisher resposta;

  PainelAutoPrecision(NetworkTableInstance rede, String nome, ConfiguracaoAutoPrecision config,
      String backend) {
    tabela = rede.getTable("SmartDashboard/AutoPrecision/" + nome);
    if (tabela.getTopic("Sessao").exists()) {
      throw new IllegalArgumentException("Já existe AutoPrecision registrado com o nome " + nome);
    }
    try {
      sessao = recurso(tabela.getStringTopic("Sessao").publish());
      cliente = recurso(tabela.getStringTopic("Cliente").publish());
      estado = recurso(tabela.getStringTopic("Estado").publish());
      mensagem = recurso(tabela.getStringTopic("Mensagem").publish());
      recurso(tabela.getStringTopic("Unidade").publish()).set(config.unidade());
      recurso(tabela.getStringTopic("Backend").publish()).set(backend);
      recurso(tabela.getIntegerTopic("Protocolo").publish()).set(1);
      for (String campo : List.of("Posicao", "Velocidade", "Referencia", "Alvo", "Erro",
          "P", "I", "D", "Saida", "Corrente", "Temperatura", "Ensaio", "Tempo", "Progresso")) {
        numeros.put(campo, recurso(tabela.getDoubleTopic(campo).publish()));
      }
      amostras = recurso(tabela.getDoubleArrayTopic("Amostra").publish(
          PubSubOption.periodic(config.periodoSegundos()), PubSubOption.sendAll(true),
          PubSubOption.keepDuplicates(true)));
      resultado = recurso(tabela.getDoubleArrayTopic("Resultado").publish());
      melhor = recurso(tabela.getDoubleArrayTopic("MelhorPID").publish());
      resposta = recurso(tabela.getStringArrayTopic("Resposta").publish());
      pedidos = recurso(tabela.getStringArrayTopic("Pedido").subscribe(new String[0],
          PubSubOption.pollStorage(16), PubSubOption.keepDuplicates(true), PubSubOption.sendAll(true)));
      heartbeat = recurso(tabela.getStringArrayTopic("Heartbeat").subscribe(new String[0]));
      var l = config.limites();
      var g = config.ganhosMaximos();
      var inicial = config.ganhosIniciais();
      recurso(tabela.getDoubleArrayTopic("Configuracao").publish()).set(new double[] {
          l.posicaoMinima(), l.posicaoMaxima(), l.saidaMaxima(), l.velocidadeMaxima(),
          l.aceleracaoMaxima(), l.correnteMaxima(), l.temperaturaMaxima(), l.deslocamentoMaximoEnsaio(),
          config.toleranciaPosicao(), config.toleranciaVelocidade(), config.tempoEstavelSegundos(),
          config.duracaoMaximaEnsaioSegundos(), config.periodoSegundos(), config.timeoutHeartbeatSegundos(),
          g.p(), g.i(), g.d(), inicial.p(), inicial.i(), inicial.d(), l.velocidadeMaximaMedida()});
      estado("", "", "DESARMADO", "Aguardando armação local no modo Test.");
    } catch (RuntimeException | Error falha) {
      close();
      throw falha;
    }
  }

  void estado(String nonce, String dono, String fase, String texto) {
    sessao.set(nonce);
    cliente.set(dono);
    estado.set(fase);
    mensagem.set(texto);
  }

  void medicao(ControleAutoPrecision.Medicao m, GanhosPID ganhos, double referencia,
      double alvo, long ensaio, double tempo, double progresso) {
    double[] valores = {m.posicao(), m.velocidade(), referencia, alvo, alvo - m.posicao(),
        ganhos.p(), ganhos.i(), ganhos.d(), m.saida(), m.corrente(), m.temperatura(),
        ensaio, tempo, progresso};
    int i = 0;
    for (DoublePublisher publicador : numeros.values()) publicador.set(valores[i++]);
  }

  void amostra(double[] valores) { amostras.set(valores); }
  void resultado(long id, int fase, long quantidade, long chaveSessao) {
    resultado.set(new double[] {id, fase, quantidade, quantidade, chaveSessao});
  }
  void reiniciarSessao() {
    amostras.set(new double[0]);
    resultado.set(new double[0]);
    melhor.set(new double[0]);
    resposta.set(new String[0]);
  }
  void melhor(GanhosPID ganhos) { melhor.set(new double[] {ganhos.p(), ganhos.i(), ganhos.d()}); }
  void resposta(String nonce, String dono, long id, boolean aceita, String motivo) {
    resposta.set(new String[] {nonce, dono, Long.toString(id), aceita ? "ACEITO" : "RECUSADO", motivo});
  }

  private <T extends AutoCloseable> T recurso(T recurso) {
    recursos.add(recurso);
    return recurso;
  }

  @Override
  public void close() {
    for (int i = recursos.size() - 1; i >= 0; i--) {
      AutoCloseable recurso = recursos.get(i);
      try { recurso.close(); } catch (Exception falha) {
        // Feche todos os demais recursos, mesmo se um transporte já tiver sido encerrado.
      }
    }
    recursos.clear();
  }
}
