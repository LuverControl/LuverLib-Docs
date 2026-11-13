package frc.robot.luverlib.limelight;

import java.util.Objects;
import java.util.Optional;

/** Resultado rastreável: uma medição aceita ou a razão pela qual ela não pode ser usada. */
public record AvaliacaoPose(Motivo motivo, Optional<MedicaoVisao> medicao) {
  public enum Motivo {
    ACEITA, SEM_MEDICAO, CAMERA_DESCONECTADA, MEDICAO_ANTIGA, HORARIO_INVALIDO,
    PACOTE_INVALIDO, SEM_TAGS, TAGS_INCONSISTENTES, TAG_NAO_PERMITIDA, AMBIGUIDADE_ALTA,
    CAMPO_NAO_CONFIGURADO, FORA_DO_CAMPO, ALTURA_INVALIDA, DISTANCIA_EXCESSIVA, GIRO_RAPIDO,
    ORIENTACAO_AUSENTE, ORIENTACAO_ANTIGA, AGUARDANDO_PIPELINE, CAMERA_FECHADA
  }

  public AvaliacaoPose {
    Objects.requireNonNull(motivo);
    Objects.requireNonNull(medicao);
    if ((motivo == Motivo.ACEITA) != medicao.isPresent()) {
      throw new IllegalArgumentException("Somente uma avaliação aceita deve conter medição.");
    }
  }

  public static AvaliacaoPose rejeitada(Motivo motivo) {
    return new AvaliacaoPose(motivo, Optional.empty());
  }

  public static AvaliacaoPose aceita(MedicaoVisao medicao) {
    return new AvaliacaoPose(Motivo.ACEITA, Optional.of(medicao));
  }
}
