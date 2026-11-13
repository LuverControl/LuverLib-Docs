---
title: "PathPlanner"
tags:
  - luverlib
  - modulo
revisado: 2026-11-13
---

# PathPlanner

## O que é

O aplicativo PathPlanner cria caminhos e rotinas. `PathPlannerLuver` conecta esses arquivos ao controle do drivetrain e entrega comandos WPILib. `ConexaoPathPlanner` é o contrato entre a biblioteca e as funções reais do seu robô.

## O que fornecer

| Informação | Origem no projeto |
| --- | --- |
| Subsistema do drivetrain | Reserva do hardware pelos comandos |
| Pose de campo e reset | Odometria/estimador real |
| Velocidades relativas ao robô | Sensores e cinemática |
| Aplicação de saída e parada | Motores e controle de rodas/módulos |
| `RobotConfig` | Massa, inércia, geometria e demais características do robô |
| Arquivos `.auto` e `.path` | Exportação do aplicativo |

Os ajustes de controle e pathfinding estão em `Constants.PathPlanner`. Os PIDs de translação/rotação são usados pelo controlador holonômico do swerve. No diferencial, a fachada usa LTV. Alterar os PIDs holonômicos não afina o controlador LTV.

## Como usar

1. Implemente e confira a conexão com o drivetrain real.
2. Forneça `RobotConfig`, ou carregue `src/main/deploy/pathplanner/settings.json` com `carregarConfiguracaoDoProjeto()`.
3. Crie uma única fachada: `AutoBuilder` é global e a segunda configuração é recusada.
4. Registre todos os eventos nomeados antes de carregar arquivos ou criar o seletor.
5. Carregue os autos e prepare o seletor na inicialização.
6. Agende a seleção em `autonomousInit()` e cancele em `teleopInit()`.

```java
package guia;

import com.pathplanner.lib.config.RobotConfig;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import frc.robot.luverlib.pathplanner.ConexaoPathPlanner;
import frc.robot.luverlib.pathplanner.PathPlannerLuver;
import frc.robot.luverlib.pathplanner.SeletorAutonomo;

public final class AutonomoExemplo implements AutoCloseable {
  private final SeletorAutonomo seletor;
  private Command autonomoAtual;

  public AutonomoExemplo(ConexaoPathPlanner conexaoReal, RobotConfig roboReal,
      String nomeEvento, Command evento, String nomeAutoPadrao) {
    var caminhos = new PathPlannerLuver(conexaoReal, roboReal);
    caminhos.registrarComando(nomeEvento, evento);
    seletor = caminhos.criarSeletor("Autonomo", nomeAutoPadrao);
  }

  public void iniciarAutonomo() {
    autonomoAtual = seletor.obterSelecionado();
    if (autonomoAtual != null) CommandScheduler.getInstance().schedule(autonomoAtual);
  }

  public void entrarEmTeleop() {
    if (autonomoAtual != null) CommandScheduler.getInstance().cancel(autonomoAtual);
  }

  @Override public void close() {
    try { entrarEmTeleop(); } finally { seletor.close(); }
  }
}
```

O exemplo contém um evento. Se o seu auto usa vários, registre todos antes da chamada `criarSeletor`. Os métodos `iniciarAutonomo()` e `entrarEmTeleop()` são auxiliares: o `Robot` da equipe precisa chamá-los nos modos correspondentes.

## Diferença entre operações

`criarComandoCaminho(nome)` segue um caminho sem reset de pose. `criarComandoIrAteCaminho(nome)` faz pathfinding até o começo. `criarComandoIrAtePose(...)` recebe pose absoluta; `criarComandoIrAtePoseDaAlianca(...)` recebe alvo na referência azul e faz a transformação de aliança. Para reset explícito existe `criarComandoRedefinirPose(...)`.

`TankPathPlanner` converte RPM e cinemática diferencial em tensões kS/kV e pode montar uma `ConexaoPathPlanner`. Ele precisa da largura, do fator metros/rotação, das velocidades e da calibração reais. Não fornece odometria nem PID próprio de velocidade de roda. Veja [Preparar autonomo](../04%20-%20Tutoriais/Preparar%20autonomo.md).

O formato de arquivos aceito nesta versão é `2025.X`, embora a biblioteca seja PathPlannerLib 2026.1.2. Os limites de pathfinding não substituem os gravados em cada caminho. Arquivos/nomes inválidos, eventos ausentes e autos Choreo são recusados.

**Consulta:** [API - PathPlanner](../05%20-%20Referencia/API%20-%20PathPlanner.md) · [Integração oficial do PathPlanner](https://pathplanner.dev/pplib-getting-started.html).

---

← [Página inicial](../00%20-%20Comece%20aqui.md) · [Fontes e versão deste guia](../05%20-%20Referencia/Fontes%20e%20versao.md)
