#!/usr/bin/env python3
"""AutoPrecision — ensaios, otimização limitada, monitor e relatórios para o PC."""
import argparse
from dataclasses import asdict
from datetime import datetime
import json
import math
from pathlib import Path
from statistics import mean
import sys
import time

from cliente import FalhaRobo, Registro, Supervisor
from otimizador import Ganhos, otimizar, estabilizacao_observada


def numeros(texto):
    try:
        valores = tuple(float(t.strip()) for t in texto.split(","))
        if not valores or not all(math.isfinite(v) for v in valores): raise ValueError()
        return valores
    except ValueError as erro:
        raise argparse.ArgumentTypeError("Use números finitos separados por vírgula") from erro


def criterio_aceitacao(resultado, avaliacoes):
    """Melhora relativa e cumprimento de tolerância/dwell são condições independentes."""
    confirmacoes = [avaliacoes[i] for i, h in enumerate(resultado.historico)
                   if h["fase"] == "confirmacao-candidato"
                   and h["ganhos"] == asdict(resultado.melhor)]
    estabilizou = bool(confirmacoes) and all(m.estabilizou for grupo in confirmacoes for m in grupo)
    aprovado = resultado.confirmado and estabilizou
    return {"aprovado": aprovado, "melhora_relativa_confirmada": resultado.confirmado,
            "estabilizou_em_todos_alvos": estabilizou,
            "estado": "aprovado_nos_ensaios" if aprovado else "inconclusivo_base_preservada",
            "criterio": "Melhora confirmada e todos os alvos dentro das tolerâncias pelo tempo estável."}


class PlanoEnsaios:
    def __init__(self, supervisor, registro, alvos, duracao):
        if len(alvos) < 2 or len(set(alvos)) != len(alvos):
            raise ValueError("Use pelo menos dois alvos distintos para validar os dois sentidos")
        supervisor.config.validar(supervisor.config.iniciais, alvos, duracao)
        distancias = [abs(a - b) for a, b in zip(alvos, alvos[1:] + alvos[:1])]
        if min(distancias) < 4 * supervisor.config.valores[8]:
            raise ValueError("Separe os alvos por ao menos quatro tolerâncias de posição")
        if max(distancias) >= supervisor.config.valores[7]:
            raise ValueError("Os alvos precisam deixar margem dentro da excursão máxima do ensaio")
        self.supervisor, self.registro = supervisor, registro
        self.alvos, self.duracao = alvos, duracao
        self.avaliacoes = []

    def __call__(self, ganhos):
        # Cada candidato começa por preparação e um ciclo com os mesmos endpoints.
        dados, _ = self.supervisor.ensaio(self.supervisor.config.iniciais, self.alvos[0], self.duracao, pontuar=False)
        self.registro.ensaio(dados, None)
        if not estabilizacao_observada(dados, self.supervisor.config)[1]:
            raise FalhaRobo("A base não estabilizou na preparação; avaliação inconclusiva, sem comparar posições iniciais diferentes")
        metricas = []
        for alvo in self.alvos[1:] + self.alvos[:1]:
            dados, m = self.supervisor.ensaio(ganhos, alvo, self.duracao)
            self.registro.ensaio(dados, m)
            metricas.append(m)
        self.avaliacoes.append(metricas)
        nota = mean(m.pontuacao for m in metricas) + .25 * max(m.pontuacao for m in metricas)
        print(json.dumps({"avaliacao": len(self.avaliacoes), "pid": asdict(ganhos),
              "pontuacao": nota, "estabilizou": all(m.estabilizou for m in metricas)}, ensure_ascii=False), flush=True)
        return nota


def pasta_padrao():
    agora = datetime.now().astimezone()
    return (Path.home() / "Downloads" / ("luverlib-" + agora.strftime("%Y-%m-%d"))
            / "autoprecision" / "sessoes" / agora.strftime("%H%M%S-%f"))


def monitorar(args):
    """Monitor somente leitura: não toma posse, não envia heartbeat e não arma o robô."""
    import ntcore
    rede = ntcore.NetworkTableInstance.create()
    recursos = []
    try:
        tabela = rede.getTable("SmartDashboard/AutoPrecision/" + args.nome)
        estado = tabela.getStringTopic("Estado").subscribe("")
        recursos.append(estado)
        campos = {n: tabela.getDoubleTopic(n).subscribe(float("nan"))
                  for n in ("Posicao", "Alvo", "P", "I", "D", "Erro", "Saida", "Ensaio")}
        recursos.extend(campos.values())
        rede.startClient4("AutoPrecision-monitor")
        if args.equipe is not None: rede.setServerTeam(args.equipe)
        else: rede.setServer(args.host, args.porta)
        fim = time.monotonic() + args.segundos
        while time.monotonic() < fim:
            print(json.dumps({"conectado": rede.isConnected(), "estado": estado.get(),
                  **{n: (s.get() if math.isfinite(s.get()) else None) for n, s in campos.items()}}, ensure_ascii=False), flush=True)
            time.sleep(.25)
    finally:
        for recurso in recursos: recurso.close()
        rede.stopClient()
        ntcore.NetworkTableInstance.destroy(rede)


