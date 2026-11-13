---
title: "Exemplos Java"
tags:
  - luverlib
  - guia
revisado: 2026-11-13
---

# Exemplos Java

## Como ler os exemplos

Os exemplos completos estão no pacote didático `guia`. Eles recebem as instâncias reais do robô como argumentos e não são iniciados automaticamente. A compilação verifica nomes, imports e assinaturas; não representa ensaio de motores, câmeras ou trajetória.

| Arquivo | Explica |
| --- | --- |
| [ControleExemplo](../99%20-%20Anexos/Exemplos%20Java/ControleExemplo.java) | Criar o Xbox e associar comando a botão |
| [MotoresExemplo](../99%20-%20Anexos/Exemplos%20Java/MotoresExemplo.java) | Criar NEO, seguidor NEO e Kraken |
| [MecanismoExemplo](../99%20-%20Anexos/Exemplos%20Java/MecanismoExemplo.java) | Subsistema, comando manual e encerramento |
| [VisaoExemplo](../99%20-%20Anexos/Exemplos%20Java/VisaoExemplo.java) | Câmera com limites, orientação e consumidor |
| [LogsExemplo](../99%20-%20Anexos/Exemplos%20Java/LogsExemplo.java) | Publicação de motor, número e pose |
| [AutonomoExemplo](../99%20-%20Anexos/Exemplos%20Java/AutonomoExemplo.java) | Eventos, seletor e agenda/cancelamento |
| [AutoPrecisionExemplo](../99%20-%20Anexos/Exemplos%20Java/AutoPrecisionExemplo.java) | Autorização local e backend de posição NEO |
| [ExemploAutoPrecisionNeo](../99%20-%20Anexos/Fontes/examples/ExemploAutoPrecisionNeo.java) | Exemplo original de incorporação de ganhos revisados |

## Como adaptar

1. Leia a nota do módulo antes de copiar a classe.
2. Coloque os IDs e parâmetros no `Constants` do seu projeto.
3. Passe as instâncias que já pertencem ao subsistema; evite dispositivos duplicados.
4. Troque o pacote `guia` pela organização que a equipe adotar, com os imports correspondentes.
5. Conecte a classe ao ciclo real do robô e confira os requisitos dos comandos.
6. Compile e registre os testes físicos quando eles forem realizados.

Trechos marcados como **fragmento** mostram a composição de um projeto, com variáveis explicadas no texto. Eles precisam estar dentro da classe/método indicado. O grupo `Constants.Garra` é uma proposta didática, não uma classe presente no checkout original.

Para abrir `.java`, use o editor de código da equipe; as notas dos módulos também mostram os exemplos em blocos de código. As fontes copiadas em `99 - Anexos/Fontes` permitem consultar a base sem depender do GitHub.

---

← [Página inicial](../00%20-%20Comece%20aqui.md) · [Fontes e versão deste guia](Fontes%20e%20versao.md)
