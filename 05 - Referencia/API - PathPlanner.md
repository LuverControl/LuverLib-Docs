---
title: "API — PathPlanner"
tags:
  - luverlib
  - api
revisado: 2026-11-13
---

# API — PathPlanner

Leia primeiro [PathPlanner](../03%20-%20Modulos/PathPlanner.md). Esta referência lista as classes e suas assinaturas públicas declaradas no código consultado. Corpos de métodos foram omitidos. Records também geram acessores com os nomes de seus componentes.


## ArquivosPathPlanner


Implementação interna de nomes, formatos e validação de arquivos de caminho/auto. **Uso interno do módulo.**


Fonte: [ArquivosPathPlanner](../99%20-%20Anexos/Fontes/src/main/java/frc/robot/luverlib/pathplanner/ArquivosPathPlanner.java)


## ConexaoPathPlanner


Recebe o subsistema e callbacks de pose, velocidades, saída e parada reais.


Fonte: [ConexaoPathPlanner](../99%20-%20Anexos/Fontes/src/main/java/frc/robot/luverlib/pathplanner/ConexaoPathPlanner.java)


```java
public record ConexaoPathPlanner( Subsystem subsistema, Supplier<Pose2d> obterPoseCampo, Consumer<Pose2d> redefinirPoseCampo, Supplier<ChassisSpeeds> obterVelocidadesRelativasRobo, BiConsumer<ChassisSpeeds, DriveFeedforwards> aplicarSaida, Runnable parar);
public ConexaoPathPlanner;
```


## ConfiguracaoPathPlanner


Agrupa PID holonômico, limites de pathfinding e período do controle.


Fonte: [ConfiguracaoPathPlanner](../99%20-%20Anexos/Fontes/src/main/java/frc/robot/luverlib/pathplanner/ConfiguracaoPathPlanner.java)


```java
public record ConfiguracaoPathPlanner( PIDConstants pidTranslacao, PIDConstants pidRotacao, PathConstraints limitesPathfinding, double periodoControleSegundos);
public ConfiguracaoPathPlanner;
```


## ConfiguracaoTankPathPlanner


Agrupa a calibração diferencial de geometria, encoder, feedforward e sinais.


Fonte: [ConfiguracaoTankPathPlanner](../99%20-%20Anexos/Fontes/src/main/java/frc/robot/luverlib/pathplanner/ConfiguracaoTankPathPlanner.java)


```java
public record ConfiguracaoTankPathPlanner( double larguraEntreRodasMetros, double metrosPorRotacaoEncoder, double velocidadeMaximaRodaMetrosPorSegundo, double ksVolts, double kvVoltsPorMetroPorSegundo, double tensaoMaximaVolts, boolean inverterEncoderEsquerdo, boolean inverterEncoderDireito);
public ConfiguracaoTankPathPlanner;
```


## PathPlannerLuver


Configura uma única integração global, registra eventos e constrói comandos/seletor.


Fonte: [PathPlannerLuver](../99%20-%20Anexos/Fontes/src/main/java/frc/robot/luverlib/pathplanner/PathPlannerLuver.java)


```java
public PathPlannerLuver(ConexaoPathPlanner conexao, RobotConfig robo);
public PathPlannerLuver(ConexaoPathPlanner conexao, RobotConfig robo, ConfiguracaoPathPlanner ajustes, BooleanSupplier inverterParaVermelho);
public static RobotConfig carregarConfiguracaoDoProjeto();
public static boolean estaNaAliancaVermelha();
public void registrarComando(String nome, Command comando);
public Command carregarAutonomo(String nome);
public Command criarComandoCaminho(String nome);
public Command criarComandoIrAteCaminho(String nome);
public Command criarComandoIrAtePose(Pose2d alvoCampo);
public Command criarComandoIrAtePoseDaAlianca(Pose2d alvoAzul);
public Command criarComandoRedefinirPose(Pose2d poseAzul);
public SeletorAutonomo criarSeletor(String nomePainel);
public SeletorAutonomo criarSeletor(String nomePainel, String autonomoPadrao);
public Command criarComandoAquecimento();
public Command criarComandoParar();
public ConfiguracaoPathPlanner obterConfiguracao();
```


## SeletorAutonomo


Publica o chooser, entrega o comando selecionado e expõe falhas de carregamento.


Fonte: [SeletorAutonomo](../99%20-%20Anexos/Fontes/src/main/java/frc/robot/luverlib/pathplanner/SeletorAutonomo.java)


```java
public Command obterSelecionado();
public Map<String, String> obterFalhasCarregamento();
public SendableChooser<Command> obterSeletor();
public void close();
```


## TankPathPlanner


Converte RPM/cinemática do tank em velocidades e tensões limitadas.


Fonte: [TankPathPlanner](../99%20-%20Anexos/Fontes/src/main/java/frc/robot/luverlib/pathplanner/TankPathPlanner.java)


```java
public TankPathPlanner(ConfiguracaoTankPathPlanner ajustes);
public DifferentialDriveWheelSpeeds obterVelocidadesRodas(double rpmEsquerda, double rpmDireita);
public ChassisSpeeds obterVelocidadesRelativasRobo(double rpmEsquerda, double rpmDireita);
public Tensoes calcularTensoes(ChassisSpeeds velocidades);
public ConexaoPathPlanner criarConexao(Subsystem subsistema, Supplier<Pose2d> obterPose, Consumer<Pose2d> redefinirPose, DoubleSupplier rpmEsquerda, DoubleSupplier rpmDireita, BiConsumer<Double, Double> aplicarTensoes, Runnable parar);
public ConfiguracaoTankPathPlanner obterConfiguracao();
```


## VariaveisPathPlanner


Monta ConfiguracaoPathPlanner com os valores centrais de Constants.PathPlanner.


Fonte: [VariaveisPathPlanner](../99%20-%20Anexos/Fontes/src/main/java/frc/robot/luverlib/pathplanner/VariaveisPathPlanner.java)


Ajustes e valores: [Referencia completa de Constants](../02%20-%20Entenda%20o%20projeto/Referencia%20completa%20de%20Constants.md).


```java
public static ConfiguracaoPathPlanner configuracaoPadrao();
```

---

← [Página inicial](../00%20-%20Comece%20aqui.md) · [Fontes e versão deste guia](Fontes%20e%20versao.md)
