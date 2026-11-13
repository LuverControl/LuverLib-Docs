---
title: "Preparar autônomo"
tags:
  - luverlib
  - tutorial
revisado: 2026-11-13
---

# Preparar autônomo

## Objetivo

Conectar um drivetrain com sensores reais ao PathPlanner. O `ChassiTank` de demonstração ainda não entrega todas as funções necessárias.

## Passo 1 — prepare a medição

O drivetrain deve oferecer pose no campo, reset de pose, velocidades relativas ao robô e aplicação de saída. No tank, obtenha distâncias/RPM das rodas com sinais coerentes e combine os sensores em odometria/estimador. Uma pose fixa de exemplo não substitui essa medição.

Se usar `TankPathPlanner`, preencha `ConfiguracaoTankPathPlanner` com os ajustes medidos do grupo do mecanismo em `Constants`:

| Campo | Significado |
| --- | --- |
| `larguraEntreRodasMetros` | Distância entre os lados do drivetrain |
| `metrosPorRotacaoEncoder` | Distância percorrida para uma rotação da leitura já disponível |
| `velocidadeMaximaRodaMetrosPorSegundo` | Limite de velocidade das rodas |
| `ksVolts` | Compensação de atrito do feedforward |
| `kvVoltsPorMetroPorSegundo` | Ganho de velocidade do feedforward |
| `tensaoMaximaVolts` | Tensão máxima solicitada |
| `inverterEncoderEsquerdo/Direito` | Correção do sinal medido para frente positiva |

Para RPM bruta do motor, o fator é `diâmetroDaRoda × PI / redução`. Se o encoder já representa rotações da saída, use `diâmetroDaRoda × PI`; dividir novamente pela redução produz escala errada. A inversão elétrica do motor e a inversão do encoder têm finalidades distintas.

## Passo 2 — prepare os arquivos

No aplicativo, configure as características reais do robô e exporte para `src/main/deploy/pathplanner/`. A estrutura esperada é `settings.json`, `autos/*.auto` e `paths/*.path`. Use os nomes sem pasta/extensão nas chamadas da fachada. O formato aceito por esta base é `2025.X`.

## Passo 3 — monte a conexão

`TankPathPlanner.criarConexao(...)` recebe subsistema, leitura/reset da pose, duas fontes de RPM, aplicação de tensões esquerda/direita e parada. A saída é limitada/dessaturada pelo adaptador. Ele não oferece PID próprio de velocidade das rodas nem kA.

Para swerve, preencha `ConexaoPathPlanner` diretamente com suas funções de módulos. Preserve o referencial de velocidades do robô e a ordem FL/FR/BL/BR dos feedforwards, com os sinais ajustados pela lógica dos módulos.

## Passo 4 — registre e selecione

Use o [exemplo de integração da fachada](../03%20-%20Modulos/PathPlanner.md). Registre todos os eventos antes de carregar os autos. Confira `seletor.obterFalhasCarregamento()` na preparação e escolha uma rotina válida no painel.

## Passo 5 — ligue os modos

No `Robot` da equipe, chame o método que agenda a seleção em `autonomousInit()`. Em `teleopInit()`, cancele a instância agendada. Mantenha o scheduler em `robotPeriodic()`. O seletor, sozinho, não agenda movimento.

## Resultado esperado

Você consegue verificar pose e sinais com [AdvantageScope](../03%20-%20Modulos/AdvantageScope.md), carregar o auto sem falhas e confirmar a parada no encerramento/cancelamento. Faça a validação física com espaço e limites adequados à rotina; testes de compilação não comprovam a calibração da trajetória.

---

← [Página inicial](../00%20-%20Comece%20aqui.md) · [Fontes e versão deste guia](../05%20-%20Referencia/Fontes%20e%20versao.md)
