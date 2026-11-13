---
title: "Motores Kraken com Talon FX"
tags:
  - luverlib
  - modulo
revisado: 2026-11-13
---

# Motores Kraken com Talon FX

## O que é

`MotorKraken` organiza comandos e leituras de Kraken X60 ou X44 com Talon FX. `ModeloKraken` informa a variante. `ConfiguracaoMotorKraken` reúne corrente, inversão, freio, tensão, rampas, redução e limite de saída.

## O que configurar

Revise `Constants.Kraken` e declare os IDs no grupo do mecanismo em `Constants`. O wrapper recebe o ID por parâmetro; aceita 0 a 62. A escolha do modelo deve refletir o hardware. Use o construtor com `CANBus` quando o projeto precisar informar outro barramento.

Corrente de alimentação é a corrente da bateria; corrente de estator é a corrente do motor. Os limites não são intercambiáveis. Timeout, tentativas e frequências de leitura também estão em `Constants.Kraken`.

## Como usar

```java
// Dentro do construtor do subsistema; ID e modelo vêm dos ajustes do projeto.
var motor = new MotorKraken(idCanDeConstants,
    VariaveisMotorKraken.configuracaoPadrao(modeloDeConstants));
```

O arquivo [MotoresExemplo.java](../99%20-%20Anexos/Exemplos%20Java/MotoresExemplo.java) contém a criação compilável de NEO, seguidor NEO e Kraken, com os imports reais:

```java
package guia;

import frc.robot.luverlib.motores.neo.MotorNeo;
import frc.robot.luverlib.motores.neo.VariaveisMotor;
import frc.robot.luverlib.motores.kraken.ModeloKraken;
import frc.robot.luverlib.motores.kraken.MotorKraken;
import frc.robot.luverlib.motores.kraken.VariaveisMotorKraken;

public final class MotoresExemplo {
  private MotoresExemplo() {}

  // Os IDs recebidos devem ser declarados em Constants pelo projeto.
  public static MotorNeo criarNeo(int idCanDeConstants) {
    return new MotorNeo(idCanDeConstants, VariaveisMotor.configuracaoPadrao());
  }

  public static MotorNeo criarSeguidor(int idCanDeConstants, MotorNeo lider) {
    return new MotorNeo(idCanDeConstants,
        VariaveisMotor.Tracao.configuracaoSeguidor(), lider);
  }

  public static MotorKraken criarKraken(int idCanDeConstants, ModeloKraken modelo) {
    return new MotorKraken(idCanDeConstants,
        VariaveisMotorKraken.configuracaoPadrao(modelo));
  }
}
```

| Operação | Uso |
| --- | --- |
| `definirPotencia(valor)` | Saída percentual limitada |
| `definirTensao(volts)` | Saída de tensão limitada |
| `parar()` | Aplica parada com modo neutro configurado |
| `obterPosicaoRotacoes()` e `obterVelocidadeRPM()` | Medidas da saída, após a redução |
| `obterCorrenteAlimentacaoAmperes()` | Corrente da alimentação |
| `obterCorrenteEstatorAmperes()` | Corrente do motor |
| `estaConectado()` | Disponibilidade do dispositivo |
| `close()` | Para e libera os recursos |

## O que observar

Falha de leitura gera exceção no wrapper; a telemetria publica `NaN` para medidas indisponíveis, sem apresentar zero como se fosse uma medição válida. A configuração é bloqueante e deve ocorrer na inicialização.

Esta fachada não oferece API própria de follower ou PID Kraken, e o AutoPrecision atual não tem backend Kraken. Comandos usam requests reutilizados e não exigem FOC/Pro. Veja [API - Motores Kraken](../05%20-%20Referencia/API%20-%20Motores%20Kraken.md).

---

← [Página inicial](../00%20-%20Comece%20aqui.md) · [Fontes e versão deste guia](../05%20-%20Referencia/Fontes%20e%20versao.md)
