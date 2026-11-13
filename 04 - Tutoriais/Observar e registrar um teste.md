---
title: "Observar e registrar um teste"
tags:
  - luverlib
  - tutorial
revisado: 2026-11-13
---

# Observar e registrar um teste

## Antes do teste

Crie uma nota usando [Registro de teste](../98%20-%20Modelos/Registro%20de%20teste.md). Anote a versão do programa, o mecanismo, os IDs envolvidos e os valores atuais de `Constants`. Defina uma pergunta mensurável: por exemplo, se uma mudança de rampa reduz o pico de corrente em uma mesma manobra.

## Durante o teste

Use [Telemetria](../03%20-%20Modulos/Telemetria.md) para conferir leituras básicas. Use [AdvantageScope](../03%20-%20Modulos/AdvantageScope.md) para séries temporais e poses. Registre sinais com unidades explícitas e nomes estáveis. Para comparar resultados, mantenha condições como carga, bateria e ponto inicial tão semelhantes quanto possível.

## Depois do teste

1. Guarde o log ou relatório com a identificação da tentativa.
2. Anote o que observou e qual foi o critério de aceitação.
3. Diga se o resultado veio de compilação, simulação ou hardware real.
4. Caso mude um ajuste, registre valor anterior, valor novo e motivo.
5. Vincule a nota do mecanismo à nota do teste usando um link interno.

## Exemplo de registro útil

“Teste da rampa da garra em bancada, com o mesmo peso e faixa de movimento. Alterado apenas o parâmetro de rampa em Constants. Anexado o log com corrente e posição. O pico caiu de X para Y, mas a estabilização demorou Z; a equipe decidiu manter/reverter por este motivo.”

Substitua X, Y e Z por medidas reais. Um build verde confirma compilação/testes de software; não confirma sozinho corrente, precisão ou força do mecanismo físico.

---

← [Página inicial](../00%20-%20Comece%20aqui.md) · [Fontes e versão deste guia](../05%20-%20Referencia/Fontes%20e%20versao.md)
