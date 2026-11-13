"""Verificação independente do AutoPrecision, sem motores, rede ou bibliotecas numéricas.

A planta usa posição em unidade sintética e PID em segundos. Seus ganhos NÃO são
ganhos do SPARK MAX: servem exclusivamente para testar busca, coleta e aceitação.
O controle é integrado a 1 ms, a telemetria sai a 20 ms e o ruído tem semente fixa.
"""

from dataclasses import replace
import math
import random
import unittest

from otimizador import Amostra, Configuracao, Ganhos, avaliar, otimizar


def configuracao():
    """Configuração completa de um mecanismo sintético; ordem do protocolo publicada pelo robô."""
    return Configuracao(
        (-2.0, 2.0, 0.7, 3.0, 8.0, 80.0, 70.0, 2.0,
         0.015, 0.06, 0.25, 6.0, 0.02, 0.5, 8.0, 2.0, 3.0,
         0.25, 0.0, 0.05),
        "unidade-sintetica", "planta-teste-pid-segundos",
    )


def perfil(tempo, partida, alvo, velocidade=1.5, aceleracao=4.0):
    """Referência cinemática triangular/trapezoidal, sem consultar código de produção."""
    distancia = abs(alvo - partida)
    sinal = math.copysign(1.0, alvo - partida)
    acelerando = min(velocidade / aceleracao, math.sqrt(distancia / aceleracao))
    pico = aceleracao * acelerando
    cruzeiro = max(0.0, distancia / pico - acelerando) if pico else 0.0
    termino = 2 * acelerando + cruzeiro
    if tempo <= acelerando:
        deslocamento = aceleracao * tempo * tempo / 2
    elif tempo < acelerando + cruzeiro:
        deslocamento = aceleracao * acelerando * acelerando / 2 + pico * (tempo - acelerando)
    elif tempo < termino:
        deslocamento = distancia - aceleracao * (termino - tempo) ** 2 / 2
    else:
        deslocamento = distancia
    return partida + sinal * deslocamento


def simular(ganhos, *, alvo=0.8, partida=0.0, duracao=3.0, semente=1729, ruido=0.0003):
    """Planta x''=10*u-3*x', saída saturada e telemetria com ruído de medição.

    A avaliação recebe dados; não tem acesso à equação nem a um PID ótimo conhecido.
    Cada ensaio começa nas MESMAS condições, inclusive nas confirmações.
    """
    aleatorio = random.Random(semente)
    passo = 0.001
    periodo = 0.02
    limite = 0.7
    posicao = partida
    velocidade = 0.0
    integral = 0.0
    saida = 0.0
    temperatura = 25.0
    amostras = []
    passos = round(duracao / passo)
    for indice in range(passos + 1):
        tempo = indice * passo
        referencia = perfil(tempo, partida, alvo)
        if indice % round(periodo / passo) == 0:
            amostras.append(Amostra(
                41, tempo, posicao + aleatorio.gauss(0, ruido),
                velocidade + aleatorio.gauss(0, ruido * 2), referencia, alvo,
                saida, 2 + 40 * abs(saida), temperatura,
                ganhos.p, ganhos.i, ganhos.d, len(amostras) + 1,
                3 if indice == passos else 2, 1000.0 + tempo,
            ))
        if indice == passos:
            break
        erro = referencia - posicao
        integral = max(-0.4, min(0.4, integral + erro * passo))
        # Derivada na medição evita impulso ao mudar a referência na planta sintética.
        pedido = ganhos.p * erro + ganhos.i * integral - ganhos.d * velocidade
        saida = max(-limite, min(limite, pedido))
        aceleracao = 10 * saida - 3 * velocidade
        velocidade += aceleracao * passo
        posicao += velocidade * passo
        temperatura += saida * saida * passo * 0.2
    return amostras


