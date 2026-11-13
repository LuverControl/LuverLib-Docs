---
title: "Conceitos essenciais"
tags:
  - luverlib
  - guia
revisado: 2026-11-13
---

# Conceitos essenciais

## Peças físicas e comunicação

| Termo | Explicação simples | Exemplo na LuverLib |
| --- | --- | --- |
| roboRIO | Computador que executa o programa Java do robô | Executa `Robot` |
| Controlador de motor | Eletrônica que recebe comandos e alimenta o motor | SPARK MAX ou Talon FX |
| CAN | Rede usada por diversos dispositivos do robô | Motores e Pigeon2 |
| ID CAN | Endereço do dispositivo naquele barramento | `Constants.Tracao.LIDER_ESQUERDO` |
| Encoder | Sensor que mede rotação/posição | Encoder primário do NEO |
| Giroscópio | Sensor de orientação e velocidade angular | Pigeon2 |
| Driver Station | Aplicação do operador que habilita modos e mostra o estado do robô | Teleop, Autonomous, Test e Disabled |

Escrever um ID no Java informa com qual dispositivo conversar. Isso não reprograma o endereço físico: o dispositivo precisa estar configurado com o mesmo ID e não pode conflitar no barramento.

## Organização do programa

Uma **classe** descreve um tipo de objeto; `new` cria uma instância. Um **método** é uma ação ou consulta, como `parar()` ou `obterVelocidadeRPM()`. `public static final` costuma identificar um valor de configuração compartilhado. `record` agrupa dados, como uma configuração ou uma medição.

Um **subsistema** representa uma parte do robô. Um **comando** usa esse subsistema para realizar uma tarefa. O **CommandScheduler** decide quais comandos executar; declarar o requisito do subsistema evita duas tarefas disputando o mesmo mecanismo. Um **Trigger** é uma condição, como um botão pressionado, que pode agendar um comando.

Uma função como `motor::obterVelocidadeRPM` é um fornecedor: a biblioteca chama a função quando precisa ler o valor. Isso permite conectar o robô sem a biblioteca conhecer o seu subsistema.

## Controle e medição

**Malha aberta** manda uma potência ou tensão sem corrigir automaticamente um erro de posição. **Malha fechada** usa uma medição para aproximar o mecanismo de um alvo. O **PID** calcula correções com três ganhos: P reage ao erro atual; I acumula erro ao longo do tempo; D reage à mudança do erro.

Um **perfil de movimento** limita como a referência muda, por velocidade e aceleração. **Feedforward** estima a saída necessária pelo modelo do mecanismo; não é o mesmo que PID. Uma **pose** representa posição e orientação. A **odometria** estima o deslocamento a partir dos sensores.

**Telemetria** é publicar o que o robô mede ou faz. **NetworkTables/NT4** é o transporte de dados usado pelos painéis. Um **log** grava esses dados para consulta posterior.

Consulte [Glossario](../05%20-%20Referencia/Glossario.md) e [Unidades e sinais](../05%20-%20Referencia/Unidades%20e%20sinais.md) durante a leitura dos módulos.

---

← [Página inicial](../00%20-%20Comece%20aqui.md) · [Fontes e versão deste guia](../05%20-%20Referencia/Fontes%20e%20versao.md)
