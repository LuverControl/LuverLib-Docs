---
title: "Fontes e versão"
tags:
  - luverlib
  - referencia
revisado: 2026-11-13
---

# Fontes e versão

## Qual base este manual descreve

- Data da consulta: **13/11/2026**.
- Repositório identificado: [LuverControl/LuverLib](https://github.com/LuverControl/LuverLib).
- Base consultada: checkout local da equipe, com fontes de referência preservadas nos anexos.
- Branch no início da entrega: `chore/bootstrap-frc-cicd`.
- HEAD no início da entrega: `e504a04c5f51b0968e11878cb87591ceddf9bd0c`.
- Versão declarada no Gradle: `1.1.0`.

O checkout contém mudanças locais de `Constants` e das referências relacionadas. Elas ainda não estão incluídas no HEAD acima. Por isso, os links do GitHub no commit não substituem a cópia consultada: as fontes e os respectivos hashes foram guardados nos anexos e em `fontes-manifesto.json`, na raiz deste vault.

## Situação da edição publicada

Este repositório separado é a documentação e o manual de uso da LuverLib. A edição inicial preserva a base de fontes revisada acima. O código de produção compilado fica na raiz do repositório [LuverControl/LuverLib](https://github.com/LuverControl/LuverLib); as cópias em `99 - Anexos/Fontes` são referências offline e não fazem parte do build.

Na publicação, a `main` estava em [4ff094c](https://github.com/LuverControl/LuverLib/commit/4ff094c). Ela já usa Gradle Wrapper 8.14.7, pyntcore 2026.2.2, JUnit 5.14.4 e Jackson 2.23.0. Confira `build.gradle`, `gradle/wrapper/gradle-wrapper.properties` e `ferramentas/autoprecision/requirements.txt` no repositório da biblioteca para instalar a revisão em uso.

A centralização dos 77 ajustes descrita neste manual ainda está no trabalho local da equipe. Na `main` de referência, `Constants` contém os IDs da tração e os outros ajustes permanecem nas classes `Variaveis...`. Os exemplos que usam `Constants.Neo`, `Constants.Controle` e os demais grupos correspondem às fontes anexadas. Consulte essa diferença antes de aplicar um exemplo ao clone atual.

O Luver Atlas Synapse 0.1v também está em desenvolvimento local e não é declarado como funcionalidade publicada por esta edição do manual.

## Versões lidas no código

| Item | Versão declarada |
| --- | --- |
| Java | 17 |
| GradleRIO | 2026.2.1 |
| Python usado pelo supervisor no PC | 3.13 |
| pyntcore | 2026.2.1 |
| `AdvantageKit` | `26.0.2` |
| `PathplannerLib` | `2026.1.2` |
| `Phoenix6-26.3.0` | `26.3.0` |
| `REVLib` | `2026.0.5` |
| `WPILibNewCommands` | `1.0.0` |

## O que foi e não foi verificado

A tarefa anterior de centralização registrou build e 440 testes Java aprovados, sem falhas/erros/pulados. São evidências de software em simulação, realizadas naquela tarefa. Nesta entrega documental, a revisão cobre a estrutura do vault, referências internas, correspondência dos campos/classes com as fontes e compilação dos exemplos Java. As verificações originais ficaram na entrega local. Na publicação, `python scripts/check_docs.py` confere links, Canvas, configurações e integridade das fontes anexadas; esse comando também roda no CI.

Não houve teste novo de motores, sensores, câmera ou roboRIO físico para produzir este manual. O checkout não contém target de deploy roboRIO. Os valores genéricos são de exemplo e a equipe deve verificar sua própria configuração mecânica.

## Fontes locais

A pasta `99 - Anexos/Fontes` contém os arquivos Java de produção, o exemplo original, a CLI Python, vendordeps, build e licença consultados. O código continua pertencendo ao checkout do projeto; essas cópias são para referência offline. A licença BSD 3-Clause está incluída como `99 - Anexos/Fontes/LICENSE`.

## Referências oficiais complementares

- [Abrir uma pasta como vault no Obsidian](https://obsidian.md/help/vault).
- [Links internos do Obsidian](https://obsidian.md/help/links).
- [Canvas do Obsidian](https://obsidian.md/help/plugins/canvas) e [formato JSON Canvas](https://jsoncanvas.org/spec/1.0/).
- [Templates nativos do Obsidian](https://obsidian.md/help/plugins/templates).
- [Instalação da WPILib](https://docs.wpilib.org/en/stable/docs/zero-to-robot/step-2/wpilib-setup.html).
- [Build e deploy WPILib](https://docs.wpilib.org/en/stable/docs/software/vscode-overview/deploying-robot-code.html).
- [Associação de triggers e comandos](https://docs.wpilib.org/en/stable/docs/software/commandbased/binding-commands-to-triggers.html).
- [Unidades dos controladores SPARK](https://docs.revrobotics.com/revlib/spark/closed-loop/units).
- [Integração PathPlanner](https://pathplanner.dev/pplib-getting-started.html).

As explicações da API LuverLib se baseiam no código local, não na suposição de que uma versão nova de um fornecedor mantém a mesma assinatura. As referências oficiais complementam os conceitos e a operação das ferramentas.

---

← [Página inicial](../00%20-%20Comece%20aqui.md) · [Fontes e versão deste guia](Fontes%20e%20versao.md)
