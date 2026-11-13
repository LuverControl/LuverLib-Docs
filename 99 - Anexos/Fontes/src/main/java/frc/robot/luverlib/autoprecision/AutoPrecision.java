package frc.robot.luverlib.autoprecision;

import edu.wpi.first.math.trajectory.TrapezoidProfile;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.NetworkTablesJNI;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj2.command.Subsystem;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;

/**
 * Servidor de ensaios PID. O PC otimiza; o controle e os limites são executados localmente.
 * Armação exige modo Test e autorização local durante toda a sessão.
 */
public final class AutoPrecision extends SubsystemBase implements AutoCloseable {
  public enum Estado { DESARMADO, PRONTO, EXECUTANDO, CONCLUIDO, FALHA }

  private final ConfiguracaoAutoPrecision config;
  private final ControleAutoPrecision controle;
  private final BooleanSupplier autorizacao;
  private final BooleanSupplier modoTeste;
  private final DoubleSupplier relogio;
  private final PainelAutoPrecision painel;
  private final Command comando;
  private final TrapezoidProfile perfil;
  private TrapezoidProfile.State referencia = new TrapezoidProfile.State();
  private GanhosPID ganhos;
  private Estado estado = Estado.DESARMADO;
  private String sessao = "";
  private String cliente = "";
  private String mensagem = "Aguardando armação local.";
  private long ultimoHeartbeat = -1;
  private long ultimoPedido = -1;
  private long ensaio;
  private long quantidade;
  private long chaveSessao;
  private double ultimoContato;
  private double inicio;
  private double ultimoCiclo;
  private double alvo;
  private double duracao;
  private double posicaoInicial;
  private boolean backendEmUso;
  private boolean encerrando;
  private boolean encerrar;
  private boolean fechado;

  public AutoPrecision(String nome, Subsystem mecanismo, ControleAutoPrecision controle,
      ConfiguracaoAutoPrecision config, BooleanSupplier autorizacaoLocal) {
    this(nome, mecanismo, controle, config, autorizacaoLocal, NetworkTableInstance.getDefault(),
        Timer::getFPGATimestamp, DriverStation::isTestEnabled);
  }

  // Injeção de relógio/rede/modo restrita ao pacote para simulação e testes determinísticos.
  AutoPrecision(String nome, Subsystem mecanismo, ControleAutoPrecision controle,
      ConfiguracaoAutoPrecision config, BooleanSupplier autorizacaoLocal,
      NetworkTableInstance rede, DoubleSupplier relogio, BooleanSupplier modoTeste) {
    super(validarNome(nome));
    PainelAutoPrecision criado = null;
    try {
    this.controle = Objects.requireNonNull(controle);
    this.config = Objects.requireNonNull(config);
    autorizacao = Objects.requireNonNull(autorizacaoLocal);
    this.relogio = Objects.requireNonNull(relogio);
    this.modoTeste = Objects.requireNonNull(modoTeste);
    Objects.requireNonNull(mecanismo);
    ganhos = config.ganhosIniciais();
    perfil = new TrapezoidProfile(new TrapezoidProfile.Constraints(
        config.limites().velocidadeMaxima(), config.limites().aceleracaoMaxima()));
    criado = new PainelAutoPrecision(rede, nome, config, controle.identificador());
    painel = criado;
    comando = new Command() {
      { addRequirements(AutoPrecision.this, mecanismo); setName("AutoPrecision " + nome); }
      @Override public void initialize() { armar(); }
      @Override public void execute() { ciclo(); }
      @Override public boolean isFinished() { return encerrar; }
      @Override public void end(boolean interrompido) { desarmar(); }
    };
    } catch (RuntimeException | Error falha) {
      if (criado != null) criado.close();
      CommandScheduler.getInstance().unregisterSubsystem(this);
      throw falha;
    }
  }

  /** Associe a whileTrue de um botão local, ou agende no testInit com autorização local separada. */
  public Command criarComando() {
    if (fechado) throw new IllegalStateException("AutoPrecision encerrado.");
    return comando;
  }

  public Estado obterEstado() { return estado; }
  public String obterSessao() { return sessao; }

