---
title: "Configuração AutoPrecision campo a campo"
tags:
  - luverlib
  - guia
revisado: 2026-11-13
---

# Configuração AutoPrecision campo a campo

## Como montar

Primeiro construa `LimitesAutoPrecision`. Depois use esse objeto em `ConfiguracaoAutoPrecision`, junto com `GanhosPID` iniciais e máximos. Os valores definidos pelo robô devem vir de seu grupo em `Constants`.

## LimitesAutoPrecision — ordem de nove argumentos

| Campo | O que significa |
| --- | --- |
| `posicaoMinima` | Menor referência física permitida |
| `posicaoMaxima` | Maior referência física permitida; deve ser maior que a mínima |
| `saidaMaxima` | Duty máximo, maior que zero e até 1; deve caber no limite do MotorNeo |
| `velocidadeMaxima` | Velocidade da referência perfilada, em unidade de posição/s |
| `aceleracaoMaxima` | Aceleração da referência, em unidade de posição/s² |
| `correnteMaxima` | Corte de corrente medida, em A |
| `temperaturaMaxima` | Corte de temperatura, em °C |
| `deslocamentoMaximoEnsaio` | Limite da distância ao alvo e da excursão realmente medida |
| `velocidadeMaximaMedida` | Corte de velocidade física, independente do perfil; não pode ser menor que a velocidade do perfil |

O construtor de oito argumentos usa `velocidadeMaxima` também como corte medido. A excursão máxima precisa caber na faixa física. Alvos e posições dos ensaios precisam respeitar as condições de faixa exigidas pelo backend.

## ConfiguracaoAutoPrecision — ordem de doze argumentos

| Campo | O que significa |
| --- | --- |
| `unidade` | Texto que identifica a unidade real, por exemplo rotações da saída |
| `limites` | Objeto físico descrito acima |
| `ganhosIniciais` | Base P/I/D já funcional, usada também na preparação |
| `ganhosMaximos` | Limites superiores da busca de P/I/D |
| `toleranciaPosicao` | Erro de posição aceito para estabilização |
| `toleranciaVelocidade` | Velocidade máxima aceita para considerar o mecanismo estável |
| `tempoEstavelSegundos` | Tempo contínuo que precisa permanecer dentro das tolerâncias |
| `duracaoMaximaEnsaioSegundos` | Prazo máximo de um movimento |
| `periodoSegundos` | Período real de atualização |
| `timeoutHeartbeatSegundos` | Prazo sem supervisão que encerra a sessão |
| `zonaIntegral` | Faixa de erro em que a ação integral é permitida |
| `acumuladorIntegralMaximo` | Limite do acumulador integral |

## Validações da configuração

Os ganhos devem ser finitos, não negativos e caber em float; os iniciais precisam estar dentro dos máximos. Tolerâncias e tempos devem ser positivos. O período aceito é de 0.005 a 0.1 s. O heartbeat deve ser de pelo menos três períodos e até 2 s. A duração máxima é 30 s; o tempo estável deve ser menor que a duração. Duas tolerâncias de posição devem ser menores que a faixa física.

Além dessas validações estruturais, o mecanismo precisa ter uma base capaz de estabilizar e limites adequados à sua mecânica. A ferramenta mede e compara; ela não descobre sozinha posição zero, faixa física ou limites apropriados.

**Uso completo:** [Ajustar PID com AutoPrecision](../04%20-%20Tutoriais/Ajustar%20PID%20com%20AutoPrecision.md) · [API - AutoPrecision](API%20-%20AutoPrecision.md).

---

← [Página inicial](../00%20-%20Comece%20aqui.md) · [Fontes e versão deste guia](Fontes%20e%20versao.md)
