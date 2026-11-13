---
title: "AutoPrecision"
tags:
  - luverlib
  - modulo
revisado: 2026-11-13
---

# AutoPrecision

## O que é

AutoPrecision executa ensaios para comparar ganhos PID de **posição** de mecanismos NEO/SPARK MAX. O robô mantém o controle e os limites; o supervisor Python no PC envia solicitações, coleta amostras e compara respostas. A ferramenta não deduz limites físicos pelo modelo do motor.

## Peças do módulo

| Classe ou arquivo | Responsabilidade |
| --- | --- |
| `AutoPrecision` | Estado da sessão, autorização, supervisão e comando WPILib |
| `ControleNeoAutoPrecision` | Backend do PID de posição interno do SPARK MAX |
| `ConfiguracaoAutoPrecision` | Ganhos, tolerâncias e temporização do mecanismo |
| `LimitesAutoPrecision` | Faixa física, saída, corrente, temperatura, velocidade e excursão |
| `GanhosPID` | P, I e D |
| `PainelAutoPrecision` | Comunicação e diagnóstico internos |
| `autoprecision.py` | CLI do PC |
| `cliente.py`, `otimizador.py`, `simulador.py` | Transporte, busca limitada e bancada simulada |

## O que configurar

Declare os parâmetros do mecanismo em seu grupo de `Constants`: ganhos iniciais/máximos, posição mínima/máxima, saída, perfil, limites de medição, tolerâncias e duração. `Constants.AutoPrecision` fornece a temporização inicial, mas a instância recebe uma configuração explícita.

O backend requer motor líder já pertencente ao mecanismo, encoder primário, posição sem wrapping e slot escolhido explicitamente. Ele não zera o encoder. Definir um zero físico continua sendo responsabilidade do mecanismo.

## Como integrar ao robô

```java
package guia;

import com.revrobotics.spark.ClosedLoopSlot;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj2.command.Subsystem;
import edu.wpi.first.wpilibj2.command.button.Trigger;
import java.util.Objects;
import frc.robot.luverlib.autoprecision.AutoPrecision;
import frc.robot.luverlib.autoprecision.ConfiguracaoAutoPrecision;
import frc.robot.luverlib.autoprecision.ControleNeoAutoPrecision;
import frc.robot.luverlib.motores.neo.MotorNeo;

public final class AutoPrecisionExemplo implements AutoCloseable {
  private final AutoPrecision afinador;

  public AutoPrecisionExemplo(String nome, Subsystem mecanismoReal,
      MotorNeo motorExistente, ClosedLoopSlot slotDeConstants,
      ConfiguracaoAutoPrecision configuracaoReal, Trigger autorizacaoLocal) {
    Objects.requireNonNull(autorizacaoLocal, "A autorização local é obrigatória.");
    afinador = new AutoPrecision(nome, mecanismoReal,
        new ControleNeoAutoPrecision(motorExistente, slotDeConstants),
        configuracaoReal, autorizacaoLocal::getAsBoolean);
    try {
      autorizacaoLocal.and(DriverStation::isTestEnabled)
          .whileTrue(afinador.criarComando());
    } catch (RuntimeException | Error falha) {
      try { afinador.close(); } catch (RuntimeException | Error limpeza) { falha.addSuppressed(limpeza); }
      throw falha;
    }
  }

  public AutoPrecision.Estado estado() {
    return afinador.obterEstado();
  }

  @Override public void close() {
    afinador.close();
  }
}
```

O botão local deve continuar pressionado em Test habilitado. O comando reserva o subsistema do mecanismo. Uma nova sessão exige soltar e pressionar novamente a autorização depois do encerramento. O scheduler deve continuar rodando.

## Como usar no PC

Primeiro instale o ambiente Python descrito em [Ajustar PID com AutoPrecision](../04%20-%20Tutoriais/Ajustar%20PID%20com%20AutoPrecision.md). Os subcomandos são `simular`, `monitorar`, `ensaio` e `otimizar`. `monitorar` apenas lê; não arma nem move o mecanismo. A simulação usa outro modelo e outra base de unidades de PID.

## Como interpretar o resultado

`CONCLUIDO` indica o encerramento de um movimento. A aprovação da otimização exige melhora confirmada e estabilização em todos os alvos das confirmações do candidato. Use `ganhos_aprovados` somente quando `aceitacao.aprovado` for verdadeiro; caso inconclusivo, ele é `null`.

Durante os ensaios, mudanças do slot e de proteções são temporárias em RAM. O encerramento para a saída e restaura os parâmetros originais. A ferramenta não grava automaticamente um PID no programa. Depois de revisar um resultado aprovado, registre os ganhos e as condições em `Constants` e valide a aplicação normal. O exemplo original `ExemploAutoPrecisionNeo.java`, incluído nos anexos, mostra esse processo.

Os limites e as unidades de cada campo estão detalhados em [Configuracao AutoPrecision campo a campo](../05%20-%20Referencia/Configuracao%20AutoPrecision%20campo%20a%20campo.md) e [Unidades e sinais](../05%20-%20Referencia/Unidades%20e%20sinais.md). Esta versão não oferece ajuste de velocidade, backend Kraken ou identificação automática de feedforward.

---

← [Página inicial](../00%20-%20Comece%20aqui.md) · [Fontes e versão deste guia](../05%20-%20Referencia/Fontes%20e%20versao.md)
