---
title: "Ligar botões a comandos"
tags:
  - luverlib
  - tutorial
revisado: 2026-11-13
---

# Ligar botões a comandos

## O que você está ligando

Um botão é uma condição (`Trigger`). O comando representa a tarefa e precisa declarar qual subsistema usa. A ligação normalmente é feita uma vez no `RobotContainer`.

## Escolha o comportamento

| Ligação WPILib | Comportamento |
| --- | --- |
| `onTrue(comando)` | Agenda quando a condição passa de falso para verdadeiro |
| `onFalse(comando)` | Agenda quando passa de verdadeiro para falso |
| `whileTrue(comando)` | Agenda ao ativar e cancela ao desativar |
| `toggleOnTrue(comando)` | Alterna entre agendar e cancelar a cada ativação |

Um comando que termina enquanto o botão continua pressionado não é reagendado continuamente por `whileTrue`; solte e pressione de novo para nova ativação. Isso também é importante no AutoPrecision.

## Como fazer

1. Crie o controle e o subsistema.
2. Faça o subsistema oferecer um comando, como no [tutorial do mecanismo](Adicionar%20um%20mecanismo.md).
3. Associe a condição ao comando.
4. Confira o scheduler e o encerramento do comando.

```java
// Fragmento: garra, piloto e Constants.Garra já foram criados/revisados.
piloto.botaoA().whileTrue(
    garra.criarComandoManual(Constants.Garra.POTENCIA_MANUAL));
```

Para uma autorização que depende do modo, combine a condição com `and(...)`. O [AutoPrecision](../03%20-%20Modulos/AutoPrecision.md) exige Test habilitado e autorização local mantida. Use o comando do módulo para preservar seu controle de sessão.

## Resultado esperado

Ao pressionar, a tarefa é agendada. Ao soltar, a tarefa contínua é cancelada e sua função de encerramento para o motor. Um comando incompatível que exige o mesmo subsistema interrompe a tarefa anterior conforme as regras do scheduler.

Evite uma lambda solta que mova o motor sem requisito ou sem operação de parada. Consulte a [documentação de triggers da WPILib](https://docs.wpilib.org/en/stable/docs/software/commandbased/binding-commands-to-triggers.html) para aprofundar a semântica.

---

← [Página inicial](../00%20-%20Comece%20aqui.md) · [Fontes e versão deste guia](../05%20-%20Referencia/Fontes%20e%20versao.md)
