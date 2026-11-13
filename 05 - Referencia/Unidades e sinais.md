---
title: "Unidades e sinais"
tags:
  - luverlib
  - guia
revisado: 2026-11-13
---

# Unidades e sinais

Um número só faz sentido junto de sua unidade e de seu referencial. Use esta página antes de copiar uma medição ou ganho entre módulos.

| Informação | Unidade ou convenção nesta base |
| --- | --- |
| Potência/duty | Fração de -1 a 1 |
| Limite de saída | Fração de 0 a 1; AutoPrecision exige valor positivo |
| Corrente | Amperes, A |
| Tensão | Volts, V |
| Temperatura | Graus Celsius, °C |
| Posição dos wrappers de motor | Rotações da saída, depois da redução configurada |
| Velocidade dos wrappers | Rotações da saída por minuto, RPM |
| Velocidade do AutoPrecision NEO | Rotações da saída por segundo |
| Aceleração do perfil AutoPrecision | Rotações da saída por segundo ao quadrado |
| Tempos da API/CLI | Segundos |
| Poses de campo | Metros e orientação, com origem azul fixa |
| `ChassisSpeeds` | m/s e rad/s, relativos ao robô |
| `OrientacaoRobo` | Ângulos em graus e taxas em graus/s |
| `PosicaoCamera` | Frente/direita/cima em m; roll/pitch/yaw em graus |
| `Constants.PathPlanner` angular | Graus/s e graus/s²; a montagem converte para radianos |

## Conversões frequentes

`rotaçõesPorSegundo = RPM / 60`. Para RPM bruta do motor e roda diretamente ligada à saída da redução, `metrosPorRotacao = PI × diametroDaRoda / reducao`. Se a leitura já está na saída, não divida a redução de novo.

`OrientacaoRobo.plana(rotacao, omegaRadPorSegundo)` faz a conversão da taxa angular para graus/s. Yaw positivo segue anti-horário na referência azul. No Xbox, confira o sinal de cada eixo; a tração atual usa gatilhos para frente/trás e X bruto para giro.

## Unidades dos ganhos PID no SPARK

Para posição em uma unidade `u`, o backend de posição SPARK usa P em duty/u, I em duty/(u × ms) e D em duty × ms/u. Esses ganhos seguem a base nativa em milissegundos documentada pela [REV](https://docs.revrobotics.com/revlib/spark/closed-loop/units).

O simulador Python usa segundos e outro modelo. Ganhos do simulador ou de um `PIDController` WPILib não devem ser copiados diretamente para o SPARK. Redução, fatores de conversão e backend alteram o significado dos números.

## Referenciais do autônomo

A pose continua na origem azul mesmo na aliança vermelha; a transformação do caminho/alvo é tratada separadamente. As velocidades que chegam a `ConexaoPathPlanner` são relativas ao robô. Confira também sinais dos encoders, ordem dos módulos e aplicação de feedforward antes de um caminho real.

---

← [Página inicial](../00%20-%20Comece%20aqui.md) · [Fontes e versão deste guia](Fontes%20e%20versao.md)
