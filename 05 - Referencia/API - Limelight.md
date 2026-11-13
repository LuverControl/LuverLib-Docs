---
title: "API — Limelight"
tags:
  - luverlib
  - api
revisado: 2026-11-13
---

# API — Limelight

Leia primeiro [Limelight](../03%20-%20Modulos/Limelight.md). Esta referência lista as classes e suas assinaturas públicas declaradas no código consultado. Corpos de métodos foram omitidos. Records também geram acessores com os nomes de seus componentes.


## AlvoLimelight


Representa o alvo principal, com dados extraídos da câmera.


Fonte: [AlvoLimelight](../99%20-%20Anexos/Fontes/src/main/java/frc/robot/luverlib/limelight/AlvoLimelight.java)


```java
public record AlvoLimelight(double txGraus, double tyGraus, double areaPercentual, int idAprilTag, int classeDetector, int classeClassificador, double instanteCapturaSegundos);
public OptionalDouble estimarDistanciaHorizontalMetros( double alturaCameraMetros, double alturaAlvoMetros, double inclinacaoParaCimaGraus);
```


## AvaliacaoPose


Relaciona uma medição aceita ou o motivo rastreável da rejeição.


Fonte: [AvaliacaoPose](../99%20-%20Anexos/Fontes/src/main/java/frc/robot/luverlib/limelight/AvaliacaoPose.java)


```java
public record AvaliacaoPose(Motivo motivo, Optional<MedicaoVisao> medicao);
public AvaliacaoPose;
public static AvaliacaoPose rejeitada(Motivo motivo);
public static AvaliacaoPose aceita(MedicaoVisao medicao);
```


## ConfiguracaoLimelight


Define política de qualidade, método e limites de campo por câmera.


Fonte: [ConfiguracaoLimelight](../99%20-%20Anexos/Fontes/src/main/java/frc/robot/luverlib/limelight/ConfiguracaoLimelight.java)


```java
public record ConfiguracaoLimelight( MetodoPose metodoPose, double idadeMaximaQuadroSegundos, double idadeMaximaOrientacaoSegundos, double giroMaximoGrausSegundo, double ambiguidadeMaximaTagUnica, double erroZMaximoMetros, double distanciaMaximaTagsMetros, double desvioLinearBaseMetros, double desvioAngularBaseRadianos, double fatorConfianca, double fatorLinearMegaTag2, Optional<LimitesCampo> limitesCampo);
public ConfiguracaoLimelight;
public ConfiguracaoLimelight comMetodo(MetodoPose metodo);
public ConfiguracaoLimelight comLimitesCampo(LimitesCampo campo);
public ConfiguracaoLimelight comFatorConfianca(double fator);
```


## DecodificadorLimelight


Implementação interna que interpreta pacotes e avalia medições; use a fachada Limelight. **Uso interno do módulo.**


Fonte: [DecodificadorLimelight](../99%20-%20Anexos/Fontes/src/main/java/frc/robot/luverlib/limelight/DecodificadorLimelight.java)


## DeteccaoNeural


Representa uma detecção do pipeline neural.


Fonte: [DeteccaoNeural](../99%20-%20Anexos/Fontes/src/main/java/frc/robot/luverlib/limelight/DeteccaoNeural.java)


```java
public record DeteccaoNeural(int classe, double txGraus, double tyGraus, double areaPercentual, List<Translation2d> cantosPixels);
public DeteccaoNeural;
```


## Limelight


Recebe alvos, IA e pose pela NT e aplica os critérios de qualidade.


Fonte: [Limelight](../99%20-%20Anexos/Fontes/src/main/java/frc/robot/luverlib/limelight/Limelight.java)


```java
public Limelight(String nomeTabela);
public Limelight(String nomeTabela, ConfiguracaoLimelight configuracao);
public Limelight(String nomeTabela, ConfiguracaoLimelight configuracao, NetworkTableInstance rede, DoubleSupplier relogio);
public List<MedicaoVisao> atualizar();
public boolean estaConectada();
public Optional<AlvoLimelight> obterAlvo();
public List<DeteccaoNeural> obterDeteccoesNeurais();
public Optional<DeteccaoNeural> buscarDeteccaoNeural(int classe);
public AvaliacaoPose obterUltimaAvaliacaoPose();
public Optional<List<Double>> obterHardware();
public String obterClasseDetectorPrimaria();
public String obterNome();
public ConfiguracaoLimelight obterConfiguracao();
public long obterQuantidadeAceitas();
public long obterQuantidadeRejeitadas();
public boolean pacoteNeuralValido();
public int obterPipelineAtivo();
public void enviarOrientacao(OrientacaoRobo orientacao);
public void selecionarPipeline(int indice);
public void definirLed(ModoLed modo);
public void definirModoImu(ModoImu modo);
public void configurarMontagem(PosicaoCamera posicao);
public void definirTagsPermitidas(Set<Integer> ids);
public void definirTagPrioritaria(int id);
public void definirRecorte(double xMin, double xMax, double yMin, double yMax);
public void definirQuadrosIgnorados(int quantidade);
public void close();
```


