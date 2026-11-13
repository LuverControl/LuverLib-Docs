---
title: "Tração tank"
tags:
  - luverlib
  - modulo
revisado: 2026-11-13
---

# Tração tank

## O que é

`ChassiTank` é o subsistema do robô de exemplo. Possui quatro NEO e um Pigeon2. Os NEO dianteiros são líderes; os traseiros seguem seus respectivos líderes. `DirigirTank` é o comando de pilotagem manual.

## Quais dispositivos o exemplo usa

| Dispositivo | ID em `Constants.Tracao` |
| --- | --- |
| Líder esquerdo | `LIDER_ESQUERDO = 4` |
| Seguidor esquerdo | `SEGUIDOR_ESQUERDO = 2` |
| Líder direito | `LIDER_DIREITO = 1` |
| Seguidor direito | `SEGUIDOR_DIREITO = 3` |
| Pigeon2 | `PIGEON = 20` |

Esses IDs descrevem a referência DualFire. Confira a fiação e os endereços do robô da equipe antes de adotá-los. Esquerda e direita devem ser definidas olhando para a frente do robô.

## Como a pilotagem funciona

O comando calcula `frente = (gatilhoDireito - gatilhoEsquerdo) × ESCALA_FRENTE` e `giro = X_esquerdo_bruto × ESCALA_GIRO`. Antes disso, aplica um corte simples em cada entrada: até `ZONA_MORTA`, o valor vira zero. Esse corte não reescala o restante do curso.

O chassi mistura `esquerda = frente + giro` e `direita = frente - giro`. Os wrappers limitam cada saída. Como os lados podem saturar separadamente, a mistura não mantém necessariamente a mesma proporção em comandos muito altos.

## Como usar

1. Confira [os ajustes da tração](../02%20-%20Entenda%20o%20projeto/Constants%20-%20onde%20configurar.md).
2. Deixe `RobotContainer` criar o chassi e seu comando padrão uma única vez.
3. Mantenha o scheduler no ciclo do robô.
4. Em um projeto preparado para hardware, confira conexão do Xbox e estado da Driver Station antes do primeiro teste controlado.
5. Observe gatilhos, giro e sentido de cada lado. Registre qualquer mudança em [Como registrar os testes](../06%20-%20Caderno%20da%20equipe/Como%20registrar%20os%20testes.md).

`mover(frenteTras, giro)` é a operação de saída; `parar()` zera os líderes; `obterRotacao()` retorna o yaw do Pigeon como `Rotation2d`. Entrada não finita manda parar e gera exceção. `close()` encerra os dispositivos quando o subsistema deixa de ser usado.

## Antes de usar caminhos

O exemplo não entrega pose/odometria, leituras públicas de distância/RPM ou callbacks completos para PathPlanner. `TankPathPlanner` é um adaptador separado; a equipe precisa fornecer essas medidas e aplicações de tensão reais. Leia [Preparar autonomo](../04%20-%20Tutoriais/Preparar%20autonomo.md).

---

← [Página inicial](../00%20-%20Comece%20aqui.md) · [Fontes e versão deste guia](../05%20-%20Referencia/Fontes%20e%20versao.md)
