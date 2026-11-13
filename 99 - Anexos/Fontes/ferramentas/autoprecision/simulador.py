"""Bancada numérica sem hardware. Ganhos em segundos, NÃO compatíveis com PID nativo SPARK."""
import math
import random

from otimizador import Amostra, Configuracao, avaliar, conferir_coleta


class BancadaSimulada:
    def __init__(self, semente=123):
        self.config = Configuracao((-1, 1, .5, .8, 1.6, 35, 65, 1.2,
            .01, .04, .25, 5, .02, .5, 8, .5, 2, .8, 0, .05, 1.5),
            "rotacoes_simuladas", "simulacao-python-segundos")
        self.posicao = 0.0
        self.velocidade = 0.0
        self.horario = 0.0
        self.numero = 0
        self.aleatorio = random.Random(semente)

    def ensaio(self, ganhos, alvo, duracao, pontuar=True):
        self.config.validar(ganhos, [alvo], duracao)
        if abs(alvo - self.posicao) > self.config.valores[7]:
            raise ValueError("Deslocamento da bancada excede o limite")
        self.numero += 1
        dados = []
        inicio, origem = self.horario, self.posicao
        referencia, velocidade_ref = self.posicao, self.velocidade
        integral, erro_anterior, saida = 0.0, 0.0, 0.0
        for ciclo in range(round(duracao / .02) + 1):
            tempo = ciclo * .02
            estado = 3 if ciclo == round(duracao / .02) else 2
            medida = self.posicao + self.aleatorio.gauss(0, 0.0001)
            dados.append(Amostra(self.numero, tempo, medida, self.velocidade,
                referencia, alvo, saida, abs(saida) * 10, 25,
                ganhos.p, ganhos.i, ganhos.d, ciclo + 1, estado, inicio + tempo, 1))
            if estado == 3: break
            distancia = alvo - referencia
            desejada = math.copysign(min(.8, math.sqrt(2 * 1.6 * abs(distancia))), distancia)
            velocidade_ref += max(-1.6 * .02, min(1.6 * .02, desejada - velocidade_ref))
            proxima = referencia + velocidade_ref * .02
            if (alvo - referencia) * (alvo - proxima) <= 0:
                referencia, velocidade_ref = alvo, 0
            else: referencia = proxima
            for _ in range(20):
                erro = referencia - self.posicao
                integral = max(-.2, min(.2, integral + erro * .001))
                derivada = (erro - erro_anterior) / .001
                saida = max(-.5, min(.5, ganhos.p * erro + ganhos.i * integral + ganhos.d * derivada))
                erro_anterior = erro
                self.velocidade += (8 * saida - 3 * self.velocidade) * .001
                self.posicao += self.velocidade * .001
                if not -1 < self.posicao < 1 or abs(self.posicao - origem) > 1.2:
                    raise RuntimeError("A bancada simulada atingiu seu limite; ensaio interrompido")
        self.horario = inicio + duracao + .02
        self.velocidade *= .98
        conferir_coleta(dados, self.config, len(dados))
        return dados, avaliar(dados, self.config, len(dados)) if pontuar else None

    def aplicar(self, ganhos): pass
    def close(self): pass
