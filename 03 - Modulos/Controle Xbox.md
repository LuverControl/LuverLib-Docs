---
title: "Controle Xbox"
tags:
  - luverlib
  - modulo
revisado: 2026-11-13
---

# Controle Xbox

## O que é

`ControleXbox` transforma leituras do Xbox em valores e condições para comandos WPILib. `PerfilEixo` trata cada analógico nesta ordem: zona morta, curva, limite e inversão. `VariaveisControle` repassa os ajustes de `Constants`.

## O que configurar

Em `Constants.Controle`, revise as portas USB da Driver Station, os quatro `PerfilEixo` e `LIMIAR_GATILHOS`. Em `Constants.Tracao`, ficam as escalas e o corte usados especificamente pela pilotagem do exemplo. Veja [Referencia completa de Constants](../02%20-%20Entenda%20o%20projeto/Referencia%20completa%20de%20Constants.md).

`new PerfilEixo(0.08, 1.0, 1.0, false)` significa: ignorar os 8% centrais, manter curva linear, permitir saída até 100% e preservar o sinal. Depois da zona morta, a leitura é reescalonada para aproveitar o curso restante. Expoente maior suaviza a resposta perto do centro.

## Como usar

Crie um controle uma vez no `RobotContainer`. Associe botões a comandos que declarem o requisito do mecanismo. O exemplo abaixo recebe um comando já criado:

```java
package guia;

import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.Constants;
import frc.robot.luverlib.controles.ControleXbox;

public final class ControleExemplo {
  private final ControleXbox piloto =
      new ControleXbox(Constants.Controle.PORTA_PILOTO);

  public void associarBotaoA(Command comandoDoMecanismo) {
    piloto.botaoA().whileTrue(comandoDoMecanismo);
  }

  public double lerGatilhoDireito() {
    return piloto.valorGatilhoDireito();
  }

  public void pararVibracao() {
    piloto.pararVibracao();
  }
}
```

## Leituras e botões

| Método | O que entrega |
| --- | --- |
| `analogicoEsquerdoX/Y()` e `analogicoDireitoX/Y()` | Analógicos tratados pelos respectivos perfis |
| `valorGatilhoEsquerdo/Direito()` | Valor contínuo do gatilho, de 0 a 1 |
| `botaoA/B/X/Y()`, `botaoLB/RB()` | Condições dos botões |
| `gatilhoEsquerdo/Direito()` | Condição ativada acima do limiar configurado |
| `direcionalCima/Direita/Baixo/Esquerda()` | Condições do POV |
| `estaConectado()` | Presença do controle |
| `definirVibracao(esquerda, direita)` | Intensidades entre 0 e 1 |
| `obterDispositivo()` | Acesso ao Xbox WPILib e às leituras brutas |

Na WPILib, o Y bruto normalmente fica negativo ao empurrar para frente. A pilotagem atual usa gatilhos e X bruto; trocar `PerfilEixo` não altera o giro do `DirigirTank`. O scheduler precisa continuar sendo executado para os triggers agendarem os comandos.

**Depois:** [Ligar botoes a comandos](../04%20-%20Tutoriais/Ligar%20botoes%20a%20comandos.md) · [API - Controles](../05%20-%20Referencia/API%20-%20Controles.md).

---

← [Página inicial](../00%20-%20Comece%20aqui.md) · [Fontes e versão deste guia](../05%20-%20Referencia/Fontes%20e%20versao.md)
