"""Métricas e busca limitada de PID. Sem dependência de IA, rede ou bibliotecas numéricas."""
from dataclasses import asdict, dataclass
import math
from statistics import mean, median
from typing import Callable


@dataclass(frozen=True)
class Ganhos:
    p: float
    i: float
    d: float

    def __post_init__(self):
        if any(not math.isfinite(v) or v < 0 for v in self.valores()):
            raise ValueError("Ganhos devem ser finitos e não negativos")

    def valores(self):
        return (self.p, self.i, self.d)


@dataclass(frozen=True)
class Configuracao:
    valores: tuple[float, ...]
    unidade: str
    backend: str

    def __post_init__(self):
        v = self.valores
        if len(v) not in (20, 21) or not all(math.isfinite(x) for x in v):
            raise ValueError("Configuração remota inválida ou protocolo incompatível")
        if v[0] >= v[1] or not 0 < v[2] <= 1 or any(x <= 0 for x in v[3:14]):
            raise ValueError("Limites remotos inválidos")
        if not all(0 <= inicio <= maximo for inicio, maximo in zip(v[17:20], v[14:17])):
            raise ValueError("Ganhos iniciais incompatíveis com os limites")
        if not self.unidade or not self.backend:
            raise ValueError("Unidade e backend precisam ser informados pelo robô")
        if len(v) == 21 and v[20] < v[3]:
            raise ValueError("Corte de velocidade medida menor que a velocidade do perfil")

    @property
    def iniciais(self): return Ganhos(*self.valores[17:20])

    @property
    def maximos(self): return Ganhos(*self.valores[14:17])

    def validar(self, ganhos, alvos, duracao):
        if not all(a <= b for a, b in zip(ganhos.valores(), self.maximos.valores())):
            raise ValueError("Ganhos acima dos limites do mecanismo")
        if not alvos or not all(math.isfinite(x) and self.valores[0] < x < self.valores[1] for x in alvos):
            raise ValueError("Informe alvos dentro da faixa de movimento registrada no robô")
        if not math.isfinite(duracao) or not self.valores[12] * 3 <= duracao <= self.valores[11]:
            raise ValueError("Duração fora dos limites registrados no robô")


@dataclass(frozen=True)
class Amostra:
    ensaio: int
    tempo: float
    posicao: float
    velocidade: float
    referencia: float
    alvo: float
    saida: float
    corrente: float
    temperatura: float
    p: float
    i: float
    d: float
    sequencia: int
    estado: int
    horario_robo: float
    chave_sessao: int = 0

    @classmethod
    def ler(cls, valores):
        if len(valores) != 16 or not all(math.isfinite(v) for v in valores):
            raise ValueError("Amostra incompleta ou não finita")
        for i in (0, 12, 13, 15):
            if valores[i] != int(valores[i]): raise ValueError("Contador fracionário na amostra")
        if valores[1] < 0 or valores[12] < 1:
            raise ValueError("Tempo ou sequência inválidos")
        return cls(*[int(v) if i in (0, 12, 13, 15) else float(v) for i, v in enumerate(valores)])


@dataclass(frozen=True)
class Metricas:
    pontuacao: float
    erro_rms: float
    erro_final: float
    ultrapassagem: float
    estabilizacao_segundos: float
    variacao_saida: float
    fracao_saturada: float
    estabilizou: bool


def estabilizacao_observada(amostras, config):
    """Tempo em banda comprovado por amostras; nunca presume dt nominal em uma lacuna."""
    fora = [i for i, a in enumerate(amostras)
            if abs(a.alvo - a.posicao) > config.valores[8] or abs(a.velocidade) > config.valores[9]]
    indice = max(fora) + 1 if fora else 0
    entrada = amostras[indice].tempo if indice < len(amostras) else amostras[-1].tempo
    return entrada, amostras[-1].tempo - entrada >= config.valores[10]


