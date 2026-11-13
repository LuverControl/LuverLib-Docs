package frc.robot.luverlib.limelight;

import static frc.robot.luverlib.limelight.AvaliacaoPose.Motivo.*;

import edu.wpi.first.networktables.DoubleArrayPublisher;
import edu.wpi.first.networktables.DoubleArraySubscriber;
import edu.wpi.first.networktables.DoublePublisher;
import edu.wpi.first.networktables.DoubleSubscriber;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.PubSubOption;
import edu.wpi.first.networktables.StringSubscriber;
import edu.wpi.first.wpilibj.Timer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.DoubleSupplier;

/**
 * Uma câmera LimelightOS, identificada pelo projeto. Use VisaoLimelight para atualização automática
 * ou chame atualizar() no laço principal. Nenhum nome, pipeline ou montagem é imposto ao criar.
 * Leituras de alvo usam o pacote atômico t2d; poses usam somente um algoritmo e a origem azul.
 */
public final class Limelight implements AutoCloseable {
  public enum ModoLed {
    PIPELINE(0), DESLIGADO(1), PISCAR(2), LIGADO(3), ESQUERDA(4), DIREITA(5),
    ALTERNAR(6), PISCAR_ESQUERDA(7), PISCAR_DIREITA(8);
    private final int valor;
    ModoLed(int valor) { this.valor = valor; }
  }

  public enum ModoImu {
    EXTERNA(0), EXTERNA_COM_INICIALIZACAO(1), INTERNA(2),
    INTERNA_ASSISTIDA_MEGATAG1(3), INTERNA_ASSISTIDA_EXTERNA(4);
    private final int valor;
    ModoImu(int valor) { this.valor = valor; }
  }

  private final String nome;
  private final ConfiguracaoLimelight configuracao;
  private final NetworkTableInstance rede;
  private final NetworkTable tabela;
  private final DoubleSupplier relogio;
  private final List<AutoCloseable> recursos = new ArrayList<>();
  private final Map<String, DoublePublisher> numeros = new HashMap<>();
  private final Map<String, DoubleArrayPublisher> vetores = new HashMap<>();
  private final DoubleSubscriber heartbeat;
  private final DoubleSubscriber pipeline;
  private final DoubleArraySubscriber alvo;
  private final DoubleArraySubscriber poses;
  private final DoubleArraySubscriber neural;
  private final DoubleArraySubscriber hardware;
  private final DoubleArraySubscriber imu;
  private final StringSubscriber classeDetector;
  private Optional<AlvoLimelight> ultimoAlvo = Optional.empty();
  private List<DeteccaoNeural> deteccoes = List.of();
  private long horarioNeuralMicros;
  private long horarioHeartbeatMicros;
  private long ultimaPoseProcessadaMicros;
  private double ultimoHeartbeat = -1;
  private double instanteMinimoDados;
  private double instanteOrientacao = Double.NEGATIVE_INFINITY;
  private double primeiroEnvioOrientacao = Double.POSITIVE_INFINITY;
  private double primeiraInicializacaoImu = Double.POSITIVE_INFINITY;
  private OrientacaoRobo orientacao;
  private Set<Integer> tagsPermitidas = Set.of();
  private ModoImu modoImu = ModoImu.EXTERNA;
  private int pipelineSolicitado = -1;
  private long aceitas;
  private long rejeitadas;
  private boolean pacoteNeuralValido = true;
  private boolean fechada;
  private AvaliacaoPose ultimaAvaliacao = AvaliacaoPose.rejeitada(SEM_MEDICAO);

  public Limelight(String nomeTabela) {
    this(nomeTabela, VariaveisLimelight.configuracaoPadrao());
  }

  public Limelight(String nomeTabela, ConfiguracaoLimelight configuracao) {
    this(nomeTabela, configuracao, NetworkTableInstance.getDefault(), Timer::getFPGATimestamp);
  }

