---
title: "API — AutoPrecision"
tags:
  - luverlib
  - api
revisado: 2026-11-13
---

# API — AutoPrecision

Leia primeiro [AutoPrecision](../03%20-%20Modulos/AutoPrecision.md). Esta referência lista as classes e suas assinaturas públicas declaradas no código consultado. Corpos de métodos foram omitidos. Records também geram acessores com os nomes de seus componentes.


## AutoPrecision


Gerencia estado da sessão, comando, autorização e supervisão dos ensaios.


Fonte: [AutoPrecision](../99%20-%20Anexos/Fontes/src/main/java/frc/robot/luverlib/autoprecision/AutoPrecision.java)


```java
public AutoPrecision(String nome, Subsystem mecanismo, ControleAutoPrecision controle, ConfiguracaoAutoPrecision config, BooleanSupplier autorizacaoLocal);
public Command criarComando();
public Estado obterEstado();
public String obterSessao();
public void periodic();
public void close();
```


## ConfiguracaoAutoPrecision


Agrupa ganhos, limites, tolerâncias e temporização de cada mecanismo.


Fonte: [ConfiguracaoAutoPrecision](../99%20-%20Anexos/Fontes/src/main/java/frc/robot/luverlib/autoprecision/ConfiguracaoAutoPrecision.java)


```java
public record ConfiguracaoAutoPrecision( String unidade, LimitesAutoPrecision limites, GanhosPID ganhosIniciais, GanhosPID ganhosMaximos, double toleranciaPosicao, double toleranciaVelocidade, double tempoEstavelSegundos, double duracaoMaximaEnsaioSegundos, double periodoSegundos, double timeoutHeartbeatSegundos, double zonaIntegral, double acumuladorIntegralMaximo);
public ConfiguracaoAutoPrecision;
```


## ControleAutoPrecision


Contrato do backend para medição, ganho, referência e encerramento.


Fonte: [ControleAutoPrecision](../99%20-%20Anexos/Fontes/src/main/java/frc/robot/luverlib/autoprecision/ControleAutoPrecision.java)


```java
public String identificador();
public Medicao medir();
public void iniciar(ConfiguracaoAutoPrecision configuracao);
public void aplicarGanhos(GanhosPID ganhos);
public void aplicarReferencia(double posicao);
public void parar();
public void finalizar();
```


O contrato inclui `medir`, `iniciar`, `aplicarGanhos`, `aplicarReferencia` e `finalizar`; seus métodos de interface são implicitamente públicos. O record `Medicao` representa as medidas devolvidas pelo backend.


## ControleNeoAutoPrecision


Backend de posição do SPARK MAX com restauração dos ajustes temporários.


Fonte: [ControleNeoAutoPrecision](../99%20-%20Anexos/Fontes/src/main/java/frc/robot/luverlib/autoprecision/ControleNeoAutoPrecision.java)


```java
public ControleNeoAutoPrecision(MotorNeo motor, ClosedLoopSlot slot);
public Medicao medir();
public void iniciar(ConfiguracaoAutoPrecision config);
public void aplicarGanhos(GanhosPID ganhos);
public void aplicarReferencia(double posicao);
public void finalizar();
```


## GanhosPID


Agrupa P/I/D e valida ganhos finitos, não negativos e dentro dos limites.


Fonte: [GanhosPID](../99%20-%20Anexos/Fontes/src/main/java/frc/robot/luverlib/autoprecision/GanhosPID.java)


```java
public record GanhosPID(double p, double i, double d);
public GanhosPID;
public boolean dentroDe(GanhosPID maximos);
```


## LimitesAutoPrecision


Define faixa física, saída, perfil e cortes de medição do ensaio.


Fonte: [LimitesAutoPrecision](../99%20-%20Anexos/Fontes/src/main/java/frc/robot/luverlib/autoprecision/LimitesAutoPrecision.java)


```java
public record LimitesAutoPrecision( double posicaoMinima, double posicaoMaxima, double saidaMaxima, double velocidadeMaxima, double aceleracaoMaxima, double correnteMaxima, double temperaturaMaxima, double deslocamentoMaximoEnsaio, double velocidadeMaximaMedida);
public LimitesAutoPrecision(double posicaoMinima, double posicaoMaxima, double saidaMaxima, double velocidadeMaxima, double aceleracaoMaxima, double correnteMaxima, double temperaturaMaxima, double deslocamentoMaximoEnsaio);
public LimitesAutoPrecision;
```


## PainelAutoPrecision


Implementação interna de publicação NT4 e recursos do painel de ensaios. **Uso interno do módulo.**


Fonte: [PainelAutoPrecision](../99%20-%20Anexos/Fontes/src/main/java/frc/robot/luverlib/autoprecision/PainelAutoPrecision.java)


```java
public void close();
```


## VariaveisAutoPrecision


Repassa a temporização de Constants e mantém a versão interna do protocolo.


Fonte: [VariaveisAutoPrecision](../99%20-%20Anexos/Fontes/src/main/java/frc/robot/luverlib/autoprecision/VariaveisAutoPrecision.java)


Ajustes e valores: [Referencia completa de Constants](../02%20-%20Entenda%20o%20projeto/Referencia%20completa%20de%20Constants.md).

---

← [Página inicial](../00%20-%20Comece%20aqui.md) · [Fontes e versão deste guia](Fontes%20e%20versao.md)
