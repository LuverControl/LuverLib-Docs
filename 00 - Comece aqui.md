---
title: "LuverLib — comece aqui"
tags:
  - luverlib
  - inicio
revisado: 2026-11-13
---

# LuverLib — comece aqui

> [!abstract] O que você vai aprender
> Entender o código da LuverLib, configurar os valores do seu robô e usar controles, motores, visão, telemetria, caminhos e ajuste de PID. A leitura começa pelo básico e avança conforme sua necessidade.

## Primeiro acesso

Abra esta pasta como um vault no Obsidian. Se estiver vendo os arquivos fora dele, siga [Como usar este vault](01%20-%20Primeiros%20passos/Como%20usar%20este%20vault.md). O material funciona com recursos nativos do Obsidian.

## Trilha de quem está começando

1. [Entenda para que serve a biblioteca](01%20-%20Primeiros%20passos/O%20que%20e%20a%20LuverLib.md).
2. [Aprenda os termos usados no código](02%20-%20Entenda%20o%20projeto/Conceitos%20essenciais.md).
3. [Prepare o computador e compile](01%20-%20Primeiros%20passos/Instalar%20e%20compilar.md).
4. [Aprenda onde alterar IDs, PIDs e ajustes](02%20-%20Entenda%20o%20projeto/Constants%20-%20onde%20configurar.md).
5. [Entenda a primeira pilotagem](01%20-%20Primeiros%20passos/Primeiro%20uso%20-%20controle%20e%20tracao.md).
6. [Crie seu primeiro mecanismo](04%20-%20Tutoriais/Adicionar%20um%20mecanismo.md).

## Quero usar um recurso específico

| Quero… | Comece por… |
| --- | --- |
| Ler botões, gatilhos e analógicos | [Controle Xbox](03%20-%20Modulos/Controle%20Xbox.md) |
| Acionar NEO com SPARK MAX | [Motores NEO](03%20-%20Modulos/Motores%20NEO.md) |
| Acionar Kraken com Talon FX | [Motores Kraken](03%20-%20Modulos/Motores%20Kraken.md) |
| Entender os quatro motores do exemplo | [Tracao tank](03%20-%20Modulos/Tracao%20tank.md) |
| Ler AprilTags ou detecções de uma câmera | [Limelight](03%20-%20Modulos/Limelight.md) |
| Acompanhar dados no painel | [Telemetria](03%20-%20Modulos/Telemetria.md) |
| Ver gráficos, poses e logs | [AdvantageScope](03%20-%20Modulos/AdvantageScope.md) |
| Preparar caminhos autônomos | [PathPlanner](03%20-%20Modulos/PathPlanner.md) |
| Medir respostas e ajustar PID de posição | [AutoPrecision](03%20-%20Modulos/AutoPrecision.md) |

## Mapa e consulta rápida

- [Abrir o mapa visual interativo](Mapa%20da%20LuverLib.canvas).
- [Entender como as partes se conectam](02%20-%20Entenda%20o%20projeto/Arquitetura%20e%20ciclo%20do%20robo.md).
- [Consultar cada valor configurável](02%20-%20Entenda%20o%20projeto/Referencia%20completa%20de%20Constants.md).
- [Encontrar uma classe ou método](05%20-%20Referencia/Mapa%20de%20classes.md).
- [Conferir unidades antes de usar um número](05%20-%20Referencia/Unidades%20e%20sinais.md).
- [Resolver um problema](05%20-%20Referencia/Erros%20comuns.md).
- [Consultar os exemplos compiláveis](05%20-%20Referencia/Exemplos%20Java.md).
- [Registrar o que sua equipe testou](06%20-%20Caderno%20da%20equipe/Como%20registrar%20os%20testes.md).

> [!info] Base deste manual
> O guia descreve o checkout local revisado em 13/11/2026, incluindo a centralização dos ajustes em `Constants`. O exemplo pronto executa pilotagem manual. Visão, autônomo e AutoPrecision precisam de integração com o robô da equipe. Consulte [Fontes e versao](05%20-%20Referencia/Fontes%20e%20versao.md) para saber exatamente o que foi verificado.
