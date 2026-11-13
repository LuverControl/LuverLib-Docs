---
title: "Erros comuns"
tags:
  - luverlib
  - guia
revisado: 2026-11-13
---

# Erros comuns

## Build e configuração

| Sintoma | O que conferir | Onde entender |
| --- | --- | --- |
| Gradle falha antes de compilar | JDK ativo, wrapper e dependências do projeto | [Instalar e compilar](../01%20-%20Primeiros%20passos/Instalar%20e%20compilar.md) |
| `Constants.Controle` não existe | Sua cópia pode ser anterior à centralização local | [Fontes e versao](Fontes%20e%20versao.md) |
| Alterei o número e nada mudou | Edite o projeto real; recompile e atualize sua execução | [Constants - onde configurar](../02%20-%20Entenda%20o%20projeto/Constants%20-%20onde%20configurar.md) |
| Deploy não está disponível | Este checkout não define target roboRIO; use projeto de destino configurado | [Integrar em outro robo](../01%20-%20Primeiros%20passos/Integrar%20em%20outro%20robo.md) |

## Controle e motores

| Sintoma | O que conferir |
| --- | --- |
| Xbox não responde | Porta USB da Driver Station, conexão, modo habilitado e scheduler |
| Perfil do X mudou, mas o giro não | `DirigirTank` usa X bruto; ajuste as escalas/corte em `Constants.Tracao` |
| NEO não foi criado | ID entre 1 e 62, presença do dispositivo e sucesso da configuração REV |
| Lado da tração gira invertido | Definição de frente, inversão elétrica e relação líder/seguidor |
| Posição está com escala errada | Redução e se o encoder já foi convertido para a saída |
| Seguidor ignora o limite do wrapper | O follow é nativo; limite o comando no líder |
| Freio não segura posição | Freio elétrico não oferece sozinho correção de erro como PID |
| Kraken aparece com `NaN` no painel | Medição indisponível; confira conexão/estado do Talon |

## Visão — motivo da avaliação

| Motivo | Interpretação e próxima conferência |
| --- | --- |
| `CAMPO_NAO_CONFIGURADO` | Forneça `comLimitesCampo(...)` com dimensões reais |
| `ORIENTACAO_AUSENTE` / `ORIENTACAO_ANTIGA` | MegaTag2 precisa de gyro atual no coordenador/ciclo |
| `CAMERA_DESCONECTADA` | Confira nome da tabela, rede e câmera |
| `MEDICAO_ANTIGA` / `HORARIO_INVALIDO` | Confira tempo de captura e validade dos dados |
| `AGUARDANDO_PIPELINE` | A troca de pipeline ainda não foi confirmada |
| `SEM_TAGS` / `PACOTE_INVALIDO` | Confira pipeline e dados publicados |
| `FORA_DO_CAMPO` / `ALTURA_INVALIDA` | Confira montagem, pose e limites configurados |
| `AMBIGUIDADE_ALTA` / `DISTANCIA_EXCESSIVA` / `GIRO_RAPIDO` | A medição não cumpriu o critério de qualidade; investigue a condição antes de relaxar o filtro |

## Autônomo e AutoPrecision

| Sintoma | O que conferir |
| --- | --- |
| `AutoBuilder já configurado` | Há duas configurações globais; mantenha um único responsável |
| Evento não encontrado | Registre todos os comandos antes de carregar arquivos e confira os nomes |
| Caminho/auto não carrega | Pasta exportada, formato aceito e `obterFalhasCarregamento()` |
| Auto selecionado não se move | Seleção precisa ser agendada em `autonomousInit()`; pose e saídas precisam ser reais |
| AutoPrecision não arma | Test habilitado, botão local mantido, motor líder, encoder e configuração válidos |
| Nova tentativa não inicia | Solte e pressione novamente a autorização para nova sessão |
| Sessão encerra por heartbeat | Supervisão/conexão não manteve o prazo configurado |
| Melhor nota, mas resultado inconclusivo | Confira confirmação e estabilização em todos os alvos |
| PID desaparece após desarmar | O backend restaura parâmetros temporários; incorporar ganhos é uma etapa explícita |

Registre a mensagem completa, os ajustes e a condição em [Como registrar os testes](../06%20-%20Caderno%20da%20equipe/Como%20registrar%20os%20testes.md). Não substitua uma falha de medição por zero para esconder o problema.

---

← [Página inicial](../00%20-%20Comece%20aqui.md) · [Fontes e versão deste guia](Fontes%20e%20versao.md)