## LimitesCampo


Define comprimento, largura e margem da região válida em metros.


Fonte: [LimitesCampo](../99%20-%20Anexos/Fontes/src/main/java/frc/robot/luverlib/limelight/LimitesCampo.java)


```java
public record LimitesCampo(double comprimentoMetros, double larguraMetros, double margemMetros);
public LimitesCampo;
public boolean contem(double x, double y);
```


## MarcadorAprilTag


Agrupa os dados de uma tag incluída na medição.


Fonte: [MarcadorAprilTag](../99%20-%20Anexos/Fontes/src/main/java/frc/robot/luverlib/limelight/MarcadorAprilTag.java)


```java
public record MarcadorAprilTag(int id, double txGraus, double tyGraus, double areaPercentual, double distanciaCameraMetros, double distanciaRoboMetros, double ambiguidade);
```


## MedicaoVisao


Agrupa pose, horário de captura, tags e incerteza para o estimador.


Fonte: [MedicaoVisao](../99%20-%20Anexos/Fontes/src/main/java/frc/robot/luverlib/limelight/MedicaoVisao.java)


```java
public record MedicaoVisao(String camera, MetodoPose metodo, Pose3d pose, double instanteCapturaSegundos, double latenciaSegundos, double distanciaMediaTagsMetros, List<MarcadorAprilTag> tags, double desvioLinearMetros, double desvioAngularRadianos);
public MedicaoVisao;
public Pose2d pose2d();
public Matrix<N3, N1> desviosPadrao();
```


## MetodoPose


Seleciona o método MegaTag1 ou MegaTag2.


Fonte: [MetodoPose](../99%20-%20Anexos/Fontes/src/main/java/frc/robot/luverlib/limelight/MetodoPose.java)


```java
public String topico();
```


## OrientacaoRobo


Agrupa ângulos e taxas do gyro e oferece conversão para orientação plana.


Fonte: [OrientacaoRobo](../99%20-%20Anexos/Fontes/src/main/java/frc/robot/luverlib/limelight/OrientacaoRobo.java)


```java
public record OrientacaoRobo(double yawGraus, double velocidadeYawGrausSegundo, double pitchGraus, double velocidadePitchGrausSegundo, double rollGraus, double velocidadeRollGrausSegundo);
public OrientacaoRobo;
public static OrientacaoRobo plana(Rotation2d rotacao, double velocidadeRadianosSegundo);
```


## PosicaoCamera


Descreve montagem física em frente/direita/cima e roll/pitch/yaw.


Fonte: [PosicaoCamera](../99%20-%20Anexos/Fontes/src/main/java/frc/robot/luverlib/limelight/PosicaoCamera.java)


```java
public record PosicaoCamera(double frenteMetros, double direitaMetros, double cimaMetros, double rollGraus, double pitchGraus, double yawGraus);
public PosicaoCamera;
```


## VariaveisLimelight


Monta a configuração inicial de visão com Constants.Limelight.


Fonte: [VariaveisLimelight](../99%20-%20Anexos/Fontes/src/main/java/frc/robot/luverlib/limelight/VariaveisLimelight.java)


Ajustes e valores: [Referencia completa de Constants](../02%20-%20Entenda%20o%20projeto/Referencia%20completa%20de%20Constants.md).


```java
public static ConfiguracaoLimelight configuracaoPadrao();
```


## VisaoLimelight


Coordena orientação, coleta e entrega ordenada das medições de uma ou mais câmeras.


Fonte: [VisaoLimelight](../99%20-%20Anexos/Fontes/src/main/java/frc/robot/luverlib/limelight/VisaoLimelight.java)


```java
public VisaoLimelight(Limelight... cameras);
public VisaoLimelight(Supplier<OrientacaoRobo> fornecedorOrientacao, Consumer<MedicaoVisao> consumidor, Limelight... cameras);
public void periodic();
public List<MedicaoVisao> obterMedicoesUltimoCiclo();
public void close();
```

---

← [Página inicial](../00%20-%20Comece%20aqui.md) · [Fontes e versão deste guia](Fontes%20e%20versao.md)
