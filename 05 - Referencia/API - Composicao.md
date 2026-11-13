---
title: "API — Composicao"
tags:
  - luverlib
  - api
revisado: 2026-11-13
---

# API — Composicao

Leia primeiro [Arquitetura e ciclo do robo](../02%20-%20Entenda%20o%20projeto/Arquitetura%20e%20ciclo%20do%20robo.md). Esta referência lista as classes e suas assinaturas públicas declaradas no código consultado. Corpos de métodos foram omitidos. Records também geram acessores com os nomes de seus componentes.


## Constants


Centraliza IDs, PIDs e ajustes de acesso rápido; consulte a referência campo a campo.


Fonte: [Constants](../99%20-%20Anexos/Fontes/src/main/java/frc/robot/Constants.java)


Ajustes e valores: [Referencia completa de Constants](../02%20-%20Entenda%20o%20projeto/Referencia%20completa%20de%20Constants.md).


## Main


Ponto de entrada que inicia Robot pela WPILib.


Fonte: [Main](../99%20-%20Anexos/Fontes/src/main/java/frc/robot/Main.java)


```java
public static void main(String... args);
```


## Robot


Executa o ciclo WPILib, scheduler e cancelamento em Disabled.


Fonte: [Robot](../99%20-%20Anexos/Fontes/src/main/java/frc/robot/Robot.java)


```java
public void robotPeriodic();
public void disabledInit();
```


## RobotContainer


Cria dispositivos da composição e conecta comandos e controles.


Fonte: [RobotContainer](../99%20-%20Anexos/Fontes/src/main/java/frc/robot/RobotContainer.java)


```java
public RobotContainer();
public RobotContainer(ChassiTank chassi);
public ChassiTank obterChassi();
public void pararVibracao();
public void pararTudo();
public void close();
```


## DirigirTank


Comando padrão que lê gatilhos/X bruto e pilota em teleop habilitado.


Fonte: [DirigirTank](../99%20-%20Anexos/Fontes/src/main/java/frc/robot/commands/DirigirTank.java)


```java
public DirigirTank(ChassiTank chassi, ControleXbox controle);
public void execute();
public void end(boolean interrompido);
public boolean isFinished();
```


## ChassiTank


Possui os dispositivos da tração DualFire e aplica a mistura esquerda/direita.


Fonte: [ChassiTank](../99%20-%20Anexos/Fontes/src/main/java/frc/robot/subsystems/ChassiTank.java)


```java
public ChassiTank();
public void mover(double frenteTras, double giro);
public Rotation2d obterRotacao();
public void parar();
public void close();
```

---

← [Página inicial](../00%20-%20Comece%20aqui.md) · [Fontes e versão deste guia](Fontes%20e%20versao.md)