def conferir_coleta(amostras: list[Amostra], config: Configuracao, quantidade: int):
    """Validação completa também para movimentos de preparação, que não recebem pontuação."""
    if type(quantidade) is not int or len(amostras) != quantidade or len(amostras) < 4:
        raise ValueError("Coleta incompleta: quantidade de amostras diferente do resultado do robô")
    primeiro = amostras[0]
    for i, a in enumerate(amostras, 1):
        if not all(math.isfinite(v) for v in asdict(a).values()):
            raise ValueError("Amostra com valor não finito")
        if a.chave_sessao != primeiro.chave_sessao:
            raise ValueError("Coleta mistura sessões diferentes")
        if a.estado != (3 if i == quantidade else 2):
            raise ValueError("Estado inválido dentro da coleta")
        v = config.valores
        if (not v[0] < a.posicao < v[1] or abs(a.saida) > v[2] + 1e-4
                or not 0 <= a.corrente <= v[5] or a.temperatura > v[6]
                or not v[0] < a.alvo < v[1] or not v[0] < a.referencia < v[1]
                or abs(a.posicao - primeiro.posicao) > v[7]
                or len(v) == 21 and abs(a.velocidade) > v[20]
                or not all(0 <= g <= maximo for g, maximo in zip((a.p, a.i, a.d), v[14:17]))):
            raise ValueError("Coleta excede os limites do mecanismo")
        if not math.isclose(a.horario_robo - primeiro.horario_robo,
                            a.tempo - primeiro.tempo, rel_tol=0, abs_tol=1e-6):
            raise ValueError("Tempo de ensaio não corresponde ao relógio do robô")
        if a.ensaio != primeiro.ensaio or a.sequencia != i:
            raise ValueError("Coleta com repetição, mistura de ensaios ou lacunas")
        if a.alvo != primeiro.alvo or (a.p, a.i, a.d) != (primeiro.p, primeiro.i, primeiro.d):
            raise ValueError("Alvo ou ganhos mudaram durante a coleta")
        if i > 1 and (a.tempo <= amostras[i - 2].tempo or a.horario_robo <= amostras[i - 2].horario_robo):
            raise ValueError("Relógio das amostras não é crescente")
        if i > 1 and a.tempo - amostras[i - 2].tempo > config.valores[12] * 4:
            raise ValueError("Intervalo de coleta grande demais para avaliar os picos")
    if amostras[-1].estado != 3:
        raise ValueError("O ensaio não foi concluído normalmente")
    duracao = amostras[-1].tempo
    if duracao <= 0 or primeiro.tempo > config.valores[12] * 2:
        raise ValueError("O início do ensaio não foi coletado")


def avaliar(amostras: list[Amostra], config: Configuracao, quantidade: int) -> Metricas:
    """Pontuação normalizada por deslocamento; menor é melhor. Não interpola amostras perdidas."""
    conferir_coleta(amostras, config, quantidade)
    primeiro, duracao = amostras[0], amostras[-1].tempo
    amplitude = abs(primeiro.alvo - primeiro.posicao)
    tolerancia = config.valores[8]
    if amplitude < tolerancia * 4:
        raise ValueError("Ensaio sem deslocamento suficiente para avaliar o PID")
    erros = [a.alvo - a.posicao for a in amostras]
    integral_quadrada = 0.0
    integral_tempo = 0.0
    for anterior, atual, e0, e1 in zip(amostras, amostras[1:], erros, erros[1:]):
        dt = atual.tempo - anterior.tempo
        if dt > config.valores[12] * 4:
            raise ValueError("Intervalo de coleta grande demais para avaliar os picos")
        integral_quadrada += dt * (e0 * e0 + e1 * e1) / 2
        integral_tempo += dt * (anterior.tempo * abs(e0) + atual.tempo * abs(e1)) / 2
    sinal = math.copysign(1, primeiro.alvo - primeiro.posicao)
    ultrapassagem = max(0.0, max(sinal * (a.posicao - a.alvo) for a in amostras)) / amplitude
    cauda = [e for a, e in zip(amostras, erros) if a.tempo >= duracao - min(0.5, duracao / 3)]
    erro_final = mean(abs(e) for e in cauda)
    estabilizacao, estabilizou = estabilizacao_observada(amostras, config)
    if not estabilizou: estabilizacao = duracao * 1.5
    variacao = sum(abs(a.saida - b.saida) for a, b in zip(amostras, amostras[1:]))
    limite_saida = config.valores[2]
    saturacao = mean(abs(a.saida) >= limite_saida * 0.98 for a in amostras)
    esforco = mean((a.saida / limite_saida) ** 2 for a in amostras)
    rms = math.sqrt(integral_quadrada / duracao)
    itae = integral_tempo / (amplitude * duracao * duracao / 2)
    # Todas as parcelas são adimensionais; penaliza erro final e ultrapassagem mais fortemente.
    pontuacao = (3 * itae + 4 * erro_final / amplitude + estabilizacao / duracao
                 + 4 * ultrapassagem + 0.04 * variacao / limite_saida
                 + 0.15 * saturacao + 0.05 * esforco + (0 if estabilizou else 1))
    return Metricas(pontuacao, rms, erro_final, ultrapassagem, estabilizacao,
                    variacao, saturacao, estabilizou)