  /** Permite rede/relógio próprios em simulação. O relógio deve usar a mesma base NT/FPGA, em s. */
  public Limelight(String nomeTabela, ConfiguracaoLimelight configuracao,
      NetworkTableInstance rede, DoubleSupplier relogio) {
    if (nomeTabela == null || nomeTabela.isBlank() || nomeTabela.contains("/")
        || !nomeTabela.equals(nomeTabela.strip())) {
      throw new IllegalArgumentException("Informe o nome exato da câmera, sem barras ou espaços externos.");
    }
    this.nome = nomeTabela;
    this.configuracao = Objects.requireNonNull(configuracao);
    this.rede = Objects.requireNonNull(rede);
    this.relogio = Objects.requireNonNull(relogio);
    tabela = rede.getTable(nomeTabela);
    heartbeat = assinarNumero("hb", -1);
    pipeline = assinarNumero("getpipe", -1);
    alvo = assinarVetor("t2d");
    poses = assinarVetor(configuracao.metodoPose().topico());
    neural = assinarVetor("rawdetections");
    hardware = assinarVetor("hw");
    imu = assinarVetor("imu");
    classeDetector = tabela.getStringTopic("tdclass").subscribe("");
    recursos.add(classeDetector);
  }

  /** Retorna somente medições novas e aceitas. Repetir a chamada não reaplica o mesmo quadro. */
  public List<MedicaoVisao> atualizar() {
    garantirAberta();
    double agora = agora();
    var hb = heartbeat.getAtomic();
    boolean reiniciou = ultimoHeartbeat >= 0 && hb.value >= 0 && hb.value < ultimoHeartbeat;
    if (reiniciou) {
      orientacao = null;
      primeiroEnvioOrientacao = Double.POSITIVE_INFINITY;
      primeiraInicializacaoImu = Double.POSITIVE_INFINITY;
      instanteOrientacao = Double.NEGATIVE_INFINITY;
      instanteMinimoDados = agora;
      ultimoAlvo = Optional.empty();
      deteccoes = List.of();
    }
    ultimoHeartbeat = hb.value;
    horarioHeartbeatMicros = Double.isFinite(hb.value) && hb.value >= 0 ? hb.timestamp : 0;
    boolean conectada = estaConectada();
    boolean pipelinePronto = pipelineConfirmado();
    var amostraAlvo = alvo.getAtomic();
    ultimoAlvo = conectada && pipelinePronto
        ? DecodificadorLimelight.alvo(amostraAlvo, agora,
            configuracao.idadeMaximaQuadroSegundos(), instanteMinimoDados) : Optional.empty();
    var amostraNeural = neural.getAtomic();
    horarioNeuralMicros = amostraNeural.timestamp;
    var decodificadas = DecodificadorLimelight.deteccoes(amostraNeural.value);
    pacoteNeuralValido = decodificadas.isPresent();
    deteccoes = conectada && pipelinePronto && recente(horarioNeuralMicros, agora)
        && horarioNeuralMicros * 1e-6 >= instanteMinimoDados
        ? decodificadas.orElse(List.of()) : List.of();

    OrientacaoRobo orientacaoFiltro = orientacao;
    double horarioOrientacaoFiltro = instanteOrientacao;
    if (modoImu == ModoImu.INTERNA || modoImu == ModoImu.INTERNA_ASSISTIDA_MEGATAG1
        || modoImu == ModoImu.INTERNA_ASSISTIDA_EXTERNA) {
      var leituraImu = imu.getAtomic();
      double[] v = leituraImu.value;
      if (v.length >= 10 && DecodificadorLimelight.finitos(v) && recente(leituraImu.timestamp, agora)
          && leituraImu.timestamp * 1e-6 >= primeiraInicializacaoImu) {
        orientacaoFiltro = new OrientacaoRobo(v[0], v[6], v[2], v[5], v[1], v[4]);
        horarioOrientacaoFiltro = leituraImu.timestamp * 1e-6;
      } else {
        orientacaoFiltro = null;
      }
    } else if (configuracao.metodoPose() == MetodoPose.MEGATAG_1
        && agora - instanteOrientacao > configuracao.idadeMaximaOrientacaoSegundos()) {
      orientacaoFiltro = null;
    }

    List<MedicaoVisao> novas = new ArrayList<>();
    for (var amostra : poses.readQueue()) {
      if (amostra.timestamp <= ultimaPoseProcessadaMicros) continue;
      ultimaPoseProcessadaMicros = amostra.timestamp;
      AvaliacaoPose avaliacao;
      if (!conectada) avaliacao = AvaliacaoPose.rejeitada(CAMERA_DESCONECTADA);
      else if (!pipelinePronto || amostra.timestamp * 1e-6 < instanteMinimoDados) {
        avaliacao = AvaliacaoPose.rejeitada(AGUARDANDO_PIPELINE);
      } else {
        avaliacao = DecodificadorLimelight.pose(nome, configuracao, amostra, agora,
            orientacaoFiltro, horarioOrientacaoFiltro, primeiroEnvioOrientacao, tagsPermitidas);
        if (avaliacao.medicao().isPresent()
            && avaliacao.medicao().get().instanteCapturaSegundos() < instanteMinimoDados) {
          avaliacao = AvaliacaoPose.rejeitada(AGUARDANDO_PIPELINE);
        }
      }
      ultimaAvaliacao = avaliacao;
      if (avaliacao.medicao().isPresent()) {
        novas.add(avaliacao.medicao().get());
        aceitas++;
      } else {
        rejeitadas++;
      }
    }
    if (!conectada) ultimaAvaliacao = AvaliacaoPose.rejeitada(CAMERA_DESCONECTADA);
    else if (!pipelinePronto) ultimaAvaliacao = AvaliacaoPose.rejeitada(AGUARDANDO_PIPELINE);
    else if (ultimaAvaliacao.medicao().isPresent()
        && agora - ultimaAvaliacao.medicao().get().instanteCapturaSegundos()
            > configuracao.idadeMaximaQuadroSegundos()) {
      ultimaAvaliacao = AvaliacaoPose.rejeitada(MEDICAO_ANTIGA);
    }
    return List.copyOf(novas);
  }

