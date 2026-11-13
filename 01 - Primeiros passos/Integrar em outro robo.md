---
title: "Integrar em outro robô"
tags:
  - luverlib
  - tutorial
revisado: 2026-11-13
---

# Integrar em outro robô

## Objetivo

Levar os módulos para o projeto WPILib do robô da equipe, preservando os dispositivos e a arquitetura que esse projeto já possui.

## Passos

1. Use um projeto Java WPILib 2026 com Java 17 e target roboRIO configurado, se for executar no hardware.
2. Copie `src/main/java/frc/robot/luverlib` completo para evitar faltar dependências entre os módulos.
3. Mescle os grupos de `Constants.java` com o `Constants` do seu robô. Preserve os IDs já corretos do robô e revise os ajustes recebidos.
4. Mescle os vendordeps compatíveis. Compare as versões no arquivo JSON, evitando duas versões da mesma biblioteca. [Fontes e versao](../05%20-%20Referencia/Fontes%20e%20versao.md) registra as usadas neste manual.
5. Mantenha `Robot` e `RobotContainer` do projeto de destino. Copie/adapte a composição de exemplo apenas se ela fizer sentido para a mecânica real.
6. Crie os dispositivos uma vez no subsistema correspondente e passe as mesmas instâncias aos módulos auxiliares.
7. Mantenha o scheduler no ciclo e associe os comandos aos requisitos corretos.
8. Compile, teste e use o fluxo local de deploy do projeto de destino.

## Exemplo de decisão

Se a equipe já tem uma garra com `MotorNeo`, a telemetria e o AutoPrecision recebem aquele motor. Não crie outro `MotorNeo` no mesmo ID para fazer a integração. Se o drivetrain já configurou `AutoBuilder`, não construa uma segunda `PathPlannerLuver`.

## O que conferir na migração

| Item | Pergunta prática |
| --- | --- |
| IDs | Correspondem aos dispositivos físicos e ao barramento? |
| Unidades | O encoder está em rotações de motor ou de saída? |
| Ciclo | O módulo atualiza sozinho no scheduler ou precisa de chamada manual? |
| Propriedade | Quem criou o dispositivo e quem deve fechá-lo? |
| Arquivos | O projeto inclui os assets reais do PathPlanner, quando usados? |
| Constants | Os ajustes continuam centralizados e fáceis de encontrar? |

Os anexos contêm uma cópia da base consultada, não um projeto adicional com deploy. O JAR de exemplo também não substitui essa integração de código-fonte.

---

← [Página inicial](../00%20-%20Comece%20aqui.md) · [Fontes e versão deste guia](../05%20-%20Referencia/Fontes%20e%20versao.md)
