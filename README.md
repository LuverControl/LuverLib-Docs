# LuverLib — documentação e manual de uso

Este repositório contém a documentação da [LuverLib](https://github.com/LuverControl/LuverLib): primeiros passos, manual de uso, tutoriais, referência de API, exemplos e modelos para a equipe LuverControl 10298.

**[Comece a leitura aqui](00%20-%20Comece%20aqui.md).**

Você pode navegar pelas notas no GitHub ou abrir **a raiz deste repositório** como vault no Obsidian. O mapa visual, os marcadores, os modelos e o tema usam recursos nativos; não é preciso instalar plugins comunitários.

| Quero… | Abra… |
| --- | --- |
| Aprender a usar a biblioteca | [Trilha inicial](00%20-%20Comece%20aqui.md) |
| Abrir o manual no Obsidian | [Como usar este vault](01%20-%20Primeiros%20passos/Como%20usar%20este%20vault.md) |
| Instalar e compilar a biblioteca | [Instalar e compilar](01%20-%20Primeiros%20passos/Instalar%20e%20compilar.md) |
| Conferir a edição documentada | [Fontes e versão](05%20-%20Referencia/Fontes%20e%20versao.md) |
| Explorar o mapa no Obsidian | [Mapa da LuverLib](Mapa%20da%20LuverLib.canvas) |

O código da biblioteca fica em **LuverControl/LuverLib**. Este é o repositório do manual. As cópias de fontes em `99 - Anexos/Fontes` são referências offline identificadas por hash, fora do build do robô.

A edição inicial documenta uma fotografia das fontes de 13/11/2026, incluindo a centralização local de `Constants`, ainda pendente de integração à biblioteca. Consulte a nota de versão antes de aplicar os exemplos ao clone da LuverLib em uso.

Para verificar os links, o Canvas e as fontes anexadas:

```sh
python scripts/check_docs.py
```

A mesma verificação roda no CI. Ao atualizar uma API ou um tutorial, revise as notas relacionadas e a proveniência das fontes. Não versione estado pessoal do Obsidian, credenciais ou registros privados de operadores.