  public boolean estaConectada() {
    return !fechada && recente(horarioHeartbeatMicros, agora());
  }

  public Optional<AlvoLimelight> obterAlvo() {
    return estaConectada() && ultimoAlvo.isPresent()
        && agora() - ultimoAlvo.get().instanteCapturaSegundos()
            <= configuracao.idadeMaximaQuadroSegundos() ? ultimoAlvo : Optional.empty();
  }

  /** São detecções brutas: seleção por classe e limiar de confiança pertencem ao modelo/pipeline. */
  public List<DeteccaoNeural> obterDeteccoesNeurais() {
    return estaConectada() && recente(horarioNeuralMicros, agora()) ? deteccoes : List.of();
  }

  public Optional<DeteccaoNeural> buscarDeteccaoNeural(int classe) {
    if (classe < 0) throw new IllegalArgumentException("A classe neural não pode ser negativa.");
    return obterDeteccoesNeurais().stream().filter(d -> d.classe() == classe)
        .max(Comparator.comparingDouble(DeteccaoNeural::areaPercentual));
  }

  /** Snapshot de qualidade; ausência de medição nunca é substituída por uma pose (0,0,0). */
  public AvaliacaoPose obterUltimaAvaliacaoPose() {
    if (fechada) return AvaliacaoPose.rejeitada(CAMERA_FECHADA);
    if (!estaConectada()) return AvaliacaoPose.rejeitada(CAMERA_DESCONECTADA);
    if (ultimaAvaliacao.medicao().isPresent() && agora()
        - ultimaAvaliacao.medicao().get().instanteCapturaSegundos()
            > configuracao.idadeMaximaQuadroSegundos()) return AvaliacaoPose.rejeitada(MEDICAO_ANTIGA);
    return ultimaAvaliacao;
  }

