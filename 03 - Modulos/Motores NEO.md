---
title: "Motores NEO com SPARK MAX"
tags:
  - luverlib
  - modulo
revisado: 2026-11-13
---

# Motores NEO com SPARK MAX

## O que é

`MotorNeo` encapsula um NEO brushless ligado a SPARK MAX. Ele aplica configuração na criação e oferece potência, tensão, parada e leituras. `ConfiguracaoMotorNeo` é um record imutável; métodos `com...` retornam uma nova configuração.

## O que configurar

Use `Constants.Neo` para os padrões genéricos e o grupo do mecanismo para seus IDs e ajustes específicos. A tração pronta usa `Constants.Tracao`, com correntes, freio e rampas próprios. Os campos estão explicados em [Referencia completa de Constants](../02%20-%20Entenda%20o%20projeto/Referencia%20completa%20de%20Constants.md).

Confira corrente, inversão, freio, tensão, rampas, redução, limite de saída e persistência antes de criar o motor. A redução define a relação entre voltas do motor e voltas da saída. As leituras já consideram essa conversão.

## Como usar

Crie uma instância no subsistema e use sempre a mesma. O ID passado ao construtor precisa corresponder ao SPARK físico. A faixa aceita por este wrapper é 1 a 62. O exemplo compilável de criação está em [MotoresExemplo.java](../99%20-%20Anexos/Exemplos%20Java/MotoresExemplo.java); a forma básica é:

```java
// Dentro do construtor do subsistema; idCanDeConstants vem de Constants.
MotorNeo motor = new MotorNeo(idCanDeConstants,
    VariaveisMotor.configuracaoPadrao());
```

| Operação | Uso e unidade |
| --- | --- |
| `definirPotencia(potencia)` | Solicita saída entre -1 e 1, saturada pelo limite configurado |
| `definirTensao(volts)` | Solicita tensão, limitada pela tensão nominal × limite de saída |
| `parar()` | Interrompe a saída |
| `obterPosicaoRotacoes()` | Lê rotações da saída do mecanismo |
| `obterVelocidadeRPM()` | Lê rotações da saída por minuto |
| `zerarEncoder()` | Muda a referência do encoder; não encontra sozinho um zero físico |
| `obterCorrenteAmperes()` e `obterTemperaturaCelsius()` | Diagnóstico do motor |
| `close()` | Para e libera o controlador ao encerrar seu uso |

## Seguidor

O construtor com terceiro argumento recebe o líder existente e configura follow nativo. Líder e seguidor precisam de IDs diferentes. No seguidor, `invertido` é relativo ao líder. O limite de saída do wrapper não limita o vínculo nativo de follow: limite a saída no líder.

## O que observar

Freio elétrico ajuda a resistir ao giro, mas não é um controle de posição para sustentar braço ou elevador. `obterControlador()` dá acesso ao SPARK e pode contornar os limites do wrapper. Não crie motores dentro de `periodic()` e não grave configurações repetidamente em flash.

Para um mecanismo controlado por comando, siga [Adicionar um mecanismo](../04%20-%20Tutoriais/Adicionar%20um%20mecanismo.md). Para PID de posição, veja [AutoPrecision](AutoPrecision.md) e [API - Motores NEO](../05%20-%20Referencia/API%20-%20Motores%20NEO.md).

---

← [Página inicial](../00%20-%20Comece%20aqui.md) · [Fontes e versão deste guia](../05%20-%20Referencia/Fontes%20e%20versao.md)