def resposta_controlada(*, alvo=0.8, escala=1.0, overshoot=0.0, oscilando=False):
    """Traço simples com referência e chegada conhecidas, para comparar propriedades das métricas."""
    resultado = []
    ganhos = Ganhos(1, 0, 0.1)
    for indice in range(151):
        tempo = indice * 0.02
        if tempo < 1.0:
            posicao = alvo * tempo
            velocidade = alvo
        elif oscilando:
            posicao = alvo + 0.1 * math.sin((tempo - 1) * 18)
            velocidade = 1.8 * math.cos((tempo - 1) * 18)
        elif 1.0 <= tempo <= 1.5 and overshoot:
            posicao = alvo + math.copysign(overshoot, alvo) * math.sin((tempo - 1) * 2 * math.pi)
            velocidade = math.copysign(overshoot, alvo) * 2 * math.pi * math.cos((tempo - 1) * 2 * math.pi)
        else:
            posicao, velocidade = alvo, 0.0
        resultado.append(Amostra(17, tempo, posicao * escala, velocidade * escala,
                                 alvo * escala, alvo * escala, 0.2, 10.0, 25.0,
                                 *ganhos.valores(), indice + 1,
                                 3 if indice == 150 else 2, 2000.0 + tempo))
    return resultado


def pontuar(amostras, config=None):
    return avaliar(amostras, config or configuracao(), len(amostras))


