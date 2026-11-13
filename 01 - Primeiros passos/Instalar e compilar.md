---
title: "Instalar e compilar"
tags:
  - luverlib
  - guia
revisado: 2026-11-13
---

# Instalar e compilar

## O que preparar

Use a instalação da WPILib 2026 com seu VS Code e JDK. O código deste checkout declara Java 17, GradleRIO 2026.2.1 e `version = 1.1.0`. O supervisor AutoPrecision no PC usa Python 3.13 e `pyntcore==2026.2.1`. As versões dos vendordeps estão em [Fontes e versao](../05%20-%20Referencia/Fontes%20e%20versao.md).

Siga o [instalador oficial da WPILib](https://docs.wpilib.org/en/stable/docs/zero-to-robot/step-2/wpilib-setup.html) para o seu sistema. Depois, abra a pasta do projeto no VS Code da WPILib, a que contém `build.gradle` e `gradlew`.

## Obter o código

Se já recebeu o projeto, use a pasta fornecida pela equipe. Para uma nova cópia:

```sh
git clone https://github.com/LuverControl/LuverLib.git
cd LuverLib
```

O manual inclui mudanças locais de `Constants` que podem não estar no commit remoto. Antes de seguir os nomes dos grupos, compare seu `Constants.java` com [Referencia completa de Constants](../02%20-%20Entenda%20o%20projeto/Referencia%20completa%20de%20Constants.md). A nota [Fontes e versao](../05%20-%20Referencia/Fontes%20e%20versao.md) identifica a base exata; a cópia de fontes do vault serve para consulta dessa base.

## Compilar e testar

Linux/macOS, na raiz do projeto:

```sh
./gradlew --no-daemon --console=plain check build
```

Windows, PowerShell, na raiz:

```powershell
.\gradlew.bat --no-daemon --console=plain check build
```

O primeiro build pode baixar dependências. `check` também compila o exemplo em `examples/`. O resultado esperado é `BUILD SUCCESSFUL`. Os relatórios Java ficam em `build/reports/tests/test/index.html` e o JAR em `build/libs/`.

## Simulação e hardware

O build atual configura testes de desktop e dependências nativas. Esses testes verificam chamadas e comportamentos simulados, sem reproduzir toda a mecânica real. Execute o comando de simulação disponibilizado pelo projeto WPILib que estiver usando; os testes fornecidos são o caminho reproduzível de validação deste checkout.

> [!important] Deploy deste checkout
> O `build.gradle` revisado não contém o bloco de targets `deploy` para roboRIO. Portanto, este manual não apresenta `./gradlew deploy` como um recurso pronto desta base. Para usar hardware, integre a LuverLib a um projeto de robô com target roboRIO configurado e conferido. Veja [Integrar em outro robo](Integrar%20em%20outro%20robo.md).

A [documentação oficial de build e deploy](https://docs.wpilib.org/en/stable/docs/software/vscode-overview/deploying-robot-code.html) explica o fluxo no VS Code. O botão de deploy depende da configuração do projeto e da conexão com o roboRIO.

## Se falhar

Confira primeiro o JDK ativo. No terminal da WPILib, `java -version` precisa corresponder ao ambiente suportado por este projeto; um Java global diferente pode impedir até o início do Gradle. No Linux desta máquina, o JDK usado na verificação foi `~/wpilib/2026/jdk`. Em seguida, veja [Erros comuns](../05%20-%20Referencia/Erros%20comuns.md).

**Próximo passo:** [Constants - onde configurar](../02%20-%20Entenda%20o%20projeto/Constants%20-%20onde%20configurar.md).

---

← [Página inicial](../00%20-%20Comece%20aqui.md) · [Fontes e versão deste guia](../05%20-%20Referencia/Fontes%20e%20versao.md)
