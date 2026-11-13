---
title: "Constants — onde configurar"
tags:
  - luverlib
  - guia
revisado: 2026-11-13
---

# Constants — onde configurar

`src/main/java/frc/robot/Constants.java` é o lugar central para **IDs de qualquer motor/dispositivo, PIDs e parâmetros que precisam ser fáceis de alterar**. Isso inclui limites de corrente, rampas, inversões, redução, perfis dos controles e ajustes das integrações.

> [!important] Regra do projeto
> Qualquer inclusão fora de identificação, PIDs e ajustes rápidos exige autorização prévia do dono. Essa regra também vale para quem editar o projeto com ajuda de um assistente.

## Como os grupos estão organizados

| Grupo | Conteúdo | Veja em uso |
| --- | --- | --- |
| `Constants.Tracao` | IDs, configuração dos quatro NEO e escalas de pilotagem | [Tracao tank](../03%20-%20Modulos/Tracao%20tank.md) |
| `Constants.Neo` | Configuração inicial genérica NEO/SPARK MAX | [Motores NEO](../03%20-%20Modulos/Motores%20NEO.md) |
| `Constants.Kraken` | Modelo, limites e comunicação Talon FX | [Motores Kraken](../03%20-%20Modulos/Motores%20Kraken.md) |
| `Constants.Controle` | Portas USB, perfis dos analógicos e gatilhos | [Controle Xbox](../03%20-%20Modulos/Controle%20Xbox.md) |
| `Constants.PathPlanner` | PID holonômico, pathfinding e período | [PathPlanner](../03%20-%20Modulos/PathPlanner.md) |
| `Constants.Limelight` | Critérios de qualidade da visão | [Limelight](../03%20-%20Modulos/Limelight.md) |
| `Constants.AdvantageScope` | Tabela, intervalo e gravação | [AdvantageScope](../03%20-%20Modulos/AdvantageScope.md) |
| `Constants.AutoPrecision` | Período e heartbeat iniciais | [AutoPrecision](../03%20-%20Modulos/AutoPrecision.md) |

## Como mudar um ajuste

1. Identifique o mecanismo e o parâmetro na [referência completa](Referencia%20completa%20de%20Constants.md).
2. Anote o valor atual e o motivo da mudança em [Como registrar os testes](../06%20-%20Caderno%20da%20equipe/Como%20registrar%20os%20testes.md).
3. Edite o valor no grupo correspondente de `Constants.java`.
4. Compile e rode os testes. Atualize o programa do robô pelo fluxo de deploy do projeto da equipe, quando for o caso.
5. Observe o resultado e registre a condição do teste.

Exemplo de leitura do ajuste no código:

```java
import frc.robot.Constants;

// Dentro de uma classe ou método que precise consultar a porta:
int portaDoPiloto = Constants.Controle.PORTA_PILOTO;
```

## Por que ainda existem classes Variaveis

As classes `VariaveisMotor`, `VariaveisControle` e as demais preservam chamadas existentes e montam os objetos de configuração. Os valores delas apontam para `Constants`. Para trocar o valor central, edite `Constants`; não crie uma segunda versão do número em outro arquivo.

Alterar `Constants` exige recompilar e reiniciar o programa. Não há edição ao vivo desses valores pelo painel. Quando um construtor recebe uma configuração específica, ela pode substituir o padrão; os valores dessa configuração do robô também devem vir do grupo correspondente em `Constants`.

## Novo mecanismo e ganhos PID

Crie um grupo para o mecanismo, por exemplo `Constants.Garra`, com os IDs e ajustes medidos para ele. Esse grupo ainda não existe nesta base. [Adicionar um mecanismo](../04%20-%20Tutoriais/Adicionar%20um%20mecanismo.md) mostra um exemplo didático, sem alterar o checkout.

Os PIDs presentes hoje são os do PathPlanner. PIDs do AutoPrecision são recebidos por mecanismo; defina ganhos iniciais, máximos e aprovados em `Constants` quando integrar esse mecanismo. Não copie ganhos de outro backend só porque também se chamam P, I e D.

A versão interna do protocolo AutoPrecision fica no módulo: identifica o contrato de comunicação, não um ajuste de pilotagem. Objetos de hardware, comandos, lógica de controle e configuração de autenticação não são parâmetros para colocar em `Constants`.

---

← [Página inicial](../00%20-%20Comece%20aqui.md) · [Fontes e versão deste guia](../05%20-%20Referencia/Fontes%20e%20versao.md)
