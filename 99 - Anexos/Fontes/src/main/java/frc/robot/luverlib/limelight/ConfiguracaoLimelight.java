package frc.robot.luverlib.limelight;

import java.util.Objects;
import java.util.Optional;

/** Política de qualidade por câmera. Ajustes iniciais em Constants.Limelight; geometria no projeto. */
public record ConfiguracaoLimelight(
    MetodoPose metodoPose,
    double idadeMaximaQuadroSegundos,
    double idadeMaximaOrientacaoSegundos,
    double giroMaximoGrausSegundo,
    double ambiguidadeMaximaTagUnica,
    double erroZMaximoMetros,
    double distanciaMaximaTagsMetros,
    double desvioLinearBaseMetros,
    double desvioAngularBaseRadianos,
    double fatorConfianca,
    double fatorLinearMegaTag2,
    Optional<LimitesCampo> limitesCampo) {
  public ConfiguracaoLimelight {
    Objects.requireNonNull(metodoPose, "Informe o método de localização.");
    Objects.requireNonNull(limitesCampo, "Use Optional.empty() para campo não configurado.");
    positivo(idadeMaximaQuadroSegundos, "Idade máxima do quadro");
    positivo(idadeMaximaOrientacaoSegundos, "Idade máxima da orientação");
    positivo(giroMaximoGrausSegundo, "Giro máximo");
    if (!Double.isFinite(ambiguidadeMaximaTagUnica)
        || ambiguidadeMaximaTagUnica < 0 || ambiguidadeMaximaTagUnica > 1) {
      throw new IllegalArgumentException("A ambiguidade deve estar entre 0 e 1.");
    }
    positivo(erroZMaximoMetros, "Erro Z máximo");
    positivo(distanciaMaximaTagsMetros, "Distância máxima");
    positivo(desvioLinearBaseMetros, "Desvio linear");
    positivo(desvioAngularBaseRadianos, "Desvio angular");
    positivo(fatorConfianca, "Fator de confiança");
    positivo(fatorLinearMegaTag2, "Fator MegaTag2");
  }

  public ConfiguracaoLimelight comMetodo(MetodoPose metodo) {
    return new ConfiguracaoLimelight(metodo, idadeMaximaQuadroSegundos,
        idadeMaximaOrientacaoSegundos, giroMaximoGrausSegundo, ambiguidadeMaximaTagUnica,
        erroZMaximoMetros, distanciaMaximaTagsMetros, desvioLinearBaseMetros,
        desvioAngularBaseRadianos, fatorConfianca, fatorLinearMegaTag2, limitesCampo);
  }

  public ConfiguracaoLimelight comLimitesCampo(LimitesCampo campo) {
    return new ConfiguracaoLimelight(metodoPose, idadeMaximaQuadroSegundos,
        idadeMaximaOrientacaoSegundos, giroMaximoGrausSegundo, ambiguidadeMaximaTagUnica,
        erroZMaximoMetros, distanciaMaximaTagsMetros, desvioLinearBaseMetros,
        desvioAngularBaseRadianos, fatorConfianca, fatorLinearMegaTag2, Optional.of(campo));
  }

  /** Fator maior reduz a confiança dessa câmera, aumentando o desvio informado à odometria. */
  public ConfiguracaoLimelight comFatorConfianca(double fator) {
    return new ConfiguracaoLimelight(metodoPose, idadeMaximaQuadroSegundos,
        idadeMaximaOrientacaoSegundos, giroMaximoGrausSegundo, ambiguidadeMaximaTagUnica,
        erroZMaximoMetros, distanciaMaximaTagsMetros, desvioLinearBaseMetros,
        desvioAngularBaseRadianos, fator, fatorLinearMegaTag2, limitesCampo);
  }

  private static void positivo(double valor, String nome) {
    if (!Double.isFinite(valor) || valor <= 0) {
      throw new IllegalArgumentException(nome + " deve ser finito e positivo.");
    }
  }
}
