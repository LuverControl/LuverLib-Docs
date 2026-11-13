package frc.robot.luverlib.limelight;

import static frc.robot.luverlib.limelight.AvaliacaoPose.Motivo.*;

import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.networktables.TimestampedDoubleArray;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/** Contrato numérico do LimelightOS. Pacotes externos inválidos são rejeitados, sem lançar no robô. */
final class DecodificadorLimelight {
  static boolean finitos(double[] valores) {
    for (double valor : valores) if (!Double.isFinite(valor)) return false;
    return true;
  }

  static boolean inteiro(double valor, int minimo) {
    return Double.isFinite(valor) && valor >= minimo && valor <= Integer.MAX_VALUE
        && valor == Math.rint(valor);
  }

  static boolean recente(long micros, double agora, double idadeMaxima) {
    double idade = agora - micros * 1e-6;
    return micros > 0 && Double.isFinite(agora) && idade >= -1e-6 && idade <= idadeMaxima;
  }

  static Optional<AlvoLimelight> alvo(TimestampedDoubleArray amostra, double agora,
      double idadeMaxima, double instanteMinimo) {
    double[] v = amostra.value;
    if (v.length < 17 || !finitos(v) || v[0] != 1 || !inteiro(v[1], 1)
        || v[2] < 0 || v[3] < 0 || v[8] < 0 || v[8] > 100
        || !inteiro(v[9], -1) || !inteiro(v[10], -1) || !inteiro(v[11], -1)) {
      return Optional.empty();
    }
    double captura = amostra.timestamp * 1e-6 - (v[2] + v[3]) * 1e-3;
    if (!recente(amostra.timestamp, agora, idadeMaxima) || captura < instanteMinimo
        || agora - captura > idadeMaxima || captura < 0) return Optional.empty();
    return Optional.of(new AlvoLimelight(v[4], v[5], v[8], (int) v[9],
        (int) v[10], (int) v[11], captura));
  }

  static Optional<List<DeteccaoNeural>> deteccoes(double[] v) {
    if (v.length % 12 != 0 || !finitos(v)) return Optional.empty();
    List<DeteccaoNeural> resultados = new ArrayList<>();
    for (int i = 0; i < v.length; i += 12) {
      if (!inteiro(v[i], 0) || v[i + 3] < 0 || v[i + 3] > 100) return Optional.empty();
      resultados.add(new DeteccaoNeural((int) v[i], v[i + 1], v[i + 2], v[i + 3], List.of(
          new Translation2d(v[i + 4], v[i + 5]), new Translation2d(v[i + 6], v[i + 7]),
          new Translation2d(v[i + 8], v[i + 9]), new Translation2d(v[i + 10], v[i + 11]))));
    }
    return Optional.of(List.copyOf(resultados));
  }

