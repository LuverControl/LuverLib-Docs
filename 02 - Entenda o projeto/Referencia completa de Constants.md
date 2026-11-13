---
title: "Referência completa de Constants"
tags:
  - luverlib
  - referencia
revisado: 2026-11-13
---

# Referência completa de Constants

Cada linha abaixo foi extraída de `Constants.java` da base consultada. O valor é o estado do exemplo em 13/11/2026, não uma calibração recomendada para outro mecanismo. Para alterar, siga [Constants - onde configurar](Constants%20-%20onde%20configurar.md).

## Constants.Tracao

| Campo | Valor atual | O que significa |
| --- | --- | --- |
| `LIDER_ESQUERDO` | `4` | ID CAN do NEO líder esquerdo da tração. |
| `SEGUIDOR_ESQUERDO` | `2` | ID CAN do NEO que segue o líder esquerdo. |
| `LIDER_DIREITO` | `1` | ID CAN do NEO líder direito da tração. |
| `SEGUIDOR_DIREITO` | `3` | ID CAN do NEO que segue o líder direito. |
| `PIGEON` | `20` | ID CAN do giroscópio Pigeon2. |
| `INVERTER_ESQUERDA` | `false` | Inversão elétrica do líder esquerdo. |
| `INVERTER_DIREITA` | `true` | Inversão elétrica do líder direito. |
| `INVERTER_SEGUIDORES` | `false` | Inversão dos seguidores em relação a seus líderes. |
| `CORRENTE_LIDERES_AMPERES` | `40` | Limite inteligente de corrente dos líderes, em A. |
| `FREAR_LIDERES` | `true` | true usa freio elétrico nos líderes; false usa giro livre. |
| `CORRENTE_SEGUIDORES_AMPERES` | `80` | Limite inteligente de corrente dos seguidores, em A. |
| `FREAR_SEGUIDORES` | `false` | true usa freio elétrico nos seguidores; false usa giro livre. |
| `TENSAO_NOMINAL_VOLTS` | `12.0` | Tensão de referência, em V; na tração, a compensação depende de COMPENSAR_TENSAO. |
| `RAMPA_MALHA_ABERTA_SEGUNDOS` | `0.0` | Tempo da rampa de potência/tensão, em s; zero desativa. |
| `RAMPA_MALHA_FECHADA_SEGUNDOS` | `0.0` | Tempo da rampa de controle fechado, em s; zero desativa. |
| `ROTACOES_MOTOR_POR_ROTACAO_SAIDA` | `1.0` | Relação da redução: voltas do motor para uma volta do mecanismo. |
| `LIMITE_POTENCIA` | `1.0` | Maior módulo de potência solicitado pelo wrapper, em fração de 0 a 1. |
| `COMPENSAR_TENSAO` | `false` | Habilita compensação de tensão; false mantém a resposta percentual sem ela. |
| `GRAVAR_CONFIGURACAO` | `true` | Escolhe persistência da configuração do SPARK na inicialização. |
| `ESCALA_FRENTE` | `0.4` | Multiplicador da diferença entre os gatilhos para frente/trás. |
| `ESCALA_GIRO` | `0.2` | Multiplicador do X esquerdo bruto para giro. |
| `ZONA_MORTA` | `0.05` | Corte simples das entradas da pilotagem; não reescala o restante do curso. |

## Constants.Neo

| Campo | Valor atual | O que significa |
| --- | --- | --- |
| `LIMITE_CORRENTE_AMPERES` | `40` | Limite inteligente de corrente inicial do NEO, em A. |
| `INVERTER_SENTIDO` | `false` | Troca o sentido elétrico de rotação quando true. |
| `FREAR_AO_PARAR` | `true` | true usa freio elétrico; não oferece sustentação de posição por PID. |
| `TENSAO_NOMINAL_VOLTS` | `12.0` | Tensão de referência, em V; na tração, a compensação depende de COMPENSAR_TENSAO. |
| `RAMPA_MALHA_ABERTA_SEGUNDOS` | `0.2` | Tempo da rampa de potência/tensão, em s; zero desativa. |
| `RAMPA_MALHA_FECHADA_SEGUNDOS` | `0.0` | Tempo da rampa de controle fechado, em s; zero desativa. |
| `ROTACOES_MOTOR_POR_ROTACAO_SAIDA` | `1.0` | Relação da redução: voltas do motor para uma volta do mecanismo. |
| `LIMITE_SAIDA` | `1.0` | Limite dos comandos do wrapper, como fração de 0 a 1. |
| `GRAVAR_CONFIGURACAO` | `true` | Escolhe persistência da configuração do SPARK na inicialização. |

