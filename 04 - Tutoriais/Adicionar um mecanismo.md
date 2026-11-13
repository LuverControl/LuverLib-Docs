---
title: "Adicionar um mecanismo"
tags:
  - luverlib
  - tutorial
revisado: 2026-11-13
---

# Adicionar um mecanismo

## Objetivo

Criar um subsistema simples com um NEO e um comando manual que pare ao terminar. Este é um exemplo de estrutura; um mecanismo que exige homing, proteção de curso ou sustentação de carga precisa dessas regras no seu subsistema.

## Passo 1 — declare os ajustes em Constants

O grupo abaixo é didático e **ainda não existe** na base. Os números não foram calibrados para uma garra real. Se usar essa estrutura, escolha ID e ajustes verificados para o seu hardware:

```java
// Inserir dentro da classe Constants do projeto, depois de revisar os valores.
public static final class Garra {
  public static final int MOTOR = 10;
  public static final int CORRENTE_AMPERES = 20;
  public static final double POTENCIA_MANUAL = 0.15;
  private Garra() {}
}
```

Esse grupo só contém identificação e parâmetros de ajuste. Para incluir outra categoria de conteúdo em `Constants`, peça autorização ao dono.

## Passo 2 — crie o subsistema

O exemplo compilável recebe ID e configuração do projeto. `runEnd` cria um comando que exige este subsistema e chama a parada quando termina. `onlyWhile` limita a operação manual a teleop habilitado.

```java
package guia;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.luverlib.motores.neo.ConfiguracaoMotorNeo;
import frc.robot.luverlib.motores.neo.MotorNeo;
import frc.robot.luverlib.telemetria.Telemetria;
import frc.robot.luverlib.telemetria.TelemetriaMotorNeo;

public final class MecanismoExemplo extends SubsystemBase implements AutoCloseable {
  private final MotorNeo motor;
  private final TelemetriaMotorNeo painel;

  public MecanismoExemplo(int idDefinidoEmConstants, ConfiguracaoMotorNeo ajustes) {
    motor = new MotorNeo(idDefinidoEmConstants, ajustes);
    try {
      painel = Telemetria.registrarMotor("Mecanismo", motor);
    } catch (RuntimeException | Error falha) {
      try { motor.close(); } catch (RuntimeException | Error limpeza) { falha.addSuppressed(limpeza); }
      throw falha;
    }
  }

  public Command criarComandoManual(double potenciaDefinidaEmConstants) {
    return runEnd(() -> motor.definirPotencia(potenciaDefinidaEmConstants), motor::parar)
        .onlyWhile(DriverStation::isTeleopEnabled);
  }

  @Override public void close() {
    try { painel.close(); } finally { motor.close(); }
  }
}
```

## Passo 3 — conecte no RobotContainer

Depois de adicionar e revisar `Constants.Garra`, a composição pode seguir este formato:

```java
// Fragmento para o RobotContainer; requer o grupo Constants.Garra mostrado acima.
var ajustes = VariaveisMotor.configuracaoPadrao()
    .comLimiteCorrente(Constants.Garra.CORRENTE_AMPERES);
var garra = new MecanismoExemplo(Constants.Garra.MOTOR, ajustes);
piloto.botaoA().whileTrue(
    garra.criarComandoManual(Constants.Garra.POTENCIA_MANUAL));
```

Os objetos precisam ser mantidos em campos no `RobotContainer` real para ter ciclo de vida claro. Ajuste o pacote/imports conforme onde colocar `MecanismoExemplo`. Não acrescente esse exemplo à tração usando o mesmo ID de um dispositivo existente.

## Passo 4 — confira o uso

- [ ] O build compila.
- [ ] O ID vem de `Constants` e corresponde ao controlador.
- [ ] O comando declara o subsistema e termina com parada.
- [ ] A liberação do botão e a saída de teleop encerram a operação manual.
- [ ] O registro de teste explica o que foi medido e em qual condição.

Os anexos têm exemplos completos para compilação; os fragmentos de composição dependem dos campos que você criar. Veja [Exemplos Java](../05%20-%20Referencia/Exemplos%20Java.md).

---

← [Página inicial](../00%20-%20Comece%20aqui.md) · [Fontes e versão deste guia](../05%20-%20Referencia/Fontes%20e%20versao.md)
