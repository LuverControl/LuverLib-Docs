---
title: "API — AdvantageScope"
tags:
  - luverlib
  - api
revisado: 2026-11-13
---

# API — AdvantageScope

Leia primeiro [AdvantageScope](../03%20-%20Modulos/AdvantageScope.md). Esta referência lista as classes e suas assinaturas públicas declaradas no código consultado. Corpos de métodos foram omitidos. Records também geram acessores com os nomes de seus componentes.


## AdvantageScope


Registra e publica canais NT4 tipados, com atualização automática opcional.


Fonte: [AdvantageScope](../99%20-%20Anexos/Fontes/src/main/java/frc/robot/luverlib/advantagescope/AdvantageScope.java)


```java
public AdvantageScope();
public AdvantageScope(ConfiguracaoAdvantageScope configuracao);
public AdvantageScope(NetworkTableInstance rede, ConfiguracaoAdvantageScope configuracao);
public CanalAdvantageScope<Double> registrarNumero( String nome, String unidade, DoubleSupplier fonte);
public CanalAdvantageScope<Boolean> registrarBooleano(String nome, BooleanSupplier fonte);
public CanalAdvantageScope<String> registrarTexto(String nome, Supplier<String> fonte);
public <T> CanalAdvantageScope<T> criarEstrutura(String nome, Struct<T> tipo);
public <T> CanalAdvantageScope<T[]> criarEstruturas(String nome, Struct<T> tipo);
public <T> CanalAdvantageScope<T> registrarEstrutura( String nome, Struct<T> tipo, Supplier<T> fonte);
public <T> CanalAdvantageScope<T[]> registrarEstruturas( String nome, Struct<T> tipo, Supplier<T[]> fonte);
public CanalAdvantageScope<Pose2d> registrarPose2d(String nome, Supplier<Pose2d> fonte);
public CanalAdvantageScope<Pose3d> registrarPose3d(String nome, Supplier<Pose3d> fonte);
public CanalAdvantageScope<Pose2d[]> registrarPoses2d(String nome, Supplier<Pose2d[]> fonte);
public CanalAdvantageScope<Pose3d[]> registrarPoses3d(String nome, Supplier<Pose3d[]> fonte);
public CanalAdvantageScope<Rotation2d> registrarRotacao(String nome, Supplier<Rotation2d> fonte);
public CanalAdvantageScope<ChassisSpeeds> registrarVelocidades( String nome, Supplier<ChassisSpeeds> fonte);
public CanalAdvantageScope<SwerveModuleState[]> registrarSwerve( String nome, Supplier<SwerveModuleState[]> fonte);
public void registrarMotorNeo(String nome, MotorNeo motor);
public void registrarMotorKraken(String nome, MotorKraken motor);
public void registrarEstadoRobo();
public void periodic();
public void atualizar();
public String obterRaizTabela();
public void close();
```


## CanalAdvantageScope


Representa um canal publicável, sua validade e quantidade de falhas.


Fonte: [CanalAdvantageScope](../99%20-%20Anexos/Fontes/src/main/java/frc/robot/luverlib/advantagescope/CanalAdvantageScope.java)


```java
public synchronized void accept(T valor);
public synchronized boolean estaValido();
public synchronized long obterQuantidadeFalhas();
```


## ConfiguracaoAdvantageScope


Define raiz da tabela, intervalo e atualização automática.


Fonte: [ConfiguracaoAdvantageScope](../99%20-%20Anexos/Fontes/src/main/java/frc/robot/luverlib/advantagescope/ConfiguracaoAdvantageScope.java)


```java
public record ConfiguracaoAdvantageScope( String raizTabela, double intervaloSegundos, boolean atualizacaoAutomatica);
public ConfiguracaoAdvantageScope;
public ConfiguracaoAdvantageScope comRaiz(String raiz);
public ConfiguracaoAdvantageScope comAtualizacaoAutomatica(boolean automatica);
```


## GravacaoAdvantageScope


Inicia logging global WPILib ou anexa captura a um DataLog existente.


Fonte: [GravacaoAdvantageScope](../99%20-%20Anexos/Fontes/src/main/java/frc/robot/luverlib/advantagescope/GravacaoAdvantageScope.java)


```java
public static DataLog iniciar();
public static DataLog iniciar(String diretorio, boolean gravarJoysticks);
public static GravacaoAdvantageScope anexar(NetworkTableInstance rede, DataLog log);
public void close();
```


## VariaveisAdvantageScope


Repassa Constants.AdvantageScope e monta a configuração de publicação.


Fonte: [VariaveisAdvantageScope](../99%20-%20Anexos/Fontes/src/main/java/frc/robot/luverlib/advantagescope/VariaveisAdvantageScope.java)


Ajustes e valores: [Referencia completa de Constants](../02%20-%20Entenda%20o%20projeto/Referencia%20completa%20de%20Constants.md).


```java
public static ConfiguracaoAdvantageScope configuracaoPadrao();
```

---

← [Página inicial](../00%20-%20Comece%20aqui.md) · [Fontes e versão deste guia](Fontes%20e%20versao.md)