## Constants.Kraken

| Campo | Valor atual | O que significa |
| --- | --- | --- |
| `MODELO_PADRAO` | `ModeloKraken.X60` | Variante de Kraken adotada pelo construtor padrão; deve corresponder ao hardware. |
| `LIMITE_ALIMENTACAO_AMPERES` | `40` | Limite de corrente da alimentação/bateria do Talon, em A. |
| `LIMITE_ESTATOR_AMPERES` | `60` | Limite de corrente no motor/estator do Talon, em A. |
| `INVERTER_SENTIDO` | `false` | Troca o sentido elétrico de rotação quando true. |
| `FREAR_AO_PARAR` | `true` | true usa freio elétrico; não oferece sustentação de posição por PID. |
| `TENSAO_MAXIMA_VOLTS` | `12` | Limite de tensão solicitado ao Talon FX, em V. |
| `RAMPA_MALHA_ABERTA_SEGUNDOS` | `0.2` | Tempo da rampa de potência/tensão, em s; zero desativa. |
| `RAMPA_MALHA_FECHADA_SEGUNDOS` | `0` | Tempo da rampa de controle fechado, em s; zero desativa. |
| `ROTACOES_MOTOR_POR_ROTACAO_SAIDA` | `1` | Relação da redução: voltas do motor para uma volta do mecanismo. |
| `LIMITE_SAIDA` | `1` | Limite dos comandos do wrapper, como fração de 0 a 1. |
| `TIMEOUT_CONFIGURACAO_SEGUNDOS` | `0.25` | Prazo por operação de configuração/comunicação, em s. |
| `TENTATIVAS_CONFIGURACAO` | `3` | Quantidade de tentativas da operação de configuração. |
| `FREQUENCIA_ENCODER_HZ` | `50` | Taxa de atualização de posição/velocidade, em Hz. |
| `FREQUENCIA_TELEMETRIA_HZ` | `10` | Taxa de atualização dos sinais de diagnóstico, em Hz. |

## Constants.Controle

| Campo | Valor atual | O que significa |
| --- | --- | --- |
| `PORTA_PILOTO` | `0` | Posição USB do controle do piloto na Driver Station; entre 0 e 5. |
| `PORTA_OPERADOR` | `1` | Posição USB reservada ao operador; o exemplo não instancia esse controle. |
| `ANALOGICO_ESQUERDO_X` | `new PerfilEixo(0.08, 1.0, 1.0, false)` | Perfil do X esquerdo: zona morta, expoente, limite e inversão; não afeta o X bruto de DirigirTank. |
| `ANALOGICO_ESQUERDO_Y` | `new PerfilEixo(0.08, 1.0, 1.0, false)` | Perfil do Y esquerdo: zona morta, expoente, limite e inversão. |
| `ANALOGICO_DIREITO_X` | `new PerfilEixo(0.08, 1.0, 1.0, false)` | Perfil do X direito: zona morta, expoente, limite e inversão. |
| `ANALOGICO_DIREITO_Y` | `new PerfilEixo(0.08, 1.0, 1.0, false)` | Perfil do Y direito: zona morta, expoente, limite e inversão. |
| `LIMIAR_GATILHOS` | `0.5` | Fração de 0 a 1 que deve ser ultrapassada para ativar o Trigger do gatilho. |

## Constants.PathPlanner