  @Override
  public void periodic() {
    if (fechado || comando.isScheduled()) return;
    try {
      var m = controle.medir();
      if (!fechado && m.finita()) painel.medicao(m, ganhos, m.posicao(), m.posicao(), ensaio, 0, 0);
    } catch (RuntimeException falha) {
      painel.estado(sessao, cliente, estado.name(), "Leitura indisponível: " + falha.getMessage());
    }
  }

  private void armar() {
    if (fechado) { encerrar = true; return; }
    encerrar = false;
    UUID nonce = UUID.randomUUID();
    sessao = nonce.toString();
    chaveSessao = nonce.getLeastSignificantBits() & ((1L << 52) - 1);
    painel.reiniciarSessao();
    cliente = "";
    ultimoHeartbeat = -1;
    ultimoPedido = -1;
    quantidade = 0;
    ensaio = 0;
    estado = Estado.PRONTO;
    try {
      exigirAutorizacao();
      controle.parar();
      if (fechado || encerrar) return;
      var inicial = controle.medir();
      if (fechado || encerrar) return;
      conferirMedicao(inicial);
      backendEmUso = true;
      controle.iniciar(config);
      if (fechado || encerrar) return;
      exigirAutorizacao();
      ganhos = config.ganhosIniciais();
      ultimoCiclo = relogio.getAsDouble();
      mensagem = "Armado. Aguardando supervisor e heartbeat.";
      publicarEstado();
    } catch (RuntimeException falha) { falhar(falha.getMessage()); }
  }

  private void ciclo() {
    if (fechado || encerrar) { encerrar = true; return; }
    try {
      exigirAutorizacao();
      double agora = relogio.getAsDouble();
      if (!Double.isFinite(agora) || agora < ultimoCiclo
          || estado == Estado.EXECUTANDO && agora - ultimoCiclo > config.periodoSegundos() * 4) {
        throw new IllegalStateException("Relógio ou período de controle inválido.");
      }
      receberHeartbeat(agora);
      if (!cliente.isEmpty() && agora - ultimoContato > config.timeoutHeartbeatSegundos()) {
        throw new IllegalStateException("Supervisor sem heartbeat; sessão encerrada.");
      }
      var medicao = controle.medir();
      if (fechado || encerrar) return;
      conferirMedicao(medicao);
      for (String[] pedido : painel.pedidos.readQueueValues()) {
        processarPedido(pedido, medicao);
        if (encerrar) break;
      }
      if (encerrar) return;
      // Configuração CAN pode bloquear. Não use autorização, lease ou leitura anteriores à operação.
      exigirAutorizacao();
      agora = relogio.getAsDouble();
      receberHeartbeat(agora);
      conferirContato(agora);
      medicao = controle.medir();
      if (fechado || encerrar) return;
      conferirMedicao(medicao);
      exigirAutorizacao();
      agora = relogio.getAsDouble();
      conferirContato(agora);
      if (estado == Estado.EXECUTANDO) {
        if (!Double.isFinite(agora) || agora < ultimoCiclo
            || agora - ultimoCiclo > config.periodoSegundos() * 4) {
          throw new IllegalStateException("A operação local atrasou o ciclo de controle.");
        }
        double tempo = agora - inicio;
        if (tempo >= duracao) {
          controle.parar();
          if (fechado || encerrar) return;
          estado = Estado.CONCLUIDO;
          mensagem = "Ensaio concluído; saída parada.";
        } else {
          referencia = perfil.calculate(Math.max(0, agora - ultimoCiclo), referencia,
              new TrapezoidProfile.State(alvo, 0));
          controle.aplicarReferencia(referencia.position);
          if (fechado || encerrar) return;
        }
        quantidade++;
        painel.amostra(new double[] {ensaio, tempo, medicao.posicao(), medicao.velocidade(),
            referencia.position, alvo, medicao.saida(), medicao.corrente(), medicao.temperatura(),
            ganhos.p(), ganhos.i(), ganhos.d(), quantidade, estado.ordinal(), agora, chaveSessao});
        painel.medicao(medicao, ganhos, referencia.position, alvo, ensaio, tempo,
            Math.min(1, tempo / duracao));
        if (estado == Estado.CONCLUIDO) painel.resultado(ensaio, estado.ordinal(), quantidade, chaveSessao);
      } else {
        painel.medicao(medicao, ganhos, medicao.posicao(), alvo, ensaio, 0, 0);
      }
      ultimoCiclo = agora;
      publicarEstado();
    } catch (RuntimeException falha) { falhar(falha.getMessage()); }
  }

