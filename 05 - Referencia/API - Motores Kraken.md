---
title: "API — Motores Kraken"
tags:
  - luverlib
  - api
revisado: 2026-11-13
---

# API — Motores Kraken

Leia primeiro [Motores Kraken](../03%20-%20Modulos/Motores%20Kraken.md). Esta referência lista as classes e suas assinaturas públicas declaradas no código consultado. Corpos de métodos foram omitidos. Records também geram acessores com os nomes de seus componentes.


## ConfiguracaoMotorKraken


Agrupa os limites e parâmetros do Talon e gera sua configuração Phoenix.


Fonte: [ConfiguracaoMotorKraken](../99%20-%20Anexos/Fontes/src/main/java/frc/robot/luverlib/motores/kraken/ConfiguracaoMotorKraken.java)


```java
public record ConfiguracaoMotorKraken( ModeloKraken modelo, double limiteCorrenteAlimentacaoAmperes, double limiteCorrenteEstatorAmperes, boolean invertido, boolean frearAoParar, double tensaoMaximaVolts, double rampaMalhaAbertaSegundos, double rampaMalhaFechadaSegundos, double rotacoesMotorPorRotacaoSaida, double limiteSaida);
public ConfiguracaoMotorKraken;
public ConfiguracaoMotorKraken comCorrentes(double alimentacaoAmperes, double estatorAmperes);
public ConfiguracaoMotorKraken comInversao(boolean inverter);
public ConfiguracaoMotorKraken comFreio(boolean frear);
public ConfiguracaoMotorKraken comReducao(double reducao);
public ConfiguracaoMotorKraken comLimiteSaida(double limite);
public ConfiguracaoMotorKraken comRampas(double abertaSegundos, double fechadaSegundos);
```


## ModeloKraken


Identifica as variantes X60 e X44 aceitas pelo wrapper.


Fonte: [ModeloKraken](../99%20-%20Anexos/Fontes/src/main/java/frc/robot/luverlib/motores/kraken/ModeloKraken.java)


## MotorKraken


Configura e comanda Kraken/Talon FX e lê medições válidas.


Fonte: [MotorKraken](../99%20-%20Anexos/Fontes/src/main/java/frc/robot/luverlib/motores/kraken/MotorKraken.java)


```java
public MotorKraken(int idCan);
public MotorKraken(int idCan, ModeloKraken modelo);
public MotorKraken(int idCan, ConfiguracaoMotorKraken configuracao);
public MotorKraken(int idCan, CANBus barramento, ConfiguracaoMotorKraken configuracao);
public void definirPotencia(double valor);
public void definirTensao(double volts);
public void parar();
public double obterPosicaoRotacoes();
public double obterVelocidadeRPM();
public double obterCorrenteAlimentacaoAmperes();
public double obterCorrenteEstatorAmperes();
public double obterTemperaturaCelsius();
public double obterTensaoAplicadaVolts();
public double obterTensaoAlimentacaoVolts();
public void definirPosicaoEncoder(double rotacoes);
public void zerarEncoder();
public boolean estaConectado();
public ConfiguracaoMotorKraken obterConfiguracao();
public TalonFX obterControlador();
public void close();
```


## VariaveisMotorKraken


Repassa Constants.Kraken e monta as configurações iniciais.


Fonte: [VariaveisMotorKraken](../99%20-%20Anexos/Fontes/src/main/java/frc/robot/luverlib/motores/kraken/VariaveisMotorKraken.java)


Ajustes e valores: [Referencia completa de Constants](../02%20-%20Entenda%20o%20projeto/Referencia%20completa%20de%20Constants.md).


```java
public static ConfiguracaoMotorKraken configuracaoPadrao();
public static ConfiguracaoMotorKraken configuracaoPadrao(ModeloKraken modelo);
```

---

← [Página inicial](../00%20-%20Comece%20aqui.md) · [Fontes e versão deste guia](Fontes%20e%20versao.md)