  /** [temperatura CPU °C, uso CPU %, RAM %, FPS], conforme API NetworkTables atual. */
  public Optional<List<Double>> obterHardware() {
    if (fechada) return Optional.empty();
    var amostra = hardware.getAtomic();
    double[] v = amostra.value;
    if (!estaConectada() || !recente(amostra.timestamp, agora()) || v.length < 4
        || !DecodificadorLimelight.finitos(v) || v[1] < 0 || v[2] < 0 || v[3] < 0) {
      return Optional.empty();
    }
    return Optional.of(List.of(v[0], v[1], v[2], v[3]));
  }

  public String obterClasseDetectorPrimaria() {
    if (fechada || obterAlvo().isEmpty()) return "";
    var amostra = classeDetector.getAtomic();
    return recente(amostra.timestamp, agora()) ? amostra.value : "";
  }

  public String obterNome() { return nome; }
  public ConfiguracaoLimelight obterConfiguracao() { return configuracao; }
  public long obterQuantidadeAceitas() { return aceitas; }
  public long obterQuantidadeRejeitadas() { return rejeitadas; }
  public boolean pacoteNeuralValido() { return pacoteNeuralValido; }
  public int obterPipelineAtivo() {
    if (fechada) return -1;
    double valor = pipeline.get();
    return DecodificadorLimelight.inteiro(valor, 0) && valor <= 9 ? (int) valor : -1;
  }

  /** Envie a cada ciclo para MegaTag2. Não modifica a orientação do giroscópio do robô. */
  public void enviarOrientacao(OrientacaoRobo orientacao) {
    enviarOrientacaoSemFlush(orientacao);
    rede.flush();
  }

  void enviarOrientacaoSemFlush(OrientacaoRobo orientacao) {
    garantirAberta();
    this.orientacao = Objects.requireNonNull(orientacao);
    instanteOrientacao = agora();
    if (!Double.isFinite(primeiroEnvioOrientacao)) primeiroEnvioOrientacao = instanteOrientacao;
    if (modoImu == ModoImu.EXTERNA_COM_INICIALIZACAO && !Double.isFinite(primeiraInicializacaoImu)) {
      primeiraInicializacaoImu = instanteOrientacao;
    }
    publicarVetor("robot_orientation_set", orientacao.paraArray());
  }

  NetworkTableInstance rede() { return rede; }

  public void selecionarPipeline(int indice) {
    garantirAberta();
    if (indice < 0 || indice > 9) throw new IllegalArgumentException("Pipeline deve ser de 0 a 9.");
    if (pipelineSolicitado == indice) return;
    pipelineSolicitado = indice;
    invalidarDados();
    publicarNumero("pipeline", indice);
  }

  public void definirLed(ModoLed modo) {
    publicarNumero("ledMode", Objects.requireNonNull(modo).valor);
  }

  /**
   * Modos internos exigem IMU presente e envio de orientação enquanto estiver no modo
   * EXTERNA_COM_INICIALIZACAO. Enviar orientação no modo EXTERNA não inicializa a IMU da câmera.
   * A aplicação deve aguardar a inicialização física antes de selecionar um modo interno.
   */
  public void definirModoImu(ModoImu modo) {
    garantirAberta();
    Objects.requireNonNull(modo);
    if (modoImu == modo && numeros.containsKey("imumode_set")) return;
    modoImu = modo;
    if (modo == ModoImu.EXTERNA || modo == ModoImu.EXTERNA_COM_INICIALIZACAO) {
      primeiraInicializacaoImu = Double.POSITIVE_INFINITY;
    }
    invalidarDados();
    publicarNumero("imumode_set", modo.valor);
  }

  public void configurarMontagem(PosicaoCamera posicao) {
    garantirAberta();
    publicarVetor("camerapose_robotspace_set", Objects.requireNonNull(posicao).paraArray());
    invalidarDados();
  }

  /** Conjunto vazio remove o filtro. priorityid não substitui o filtro de localização. */
  public void definirTagsPermitidas(Set<Integer> ids) {
    garantirAberta();
    Set<Integer> copia = Set.copyOf(ids);
    if (copia.stream().anyMatch(id -> id < 0)) throw new IllegalArgumentException("Tag inválida.");
    tagsPermitidas = copia;
    publicarVetor("fiducial_id_filters_set", copia.stream().sorted().mapToDouble(i -> i).toArray());
    invalidarDados();
  }