@dataclass
class ResultadoBusca:
    inicial: Ganhos
    melhor: Ganhos
    pontuacao_inicial: float
    pontuacao_melhor: float
    avaliacoes: int
    confirmado: bool
    historico: list[dict]


def otimizar(inicial: Ganhos, maximos: Ganhos, ensaiar: Callable[[Ganhos], float],
             avaliacoes: int = 30, confirmacoes: int = 2) -> ResultadoBusca:
    """Busca por coordenadas limitada e confirmação contra a base; não afirma ótimo global."""
    if (type(avaliacoes) is not int or type(confirmacoes) is not int
            or confirmacoes < 1 or avaliacoes < 2 * confirmacoes + 3):
        raise ValueError("Reserve ao menos três avaliações mais as confirmações de base e candidato")
    if not all(a <= b for a, b in zip(inicial.valores(), maximos.valores())):
        raise ValueError("Ganhos iniciais acima do limite")
    historico = []

    def medir(ganhos, fase):
        valor = float(ensaiar(ganhos))
        if not math.isfinite(valor) or valor < 0:
            raise ValueError("Avaliação não finita ou negativa: interrompendo a busca")
        historico.append({"ganhos": asdict(ganhos), "pontuacao": valor, "fase": fase})
        return valor

    base = medir(inicial, "base")
    melhor, nota = inicial, base
    passos = [max(v * 0.5, limite * 0.01) for v, limite in zip(inicial.valores(), maximos.valores())]
    limite_busca = avaliacoes - confirmacoes * 2
    while len(historico) < limite_busca:
        houve_melhora = False
        for eixo in range(3):
            for sentido in (1, -1):
                valores = list(melhor.valores())
                valores[eixo] = min(maximos.valores()[eixo], max(0, valores[eixo] + sentido * passos[eixo]))
                candidato = Ganhos(*valores)
                if candidato == melhor or len(historico) >= limite_busca: continue
                nova = medir(candidato, "busca")
                if nova < nota * 0.995:
                    melhor, nota, houve_melhora = candidato, nova, True
            if len(historico) >= limite_busca: break
        passos = [p * (1.15 if houve_melhora else 0.5) for p in passos]
        if all(p <= max(limite * 1e-6, 1e-12) for p, limite in zip(passos, maximos.valores())):
            break
    # Repete base e candidato; uma única tentativa com ruído não determina o resultado final.
    notas_base, notas_melhor = [], []
    for repeticao in range(confirmacoes):
        if repeticao % 2 == 0:
            notas_base.append(medir(inicial, "confirmacao-base"))
            notas_melhor.append(medir(melhor, "confirmacao-candidato"))
        else:
            notas_melhor.append(medir(melhor, "confirmacao-candidato"))
            notas_base.append(medir(inicial, "confirmacao-base"))
    base_confirmada, melhor_confirmada = median(notas_base), median(notas_melhor)
    confirmado = melhor != inicial and melhor_confirmada < base_confirmada * 0.995
    if not confirmado:
        melhor, melhor_confirmada = inicial, base_confirmada
    return ResultadoBusca(inicial, melhor, base_confirmada, melhor_confirmada,
                          len(historico), confirmado, historico)