class TestIntegridadeAmostras(unittest.TestCase):
    def setUp(self):
        self.amostras = resposta_controlada()

    def test_wire_novo_tem_chave_de_sessao_e_rejeita_legado_sem_chave(self):
        amostra = self.amostras[0]
        wire = [getattr(amostra, campo) for campo in (
            "ensaio", "tempo", "posicao", "velocidade", "referencia", "alvo", "saida", "corrente",
            "temperatura", "p", "i", "d", "sequencia", "estado", "horario_robo")]
        decodificada = Amostra.ler(wire + [123456])
        self.assertEqual(decodificada.chave_sessao, 123456)
        self.assertEqual(decodificada.ensaio, amostra.ensaio)
        self.assertEqual(decodificada.sequencia, 1)
        with self.assertRaises(ValueError):
            Amostra.ler(wire)

    def test_decodificador_rejeita_nao_finitos_em_todos_os_campos(self):
        wire = [41, 0.0, 0.0, 0.0, 0.0, 0.8, 0.1, 10.0, 25.0, 1.0, 0.0, 0.1,
                1, 2, 1000.0, 123456]
        for indice in range(len(wire)):
            for invalido in (math.nan, math.inf, -math.inf):
                with self.subTest(indice=indice, invalido=invalido):
                    alterado = list(wire)
                    alterado[indice] = invalido
                    with self.assertRaises(ValueError):
                        Amostra.ler(alterado)

    def test_coleta_incompleta_duplicada_ou_misturada_nao_e_pontuada(self):
        casos = {
            "faltou_primeiro": self.amostras[1:],
            "faltou_meio": self.amostras[:30] + self.amostras[31:],
            "duplicada": self.amostras[:30] + [self.amostras[29]] + self.amostras[30:],
            "outro_ensaio": self.amostras[:30] + [replace(self.amostras[30], ensaio=999)] + self.amostras[31:],
            "outro_alvo": self.amostras[:30] + [replace(self.amostras[30], alvo=0.9)] + self.amostras[31:],
            "outros_ganhos": self.amostras[:30] + [replace(self.amostras[30], p=2.0)] + self.amostras[31:],
        }
        for nome, amostras in casos.items():
            with self.subTest(caso=nome), self.assertRaises(ValueError):
                avaliar(amostras, configuracao(), len(self.amostras))

    def test_relogios_repetidos_retrocedendo_e_lacuna_temporal_sao_recusados(self):
        for campo, novo in (("tempo", self.amostras[29].tempo),
                            ("horario_robo", self.amostras[29].horario_robo),
                            ("tempo", -0.1)):
            with self.subTest(campo=campo, novo=novo):
                amostras = list(self.amostras)
                amostras[30] = replace(amostras[30], **{campo: novo})
                with self.assertRaises(ValueError):
                    pontuar(amostras)
        amostras = [replace(a, tempo=a.tempo + (0.2 if i >= 30 else 0),
                            horario_robo=a.horario_robo + (0.2 if i >= 30 else 0))
                    for i, a in enumerate(self.amostras)]
        with self.assertRaises(ValueError):
            pontuar(amostras)

    def test_falha_ou_desarmamento_no_meio_nao_pode_virar_ensaio_concluido(self):
        for estado in (0, 1, 3, 4, 99):
            with self.subTest(estado=estado):
                amostras = list(self.amostras)
                amostras[40] = replace(amostras[40], estado=estado)
                with self.assertRaises(ValueError):
                    pontuar(amostras)

    def test_ensaio_nao_concluido_nao_recebe_nota(self):
        for estado in (0, 1, 2, 4):
            with self.subTest(estado=estado):
                with self.assertRaises(ValueError):
                    pontuar(self.amostras[:-1] + [replace(self.amostras[-1], estado=estado)])

    def test_amostra_construida_diretamente_tambem_e_validada(self):
        for campo in ("posicao", "velocidade", "referencia", "saida", "corrente", "temperatura",
                      "tempo", "horario_robo", "p", "i", "d"):
            with self.subTest(campo=campo):
                amostras = list(self.amostras)
                amostras[40] = replace(amostras[40], **{campo: math.nan})
                with self.assertRaises(ValueError):
                    pontuar(amostras)

    def test_violacao_fisica_nao_pode_ser_rotulada_como_melhoria(self):
        for campo, invalido in (("posicao", -2.01), ("posicao", 2.01),
                                ("saida", 0.9), ("saida", -0.9),
                                ("corrente", 81.0), ("corrente", -1.0),
                                ("temperatura", 71.0)):
            with self.subTest(campo=campo, invalido=invalido):
                amostras = list(self.amostras)
                amostras[40] = replace(amostras[40], **{campo: invalido})
                with self.assertRaises(ValueError):
                    pontuar(amostras)

    def test_cache_de_outra_sessao_e_recusado_mesmo_com_mesmo_id(self):
        amostras = [replace(a, chave_sessao=123456) for a in self.amostras]
        amostras[40] = replace(amostras[40], chave_sessao=654321)
        with self.assertRaises(ValueError):
            pontuar(amostras)

    def test_tempo_do_ensaio_e_horario_do_robo_precisam_corresponder(self):
        # Os dois relógios seguem crescentes; a amostra ainda deve ser recusada porque
        # tempo=horario_robo-inicio deixou de valer em um único pacote.
        amostras = list(self.amostras)
        amostras[40] = replace(amostras[40], horario_robo=amostras[40].horario_robo + 0.005)
        with self.assertRaises(ValueError):
            pontuar(amostras)

    def test_movimento_insuficiente_nao_gera_otimo_artificial(self):
        amostras = [replace(a, posicao=a.alvo, velocidade=0) for a in self.amostras]
        with self.assertRaises(ValueError):
            pontuar(amostras)