def executar(args):
    if args.acao == "monitorar":
        monitorar(args)
        return 0
    if args.acao == "simular":
        from simulador import BancadaSimulada
        supervisor = BancadaSimulada(args.semente)
        alvos = tuple(args.alvos or (-.2, .2))
    else:
        supervisor = Supervisor(args.nome, args.host, args.equipe, args.porta, args.espera)
        alvos = tuple(args.alvos)
    pasta = args.saida or pasta_padrao()
    registro = None
    encerrado = False
    try:
        registro = Registro(pasta, supervisor.config)
        inicial = Ganhos(*args.ganhos) if args.ganhos else supervisor.config.iniciais
        supervisor.config.validar(inicial, alvos, args.duracao)
        if args.acao == "ensaio":
            dados, metricas = supervisor.ensaio(inicial, alvos[0], args.duracao)
            registro.ensaio(dados, metricas)
            encerrado = True
            supervisor.close()
            registro.concluir_ensaio()
            print(json.dumps({"pid": asdict(inicial), "metricas": asdict(metricas),
                              "pasta": str(pasta)}, ensure_ascii=False), flush=True)
            return 0
        plano = PlanoEnsaios(supervisor, registro, alvos, args.duracao)
        resultado = otimizar(inicial, supervisor.config.maximos, plano,
                             args.avaliacoes, args.confirmacoes)
        aceitacao = criterio_aceitacao(resultado, plano.avaliacoes)
        if aceitacao["aprovado"]:
            supervisor.aplicar(resultado.melhor)
        encerrado = True
        supervisor.close()
        registro.finalizar(resultado, aceitacao)
        print(json.dumps({"resultado": asdict(resultado), "aceitacao": aceitacao,
                          "pasta": str(pasta)}, ensure_ascii=False), flush=True)
        return 0 if aceitacao["aprovado"] else 2
    except BaseException as erro:
        if registro is not None:
            registro.falhar(erro)
        raise
    finally:
        if not encerrado:
            try:
                supervisor.close()
            except BaseException as erro:
                if registro is not None:
                    registro.falhar(erro)
                raise


def argumentos(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    sub = parser.add_subparsers(dest="acao", required=True)
    for nome in ("otimizar", "ensaio", "simular", "monitorar"):
        p = sub.add_parser(nome)
        if nome != "simular":
            alvo = p.add_mutually_exclusive_group(required=True)
            alvo.add_argument("--host", help="IP/hostname do roboRIO ou127.0.0.1 em simulação")
            alvo.add_argument("--equipe", type=int)
            p.add_argument("--porta", type=int, default=5810)
            p.add_argument("--nome", required=True, help="Nome registrado no AutoPrecision do robô")
            p.add_argument("--espera", type=float, default=15)
        else: p.add_argument("--semente", type=int, default=123)
        if nome == "monitorar":
            p.add_argument("--segundos", type=float, default=60)
            continue
        p.add_argument("--alvos", type=numeros, required=nome != "simular")
        p.add_argument("--ganhos", type=numeros, help="P,I,D na unidade do backend")
        p.add_argument("--duracao", type=float, default=4)
        p.add_argument("--saida", type=Path, help="Nova pasta para CSV/JSON/relatório")
        p.add_argument("--avaliacoes", type=int, default=30)
        p.add_argument("--confirmacoes", type=int, default=2)
    args = parser.parse_args(argv)
    for campo in ("duracao", "espera", "segundos"):
        valor = getattr(args, campo, None)
        if valor is not None and (not math.isfinite(valor) or valor <= 0):
            parser.error(f"--{campo} deve ser positivo e finito")
    if hasattr(args, "porta") and not 1 <= args.porta <= 65535:
        parser.error("--porta deve estar entre 1 e 65535")
    if getattr(args, "equipe", None) is not None and not 1 <= args.equipe <= 99999:
        parser.error("--equipe deve estar entre 1 e 99999")
    if hasattr(args, "avaliacoes") and args.avaliacoes < 1:
        parser.error("--avaliacoes deve ser positivo")
    if hasattr(args, "confirmacoes") and args.confirmacoes < 1:
        parser.error("--confirmacoes deve ser positivo")
    if getattr(args, "ganhos", None) is not None and len(args.ganhos) != 3:
        parser.error("--ganhos requer exatamente P,I,D")
    if getattr(args, "acao", "") == "ensaio" and len(args.alvos) != 1:
        parser.error("ensaio requer um alvo; use otimizar para um ciclo com vários alvos")
    return args


if __name__ == "__main__":
    try: raise SystemExit(executar(argumentos()))
    except KeyboardInterrupt:
        print("AutoPrecision interrompido; supervisor encerrado e parada solicitada.", file=sys.stderr)
        raise SystemExit(130)
    except (ValueError, OSError, FalhaRobo) as erro:
        print("AutoPrecision: " + str(erro), file=sys.stderr)
        raise SystemExit(1)
