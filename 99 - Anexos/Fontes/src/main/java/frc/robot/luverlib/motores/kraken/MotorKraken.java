package frc.robot.luverlib.motores.kraken;

import com.ctre.phoenix6.BaseStatusSignal;
import com.ctre.phoenix6.CANBus;
import com.ctre.phoenix6.StatusCode;
import com.ctre.phoenix6.StatusSignal;
import com.ctre.phoenix6.controls.DutyCycleOut;
import com.ctre.phoenix6.controls.NeutralOut;
import com.ctre.phoenix6.controls.VoltageOut;
import com.ctre.phoenix6.hardware.TalonFX;
import edu.wpi.first.wpilibj.RobotBase;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Kraken X60/X44 com Talon FX integrado via Phoenix 6. Crie uma vez no subsistema, fora de periodic.
 * Padrão: CAN nativo do roboRIO. ID e modelo pertencem ao projeto; não altera IDs nem firmware.
 * Instância destinada à thread principal do robô, como os subsistemas WPILib.
 */
public final class MotorKraken implements AutoCloseable {
  private final TalonFX controlador;
  private final ConfiguracaoMotorKraken configuracao;
  // Reutilizados: nenhum request novo é alocado no laço de controle. FOC/Pro não é exigido.
  private final DutyCycleOut potencia = new DutyCycleOut(0).withEnableFOC(false);
  private final VoltageOut tensao = new VoltageOut(0).withEnableFOC(false);
  private final NeutralOut neutro = new NeutralOut();
  private final StatusSignal<?> posicao;
  private final StatusSignal<?> velocidade;
  private final StatusSignal<?> correnteAlimentacao;
  private final StatusSignal<?> correnteEstator;
  private final StatusSignal<?> temperatura;
  private final StatusSignal<?> tensaoAplicada;
  private final StatusSignal<?> tensaoAlimentacao;
  private boolean fechado;

  public MotorKraken(int idCan) {
    this(idCan, VariaveisMotorKraken.configuracaoPadrao());
  }

  public MotorKraken(int idCan, ModeloKraken modelo) {
    this(idCan, VariaveisMotorKraken.configuracaoPadrao(modelo));
  }

  public MotorKraken(int idCan, ConfiguracaoMotorKraken configuracao) {
    this(idCan, CANBus.roboRIO(), configuracao);
  }

  /** Barramento tipado opcional, escolhido pelo projeto. Não utiliza o construtor String obsoleto. */
  public MotorKraken(int idCan, CANBus barramento, ConfiguracaoMotorKraken configuracao) {
    if (idCan < 0 || idCan > 62) {
      throw new IllegalArgumentException("Use um ID CAN do Kraken entre 0 e 62.");
    }
    this.configuracao = Objects.requireNonNull(configuracao);
    Objects.requireNonNull(barramento);
    var novo = new TalonFX(idCan, barramento);
    try {
      novo.stopMotor();
      if (RobotBase.isSimulation()) {
        exigirSucesso(novo.getSimState().setMotorType(configuracao.modelo().tipoSimulacao()),
            "selecionar o modelo simulado");
      }
      var ajustes = configuracao.criarConfiguracaoPhoenix();
      aplicarComTentativas(() -> novo.getConfigurator().apply(
          ajustes, VariaveisMotorKraken.TIMEOUT_CONFIGURACAO_SEGUNDOS));
      posicao = novo.getPosition(false);
      velocidade = novo.getVelocity(false);
      correnteAlimentacao = novo.getSupplyCurrent(false);
      correnteEstator = novo.getStatorCurrent(false);
      temperatura = novo.getDeviceTemp(false);
      tensaoAplicada = novo.getMotorVoltage(false);
      tensaoAlimentacao = novo.getSupplyVoltage(false);
      exigirSucesso(BaseStatusSignal.setUpdateFrequencyForAll(
          VariaveisMotorKraken.FREQUENCIA_ENCODER_HZ, posicao, velocidade), "configurar encoder");
      exigirSucesso(BaseStatusSignal.setUpdateFrequencyForAll(
          VariaveisMotorKraken.FREQUENCIA_TELEMETRIA_HZ, correnteAlimentacao, correnteEstator,
          temperatura, tensaoAplicada, tensaoAlimentacao), "configurar telemetria");
      exigirSucesso(novo.setControl(neutro), "inicializar em neutro");
      controlador = novo;
    } catch (RuntimeException | Error falha) {
      try {
        novo.stopMotor();
      } finally {
        novo.close();
      }
      throw falha;
    }
  }