class TestMetricasControle(unittest.TestCase):
    def test_movimento_que_estabiliza_supera_oscilaçao_persistente(self):
        bom = pontuar(resposta_controlada())
        oscilante = pontuar(resposta_controlada(oscilando=True))
        self.assertTrue(bom.estabilizou)
        self.assertFalse(oscilante.estabilizou)
        self.assertLess(bom.pontuacao, oscilante.pontuacao)
        self.assertLess(bom.erro_final, oscilante.erro_final)

    def test_ultrapassagem_penalizada_nos_dois_sentidos(self):
        for alvo in (-0.8, 0.8):
            with self.subTest(alvo=alvo):
                bom = pontuar(resposta_controlada(alvo=alvo))
                ruim = pontuar(resposta_controlada(alvo=alvo, overshoot=0.16))
                self.assertGreater(ruim.ultrapassagem, 0.19)
                self.assertGreater(ruim.pontuacao, bom.pontuacao)

    def test_saturacao_e_saida_agitada_sao_observaveis(self):
        base = resposta_controlada()
        suave = pontuar(base)
        agitada = pontuar([replace(a, saida=0.7 if i % 2 else -0.7)
                           for i, a in enumerate(base)])
        self.assertEqual(agitada.fracao_saturada, 1.0)
        self.assertGreater(agitada.variacao_saida, suave.variacao_saida)
        self.assertGreater(agitada.pontuacao, suave.pontuacao)

    def test_troca_de_unidade_nao_muda_pontuacao_adimensional(self):
        config = configuracao()
        v = list(config.valores)
        # Posição, velocidades, aceleração, tolerâncias e deslocamento mudam juntos.
        for indice in (0, 1, 3, 4, 7, 8, 9):
            v[indice] *= 1000
        convertida = Configuracao(tuple(v), "mili-unidade-sintetica", config.backend)
        normal = pontuar(resposta_controlada())
        outra_unidade = pontuar(resposta_controlada(escala=1000), convertida)
        self.assertAlmostEqual(normal.pontuacao, outra_unidade.pontuacao, places=9)
        self.assertAlmostEqual(normal.erro_rms * 1000, outra_unidade.erro_rms, places=9)

    def test_planta_e_ruido_sao_reproduziveis(self):
        ganhos = Ganhos(1.2, 0.0, 0.4)
        primeira = simular(ganhos, semente=42)
        segunda = simular(ganhos, semente=42)
        self.assertEqual(primeira, segunda)
        self.assertNotEqual(primeira, simular(ganhos, semente=43))
        resultado = pontuar(primeira)
        self.assertTrue(math.isfinite(resultado.pontuacao))
        self.assertGreaterEqual(resultado.pontuacao, 0)