  public void definirTagPrioritaria(int id) {
    if (id < -1) throw new IllegalArgumentException("Use ID válido ou -1 para remover prioridade.");
    publicarNumero("priorityid", id);
  }

  public void definirRecorte(double xMin, double xMax, double yMin, double yMax) {
    double[] valores = {xMin, xMax, yMin, yMax};
    if (!DecodificadorLimelight.finitos(valores) || xMin < -1 || xMax > 1 || yMin < -1
        || yMax > 1 || xMin >= xMax || yMin >= yMax) {
      throw new IllegalArgumentException("Recorte deve ter limites crescentes entre -1 e 1.");
    }
    publicarVetor("crop", valores);
    invalidarDados();
  }

  /** Reduz processamento ao pular quadros; leituras antigas continuam sujeitas ao filtro de idade. */
  public void definirQuadrosIgnorados(int quantidade) {
    if (quantidade < 0) throw new IllegalArgumentException("Quantidade não pode ser negativa.");
    publicarNumero("throttle_set", quantidade);
  }

  private void invalidarDados() {
    instanteMinimoDados = agora();
    ultimoAlvo = Optional.empty();
    deteccoes = List.of();
    poses.readQueue();
    ultimaAvaliacao = AvaliacaoPose.rejeitada(SEM_MEDICAO);
  }

  private boolean pipelineConfirmado() {
    var atual = pipeline.getAtomic();
    return pipelineSolicitado < 0 || (atual.timestamp > 0 && atual.value == pipelineSolicitado);
  }

  private DoubleSubscriber assinarNumero(String chave, double padrao) {
    DoubleSubscriber s = tabela.getDoubleTopic(chave).subscribe(padrao, PubSubOption.keepDuplicates(true));
    recursos.add(s);
    return s;
  }

  private DoubleArraySubscriber assinarVetor(String chave) {
    DoubleArraySubscriber s = tabela.getDoubleArrayTopic(chave).subscribe(new double[0],
        PubSubOption.keepDuplicates(true), PubSubOption.sendAll(true), PubSubOption.pollStorage(64));
    recursos.add(s);
    return s;
  }

  private void publicarNumero(String chave, double valor) {
    garantirAberta();
    numeros.computeIfAbsent(chave, k -> tabela.getDoubleTopic(k).publish()).set(valor);
  }

  private void publicarVetor(String chave, double[] valor) {
    garantirAberta();
    vetores.computeIfAbsent(chave, k -> tabela.getDoubleArrayTopic(k).publish()).set(valor);
  }

  private double agora() {
    double tempo = relogio.getAsDouble();
    if (!Double.isFinite(tempo) || tempo < 0) throw new IllegalStateException("Relógio inválido.");
    return tempo;
  }

  private boolean recente(long micros, double tempo) {
    return DecodificadorLimelight.recente(micros, tempo, configuracao.idadeMaximaQuadroSegundos());
  }

  private void garantirAberta() {
    if (fechada) throw new IllegalStateException("Esta Limelight já foi fechada.");
  }

  /** Fecha apenas conexões/publicadores desta câmera; a instância de NetworkTables é compartilhada. */
  @Override
  public void close() {
    if (fechada) return;
    fechada = true;
    ultimoAlvo = Optional.empty();
    deteccoes = List.of();
    List<AutoCloseable> todos = new ArrayList<>(recursos);
    todos.addAll(numeros.values());
    todos.addAll(vetores.values());
    RuntimeException erro = null;
    for (AutoCloseable recurso : todos) {
      try { recurso.close(); }
      catch (Exception e) {
        if (erro == null) erro = new IllegalStateException("Falha ao fechar conexão da câmera.", e);
        else erro.addSuppressed(e);
      }
    }
    if (erro != null) throw erro;
  }
}
