---
title: "Limelight"
tags:
  - luverlib
  - modulo
revisado: 2026-11-13
---

# Limelight

## O que é

`Limelight` lê NetworkTables da câmera, organiza alvos e detecções neurais e valida medições de pose. `VisaoLimelight` coordena uma ou mais câmeras no scheduler. `MedicaoVisao` reúne pose, instante de captura e incerteza para o estimador do robô.

## O que configurar

Os critérios de qualidade estão em `Constants.Limelight`: idade máxima, giro, ambiguidade, altura, distância e desvios. Os nomes da câmera, pipeline, posição física e dimensões de campo precisam corresponder ao projeto; declare esses ajustes no grupo de visão de `Constants` quando integrar sua câmera.

> [!important] Para aceitar localização
> O padrão não tem limites do campo e rejeita poses até receber `comLimitesCampo(...)`. MegaTag2 também precisa de orientação recente. A câmera conectada, sozinha, não comprova que a pose foi aceita.

## Como usar

1. Configure a câmera e um pipeline compatível com o que quer ler.
2. Forneça o nome correto da tabela NT e a montagem física real.
3. Crie `LimitesCampo` com as dimensões e a margem verificadas.
4. Forneça orientação real, com yaw coerente com a origem azul, e um consumidor que insira a medição no estimador.
5. Crie o coordenador uma vez e mantenha o scheduler funcionando.

```java
package guia;

import java.util.function.Consumer;
import java.util.function.Supplier;
import frc.robot.luverlib.limelight.AvaliacaoPose;
import frc.robot.luverlib.limelight.Limelight;
import frc.robot.luverlib.limelight.LimitesCampo;
import frc.robot.luverlib.limelight.MedicaoVisao;
import frc.robot.luverlib.limelight.OrientacaoRobo;
import frc.robot.luverlib.limelight.VariaveisLimelight;
import frc.robot.luverlib.limelight.VisaoLimelight;

public final class VisaoExemplo implements AutoCloseable {
  private final Limelight camera;
  private final VisaoLimelight visao;

  public VisaoExemplo(String nomeDaCamera, LimitesCampo campo,
      Supplier<OrientacaoRobo> orientacaoReal,
      Consumer<MedicaoVisao> receberNoEstimador) {
    var ajustes = VariaveisLimelight.configuracaoPadrao().comLimitesCampo(campo);
    camera = new Limelight(nomeDaCamera, ajustes);
    try {
      visao = new VisaoLimelight(orientacaoReal, receberNoEstimador, camera);
    } catch (RuntimeException | Error falha) {
      try { camera.close(); } catch (RuntimeException | Error limpeza) { falha.addSuppressed(limpeza); }
      throw falha;
    }
  }

  public AvaliacaoPose diagnostico() {
    return camera.obterUltimaAvaliacaoPose();
  }

  @Override public void close() {
    try { visao.close(); } finally { camera.close(); }
  }
}
```

O consumidor recebe `MedicaoVisao`. Um estimador compatível com WPILib pode usar `pose2d()`, `instanteCapturaSegundos()` e `desviosPadrao()` dessa medição. Não use o horário de recebimento como se fosse o horário da captura.

## Outros usos e diagnóstico

`obterAlvo()` retorna `Optional<AlvoLimelight>`; ausência de alvo é um resultado normal. `obterDeteccoesNeurais()` retorna a lista de detecções. `selecionarPipeline(indice)` troca o pipeline e pode colocar a pose em espera até a confirmação da troca.

| Tipo de dado | Como interpretar |
| --- | --- |
| `AlvoLimelight` | `txGraus` e `tyGraus` são ângulos; a área é percentual; inclui IDs/classes e instante de captura |
| `MarcadorAprilTag` | Dados da tag no mesmo pacote de pose: ID, ângulos, distâncias e ambiguidade |
| `DeteccaoNeural` | Classe, ângulos, área e quatro cantos em pixels; `rawdetections` não fornece confiança |
| `MedicaoVisao` | Pose do robô, instante, latência, tags e desvios para o estimador |
| `AvaliacaoPose` | Medição aceita ou razão de rejeição, mantendo as duas situações coerentes |

`AlvoLimelight.estimarDistanciaHorizontalMetros(...)` recebe alturas reais da câmera/alvo e inclinação para cima; pode retornar vazio se não houver solução geométrica válida. O limiar de confiança do detector neural é configurado no pipeline da câmera.

Para leitura automática sem localização, `new VisaoLimelight(camera)` coleta dados; porém não fornece o gyro exigido por MegaTag2 nem envia medições a um estimador. Para coleta manual, chame `camera.atualizar()` uma vez por ciclo. Evite duplicar coleta manual e automática para a mesma câmera.

Confira `obterUltimaAvaliacaoPose().motivo()`, aceitas/rejeitadas e a telemetria. Os motivos mais comuns estão em [Erros comuns](../05%20-%20Referencia/Erros%20comuns.md). `PosicaoCamera` usa frente/direita/cima em metros e roll/pitch/yaw em graus; não confunda seus eixos com outros referenciais. Veja [API - Limelight](../05%20-%20Referencia/API%20-%20Limelight.md).

---

← [Página inicial](../00%20-%20Comece%20aqui.md) · [Fontes e versão deste guia](../05%20-%20Referencia/Fontes%20e%20versao.md)