class TestBuscaLimitada(unittest.TestCase):
    def test_orcamento_e_limites_incluem_todas_as_confirmacoes(self):
        for orcamento, confirmacoes in ((7, 2), (18, 1), (27, 3)):
            with self.subTest(orcamento=orcamento):
                inicial = Ganhos(0.25, 0, 0.05)
                limites = Ganhos(2, 0.5, 1)
                visitas = []

                def medir(ganhos):
                    visitas.append(ganhos)
                    self.assertLessEqual(len(visitas), orcamento)
                    self.assertTrue(all(0 <= valor <= limite
                                        for valor, limite in zip(ganhos.valores(), limites.valores())))
                    # Objetivo não conhecido pela busca, com ótimo interior e custo não negativo.
                    return 1 + (ganhos.p - 1.1) ** 2 + (ganhos.i - 0.1) ** 2 + (ganhos.d - 0.3) ** 2

                resultado = otimizar(inicial, limites, medir, orcamento, confirmacoes)
                self.assertEqual(resultado.avaliacoes, len(visitas))
                self.assertEqual(len(resultado.historico), len(visitas))
                fases = [item["fase"] for item in resultado.historico]
                self.assertEqual(fases.count("confirmacao-base"), confirmacoes)
                self.assertEqual(fases.count("confirmacao-candidato"), confirmacoes)

    def test_melhora_nao_repetida_preserva_ganhos_iniciais(self):
        inicial = Ganhos(0.25, 0, 0.05)
        visitas = []

        def medir(ganhos):
            visitas.append(ganhos)
            if ganhos == inicial:
                return 5.0
            # Uma medição excepcionalmente boa não se repete nas confirmações.
            return 1.0 if len(visitas) == 2 else 20.0

        resultado = otimizar(inicial, Ganhos(2, 1, 1), medir, 20, 3)
        self.assertFalse(resultado.confirmado)
        self.assertEqual(resultado.melhor, inicial)
        self.assertEqual(resultado.pontuacao_melhor, resultado.pontuacao_inicial)

    def test_ausencia_de_melhoria_e_faixa_toda_zero_terminam_sem_inventar_resultado(self):
        for inicial, limites in ((Ganhos(0.2, 0, 0), Ganhos(2, 1, 1)),
                                 (Ganhos(0, 0, 0), Ganhos(0, 0, 0))):
            with self.subTest(limites=limites):
                visitas = []
                resultado = otimizar(inicial, limites, lambda g: visitas.append(g) or 2.0, 20, 2)
                self.assertFalse(resultado.confirmado)
                self.assertEqual(resultado.melhor, inicial)
                self.assertLessEqual(len(visitas), 20)

    def test_avaliacao_invalida_interrompe_sem_ensaios_adicionais(self):
        for nota in (math.nan, math.inf, -math.inf, -1.0):
            with self.subTest(nota=nota):
                visitas = []

                def medir(ganhos):
                    visitas.append(ganhos)
                    return 2.0 if len(visitas) == 1 else nota

                with self.assertRaises(ValueError):
                    otimizar(Ganhos(0.2, 0, 0), Ganhos(2, 1, 1), medir, 20, 2)
                self.assertEqual(len(visitas), 2)

    def test_excecao_de_ensaio_interrompe_sem_tentar_compensar_movimento(self):
        visitas = []

        def medir(ganhos):
            visitas.append(ganhos)
            if len(visitas) == 2:
                raise RuntimeError("Supervisor perdeu a coleta")
            return 2.0

        with self.assertRaisesRegex(RuntimeError, "perdeu a coleta"):
            otimizar(Ganhos(0.2, 0, 0), Ganhos(2, 1, 1), medir, 20, 2)
        self.assertEqual(len(visitas), 2)

    def test_orcamento_fracionario_ou_nan_e_rejeitado_antes_de_mover(self):
        for invalido in (7.5, math.nan):
            with self.subTest(orcamento=invalido):
                visitas = []
                with self.assertRaises((ValueError, TypeError)):
                    otimizar(Ganhos(0.2, 0, 0), Ganhos(2, 1, 1),
                             lambda g: visitas.append(g) or 1.0, invalido, 2)
                self.assertEqual(visitas, [])

    def test_busca_melhora_planta_dinamica_com_ruido_e_confirma_em_novas_medicoes(self):
        config = configuracao()
        visitas = []

        def ensaiar(ganhos):
            visitas.append(ganhos)
            # Ruído diferente a cada ensaio, com execução completamente reproduzível.
            return pontuar(simular(ganhos, semente=1700 + len(visitas)), config).pontuacao

        resultado = otimizar(config.iniciais, config.maximos, ensaiar, 50, 3)
        self.assertTrue(resultado.confirmado)
        self.assertLess(resultado.pontuacao_melhor, resultado.pontuacao_inicial * 0.95)
        self.assertNotEqual(resultado.melhor, resultado.inicial)
        self.assertLessEqual(len(visitas), 50)
        # Validação independente, com sementes que não participaram da busca.
        for alvo in (-0.8, 0.8):
            for semente in (9001, 9002, 9003):
                with self.subTest(alvo=alvo, semente=semente):
                    base = pontuar(simular(resultado.inicial, alvo=alvo, semente=semente), config)
                    candidato = pontuar(simular(resultado.melhor, alvo=alvo, semente=semente), config)
                    self.assertLess(candidato.pontuacao, base.pontuacao)
                    self.assertTrue(candidato.estabilizou)
                    self.assertLess(candidato.erro_final, config.valores[8])
                    self.assertLess(candidato.ultrapassagem, 0.05)


if __name__ == "__main__":
    unittest.main(verbosity=2)
