---
title: "Primeiro uso — controle e tração"
tags:
  - luverlib
  - tutorial
revisado: 2026-11-13
---

# Primeiro uso — controle e tração

## Objetivo

Entender o comportamento que já está conectado no exemplo. Você deve terminar sabendo onde o Xbox é criado, como os gatilhos viram movimento e onde mudar a resposta da tração.

## Antes de começar

- [ ] Abra a pasta correta e conclua [Instalar e compilar](Instalar%20e%20compilar.md).
- [ ] Entenda [Constants - onde configurar](../02%20-%20Entenda%20o%20projeto/Constants%20-%20onde%20configurar.md).
- [ ] Identifique no código `Robot`, `RobotContainer`, `ChassiTank` e `DirigirTank`.

## Passo 1 — siga a criação dos objetos

Em `RobotContainer`, encontre `new ChassiTank()` e `new ControleXbox(Constants.Controle.PORTA_PILOTO)`. A porta inicial é 0, a posição do piloto na Driver Station. O código registra a telemetria do Xbox e chama `setDefaultCommand(new DirigirTank(...))`.

## Passo 2 — siga o comando

Abra `DirigirTank.execute()`. Ele confere teleop habilitado e controle conectado. Depois lê os dois gatilhos e o X esquerdo bruto, aplica o corte e multiplica pelas escalas. No `end()`, o comando para o chassi.

Com os valores desta base, gatilho direito completamente pressionado e giro zero produzem `frente = 0.4`. Apenas o X esquerdo em 1 produz `giro = 0.2`. Esses números são resultados do exemplo, não recomendações de potência para outro robô.

## Passo 3 — confira a saída

`ChassiTank.mover()` calcula as saídas esquerda/direita e chama os líderes. Os seguidores estão configurados nativamente. A inversão do lado direito está em `Constants.Tracao.INVERTER_DIREITA`.

## Passo 4 — experimente uma mudança controlada

Em um teste aprovado pela equipe, mude uma escala em `Constants.Tracao`, recompile e compare a resposta. Anote os valores anterior/novo e a condição do teste. No hardware, use o projeto com deploy e limites já configurados; o checkout deste manual não tem target roboRIO pronto.

## Resultado esperado

Você consegue explicar por que mudar o perfil do X tratado do Xbox não muda o giro do exemplo: o comando usa a leitura bruta. Também consegue apontar a escala, a zona morta e o ID correspondente em `Constants`.

**Próximo passo:** [Adicionar um mecanismo](../04%20-%20Tutoriais/Adicionar%20um%20mecanismo.md).

---

← [Página inicial](../00%20-%20Comece%20aqui.md) · [Fontes e versão deste guia](../05%20-%20Referencia/Fontes%20e%20versao.md)
