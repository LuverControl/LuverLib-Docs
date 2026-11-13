package frc.robot.luverlib.advantagescope;

import edu.wpi.first.networktables.BooleanPublisher;
import edu.wpi.first.networktables.DoublePublisher;
import edu.wpi.first.networktables.IntegerPublisher;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.PubSubOption;
import edu.wpi.first.networktables.Publisher;
import edu.wpi.first.networktables.StringPublisher;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

/**
 * Canal de saída, também utilizável como Consumer em callbacks. Pertence ao AdvantageScope que o
 * criou. Leituras inválidas mantêm a última amostra e marcam Valido=false; nunca inventam pose zero.
 */
public final class CanalAdvantageScope<T> implements Consumer<T> {
  @FunctionalInterface
  interface Publicacao<T> {
    void publicar(T valor, long instanteMicros);
  }

  private final Publisher publicador;
  private final Publicacao<T> publicacao;
  private final Predicate<T> validar;
  private final LongSupplier relogio;
  private final BooleanPublisher valido;
  private final IntegerPublisher falhas;
  private final DoublePublisher ultimaAmostra;
  private final StringPublisher erro;
  private Supplier<T> fonte;
  private boolean fechado;
  private boolean amostraValida;
  private long quantidadeFalhas;

  CanalAdvantageScope(Publisher publicador, Publicacao<T> publicacao, Predicate<T> validar,
      NetworkTable diagnostico, PubSubOption[] opcoes, LongSupplier relogio) {
    this.publicador = publicador;
    this.publicacao = publicacao;
    this.validar = validar;
    this.relogio = relogio;
    valido = diagnostico.getBooleanTopic("Valido").publish(opcoes);
    falhas = diagnostico.getIntegerTopic("Falhas").publish(opcoes);
    ultimaAmostra = diagnostico.getDoubleTopic("UltimaAmostraSegundos").publish(opcoes);
    ultimaAmostra.getTopic().setProperty("unit", "\"s\"");
    erro = diagnostico.getStringTopic("UltimoErro").publish(opcoes);
    long agora = relogio.getAsLong();
    valido.set(false, agora);
    falhas.set(0, agora);
    erro.set("", agora);
  }

  void definirFonte(Supplier<T> fonte) {
    this.fonte = Objects.requireNonNull(fonte, "Informe a fonte dos dados.");
  }

  /** Publica uma amostra de evento. Chamadas após encerrar o painel são ignoradas. */
  @Override
  public synchronized void accept(T valor) {
    publicar(valor, relogio.getAsLong());
  }

  synchronized void atualizar(long instanteMicros) {
    if (fechado || fonte == null) {
      return;
    }
    try {
      publicar(fonte.get(), instanteMicros);
    } catch (RuntimeException falha) {
      registrarFalha(falha, instanteMicros);
    }
  }

  private void publicar(T valor, long instanteMicros) {
    if (fechado) {
      return;
    }
    try {
      if (valor == null || !validar.test(valor)) {
        throw new IllegalArgumentException("Amostra ausente ou inválida.");
      }
      publicacao.publicar(valor, instanteMicros);
      ultimaAmostra.set(instanteMicros / 1_000_000.0, instanteMicros);
      valido.set(true, instanteMicros);
      if (!amostraValida) {
        erro.set("", instanteMicros);
      }
      amostraValida = true;
    } catch (RuntimeException falha) {
      registrarFalha(falha, instanteMicros);
    }
  }

  private void registrarFalha(RuntimeException falha, long instanteMicros) {
    amostraValida = false;
    quantidadeFalhas++;
    valido.set(false, instanteMicros);
    falhas.set(quantidadeFalhas, instanteMicros);
    // Limita o diagnóstico; não escreve stack trace a cada ciclo do robô.
    String detalhe = falha.getClass().getSimpleName() + ": " + falha.getMessage();
    erro.set(detalhe.substring(0, Math.min(160, detalhe.length())), instanteMicros);
  }

  public synchronized boolean estaValido() {
    return amostraValida && !fechado;
  }

  public synchronized long obterQuantidadeFalhas() {
    return quantidadeFalhas;
  }

  synchronized void encerrar() {
    if (!fechado) {
      fechado = true;
      publicador.close();
      valido.close();
      falhas.close();
      ultimaAmostra.close();
      erro.close();
    }
  }
}