  private void receberHeartbeat(double agora) {
    // Leitura genérica mantém valor/timestamp atômicos e evita o wrapper JNI
    // TimestampedStringArray, que falha no runtime WPILib 2026.2.1 instalado.
    var recebido = NetworkTablesJNI.getValue(painel.heartbeat.getHandle());
    if (!recebido.isStringArray()) return;
    long timestamp = recebido.getTime();
    long idadeMicros = NetworkTablesJNI.now() - timestamp;
    if (timestamp <= 0 || idadeMicros < 0
        || idadeMicros > config.timeoutHeartbeatSegundos() * 1e6) return;
    String[] valor = recebido.getStringArray();
    if (valor.length != 3 || !valor[0].equals(sessao) || valor[1].isBlank()) return;
    if (!cliente.isEmpty() && !cliente.equals(valor[1])) return;
    try {
      long numero = inteiro(valor[2]);
      if (numero > ultimoHeartbeat) {
        cliente = valor[1];
        ultimoHeartbeat = numero;
        ultimoContato = agora - idadeMicros * 1e-6;
      }
    } catch (IllegalArgumentException ignorada) { /* Heartbeat inválido não renova a sessão. */ }
  }

  private void processarPedido(String[] pedido, ControleAutoPrecision.Medicao medicao) {
    if (pedido.length != 10 || !pedido[0].equals("1") || !pedido[1].equals(sessao)
        || cliente.isEmpty() || !pedido[2].equals(cliente)) return;
    long id;
    try { id = inteiro(pedido[3]); } catch (IllegalArgumentException invalido) { return; }
    if (id <= ultimoPedido) return;
    ultimoPedido = id;
    try {
      if (pedido[4].equals("PARAR")) {
        controle.parar();
        if (fechado || encerrar) return;
        encerrar = true;
      } else {
        if (estado == Estado.EXECUTANDO) {
          throw new IllegalArgumentException("Aguarde terminar o ensaio atual.");
        }
        GanhosPID novos = new GanhosPID(Double.parseDouble(pedido[5]),
            Double.parseDouble(pedido[6]), Double.parseDouble(pedido[7]));
        if (!novos.dentroDe(config.ganhosMaximos())) {
          throw new IllegalArgumentException("Ganhos acima do limite do mecanismo.");
        }
        if (pedido[4].equals("ENSAIO")) {
          double destino = Double.parseDouble(pedido[8]);
          double segundos = Double.parseDouble(pedido[9]);
          var limites = config.limites();
          if (!Double.isFinite(destino) || destino <= limites.posicaoMinima()
              || destino >= limites.posicaoMaxima()
              || Math.abs(destino - medicao.posicao()) > limites.deslocamentoMaximoEnsaio()
              || !Double.isFinite(segundos) || segundos < config.periodoSegundos() * 3
              || segundos > config.duracaoMaximaEnsaioSegundos()) {
            throw new IllegalArgumentException("Alvo, deslocamento ou duração fora dos limites.");
          }
          double antesConfigurar = relogio.getAsDouble();
          controle.aplicarGanhos(novos);
          if (fechado || encerrar) return;
          conferirDuracaoConfiguracao(antesConfigurar);
          var partida = controle.medir();
          if (fechado || encerrar) return;
          conferirMedicao(partida);
          if (Math.abs(destino - partida.posicao()) > limites.deslocamentoMaximoEnsaio()) {
            throw new IllegalStateException("O mecanismo moveu durante a configuração; arme novamente.");
          }
          ganhos = novos;
          posicaoInicial = partida.posicao();
          referencia = new TrapezoidProfile.State(partida.posicao(), partida.velocidade());
          alvo = destino;
          duracao = segundos;
          ensaio = id;
          quantidade = 0;
          inicio = relogio.getAsDouble();
          ultimoCiclo = inicio;
          estado = Estado.EXECUTANDO;
          mensagem = "Executando ensaio " + ensaio;
        } else if (pedido[4].equals("APLICAR")) {
          double antesConfigurar = relogio.getAsDouble();
          controle.aplicarGanhos(novos);
          if (fechado || encerrar) return;
          conferirDuracaoConfiguracao(antesConfigurar);
          ganhos = novos;
          painel.melhor(novos);
          mensagem = "Ganhos selecionados em RAM; exporte para o projeto antes de desarmar.";
        } else throw new IllegalArgumentException("Ação desconhecida.");
      }
      painel.resposta(sessao, cliente, id, true, "Pedido aceito.");
    } catch (IllegalArgumentException invalido) {
      painel.resposta(sessao, cliente, id, false, invalido.getMessage());
    }
  }

