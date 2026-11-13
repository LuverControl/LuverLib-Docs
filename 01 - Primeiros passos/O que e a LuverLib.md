---
title: "O que é a LuverLib"
tags:
  - luverlib
  - guia
revisado: 2026-11-13
---

# O que é a LuverLib

A LuverLib reúne classes Java da LuverControl 10298 para projetos FRC com WPILib. Ela organiza tarefas recorrentes: ler um Xbox, configurar motores, receber dados de visão, publicar medições e conectar recursos como PathPlanner e AutoPrecision.

## O que cada parte resolve

| Parte | Problema que resolve | Resultado que entrega |
| --- | --- | --- |
| Controle Xbox | Tratar eixos e associar botões | Leituras e eventos para comandos |
| Motores NEO e Kraken | Repetir configuração e leituras | Uma interface em português para o controlador |
| Limelight | Ler, validar e organizar dados de câmera | Alvos, detecções e medições de pose |
| Telemetria | Expor medições no painel | Campos somente de leitura |
| AdvantageScope | Registrar sinais e geometrias | Dados NT4 e arquivos `.wpilog` |
| PathPlanner | Conectar caminhos ao drivetrain | Comandos autônomos, após fornecer pose e saídas reais |
| AutoPrecision | Comparar respostas de PID | Ensaios e candidatos para PID de posição NEO/SPARK MAX |

## Biblioteca e robô de exemplo

O pacote `frc.robot.luverlib` contém os módulos. `Robot`, `RobotContainer`, `ChassiTank` e `DirigirTank` compõem o robô de demonstração. `Constants` concentra a identificação e os ajustes. A [arquitetura](../02%20-%20Entenda%20o%20projeto/Arquitetura%20e%20ciclo%20do%20robo.md) mostra quem cria e atualiza cada objeto.

No exemplo atual, o Xbox controla quatro NEO e um Pigeon2 na tração. Não há odometria pronta, rotina autônoma agendada, câmera integrada ou AutoPrecision ligado a um mecanismo. As classes existem para a equipe conectar esses recursos quando precisar.

## Quando usar

Use o projeto como base de estudo ou copie os módulos para um projeto WPILib da mesma temporada, conforme [Integrar em outro robo](Integrar%20em%20outro%20robo.md). Você ainda precisa informar as características físicas e os dispositivos do seu robô.

O JAR do build contém a aplicação de exemplo e dependências. A distribuição atual não é uma dependência Maven para adicionar isoladamente a outro projeto.

**Próximo passo:** [Instalar e compilar](Instalar%20e%20compilar.md).

---

← [Página inicial](../00%20-%20Comece%20aqui.md) · [Fontes e versão deste guia](../05%20-%20Referencia/Fontes%20e%20versao.md)