| Campo | Valor atual | O que significa |
| --- | --- | --- |
| `PID_TRANSLACAO` | `new PIDConstants(5.0, 0.0, 0.0)` | P/I/D de posição X/Y do controlador holonômico; não afina o LTV do tank. |
| `PID_ROTACAO` | `new PIDConstants(5.0, 0.0, 0.0)` | P/I/D de orientação do controlador holonômico; não afina o LTV do tank. |
| `VELOCIDADE_MAXIMA_METROS_POR_SEGUNDO` | `2.0` | Limite linear do pathfinding, em m/s. |
| `ACELERACAO_MAXIMA_METROS_POR_SEGUNDO_QUADRADO` | `2.0` | Limite de aceleração linear do pathfinding, em m/s². |
| `VELOCIDADE_ANGULAR_MAXIMA_GRAUS_POR_SEGUNDO` | `360.0` | Limite angular do pathfinding, em graus/s; convertido na configuração. |
| `ACELERACAO_ANGULAR_MAXIMA_GRAUS_POR_SEGUNDO_QUADRADO` | `540.0` | Limite de aceleração angular do pathfinding, em graus/s². |
| `TENSAO_NOMINAL_VOLTS` | `12.0` | Tensão de referência, em V; na tração, a compensação depende de COMPENSAR_TENSAO. |
| `PERIODO_CONTROLE_SEGUNDOS` | `0.02` | Intervalo do controlador; deve corresponder ao ciclo real, em s. |

## Constants.Limelight

| Campo | Valor atual | O que significa |
| --- | --- | --- |
| `METODO_POSE` | `MetodoPose.MEGATAG_2` | Método usado para interpretar a pose: MegaTag1 ou MegaTag2. |
| `IDADE_MAXIMA_QUADRO_SEGUNDOS` | `0.25` | Maior idade de uma medição de câmera aceita, em s. |
| `IDADE_MAXIMA_ORIENTACAO_SEGUNDOS` | `0.10` | Maior idade da orientação usada por MegaTag2, em s. |
| `GIRO_MAXIMO_GRAUS_SEGUNDO` | `360.0` | Giro máximo para aceitar a medição, em graus/s. |
| `AMBIGUIDADE_MAXIMA_TAG_UNICA` | `0.30` | Ambiguidade máxima admitida quando há uma única tag. |
| `ERRO_Z_MAXIMO_METROS` | `0.75` | Altura absoluta máxima admitida para a pose do robô, em m. |
| `DISTANCIA_MAXIMA_TAGS_METROS` | `8.0` | Distância média máxima das tags da medição, em m. |
| `DESVIO_LINEAR_BASE_METROS` | `0.02` | Base/piso da incerteza linear enviada ao estimador, em m. |
| `DESVIO_ANGULAR_BASE_RADIANOS` | `0.06` | Base/piso da incerteza angular, em rad. |
| `FATOR_CONFIANCA` | `1.0` | Multiplica o cálculo dos desvios; maior incerteza reduz o peso da medição no estimador. |
| `FATOR_LINEAR_MEGATAG2` | `0.5` | Multiplicador específico do desvio linear de MegaTag2. |

## Constants.AdvantageScope

| Campo | Valor atual | O que significa |
| --- | --- | --- |
| `RAIZ_TABELA` | `"LuverLib"` | Nome da raiz NetworkTables dos canais publicados. |
| `INTERVALO_SEGUNDOS` | `0.02` | Intervalo de publicação desejado, em s. |
| `ATUALIZACAO_AUTOMATICA` | `true` | Registra o publicador no scheduler quando true. |
| `GRAVAR_JOYSTICKS` | `true` | Inclui dados dos controles no log iniciado pelo módulo. |

## Constants.AutoPrecision

| Campo | Valor atual | O que significa |
| --- | --- | --- |
| `PERIODO_SEGUNDOS` | `0.02` | Temporização inicial do AutoPrecision, em s; a configuração real é explícita. |
| `TIMEOUT_HEARTBEAT_SEGUNDOS` | `0.5` | Prazo inicial sem heartbeat da supervisão, em s. |


São **77 campos** nesta base. Os ganhos físicos de um mecanismo AutoPrecision ainda precisam ser definidos pelo projeto; eles não aparecem como valores prontos nesta tabela.

Uma configuração específica passada ao construtor pode substituir os padrões. O número da equipe fica em `.wpilib/wpilib_preferences.json`; a versão de protocolo e as versões das dependências são metadados internos, não ajustes operacionais desta tabela.

---

← [Página inicial](../00%20-%20Comece%20aqui.md) · [Fontes e versão deste guia](../05%20-%20Referencia/Fontes%20e%20versao.md)
