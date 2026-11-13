---
title: "Como usar este vault"
tags:
  - luverlib
  - guia
revisado: 2026-11-13
---

# Como usar este vault

## O que é um vault

É a pasta que contém as notas e os arquivos do seu manual. Os textos estão em Markdown, um formato que também pode ser aberto em um editor de texto. Os links entre notas criam a navegação. Esse funcionamento é descrito na [ajuda oficial do Obsidian](https://obsidian.md/help/vault).

## Como abrir

1. Clone ou baixe o repositório LuverControl/LuverLib-Docs. Extraia o ZIP, se necessário.
2. Abra o Obsidian e escolha **Open folder as vault / Abrir pasta como cofre**.
3. Selecione **a raiz do clone LuverLib-Docs**, que contém `00 - Comece aqui.md` e `.obsidian`.
4. Abra **00 - Comece aqui**. A validação da documentação roda no CI do repositório com `python scripts/check_docs.py`.

## Como navegar

Clique nos links para trocar de nota. Na edição, use `Ctrl` + clique se um clique comum posicionar o cursor. A busca e o alternador rápido ajudam a encontrar uma classe pelo nome. No painel de estrutura, os títulos mostram as seções da nota.

As páginas dos módulos seguem a ordem **o que é → o que configurar → como usar → o que observar**. A [referência de Constants](../02%20-%20Entenda%20o%20projeto/Referencia%20completa%20de%20Constants.md) e as notas de API são para consulta; a trilha inicial ensina o uso em ordem.

## Recursos do Obsidian presentes

| Recurso | Para que serve neste manual |
| --- | --- |
| Links internos | Ir de um conceito ao módulo que o usa |
| Backlinks | Ver quais notas se relacionam com a nota aberta |
| Canvas | Explorar o [mapa de módulos](../Mapa%20da%20LuverLib.canvas) |
| Grafo | Ver as relações entre notas |
| Callouts | Destacar uma condição importante ou um resultado esperado |
| Templates | Criar registros padronizados de mecanismo e teste |
| Checkboxes | Marcar seu progresso nos tutoriais |

Os links usam a sintaxe nativa do Obsidian, conforme a [documentação de links](https://obsidian.md/help/links). O mapa usa [Canvas](https://obsidian.md/help/plugins/canvas). Para os modelos, veja [Como registrar os testes](../06%20-%20Caderno%20da%20equipe/Como%20registrar%20os%20testes.md). Não há dependência de plugins comunitários.

## Como personalizar

Escreva observações em `06 - Caderno da equipe`. Anexe medições e imagens em `99 - Anexos`. Ao renomear notas pelo Obsidian, a atualização automática dos links está habilitada. Compartilhe a pasta inteira para manter o mapa, os anexos e os links juntos.

As fontes em `99 - Anexos/Fontes` são uma cópia de consulta. O código que deve ser editado e compilado continua no projeto Java da equipe. Alterar uma nota ou uma cópia no vault não muda o robô.

---

← [Página inicial](../00%20-%20Comece%20aqui.md) · [Fontes e versão deste guia](../05%20-%20Referencia/Fontes%20e%20versao.md)
