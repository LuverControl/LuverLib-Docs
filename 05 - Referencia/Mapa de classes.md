---
title: "Mapa de classes"
tags:
  - luverlib
  - referencia
revisado: 2026-11-13
---

# Mapa de classes

Esta tabela cobre todos os arquivos Java de produção da base consultada. Comece pelas notas dos módulos para aprender o uso; abra a API quando precisar confirmar um construtor ou método.

| Classe | Para que serve | Consulta |
| --- | --- | --- |
| `ControleXbox` | Fornece leituras, perfis e triggers dos controles Xbox. | [API](API%20-%20Controles.md#controlexbox) |
| `PerfilEixo` | Define e aplica zona morta, curva, limite e inversão de um analógico. | [API](API%20-%20Controles.md#perfileixo) |
| `VariaveisControle` | Mantém referências de compatibilidade aos ajustes em Constants.Controle e Constants.Tracao. | [API](API%20-%20Controles.md#variaveiscontrole) |
| `ConfiguracaoMotorNeo` | Agrupa os parâmetros imutáveis de um NEO; métodos com... retornam cópias. | [API](API%20-%20Motores%20NEO.md#configuracaomotorneo) |
| `MotorNeo` | Configura e comanda um NEO/SPARK MAX, incluindo follow nativo opcional. | [API](API%20-%20Motores%20NEO.md#motorneo) |
| `VariaveisMotor` | Repassa os ajustes de Constants.Neo/Tracao e monta configurações NEO. | [API](API%20-%20Motores%20NEO.md#variaveismotor) |
| `ConfiguracaoMotorKraken` | Agrupa os limites e parâmetros do Talon e gera sua configuração Phoenix. | [API](API%20-%20Motores%20Kraken.md#configuracaomotorkraken) |
| `ModeloKraken` | Identifica as variantes X60 e X44 aceitas pelo wrapper. | [API](API%20-%20Motores%20Kraken.md#modelokraken) |
| `MotorKraken` | Configura e comanda Kraken/Talon FX e lê medições válidas. | [API](API%20-%20Motores%20Kraken.md#motorkraken) |
| `VariaveisMotorKraken` | Repassa Constants.Kraken e monta as configurações iniciais. | [API](API%20-%20Motores%20Kraken.md#variaveismotorkraken) |
| `AlvoLimelight` | Representa o alvo principal, com dados extraídos da câmera. | [API](API%20-%20Limelight.md#alvolimelight) |
| `AvaliacaoPose` | Relaciona uma medição aceita ou o motivo rastreável da rejeição. | [API](API%20-%20Limelight.md#avaliacaopose) |
| `ConfiguracaoLimelight` | Define política de qualidade, método e limites de campo por câmera. | [API](API%20-%20Limelight.md#configuracaolimelight) |
| `DecodificadorLimelight` | Implementação interna que interpreta pacotes e avalia medições; use a fachada Limelight. | [API](API%20-%20Limelight.md#decodificadorlimelight) |
| `DeteccaoNeural` | Representa uma detecção do pipeline neural. | [API](API%20-%20Limelight.md#deteccaoneural) |
| `Limelight` | Recebe alvos, IA e pose pela NT e aplica os critérios de qualidade. | [API](API%20-%20Limelight.md#limelight) |
| `LimitesCampo` | Define comprimento, largura e margem da região válida em metros. | [API](API%20-%20Limelight.md#limitescampo) |
| `MarcadorAprilTag` | Agrupa os dados de uma tag incluída na medição. | [API](API%20-%20Limelight.md#marcadorapriltag) |
| `MedicaoVisao` | Agrupa pose, horário de captura, tags e incerteza para o estimador. | [API](API%20-%20Limelight.md#medicaovisao) |
| `MetodoPose` | Seleciona o método MegaTag1 ou MegaTag2. | [API](API%20-%20Limelight.md#metodopose) |
| `OrientacaoRobo` | Agrupa ângulos e taxas do gyro e oferece conversão para orientação plana. | [API](API%20-%20Limelight.md#orientacaorobo) |
| `PosicaoCamera` | Descreve montagem física em frente/direita/cima e roll/pitch/yaw. | [API](API%20-%20Limelight.md#posicaocamera) |
| `VariaveisLimelight` | Monta a configuração inicial de visão com Constants.Limelight. | [API](API%20-%20Limelight.md#variaveislimelight) |
| `VisaoLimelight` | Coordena orientação, coleta e entrega ordenada das medições de uma ou mais câmeras. | [API](API%20-%20Limelight.md#visaolimelight) |
| `Telemetria` | Registra uma vez os Sendable de controle, motores e câmera no SmartDashboard. | [API](API%20-%20Telemetria.md#telemetria) |
| `TelemetriaLimelight` | Expõe conexão, alvos e diagnóstico da avaliação de pose. | [API](API%20-%20Telemetria.md#telemetrialimelight) |
| `TelemetriaMotorKraken` | Expõe medidas do Kraken e indica indisponibilidade sem inventar zero. | [API](API%20-%20Telemetria.md#telemetriamotorkraken) |
| `TelemetriaMotorNeo` | Expõe medidas e configuração do NEO sem comandar o motor. | [API](API%20-%20Telemetria.md#telemetriamotorneo) |
| `TelemetriaXbox` | Expõe leituras do controle em campos de diagnóstico. | [API](API%20-%20Telemetria.md#telemetriaxbox) |
| `AdvantageScope` | Registra e publica canais NT4 tipados, com atualização automática opcional. | [API](API%20-%20AdvantageScope.md#advantagescope) |
| `CanalAdvantageScope` | Representa um canal publicável, sua validade e quantidade de falhas. | [API](API%20-%20AdvantageScope.md#canaladvantagescope) |
| `ConfiguracaoAdvantageScope` | Define raiz da tabela, intervalo e atualização automática. | [API](API%20-%20AdvantageScope.md#configuracaoadvantagescope) |
| `GravacaoAdvantageScope` | Inicia logging global WPILib ou anexa captura a um DataLog existente. | [API](API%20-%20AdvantageScope.md#gravacaoadvantagescope) |
| `VariaveisAdvantageScope` | Repassa Constants.AdvantageScope e monta a configuração de publicação. | [API](API%20-%20AdvantageScope.md#variaveisadvantagescope) |
| `ArquivosPathPlanner` | Implementação interna de nomes, formatos e validação de arquivos de caminho/auto. | [API](API%20-%20PathPlanner.md#arquivospathplanner) |
| `ConexaoPathPlanner` | Recebe o subsistema e callbacks de pose, velocidades, saída e parada reais. | [API](API%20-%20PathPlanner.md#conexaopathplanner) |
| `ConfiguracaoPathPlanner` | Agrupa PID holonômico, limites de pathfinding e período do controle. | [API](API%20-%20PathPlanner.md#configuracaopathplanner) |
| `ConfiguracaoTankPathPlanner` | Agrupa a calibração diferencial de geometria, encoder, feedforward e sinais. | [API](API%20-%20PathPlanner.md#configuracaotankpathplanner) |
| `PathPlannerLuver` | Configura uma única integração global, registra eventos e constrói comandos/seletor. | [API](API%20-%20PathPlanner.md#pathplannerluver) |
| `SeletorAutonomo` | Publica o chooser, entrega o comando selecionado e expõe falhas de carregamento. | [API](API%20-%20PathPlanner.md#seletorautonomo) |
| `TankPathPlanner` | Converte RPM/cinemática do tank em velocidades e tensões limitadas. | [API](API%20-%20PathPlanner.md#tankpathplanner) |
| `VariaveisPathPlanner` | Monta ConfiguracaoPathPlanner com os valores centrais de Constants.PathPlanner. | [API](API%20-%20PathPlanner.md#variaveispathplanner) |
| `AutoPrecision` | Gerencia estado da sessão, comando, autorização e supervisão dos ensaios. | [API](API%20-%20AutoPrecision.md#autoprecision) |
| `ConfiguracaoAutoPrecision` | Agrupa ganhos, limites, tolerâncias e temporização de cada mecanismo. | [API](API%20-%20AutoPrecision.md#configuracaoautoprecision) |
| `ControleAutoPrecision` | Contrato do backend para medição, ganho, referência e encerramento. | [API](API%20-%20AutoPrecision.md#controleautoprecision) |
| `ControleNeoAutoPrecision` | Backend de posição do SPARK MAX com restauração dos ajustes temporários. | [API](API%20-%20AutoPrecision.md#controleneoautoprecision) |
| `GanhosPID` | Agrupa P/I/D e valida ganhos finitos, não negativos e dentro dos limites. | [API](API%20-%20AutoPrecision.md#ganhospid) |
| `LimitesAutoPrecision` | Define faixa física, saída, perfil e cortes de medição do ensaio. | [API](API%20-%20AutoPrecision.md#limitesautoprecision) |
| `PainelAutoPrecision` | Implementação interna de publicação NT4 e recursos do painel de ensaios. | [API](API%20-%20AutoPrecision.md#painelautoprecision) |
| `VariaveisAutoPrecision` | Repassa a temporização de Constants e mantém a versão interna do protocolo. | [API](API%20-%20AutoPrecision.md#variaveisautoprecision) |
| `Constants` | Centraliza IDs, PIDs e ajustes de acesso rápido; consulte a referência campo a campo. | [API](API%20-%20Composicao.md#constants) |
| `Main` | Ponto de entrada que inicia Robot pela WPILib. | [API](API%20-%20Composicao.md#main) |
| `Robot` | Executa o ciclo WPILib, scheduler e cancelamento em Disabled. | [API](API%20-%20Composicao.md#robot) |
| `RobotContainer` | Cria dispositivos da composição e conecta comandos e controles. | [API](API%20-%20Composicao.md#robotcontainer) |
| `DirigirTank` | Comando padrão que lê gatilhos/X bruto e pilota em teleop habilitado. | [API](API%20-%20Composicao.md#dirigirtank) |
| `ChassiTank` | Possui os dispositivos da tração DualFire e aplica a mistura esquerda/direita. | [API](API%20-%20Composicao.md#chassitank) |

Os arquivos Python são descritos em [AutoPrecision](../03%20-%20Modulos/AutoPrecision.md) e no [tutorial da CLI](../04%20-%20Tutoriais/Ajustar%20PID%20com%20AutoPrecision.md).

---

← [Página inicial](../00%20-%20Comece%20aqui.md) · [Fontes e versão deste guia](Fontes%20e%20versao.md)