  private void exigirAutorizacao() {
    if (!modoTeste.getAsBoolean() || !autorizacao.getAsBoolean()) {
      throw new IllegalStateException("Requer modo Test habilitado e autorização local mantida.");
    }
  }

  private void conferirDuracaoConfiguracao(double inicioConfiguracao) {
    double decorrido = relogio.getAsDouble() - inicioConfiguracao;
    if (!Double.isFinite(decorrido) || decorrido < 0 || decorrido > config.timeoutHeartbeatSegundos()) {
      throw new IllegalStateException("Configuração CAN excedeu o tempo de supervisão permitido.");
    }
  }

  private void conferirContato(double agora) {
    if (!Double.isFinite(agora)
        || !cliente.isEmpty() && agora - ultimoContato > config.timeoutHeartbeatSegundos()) {
      throw new IllegalStateException("Supervisor sem heartbeat; sessão encerrada.");
    }
  }

  private void conferirMedicao(ControleAutoPrecision.Medicao m) {
    var l = config.limites();
    if (m == null || !m.finita() || Math.abs(m.velocidade()) > l.velocidadeMaximaMedida()
        || m.posicao() <= l.posicaoMinima()
        || m.posicao() >= l.posicaoMaxima() || m.corrente() < 0 || m.corrente() > l.correnteMaxima()
        || m.temperatura() > l.temperaturaMaxima() || Math.abs(m.saida()) > l.saidaMaxima() + 1e-4) {
      throw new IllegalStateException("Leitura inválida ou limite do mecanismo ultrapassado.");
    }
    if (estado == Estado.EXECUTANDO
        && Math.abs(m.posicao() - posicaoInicial) > l.deslocamentoMaximoEnsaio()) {
      throw new IllegalStateException("Excursão máxima do ensaio ultrapassada.");
    }
  }

  private void falhar(String motivo) {
    estado = Estado.FALHA;
    encerrar = true;
    mensagem = Objects.toString(motivo, "Falha no ensaio.");
    try { controle.parar(); } catch (RuntimeException parada) {
      mensagem += "; falha ao parar: " + parada.getMessage();
    }
    painel.resultado(ensaio, estado.ordinal(), quantidade, chaveSessao);
    publicarEstado();
  }

  private void desarmar() {
    if (fechado && !encerrando) return;
    if (backendEmUso) {
      try { controle.finalizar(); backendEmUso = false; }
      catch (RuntimeException falha) { falhar(falha.getMessage()); }
    }
    if (estado != Estado.FALHA) {
      estado = Estado.DESARMADO;
      mensagem = "Desarmado; parâmetros do controlador restaurados. P/I/D exibidos são do último ensaio.";
    }
    if (!fechado) publicarEstado();
  }

  private void publicarEstado() { painel.estado(sessao, cliente, estado.name(), mensagem); }

  private static long inteiro(String texto) {
    long valor = Long.parseLong(texto);
    if (valor < 0 || valor > (1L << 52)) throw new IllegalArgumentException("Contador inválido.");
    return valor;
  }

  private static String validarNome(String nome) {
    if (nome == null || !nome.matches("[\\p{L}\\p{N}_-]{1,64}")) {
      throw new IllegalArgumentException("Use nome simples, sem espaços ou barras, até 64 caracteres.");
    }
    return "AutoPrecision/" + nome;
  }

  @Override
  public void close() {
    if (fechado) return;
    fechado = true;
    encerrar = true;
    encerrando = true;
    try {
      CommandScheduler.getInstance().cancel(comando);
      desarmar();
      controle.parar();
      if (backendEmUso) {
        throw new IllegalStateException("Falha ao restaurar os parâmetros temporários do controlador.");
      }
    } finally {
      encerrando = false;
      painel.close();
      CommandScheduler.getInstance().unregisterSubsystem(this);
    }
  }
}
