package frc.robot.luverlib.advantagescope;

import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.util.datalog.DataLog;
import edu.wpi.first.wpilibj.DataLogManager;
import edu.wpi.first.wpilibj.DriverStation;
import java.util.Objects;

/**
 * Logs .wpilog para abrir no AdvantageScope. Gravação de dados não é replay determinístico de código;
 * este último exige arquitetura de entradas/saídas, por exemplo com AdvantageKit.
 */
public final class GravacaoAdvantageScope implements AutoCloseable {
  private final int registro;
  private final int conexoes;
  private boolean fechado;

  private GravacaoAdvantageScope(NetworkTableInstance rede, DataLog log) {
    // Captura também /.schema, SmartDashboard, FMSInfo e câmeras: o arquivo é autocontido.
    registro = rede.startEntryDataLog(log, "", "NT:");
    conexoes = rede.startConnectionDataLog(log, "NTConnection");
  }

  /**
   * Inicie uma vez no começo do robô, antes dos registros. WPILib gerencia nome, USB e espaço livre.
   * Captura NT, estado DS e joysticks; chamadas repetidas são idempotentes pela WPILib.
   * Não use junto de outra inicialização global de logs que já assuma essa responsabilidade.
   */
  public static DataLog iniciar() {
    return iniciar("", VariaveisAdvantageScope.GRAVAR_JOYSTICKS);
  }

  /** Diretório vazio usa a escolha da WPILib. Se já iniciado, WPILib mantém o diretório original. */
  public static DataLog iniciar(String diretorio, boolean gravarJoysticks) {
    Objects.requireNonNull(diretorio);
    DataLogManager.start(diretorio);
    DataLogManager.logNetworkTables(true);
    DataLog log = DataLogManager.getLog();
    DriverStation.startDataLog(log, gravarJoysticks);
    return log;
  }

  /**
   * Alternativa para um DataLog já pertencente ao projeto, testes ou outra instância NT. Não inicie
   * duas capturas da mesma rede no mesmo log. Encerre esta captura antes do DataLog e da rede.
   */
  public static GravacaoAdvantageScope anexar(NetworkTableInstance rede, DataLog log) {
    return new GravacaoAdvantageScope(Objects.requireNonNull(rede), Objects.requireNonNull(log));
  }

  /** Desliga só esta captura; o arquivo, a rede e outras gravações continuam sob seus donos. */
  @Override
  public void close() {
    if (!fechado) {
      fechado = true;
      NetworkTableInstance.stopEntryDataLog(registro);
      NetworkTableInstance.stopConnectionDataLog(conexoes);
    }
  }
}
