---
title: "Telemetria"
tags:
  - luverlib
  - modulo
revisado: 2026-11-13
---

# Telemetria

## O que é

Telemetria mostra o estado dos dispositivos no SmartDashboard/NetworkTables. `Telemetria` oferece métodos estáticos de registro. Os objetos retornados, como `TelemetriaMotorNeo`, representam os campos publicados.

## Como usar

Crie o dispositivo e registre uma vez, durante a inicialização:

```java
// piloto e motor já pertencem ao projeto.
var painelXbox = Telemetria.registrarControle("Xbox", piloto);
var painelMotor = Telemetria.registrarMotor("Garra", motor);
```

Use `registrarMotorKraken(nome, motor)` para Kraken e `registrarLimelight(nome, camera)` para a câmera. O `TimedRobot` atualiza os Sendable; não é preciso registrar novamente no periódico. A câmera continua precisando de coleta por `VisaoLimelight` ou `atualizar()`.

## O que aparece

| Dispositivo | Tipos de dados |
| --- | --- |
| Xbox | Conexão, eixos, botões e configuração dos perfis |
| NEO | Posição, RPM, corrente, temperatura, tensão e configuração |
| Kraken | Conexão, correntes de alimentação/estator, medidas e configuração |
| Limelight | Conexão, alvo, pipeline e resultado da avaliação de pose |

Os nomes escolhidos aparecem sob `SmartDashboard` na rede. Dê nomes diferentes para dispositivos diferentes. Os campos são de leitura: não comandam movimento nem editam PID pelo painel. Um valor `NaN` de Kraken indica indisponibilidade, não corrente ou posição zero.

## Encerramento

Guarde o objeto de telemetria para fechá-lo antes do dispositivo. `painelMotor.close()` remove suas referências de publicação; o motor continua pertencendo ao subsistema e só deve ser fechado pelo seu dono. O exemplo [Adicionar um mecanismo](../04%20-%20Tutoriais/Adicionar%20um%20mecanismo.md) mostra essa ordem completa.

**Consulta:** [API - Telemetria](../05%20-%20Referencia/API%20-%20Telemetria.md) · [AdvantageScope](AdvantageScope.md).

---

← [Página inicial](../00%20-%20Comece%20aqui.md) · [Fontes e versão deste guia](../05%20-%20Referencia/Fontes%20e%20versao.md)
