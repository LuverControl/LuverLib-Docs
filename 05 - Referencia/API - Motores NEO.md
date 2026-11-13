---
title: "API — Motores NEO"
tags:
  - luverlib
  - api
revisado: 2026-11-13
---

# API — Motores NEO

Leia primeiro [Motores NEO](../03%20-%20Modulos/Motores%20NEO.md). Esta referência lista as classes e suas assinaturas públicas declaradas no código consultado. Corpos de métodos foram omitidos. Records também geram acessores com os nomes de seus componentes.


## ConfiguracaoMotorNeo


Agrupa os parâmetros imutáveis de um NEO; métodos com... retornam cópias.


Fonte: [ConfiguracaoMotorNeo](../99%20-%20Anexos/Fontes/src/main/java/frc/robot/luverlib/motores/neo/ConfiguracaoMotorNeo.java)


```java
public record ConfiguracaoMotorNeo( int limiteCorrenteAmperes, boolean invertido, boolean frearAoParar, double tensaoNominalVolts, double rampaMalhaAbertaSegundos, double rampaMalhaFechadaSegundos, double rotacoesMotorPorRotacaoSaida, double limiteSaida, boolean gravarConfiguracao, boolean compensarTensao);
public ConfiguracaoMotorNeo(int limiteCorrenteAmperes, boolean invertido, boolean frearAoParar, double tensaoNominalVolts, double rampaMalhaAbertaSegundos, double rampaMalhaFechadaSegundos, double rotacoesMotorPorRotacaoSaida, double limiteSaida, boolean gravarConfiguracao);
public ConfiguracaoMotorNeo;
public ConfiguracaoMotorNeo comLimiteCorrente(int amperes);
public ConfiguracaoMotorNeo comInversao(boolean inverter);
public ConfiguracaoMotorNeo comLimiteSaida(double limite);
public ConfiguracaoMotorNeo comCompensacaoTensao(boolean habilitada);
public double fatorEncoder();
```


## MotorNeo


Configura e comanda um NEO/SPARK MAX, incluindo follow nativo opcional.


Fonte: [MotorNeo](../99%20-%20Anexos/Fontes/src/main/java/frc/robot/luverlib/motores/neo/MotorNeo.java)


```java
public MotorNeo(int idCan);
public MotorNeo(int idCan, ConfiguracaoMotorNeo configuracao);
public MotorNeo(int idCan, ConfiguracaoMotorNeo configuracao, MotorNeo lider);
public void definirPotencia(double potencia);
public void definirTensao(double volts);
public void parar();
public double obterPosicaoRotacoes();
public double obterVelocidadeRPM();
public void definirPosicaoEncoder(double rotacoes);
public void zerarEncoder();
public double obterCorrenteAmperes();
public double obterTemperaturaCelsius();
public double obterTensaoAplicadaVolts();
public ConfiguracaoMotorNeo obterConfiguracao();
public SparkMax obterControlador();
public void close();
```


## VariaveisMotor


Repassa os ajustes de Constants.Neo/Tracao e monta configurações NEO.


Fonte: [VariaveisMotor](../99%20-%20Anexos/Fontes/src/main/java/frc/robot/luverlib/motores/neo/VariaveisMotor.java)


Ajustes e valores: [Referencia completa de Constants](../02%20-%20Entenda%20o%20projeto/Referencia%20completa%20de%20Constants.md).


```java
public static ConfiguracaoMotorNeo configuracaoPadrao();
```

---

← [Página inicial](../00%20-%20Comece%20aqui.md) · [Fontes e versão deste guia](Fontes%20e%20versao.md)