  static AvaliacaoPose pose(String camera, ConfiguracaoLimelight c,
      TimestampedDoubleArray amostra, double agora, OrientacaoRobo orientacao,
      double instanteOrientacao, double primeiroEnvioOrientacao, Set<Integer> tagsPermitidas) {
    double[] v = amostra.value;
    if (v.length < 11 || !finitos(v) || v[6] < 0 || !inteiro(v[7], 0)
        || v[8] < 0 || v[9] < 0 || v[10] < 0 || v[10] > 100) {
      return AvaliacaoPose.rejeitada(PACOTE_INVALIDO);
    }
    double publicacao = amostra.timestamp * 1e-6;
    double latencia = v[6] * 1e-3;
    double captura = publicacao - latencia;
    if (amostra.timestamp <= 0 || publicacao > agora + 1e-6 || captura < 0) {
      return AvaliacaoPose.rejeitada(HORARIO_INVALIDO);
    }
    if (agora - captura > c.idadeMaximaQuadroSegundos()) {
      return AvaliacaoPose.rejeitada(MEDICAO_ANTIGA);
    }
    int quantidade = (int) v[7];
    if (quantidade == 0) return AvaliacaoPose.rejeitada(SEM_TAGS);
    if (v.length != 11L + 7L * quantidade || v[9] <= 0) {
      return AvaliacaoPose.rejeitada(TAGS_INCONSISTENTES);
    }
    List<MarcadorAprilTag> tags = new ArrayList<>();
    Set<Integer> ids = new HashSet<>();
    for (int i = 11; i < v.length; i += 7) {
      if (!inteiro(v[i], 0) || v[i + 3] < 0 || v[i + 3] > 100
          || v[i + 4] <= 0 || v[i + 5] < 0 || v[i + 6] < -1 || v[i + 6] > 1
          || !ids.add((int) v[i])) return AvaliacaoPose.rejeitada(TAGS_INCONSISTENTES);
      if (!tagsPermitidas.isEmpty() && !tagsPermitidas.contains((int) v[i])) {
        return AvaliacaoPose.rejeitada(TAG_NAO_PERMITIDA);
      }
      tags.add(new MarcadorAprilTag((int) v[i], v[i + 1], v[i + 2], v[i + 3],
          v[i + 4], v[i + 5], v[i + 6]));
    }
    if (c.metodoPose() == MetodoPose.MEGATAG_1 && quantidade == 1
        && (tags.get(0).ambiguidade() < 0
            || tags.get(0).ambiguidade() > c.ambiguidadeMaximaTagUnica())) {
      return AvaliacaoPose.rejeitada(AMBIGUIDADE_ALTA);
    }
    if (c.metodoPose() == MetodoPose.MEGATAG_2) {
      if (orientacao == null) return AvaliacaoPose.rejeitada(ORIENTACAO_AUSENTE);
      if (agora - instanteOrientacao > c.idadeMaximaOrientacaoSegundos()
          || instanteOrientacao > agora) return AvaliacaoPose.rejeitada(ORIENTACAO_ANTIGA);
      if (captura < primeiroEnvioOrientacao) return AvaliacaoPose.rejeitada(ORIENTACAO_AUSENTE);
    }
    if (orientacao != null
        && Math.abs(orientacao.velocidadeYawGrausSegundo()) > c.giroMaximoGrausSegundo()) {
      return AvaliacaoPose.rejeitada(GIRO_RAPIDO);
    }
    if (Math.abs(v[2]) > c.erroZMaximoMetros()) return AvaliacaoPose.rejeitada(ALTURA_INVALIDA);
    if (v[9] > c.distanciaMaximaTagsMetros()) return AvaliacaoPose.rejeitada(DISTANCIA_EXCESSIVA);
    if (c.limitesCampo().isEmpty()) return AvaliacaoPose.rejeitada(CAMPO_NAO_CONFIGURADO);
    if (!c.limitesCampo().get().contem(v[0], v[1])) {
      return AvaliacaoPose.rejeitada(FORA_DO_CAMPO);
    }
    double fator = v[9] * v[9] / quantidade * c.fatorConfianca();
    double fatorLinear = fator * (c.metodoPose() == MetodoPose.MEGATAG_2 ? c.fatorLinearMegaTag2() : 1);
    double linear = Math.max(c.desvioLinearBaseMetros(), c.desvioLinearBaseMetros() * fatorLinear);
    double angular = c.metodoPose() == MetodoPose.MEGATAG_2 ? Double.POSITIVE_INFINITY
        : Math.max(c.desvioAngularBaseRadianos(), c.desvioAngularBaseRadianos() * fator);
    if (!Double.isFinite(linear) || (c.metodoPose() == MetodoPose.MEGATAG_1
        && !Double.isFinite(angular))) return AvaliacaoPose.rejeitada(PACOTE_INVALIDO);
    Pose3d pose = new Pose3d(v[0], v[1], v[2], new Rotation3d(
        Math.toRadians(v[3]), Math.toRadians(v[4]), Math.toRadians(v[5])));
    return AvaliacaoPose.aceita(new MedicaoVisao(camera, c.metodoPose(), pose, captura,
        latencia, v[9], tags, linear, angular));
  }

  private DecodificadorLimelight() {}
}
