---
title: "API — Telemetria"
tags:
  - luverlib
  - api
revisado: 2026-11-13
---

# API — Telemetria

Leia primeiro [Telemetria](../03%20-%20Modulos/Telemetria.md). Esta referência lista as classes e suas assinaturas públicas declaradas no código consultado. Corpos de métodos foram omitidos. Records também geram acessores com os nomes de seus componentes.


## Telemetria


Registra uma vez os Sendable de controle, motores e câmera no SmartDashboard.


Fonte: [Telemetria](../99%20-%20Anexos/Fontes/src/main/java/frc/robot/luverlib/telemetria/Telemetria.java)


```java
public static TelemetriaXbox registrarControle(String nome, ControleXbox controle);
public static TelemetriaMotorNeo registrarMotor(String nome, MotorNeo motor);
public static TelemetriaMotorKraken registrarMotorKraken(String nome, MotorKraken motor);
public static TelemetriaLimelight registrarLimelight(String nome, Limelight camera);
```


## TelemetriaLimelight


Expõe conexão, alvos e diagnóstico da avaliação de pose.


Fonte: [TelemetriaLimelight](../99%20-%20Anexos/Fontes/src/main/java/frc/robot/luverlib/telemetria/TelemetriaLimelight.java)


```java
public TelemetriaLimelight(Limelight camera);
public void initSendable(SendableBuilder painel);
public void close();
```


## TelemetriaMotorKraken


Expõe medidas do Kraken e indica indisponibilidade sem inventar zero.


Fonte: [TelemetriaMotorKraken](../99%20-%20Anexos/Fontes/src/main/java/frc/robot/luverlib/telemetria/TelemetriaMotorKraken.java)


```java
public TelemetriaMotorKraken(MotorKraken motor);
public void initSendable(SendableBuilder painel);
public void close();
```


## TelemetriaMotorNeo


Expõe medidas e configuração do NEO sem comandar o motor.


Fonte: [TelemetriaMotorNeo](../99%20-%20Anexos/Fontes/src/main/java/frc/robot/luverlib/telemetria/TelemetriaMotorNeo.java)


```java
public TelemetriaMotorNeo(MotorNeo motor);
public void initSendable(SendableBuilder painel);
public void close();
```


## TelemetriaXbox


Expõe leituras do controle em campos de diagnóstico.


Fonte: [TelemetriaXbox](../99%20-%20Anexos/Fontes/src/main/java/frc/robot/luverlib/telemetria/TelemetriaXbox.java)


```java
public TelemetriaXbox(ControleXbox controle);
public void initSendable(SendableBuilder painel);
public void close();
```

---

← [Página inicial](../00%20-%20Comece%20aqui.md) · [Fontes e versão deste guia](Fontes%20e%20versao.md)