  /** Fração da tensão de alimentação, de -1 a 1, saturada no limite configurado. */
  public void definirPotencia(double valor) {
    validarComando(valor);
    verificarControle(controlador.setControl(potencia.withOutput(
        limitar(valor, configuracao.limiteSaida()))));
  }

  /** Tensão absoluta solicitada, limitada à tensão máxima multiplicada pelo limite de saída. */
  public void definirTensao(double volts) {
    validarComando(volts);
    verificarControle(controlador.setControl(tensao.withOutput(
        limitar(volts, configuracao.tensaoMaximaVolts() * configuracao.limiteSaida()))));
  }

  /** Usa o modo neutro configurado; freio elétrico não sustenta posição como um PID. */
  public void parar() {
    garantirAberto();
    verificarControle(controlador.setControl(neutro));
  }

  public double obterPosicaoRotacoes() { return ler(posicao); }

  public double obterVelocidadeRPM() { return ler(velocidade) * 60; }

  public double obterCorrenteAlimentacaoAmperes() { return ler(correnteAlimentacao); }

  public double obterCorrenteEstatorAmperes() { return ler(correnteEstator); }

  public double obterTemperaturaCelsius() { return ler(temperatura); }

  public double obterTensaoAplicadaVolts() { return ler(tensaoAplicada); }

  public double obterTensaoAlimentacaoVolts() { return ler(tensaoAlimentacao); }

  /** Só muda a referência; posição já está em rotações do mecanismo, sem dividir duas vezes. */
  public void definirPosicaoEncoder(double rotacoes) {
    validarComando(rotacoes);
    verificarControle(controlador.setPosition(
        rotacoes, VariaveisMotorKraken.TIMEOUT_CONFIGURACAO_SEGUNDOS));
  }

  public void zerarEncoder() { definirPosicaoEncoder(0); }

  public boolean estaConectado() {
    return !fechado && controlador.isConnected();
  }

  public ConfiguracaoMotorKraken obterConfiguracao() { return configuracao; }

  /** Acesso avançado/simulação; alterar configuração ou enviar requests diretos pode burlar limites. */
  public TalonFX obterControlador() {
    garantirAberto();
    return controlador;
  }

  @Override
  public void close() {
    if (!fechado) {
      fechado = true;
      try {
        controlador.stopMotor();
      } finally {
        controlador.close();
      }
    }
  }

  private double ler(StatusSignal<?> sinal) {
    garantirAberto();
    // Atualiza o cache sem aguardar um frame novo nem imprimir erros repetidos a cada ciclo.
    exigirSucesso(sinal.refresh(false).getStatus(), "ler " + sinal.getName());
    double valor = sinal.getValueAsDouble();
    if (!Double.isFinite(valor)) {
      throw new IllegalStateException("O Kraken retornou uma leitura não finita.");
    }
    return valor;
  }

  private void validarComando(double valor) {
    garantirAberto();
    if (!Double.isFinite(valor)) {
      controlador.stopMotor();
      throw new IllegalArgumentException("O comando deve ser finito. O motor foi parado.");
    }
  }

  private void verificarControle(StatusCode resultado) {
    if (!resultado.isOK()) {
      controlador.stopMotor();
      exigirSucesso(resultado, "comandar o Kraken " + controlador.getDeviceID());
    }
  }

  private void garantirAberto() {
    if (fechado) {
      throw new IllegalStateException("Este MotorKraken já foi fechado.");
    }
  }

  static void aplicarComTentativas(Supplier<StatusCode> operacao) {
    StatusCode resultado = StatusCode.GeneralError;
    for (int tentativa = 0; tentativa < VariaveisMotorKraken.TENTATIVAS_CONFIGURACAO; tentativa++) {
      resultado = operacao.get();
      if (resultado.isOK()) {
        return;
      }
    }
    exigirSucesso(resultado, "configurar o Kraken após as tentativas permitidas");
  }

  private static void exigirSucesso(StatusCode resultado, String operacao) {
    if (!resultado.isOK()) {
      throw new IllegalStateException("Falha ao " + operacao + ": " + resultado);
    }
  }

  private static double limitar(double valor, double limite) {
    return Math.max(-limite, Math.min(valor, limite));
  }
}
