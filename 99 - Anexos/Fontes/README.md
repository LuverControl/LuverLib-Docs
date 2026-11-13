# LuverLibV1.1

[![CI](https://github.com/LuverControl/LuverLib/actions/workflows/ci.yml/badge.svg)](https://github.com/LuverControl/LuverLib/actions/workflows/ci.yml)
[![CodeQL](https://github.com/LuverControl/LuverLib/actions/workflows/security.yml/badge.svg)](https://github.com/LuverControl/LuverLib/actions/workflows/security.yml)

Biblioteca Java da **LuverControl 10298** para projetos FRC com WPILib. Reúne controle Xbox, motores NEO/SPARK MAX e Kraken/Talon FX, Limelight, telemetria, gravação para AdvantageScope, integração com PathPlanner e **AutoPrecision**, ferramenta para ajustar PID por ensaios e medições.

**Versão:** LuverLibV1.1 · Gradle `1.1.0` · Java 17 · temporada WPILib 2026.

O repositório contém a biblioteca reutilizável em `frc.robot.luverlib` e um exemplo de robô com tração tank. Os ajustes físicos pertencem a cada robô e mecanismo.

## Conteúdo

- [Começar e instalar](#começar-e-instalar)
- [Organização do projeto](#organização-do-projeto)
- [Módulos da biblioteca](#módulos-da-biblioteca)
- [AutoPrecision](#autoprecision)
- [Testes e estado da versão](#testes-e-estado-da-versão)
- [Contribuição e entregas](#contribuição-e-entregas)

## Começar e instalar

### Requisitos

| Componente | Versão usada nesta entrega |
| --- | --- |
| Java | 17 |
| WPILib / GradleRIO | 2026.2.1 |
| REVLib | 2026.0.5 |
| CTRE Phoenix 6 | 26.3.0 |
| PathPlannerLib | 2026.1.2 |
| AdvantageKit | 26.0.2 |
| Python do supervisor AutoPrecision | 3.13 |
| pyntcore | 2026.2.1 |

As dependências de fornecedores estão em [vendordeps](vendordeps/). Use a instalação WPILib 2026 e o Gradle Wrapper incluído. Na primeira execução, o Gradle pode precisar de internet para baixar dependências.

```sh
git clone https://github.com/LuverControl/LuverLib.git
cd LuverLib
./gradlew check build
```

No PowerShell, execute `.\gradlew.bat check build`. Se o terminal não encontrar o Java 17, selecione o JDK da instalação WPILib. Depois que as dependências estiverem em cache, a verificação também pode usar `--offline`.

A versão identificada está na tag [LuverLibV1.1](https://github.com/LuverControl/LuverLib/releases/tag/LuverLibV1.1). Para abrir a simulação de desktop:

```sh
./gradlew simulateJava
```

### Usar como base ou em outro projeto

Você pode usar este checkout como base WPILib Java ou copiar os módulos necessários de [src/main/java/frc/robot/luverlib](src/main/java/frc/robot/luverlib/) para um projeto existente. Nesse segundo caso, leve as dependências correspondentes em `vendordeps/` e mantenha as dependências WPILib/GradleRIO do seu projeto.

Os wrappers de motores, a telemetria e o AutoPrecision têm dependências entre si. Copie o pacote completo `frc.robot.luverlib`, os vendordeps e os grupos de ajuste de `Constants` para o seu projeto. Se já houver uma classe `Constants`, mescle os grupos e adapte os IDs e ajustes ao seu robô. Os arquivos `Robot`, `RobotContainer`, `commands` e `subsystems` são a composição do robô de exemplo; adapte-os ao seu projeto.

Esta versão é distribuída como código-fonte e projeto base. O JAR produzido pelo build inclui a aplicação de exemplo e suas dependências; ele não é uma dependência Maven publicada da LuverLib.

## Organização do projeto

```text
src/main/java/frc/robot/
├── Constants.java                  IDs, PIDs e ajustes rápidos
├── Robot.java                      Ciclo WPILib e CommandScheduler
├── RobotContainer.java             Composição e ligação dos comandos
├── commands/DirigirTank.java        Pilotagem manual
├── subsystems/ChassiTank.java       Tração tank e Pigeon2
└── luverlib/
    ├── controles/                  Xbox, perfis de eixo e ajustes
    ├── motores/neo/                NEO com SPARK MAX
    ├── motores/kraken/             Kraken com Talon FX
    ├── limelight/                  Leituras, visão e validação de pose
    ├── telemetria/                 SmartDashboard e NetworkTables
    ├── advantagescope/             Publicação e gravação
    ├── pathplanner/                Caminhos, autônomos e adaptador tank
    └── autoprecision/              Servidor de ensaios PID
ferramentas/autoprecision/           Supervisor Python e busca de PID
examples/                           Exemplo de integração AutoPrecision
src/test/                           Testes Java e arquivos de referência
vendordeps/                         Dependências dos fabricantes
```

### Onde configurar o robô

| Arquivo/classe | O que configurar |
| --- | --- |
| [Constants](src/main/java/frc/robot/Constants.java) | Ponto central dos IDs de qualquer motor/dispositivo, PIDs e parâmetros de ajuste rápido |
| `Constants.Tracao` | IDs CAN, inversões, correntes, freio, rampas e resposta da pilotagem |
| `Constants.Neo` e `Constants.Kraken` | Ajustes iniciais dos motores de cada família |
| `Constants.Controle` | Portas USB, perfis dos eixos e limiar dos gatilhos |
| `Constants.PathPlanner` | PIDs de translação/rotação, limites e período de controle |
| `Constants.Limelight`, `Constants.AdvantageScope` e `Constants.AutoPrecision` | Filtros de visão, publicação, gravação e temporização |
| Classes `Variaveis...` | Referências a `Constants` e montagem das configurações; edite os valores em `Constants` |
| Classes `Configuracao...` | Recebem os ajustes específicos de cada dispositivo ou integração |
| `RobotContainer` | Instâncias, requisitos dos comandos e associação dos botões |

Os IDs do exemplo seguem a referência DualFire: líderes esquerdo/direito `4/1`, seguidores `2/3` e Pigeon2 `20`. São IDs desse exemplo, não uma escolha automática para outro robô. Ajuste também o número da equipe em [.wpilib/wpilib_preferences.json](.wpilib/wpilib_preferences.json).

`Constants` concentra identificação e ajustes que precisam ficar fáceis de alterar, incluindo os ganhos PID de cada mecanismo quando forem definidos. Qualquer inclusão fora desse escopo exige autorização prévia do dono. Lógica de controle, instanciação de dispositivos e implementação dos módulos ficam nas classes responsáveis.

As constantes de configuração são lidas ao criar os objetos. Alterar `Constants` exige compilar e executar novamente o código. As classes `Variaveis...` mantêm suas chamadas existentes, consultando esses valores centrais. Os valores genéricos são pontos de partida; limites físicos e redução devem corresponder ao mecanismo. A versão interna do protocolo AutoPrecision permanece no módulo, pois não é ajuste do robô.

## Módulos da biblioteca

### Controle Xbox

```java
ControleXbox piloto = new ControleXbox(Constants.Controle.PORTA_PILOTO);
piloto.definirPerfil(ControleXbox.Analogico.ESQUERDO_Y,
    new PerfilEixo(0.08, 2.0, 0.7, true));
piloto.botaoA().onTrue(comandoDoMecanismo);
```

`analogicoEsquerdoX/Y()`, `analogicoDireitoX/Y()` e `valorAnalogico(...)` aplicam zona morta reescalonada, expoente, limite e inversão. `valorGatilhoEsquerdo/Direito()` retorna 0–1 sem a curva dos analógicos; `gatilhoDireito(0.6)` ativa estritamente acima do limiar. Botões, cliques e as oito direções do D-pad retornam `Trigger`; há também a direção `CENTRO`.

`estaConectado()`, `definirVibracao(esquerda, direita)` e `pararVibracao()` completam o uso. Sem controle conectado, eixos e eventos ficam inativos. Crie uma vez e associe comandos fora do periódico. O overload com `EventLoop` próprio exige `poll()`. `obterDispositivo()` expõe leituras brutas que ignoram os perfis. Padrões: USB 0/1, zona morta 0.08, resposta linear e gatilhos 0.5; o sinal Y da WPILib é preservado.

### NEO com SPARK MAX

```java
var ajustes = VariaveisMotor.configuracaoPadrao()
    .comLimiteCorrente(40).comLimiteSaida(0.5);
MotorNeo motor = new MotorNeo(idCan, ajustes);
// Dentro do comando:
motor.definirPotencia(0.25);
// Ao terminar:
motor.parar();
```

`MotorNeo(idCan)` usa o padrão; o overload com configuração aplica os parâmetros uma vez. Aceita CAN 1–62. `definirPotencia(...)` limita a saída e `definirTensao(...)` limita volts pela tensão nominal × limite. Comandos não finitos param o motor e lançam exceção. Posição e RPM representam a saída do mecanismo após a redução; `definirPosicaoEncoder(...)`/`zerarEncoder()` só mudam a referência. Há corrente, temperatura e tensão aplicada.

`new MotorNeo(idSeguidor, configuracaoSeguidor, lider)` cria follower nativo; a inversão é relativa ao líder. O limite do wrapper não limita por si só o vínculo nativo: limite o líder. O record oferece `comLimiteCorrente`, `comInversao`, `comLimiteSaida` e `comCompensacaoTensao`. O overload de nove argumentos habilita compensação; o canônico de dez permite desativá-la. O padrão geral persiste parâmetros na construção.

`obterControlador()` expõe `SparkMax` e permite ignorar os limites do wrapper. `close()` para/libera e é repetível. Crie os dispositivos fora de `periodic()` e mantenha uma instância por CAN. O wrapper básico não oferece API própria de posição/velocidade em malha fechada; AutoPrecision usa integração separada.

### Kraken X60/X44 com Talon FX integrado

```java
var ajustes = VariaveisMotorKraken.configuracaoPadrao(ModeloKraken.X44)
    .comCorrentes(40, 60).comReducao(10).comLimiteSaida(0.5);
MotorKraken motor = new MotorKraken(idCan, ajustes);
```

Há construtores ID, ID/modelo, ID/configuração e ID/`CANBus`/configuração. O padrão usa CAN do roboRIO/modelo X60; aceita IDs 0–62. A seleção X60/X44 também escolhe o modelo simulado, sem identificar ou alterar hardware real. Métodos: `definirPotencia`, `definirTensao`, `parar`, posição, RPM, zeragem e redefinição de encoder; correntes de alimentação/estator separadas, temperatura, tensões aplicada/alimentação e `estaConectado()`.

A configuração oferece `comCorrentes`, `comInversao`, `comFreio`, `comReducao`, `comLimiteSaida` e `comRampas`; os limites são enviados ao Phoenix. Requests são reutilizados, sem exigir FOC/Pro. Leituras inválidas lançam exceção, sem inventar zero. Construção/configuração ficam fora do periódico e o uso é destinado à thread principal. `obterControlador()` expõe `TalonFX`; `close()` para/libera. Freio elétrico não sustenta posição como PID. Não há fachada própria de follower/PID nesse wrapper.

### Limelight, AprilTag e detecção neural

```java
Limelight camera = new Limelight(nomeExatoDaCamera);
VisaoLimelight visao = new VisaoLimelight(camera);
// O scheduler atualiza; o projeto decide como usar o erro.
camera.obterAlvo().ifPresent(alvo -> usarErroHorizontal(alvo.txGraus()));
```

`atualizar()` permite coleta manual e retorna somente poses novas/aceitas. `VisaoLimelight` atualiza uma ou várias câmeras e aceita `Supplier<OrientacaoRobo>` e `Consumer<MedicaoVisao>`. Ordena capturas dentro do ciclo, sem criar estimador nem mover o robô. `obterAlvo()` usa o pacote atômico `t2d`; `AlvoLimelight.estimarDistanciaHorizontalMetros(...)` retorna `OptionalDouble` quando a geometria permite.

Para localização, use `VariaveisLimelight.configuracaoPadrao().comLimitesCampo(new LimitesCampo(comprimentoMetros, larguraMetros, margemMetros))`. **O padrão não tem limites de campo e rejeita poses até configurá-los.** MegaTag2 também exige orientação atualizada: forneça-a ao coordenador ou use `enviarOrientacao(...)` a cada ciclo. `OrientacaoRobo.plana(rotacao, omegaRadianosSegundo)` converte a taxa para graus/s. O overload com somente câmeras não fornece orientação.

`MedicaoVisao.pose2d()`, `instanteCapturaSegundos()` compensado pela latência e `desviosPadrao()` alimentam o estimador do projeto. Poses mantêm origem azul em ambas as alianças. A instância escolhe MegaTag1 ou MegaTag2; neste último o desvio angular é infinito para não reutilizar o yaw dependente do giroscópio. Há filtros de heartbeat/idade, pacote, tags, ambiguidade, orientação, giro, altura, distância e campo. `obterUltimaAvaliacaoPose()` explica rejeições; contadores mostram aceitas/rejeitadas.

`obterDeteccoesNeurais()` e `buscarDeteccaoNeural(classe)` usam `rawdetections`; a busca escolhe a maior área da classe. Cada detecção tem ângulos, área e quatro cantos. Não há score de confiança nesse pacote: o limiar é do pipeline. Não há treinamento/deploy de modelos nem navegação automática até peças.

Configuração da câmera: `selecionarPipeline(0..9)`, `definirLed`, `definirModoImu`, `configurarMontagem`, `definirTagsPermitidas`, `definirTagPrioritaria`, `definirRecorte` e `definirQuadrosIgnorados`. Pipeline aguarda confirmação e mudanças relevantes invalidam dados anteriores. `obterHardware()` traz CPU/RAM/FPS quando disponíveis. Modos internos exigem IMU compatível e inicialização física feita pelo projeto. Feche primeiro `VisaoLimelight`, depois as câmeras; a rede compartilhada permanece aberta.

### Telemetria no SmartDashboard

```java
var dadosPiloto = Telemetria.registrarControle("Xbox", piloto);
var dadosNeo = Telemetria.registrarMotor("Elevador", motorNeo);
var dadosKraken = Telemetria.registrarMotorKraken("Coletor", motorKraken);
var dadosCamera = Telemetria.registrarLimelight("Visao", camera);
```

Registre uma vez; o `TimedRobot` atualiza os `Sendable`. Os campos são somente leitura: eixos/botões, medidas/configuração dos motores e diagnóstico da câmera. Não há comandos de motor ou edição de PID por esse painel. Limelight ainda precisa de coleta por coordenador/`atualizar()`. Kraken indisponível publica `NaN`; NEO delega às leituras do wrapper. Retornos são `AutoCloseable` e não são donos dos dispositivos; encerre telemetria antes de fechar os sensores/motores.

### AdvantageScope e `.wpilog`

```java
AdvantageScope painel = new AdvantageScope();
painel.registrarEstadoRobo();
painel.registrarMotorNeo("Elevador", motorNeo);
painel.registrarPose2d("Tracao/Pose", fornecedorDaPoseReal);
var alvoVisual = painel.criarEstrutura("Planejamento/Alvo", Pose2d.struct);
// Em um callback do planejamento:
alvoVisual.accept(poseAlvo);
```

Padrão: `/LuverLib`, 0.02 s e atualização pelo scheduler, inclusive desabilitado. Sem scheduler, desative `atualizacaoAutomatica` e chame `atualizar()`. Há números com unidade, booleanos, textos, structs/arrays, Pose2d/3d, rotações, `ChassisSpeeds`, motores NEO/Kraken e swerve (vazio ou quatro módulos FL/FR/BL/BR). Velocidades são relativas ao robô; geometria usa metros/radianos e origem azul. `CanalAdvantageScope` implementa `Consumer`.

Diagnósticos por canal: `Valido`, `Falhas`, `UltimaAmostraSegundos` e `UltimoErro`. Amostra inválida preserva a última e marca falha, sem interromper outros canais. Array vazio limpa caminho/alvos; `null` é falha. Struct customizado depende da validação do projeto. Nomes duplicados e `Diagnostico` são reservados.

`GravacaoAdvantageScope.iniciar()` inicia DataLog/NT/DS/joysticks; há overload com diretório/opção de joystick. `anexar(rede, log)` cria captura fechável para recursos já pertencentes ao projeto. Evite duplicar capturas. `.wpilog` permite análise, mas o módulo não implementa replay determinístico, layout/modelo 3D do robô nem exige `LoggedRobot`. Fechar painel/captura não fecha dispositivos ou a rede emprestada.

### PathPlanner: fachada e adaptador tank

`ConexaoPathPlanner` recebe subsistema, leitura/reset da pose, velocidades medidas relativas ao robô, aplicação de saída e parada real. O projeto fornece motores, sensores, odometria e controle de rodas/módulos. `PathPlannerLuver` seleciona controlador holonômico para swerve e LTV para diferencial conforme `RobotConfig`.

1. Crie o drivetrain/callbacks e forneça `RobotConfig` real ou `PathPlannerLuver.carregarConfiguracaoDoProjeto()`.
2. Construa uma única fachada: `AutoBuilder` é global e recusa outra configuração.
3. Registre todos os eventos com `registrarComando(nome, comando)` antes de carregar.
4. Pré-carregue `carregarAutonomo(nome)` e/ou `criarSeletor(nomePainel, autonomoPadrao)` na inicialização.
5. O robô agenda `seletor.obterSelecionado()` no autônomo e cancela no teleop; a biblioteca não agenda movimento sozinha.

Arquivos: `src/main/deploy/pathplanner/settings.json`, `autos/*.auto` e `paths/*.path`. Nomes são sem pasta/extensão. O formato validado é `2025.X`, usado pela PathPlannerLib 2026.1.2. Eventos ausentes, arquivos inválidos e autos Choreo são recusados. O seletor expõe `obterFalhasCarregamento()`, exclui inválidos e usa parada como padrão se não houver padrão explícito.

`criarComandoCaminho` segue sem reset; `criarComandoIrAteCaminho` faz pathfinding até o início; `criarComandoIrAtePose` usa pose absoluta azul; `criarComandoIrAtePoseDaAlianca` transforma o alvo pela aliança; `criarComandoRedefinirPose` redefine odometria; há também parada e aquecimento. Movimento reserva o drivetrain e executa parada real ao terminar/cancelar. Poses mantêm origem azul; velocidades são m/s e rad/s no robô. `DriveFeedforwards` segue FL/FR/BL/BR no swerve, com sinais ajustados pelo projeto ao otimizar módulos.

`TankPathPlanner` converte RPM e cinemática diferencial em tensões kS/kV. `ConfiguracaoTankPathPlanner` recebe largura entre rodas, metros/rotação de encoder, velocidade máxima, kS/kV, tensão e inversões dos encoders. `criarConexao(...)` recebe callbacks de RPM/tensões. Dessatura velocidades e limita tensão; não tem kA, PID próprio de velocidade de roda nem soma forças/correntes do PathPlanner como volts. Encoder já reduzido exige fator coerente, sem dividir a redução novamente. Os padrões são pontos de partida; ganhos holonômicos não afinam o LTV e limites de pathfinding não substituem os dos caminhos exportados.

### Tração tank de exemplo e integrações pendentes

`ChassiTank` cria quatro NEO e Pigeon2 usando `Constants.Tracao`. `mover(frenteTras, giro)` aplica esquerda = frente + giro / direita = frente − giro; `parar()` zera líderes e `obterRotacao()` lê yaw. Entrada não finita para e lança exceção; `close()` libera dispositivos.

Ajustes atuais em `Constants.Tracao`: líderes 40 A/freio, seguidores 80 A/coast, direita invertida e seguidores sem inversão relativa; rampas zero, compensação de tensão desativada e persistência habilitada. São os parâmetros do exemplo DualFire, a revisar se mudar o hardware.

`RobotContainer` registra Xbox e define `DirigirTank` como padrão. A pilotagem usa diferença dos gatilhos ×0.4 e X esquerdo bruto ×0.2, com corte simples 0.05; o giro não passa por `PerfilEixo`. Fora de teleop habilitado, sem controle ou no fim do comando, manda parar. `robotPeriodic()` executa o scheduler e `disabledInit()` cancela comandos/para vibração.

**A composição atual é de pilotagem teleop.** Não conecta PathPlanner, visão, estimador ou logs AdvantageScope e não possui `autonomousInit/teleopInit` de agendamento/cancelamento. O chassi não fornece odometria/distâncias para `ConexaoPathPlanner`. Esta versão não inclui autos ou configuração física prontos em `src/main/deploy/`; crie-os no PathPlanner para o seu robô. As fachadas existem para integração manual no robô real; a existência delas não torna esses recursos automaticamente ativos no exemplo.

## AutoPrecision

AutoPrecision ajusta automaticamente ganhos PID de **posição** para mecanismos com **NEO e SPARK MAX**. A busca procura respostas rápidas, suaves e precisas, comparando erro, ultrapassagem, tempo de estabilização, variação de saída, saturação e esforço. O projeto informa a faixa física e as tolerâncias; a biblioteca não deduz esses valores pelo modelo do motor.

O módulo tem três partes:

| Parte | Responsabilidade |
| --- | --- |
| Java no robô | Gera uma referência trapezoidal, executa a supervisão local e publica telemetria |
| SPARK MAX | Executa o PID interno de posição no slot selecionado |
| CLI Python no PC | Solicita ensaios por NetworkTables 4, busca ganhos e exporta dados e relatórios |

**Uso com um agente no PC:** você informa o mecanismo e o comportamento desejado. Com acesso ao projeto, à CLI e à rede do robô, o agente prepara os ensaios, acompanha a telemetria, escolhe e ajusta P/I/D e conduz as repetições para confirmar precisão, rapidez e suavidade dentro dos limites definidos. O operador mantém a autorização local durante os movimentos. O AutoPrecision fornece as medições, a busca e o transporte; o agente opera essa ferramenta externamente. O PID e a supervisão ficam no robô, independentemente da latência da internet. A biblioteca não inclui um agente de IA embutido.

**Estado da entrega:** validado em simulação Java/REV e Python. Ainda é necessário definir o mecanismo real, sua carga, seu zero, seus limites e uma base inicial de ganhos. Um resultado de simulação não é uma calibração física do robô.

### Instalação no PC

O módulo Java faz parte de `src/main/java/frc/robot/luverlib/autoprecision`. A base usa Java 17, WPILib 2026.2.1 e REVLib 2026.0.5. O projeto do robô deve manter essas dependências e o wrapper `MotorNeo` da LuverLib.

No PC, use Python **3.13**. A partir da raiz do repositório, no Linux/macOS:

```sh
cd ferramentas/autoprecision
python3.13 -m venv .venv
source .venv/bin/activate
python -m pip install -r requirements.txt
python -c 'import ntcore; print("NT4 disponível")'
```

O arquivo `requirements.txt` fixa `pyntcore==2026.2.1`. No Windows, crie o ambiente com `py -3.13 -m venv .venv` e ative com `.venv\Scripts\Activate.ps1` no PowerShell. O supervisor Python roda no PC; o código do robô continua sendo Java.

### Integração com o mecanismo

O [exemplo completo e compilado](examples/ExemploAutoPrecisionNeo.java) recebe o motor, o subsistema e a configuração reais; também mostra a aplicação dos ganhos revisados na operação normal.

Use o `MotorNeo` já pertencente ao subsistema. O backend requer **motor líder**, encoder primário e posição sem wrapping; recusa um SPARK configurado como seguidor. Não zera o encoder. Os fatores de conversão precisam corresponder à configuração do `MotorNeo`.

Declare os parâmetros do mecanismo no projeto. Este trecho mostra a ordem real dos construtores; os nomes representam valores definidos para a sua mecânica:

```java
var limites = new LimitesAutoPrecision(
    posicaoMinima, posicaoMaxima, dutyMaximo,
    velocidadeDoPerfil, aceleracaoDoPerfil,
    correnteMaxima, temperaturaMaxima,
    deslocamentoMaximoPorEnsaio, velocidadeMaximaMedida);

var configuracao = new ConfiguracaoAutoPrecision(
    "rotacoes_saida", limites,
    ganhosIniciais, ganhosMaximos, // instâncias de GanhosPID(p, i, d)
    toleranciaPosicao, toleranciaVelocidade, tempoEstavelSegundos,
    duracaoMaximaSegundos, 0.02, 0.5,
    zonaIntegral, acumuladorIntegralMaximo);
```

`dutyMaximo` deve caber no limite de saída do `MotorNeo`. A posição e os alvos devem permanecer estritamente dentro da faixa. `deslocamentoMaximoPorEnsaio` limita a distância até o alvo e a excursão realmente medida. Velocidade e aceleração do perfil determinam a referência suave; `velocidadeMaximaMedida` é um corte físico independente e deve ser maior ou igual à velocidade do perfil. Existe também um construtor de oito argumentos, que usa a velocidade do perfil como corte da velocidade medida.

Associe o comando a um botão de autorização mantido pressionado, por exemplo no `RobotContainer`. Adapte `joystick`, `mecanismo`, `motor` e `configuracao` aos objetos existentes:

```java
import com.revrobotics.spark.ClosedLoopSlot;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj2.command.button.JoystickButton;
import frc.robot.luverlib.autoprecision.AutoPrecision;
import frc.robot.luverlib.autoprecision.ControleNeoAutoPrecision;

var autorizacao = new JoystickButton(joystick, 7);
var autoPrecision = new AutoPrecision(
    "Mecanismo", mecanismo,
    new ControleNeoAutoPrecision(motor, ClosedLoopSlot.kSlot0),
    configuracao, autorizacao::getAsBoolean);

autorizacao.and(DriverStation::isTestEnabled)
    .whileTrue(autoPrecision.criarComando());
```

Mantenha o `CommandScheduler.run()` no ciclo normal do robô. O comando reserva o subsistema real durante a sessão. Para armar, habilite **Test** na Driver Station e mantenha a autorização local pressionada. Soltar o botão ou sair de Test encerra o controle. Para uma nova sessão depois de conclusão, erro ou saída da CLI, solte e pressione novamente o botão.

### Unidades e configuração do controlador

Neste backend, a posição usa rotações da **saída do mecanismo**, considerando a redução configurada no `MotorNeo`. Velocidade medida, tolerância de velocidade e velocidade do perfil usam rotações por segundo; aceleração usa rotações por segundo ao quadrado. Tempos e durações da API e da CLI usam segundos.

Os ganhos SPARK usam a base nativa em **milissegundos**. Para uma unidade de posição `u`:

| Ganho | Unidade nativa SPARK |
| --- | --- |
| P | duty / u |
| I | duty / (u × ms) |
| D | duty × ms / u |

O simulador Python usa ganhos em **segundos** e outro modelo de mecanismo. Seus ganhos não devem ser copiados para o SPARK ou para um `PIDController` WPILib. Consulte as [unidades oficiais da REV](https://docs.revrobotics.com/revlib/spark/closed-loop/units).

O backend se identifica como `sparkmax-posicao-duty-sem-compensacao-slotN`. Durante os ensaios, desabilita temporariamente a compensação de tensão para respeitar o limite de duty real mesmo com bateria abaixo da tensão nominal. Preserva o feedforward fixo preexistente no slot; não faz sua identificação automática.

Ganhos aprovados só são reproduzíveis com o mesmo backend, slot, redução, unidades, feedforward, rampas, limites e política de compensação usados nos ensaios. Para manter essa política no projeto, passe a cópia retornada por `configuracaoDoMotor.comCompensacaoTensao(false)` à criação do `MotorNeo`; o método não modifica um motor já criado. Mudar essas condições exige nova validação.

### Rede do robô e internet pelo celular

O PC pode manter Wi-Fi ou Ethernet na rede do robô e usar o compartilhamento **USB** do celular para internet. Preserve a rota para o IP do roboRIO pela interface do robô e deixe a rota de internet usar o celular. Não é necessário dar internet ao robô ou alterar sua sub-rede.

A CLI aceita `--host IP_DO_ROBORIO` ou `--equipe NUMERO_DA_EQUIPE`. O NT4 usa a porta padrão **5810**; com `--host`, `--porta` permite indicar outra porta. O argumento `--nome` precisa coincidir com o nome registrado no Java, como `Mecanismo`. A conexão direta da CLI com o robô independe da conexão do agente com a internet. Consulte a [configuração de rede FRC](https://docs.wpilib.org/en/stable/docs/networking/networking-introduction/ip-configurations.html).

### Executar

Os comandos abaixo partem de `ferramentas/autoprecision`, com o ambiente Python ativado. Primeiro, experimente a simulação numérica, que não conecta ao robô:

```sh
python autoprecision.py simular --duracao 4 --avaliacoes 18 --confirmacoes 2
```

Para observar o robô sem tomar posse, armar ou enviar heartbeat:

```sh
python autoprecision.py monitorar --host IP_DO_ROBORIO --nome Mecanismo --segundos 15
```

Depois de configurar o mecanismo real e armar localmente, use um ensaio isolado para coletar uma resposta, ou uma otimização para comparar candidatos. Os alvos abaixo são **exemplos de sintaxe**: substitua-os por posições verificadas para o seu mecanismo.

```sh
python autoprecision.py ensaio --host IP_DO_ROBORIO --nome Mecanismo --alvos=0.10 --duracao 4
python autoprecision.py otimizar --host IP_DO_ROBORIO --nome Mecanismo --alvos=-0.20,0.20 --duracao 4 --avaliacoes 18 --confirmacoes 2
```

Use `--alvos=...` com o sinal de igual quando houver números negativos. Omitir `--ganhos` usa a base declarada no robô; `--ganhos=P,I,D` permite informar outra base dentro dos máximos. A preparação de cada avaliação continua usando os ganhos iniciais fixos registrados no robô. `--saida CAMINHO` escolhe uma pasta nova para os relatórios.

Para otimizar, informe pelo menos dois alvos distintos, separados por pelo menos quatro tolerâncias de posição e com margem abaixo do deslocamento máximo. A base inicial precisa retornar ao primeiro alvo e permanecer estável pelo tempo declarado; se não conseguir, a sessão termina inconclusiva. `--duracao` vale para cada movimento e precisa caber na duração máxima do robô.

No SmartDashboard, acompanhe `/SmartDashboard/AutoPrecision/Mecanismo`: `Estado`, `Mensagem`, `Posicao`, `Alvo`, `Referencia`, `Erro`, `P`, `I`, `D`, `Saida`, `Velocidade`, `Corrente`, `Temperatura`, `Ensaio`, `Tempo` e `Progresso`. `CONCLUIDO` indica um movimento encerrado com saída parada; não equivale à aprovação de um PID.

### Supervisão e orçamento dos ensaios

A autorização em Test permanece obrigatória durante toda a sessão. O protocolo atribui a sessão a um único supervisor, usa IDs crescentes e verifica heartbeat. Perda de supervisão, medição inválida, mudança de modo, violação de limites ou erro do controlador encerra o ensaio. O cliente rejeita coleta incompleta, dados de outra sessão ou amostras incompatíveis com o PID/alvo solicitado; não reenvia automaticamente pedidos sem confirmação.

Uma avaliação com dois alvos contém **três movimentos**: preparação no primeiro alvo, ida ao segundo e retorno ao primeiro. `--avaliacoes 18` inclui a base, a busca e as confirmações, permitindo até **54 movimentos**. Com duração de 4 s, são até **216 s de movimento programado**, além da configuração CAN e da comunicação. Para N alvos, conte N+1 movimentos por avaliação; a busca pode terminar antes do orçamento.

`--confirmacoes 2` reserva quatro avaliações: duas da base e duas do candidato, alternando a ordem. O mínimo aceito é `2 × confirmacoes + 3` avaliações. A busca é limitada pelos ganhos máximos do projeto e não promete o ótimo global.

### Resultados e incorporação dos ganhos

A aprovação exige melhora relativa confirmada e estabilização em **todos os alvos de todas as confirmações do candidato**, respeitando tolerâncias de posição e velocidade pelo tempo estável configurado.

Os arquivos ficam por padrão em `~/Downloads/luverlib-AAAA-MM-DD/autoprecision/sessoes/HORARIO/`:

| Arquivo | Conteúdo |
| --- | --- |
| `configuracao.json` | Limites, unidade e backend da sessão |
| `ensaio-NNNN.csv` e `.json` | Amostras e métricas de cada movimento, incluindo a preparação |
| `relatorio.html` | Gráfico do último ensaio, histórico e veredito |
| `melhor-pid.json` | Candidato, histórico, aprovação e `ganhos_aprovados` |
| `falha.json` | Motivo de falha, quando houver |

Uma pontuação menor ou o campo `candidato` isoladamente não representam aprovação. Use apenas `ganhos_aprovados` quando `aceitacao.aprovado` for `true`; resultados inconclusivos mantêm esse campo como `null`. A otimização retorna código 0 quando aprovada, 2 quando inconclusiva e 1 em caso de erro. Um `ensaio` isolado coleta métricas e não aprova ganhos de otimização.

Ao sair, o supervisor envia `PARAR` e espera até 5 s por `DESARMADO`. A CLI só finaliza o resultado aprovado após confirmar o encerramento; perda de conexão ou falha de restauração produz erro e impede essa aprovação. `Ctrl+C` também solicita encerramento. Uma nova execução de `ensaio` ou `otimizar` exige uma nova armação local.

Ao desarmar, o backend para a saída e restaura os parâmetros temporários do slot, os soft limits e a habilitação/tensão nominal da compensação. As alterações do AutoPrecision são feitas em **RAM, sem gravar flash**. Ele não fecha o motor nem incorpora os ganhos automaticamente ao código do robô. Os P/I/D exibidos após o desarme são os do último ensaio; o controlador teve seus parâmetros originais restaurados quando o encerramento foi confirmado.

### Validação

O repositório inclui testes Java de estados, autorização, heartbeat, replay, limites, parada e restauração; testes do backend com a implementação nativa REV em simulação; e testes Python de métricas e otimização. As verificações desta versão são simuladas, incluindo bateria abaixo da nominal e ruído na resposta. Ainda não houve validação com o mecanismo físico definido pelo usuário.

Para executar as suítes existentes, a partir da raiz do repositório:

```sh
./gradlew test --tests '*autoprecision*'
python3.13 -m unittest discover -s ferramentas/autoprecision -p 'test_*.py'
```

A aprovação em ensaios e testes simulados precisa ser seguida de validação física nas condições de uso do mecanismo.

## Testes e estado da versão

Verificação de publicação em 13/11/2026, na base usada para esta versão:

| Verificação | Resultado |
| --- | --- |
| `./gradlew check build --offline` | Build concluído; **440 testes Java** passaram, sem falhas, erros ou testes pulados |
| Servidor AutoPrecision dentro da suíte Java | 66 testes |
| Backend NEO/REV do AutoPrecision dentro da suíte Java | 21 testes |
| Otimizador Python incluído no repositório | **23 testes** passaram |
| `examples/ExemploAutoPrecisionNeo.java` | Compilou com Java 17 e as dependências desta versão |

As verificações adicionais do desenvolvimento do AutoPrecision incluíram CLI, exportação de relatórios e processos Java/Python separados por NT4: ensaio normal, aplicação de ganhos sem movimento, Ctrl+C, processo morto e processo congelado. Essas integrações foram verificadas em bancada de software; os números não constituem calibração do hardware físico.

Para repetir as suítes incluídas, a partir da raiz:

```sh
./gradlew check build
ferramentas/autoprecision/.venv/bin/python -m unittest discover -s ferramentas/autoprecision -p 'test_*.py' -v
```

No Windows, use `.\gradlew.bat` e `ferramentas\autoprecision\.venv\Scripts\python.exe`.

O robô tank fornecido executa pilotagem manual. Para usar caminhos, visão e AutoPrecision em um robô, ligue explicitamente os respectivos módulos aos subsistemas e sensores reais. O AutoPrecision desta versão fornece backend de **posição para NEO/SPARK MAX**; ajuste de velocidade, backend Kraken e identificação automática de feedforward não fazem parte desta entrega.

## Contribuição e entregas

O código da equipe usa a licença [BSD 3-Clause](LICENSE). Bibliotecas dos
fabricantes e WPILib mantêm suas próprias licenças.
Veja [como contribuir](CONTRIBUTING.md) e a
[política de segurança da organização](https://github.com/LuverControl/.github/blob/main/SECURITY.md).

Cada pull request executa Java 17 em Linux e Windows, os testes Python 3.13,
a compilação de `examples/` e a validação dos workflows. Relatórios e JARs
ficam disponíveis nos artefatos da execução por 14 dias. O check
`CI completo` reúne essas verificações. CodeQL analisa Java e Python em
pull requests, na `main` e semanalmente. Dependabot propõe atualizações de
Actions, Gradle e Python; atualizações anuais de GradleRIO/pyntcore exigem
migração coordenada dos vendordeps. O grafo de dependências Gradle é enviado
ao GitHub após integrações na `main`.

Para validar a entrega, abra **Actions → Release → Run workflow** na `main`.
Essa execução repete os testes e gera os pacotes sem publicar uma versão.
Para publicar uma nova versão:

1. Atualize `version` em `build.gradle`, os requisitos e as notas no README
   em um pull request. Integre após as verificações.
2. No commit integrado da `main`, crie e envie uma tag `vMAJOR.MINOR.PATCH`
   idêntica à versão Gradle, por exemplo `v1.1.1` para `1.1.1`.
3. Acompanhe o workflow **Release**: ele exige que o commit pertença à `main`,
   repete o build/testes e publica os pacotes com atestação de origem.

As tags antigas, incluindo `LuverLibV1.1`, continuam disponíveis. O padrão
`vMAJOR.MINOR.PATCH` vale para as próximas entregas automatizadas.

| Arquivo de release | Uso |
| --- | --- |
| `LuverLib-VERSAO-source.zip` | Projeto-fonte completo versionado |
| `luverlib-example-VERSAO.jar` | Aplicação de exemplo com suas dependências; não é uma biblioteca Maven |
| `autoprecision-VERSAO.zip` | Supervisor Python para PC e instruções |
| `build-info.json` | Versão, commit e ferramentas usados |
| `SHA256SUMS` | Integridade dos arquivos entregues |
| `LICENSE` | Licença BSD 3-Clause do código da equipe |

Para conferir os downloads em Linux, rode `sha256sum --check SHA256SUMS` na
pasta com todos os arquivos. Para conferir a origem de um pacote, use
`gh attestation verify ARQUIVO --repo LuverControl/LuverLib`.

A automação entrega arquivos no GitHub. O envio ao roboRIO permanece local,
com a rede, o número da equipe e a autorização do operador. Testes de
simulação não comprovam calibração ou comportamento em hardware real.

## Referências

- [WPILib](https://docs.wpilib.org/en/stable/)
- [Controle em malha fechada REV](https://docs.revrobotics.com/revlib/spark/closed-loop)
- [Unidades de PID REV](https://docs.revrobotics.com/revlib/spark/closed-loop/units)
- [Phoenix 6](https://v6.docs.ctr-electronics.com/)
- [PathPlanner](https://pathplanner.dev/)
- [AdvantageScope](https://docs.advantagescope.org/)
