"""Supervisor NT4 do AutoPrecision. A malha PID é sempre local ao robô/controlador."""
from dataclasses import asdict
import csv
import html
import json
from pathlib import Path
import threading
import time
import uuid

from otimizador import Amostra, Configuracao, Ganhos, avaliar, conferir_coleta


class FalhaRobo(RuntimeError):
    pass


class Supervisor:
    def __init__(self, nome, host=None, equipe=None, porta=5810, espera=15):
        import ntcore
        self.inst = ntcore.NetworkTableInstance.create()
        self.cliente = str(uuid.uuid4())
        self.sessao = ""
        self.contador = 0
        self.recursos = []
        self.fim = threading.Event()
        self.thread = None
        self.erro_heartbeat = None
        self.fechado = False
        try:
            if not nome or any(c in nome for c in "/\\"):
                raise ValueError("Nome do mecanismo inválido")
            self.tabela = self.inst.getTable("SmartDashboard/AutoPrecision/" + nome)
            self.nonce = self._recurso(self.tabela.getStringTopic("Sessao").subscribe(""))
            self.dono = self._recurso(self.tabela.getStringTopic("Cliente").subscribe(""))
            self.estado = self._recurso(self.tabela.getStringTopic("Estado").subscribe(""))
            self.mensagem = self._recurso(self.tabela.getStringTopic("Mensagem").subscribe(""))
            self.unidade = self._recurso(self.tabela.getStringTopic("Unidade").subscribe(""))
            self.backend = self._recurso(self.tabela.getStringTopic("Backend").subscribe(""))
            self.protocolo = self._recurso(self.tabela.getIntegerTopic("Protocolo").subscribe(0))
            self.cfg = self._recurso(self.tabela.getDoubleArrayTopic("Configuracao").subscribe([]))
            opcoes = ntcore.PubSubOptions(periodic=0.02, sendAll=True, keepDuplicates=True, pollStorage=8192)
            self.amostras = self._recurso(self.tabela.getDoubleArrayTopic("Amostra").subscribe([], opcoes))
            self.resultado = self._recurso(self.tabela.getDoubleArrayTopic("Resultado").subscribe([]))
            self.resposta = self._recurso(self.tabela.getStringArrayTopic("Resposta").subscribe([],
                ntcore.PubSubOptions(periodic=0.02, sendAll=True, pollStorage=64)))
            self.pedidos = self._recurso(self.tabela.getStringArrayTopic("Pedido").publish(
                ntcore.PubSubOptions(periodic=0.01, sendAll=True, keepDuplicates=True)))
            self.heartbeat = self._recurso(self.tabela.getStringArrayTopic("Heartbeat").publish(
                ntcore.PubSubOptions(periodic=0.05, sendAll=True, keepDuplicates=True)))
            self.inst.startClient4("AutoPrecision-" + self.cliente)
            if equipe is not None: self.inst.setServerTeam(equipe)
            elif host: self.inst.setServer(host, porta)
            else: raise ValueError("Informe host ou equipe")
            limite = time.monotonic() + espera
            while time.monotonic() < limite:
                if (self.inst.isConnected() and self.protocolo.get() == 1
                        and self.estado.get() in ("PRONTO", "CONCLUIDO") and self.nonce.get()
                        and len(self.cfg.get()) == 21 and self.unidade.get() and self.backend.get()):
                    break
                time.sleep(0.02)
            else:
                raise FalhaRobo("Sem servidor armado: confira rede, nome, modo Test e autorização local")
            if self.dono.get():
                raise FalhaRobo("Sessão já possui supervisor; desarme e arme localmente para uma nova sessão")
            self.config = Configuracao(tuple(self.cfg.get()), self.unidade.get(), self.backend.get())
            self.sessao = self.nonce.get()
            self.chave_sessao = uuid.UUID(self.sessao).int & ((1 << 52) - 1)
            self.thread = threading.Thread(target=self._enviar_heartbeat, daemon=True)
            self.thread.start()
            limite = time.monotonic() + min(espera, 5)
            while self.dono.get() != self.cliente:
                self._conferir()
                if time.monotonic() > limite: raise FalhaRobo("Robô não concedeu a sessão ao supervisor")
                time.sleep(0.01)
        except BaseException:
            self.close()
            raise

    def _recurso(self, recurso):
        self.recursos.append(recurso)
        return recurso

    def _enviar_heartbeat(self):
        sequencia = 0
        intervalo = min(0.1, self.config.valores[13] / 4)
        try:
            while not self.fim.is_set():
                sequencia += 1
                self.heartbeat.set([self.sessao, self.cliente, str(sequencia)])
                self.inst.flush()
                self.fim.wait(intervalo)
        except BaseException as erro:
            self.erro_heartbeat = erro

    def _conferir(self):
        if self.erro_heartbeat:
            raise FalhaRobo("Thread de heartbeat interrompida") from self.erro_heartbeat
        if not self.inst.isConnected(): raise FalhaRobo("Conexão NT4 perdida")
        if self.nonce.get() != self.sessao: raise FalhaRobo("O robô iniciou uma nova sessão")
        if self.estado.get() in ("FALHA", "DESARMADO"):
            raise FalhaRobo(self.mensagem.get() or "O robô desarmou a sessão")
        dono = self.dono.get()
        if dono and dono != self.cliente: raise FalhaRobo("Outro cliente possui a sessão")

    def _enviar(self, acao, ganhos, alvo=0, duracao=0):
        self._conferir()
        self.contador += 1
        id_pedido = self.contador
        self.pedidos.set(["1", self.sessao, self.cliente, str(id_pedido), acao,
                          *map(repr, ganhos.valores()), repr(alvo), repr(duracao)])
        self.inst.flush()
        limite = time.monotonic() + 5
        while time.monotonic() < limite:
            self._conferir()
            for registro in self.resposta.readQueue():
                r = registro.value
                if len(r) == 5 and r[:3] == [self.sessao, self.cliente, str(id_pedido)]:
                    if r[3] != "ACEITO": raise FalhaRobo("Pedido recusado pelo robô: " + r[4])
                    return id_pedido
            time.sleep(0.005)
        raise FalhaRobo("Sem confirmação do pedido; não reenviar automaticamente")

    def ensaio(self, ganhos: Ganhos, alvo: float, duracao: float, pontuar=True):
        self.config.validar(ganhos, [alvo], duracao)
        self.amostras.readQueue()
        id_ensaio = self._enviar("ENSAIO", ganhos, alvo, duracao)
        dados = {}
        limite = time.monotonic() + duracao + 5
        while time.monotonic() < limite:
            self._conferir()
            for registro in self.amostras.readQueue():
                if (len(registro.value) != 16 or registro.value[0] != id_ensaio
                        or registro.value[15] != self.chave_sessao): continue
                amostra = Amostra.ler(registro.value)
                if amostra.alvo != alvo or (amostra.p, amostra.i, amostra.d) != ganhos.valores():
                    raise FalhaRobo("Amostra não corresponde ao alvo/PID solicitado")
                if amostra.sequencia in dados and dados[amostra.sequencia] != amostra:
                    raise FalhaRobo("Mesma sequência recebeu duas amostras diferentes")
                dados[amostra.sequencia] = amostra
            final = self.resultado.get()
            if len(final) == 5 and final[0] == id_ensaio and final[4] == self.chave_sessao:
                if final[1] != 3: raise FalhaRobo("Ensaio encerrado com falha")
                quantidade = int(final[2])
                if final[2] != quantidade or final[3] != quantidade:
                    raise FalhaRobo("Resultado do robô inconsistente")
                if len(dados) >= quantidade:
                    amostras = [dados[k] for k in sorted(dados)]
                    conferir_coleta(amostras, self.config, quantidade)
                    if abs(amostras[-1].tempo - duracao) > self.config.valores[12] * 4:
                        raise FalhaRobo("Duração recebida não corresponde ao ensaio solicitado")
                    return amostras, avaliar(amostras, self.config, quantidade) if pontuar else None
            time.sleep(0.005)
        raise FalhaRobo("Coleta incompleta ou ensaio sem resultado; nenhuma pontuação foi aceita")

    def aplicar(self, ganhos):
        self.config.validar(ganhos, [sum(self.config.valores[:2]) / 2], self.config.valores[11])
        self._enviar("APLICAR", ganhos)

    def close(self):
        if self.fechado: return
        self.fechado = True
        try:
            if self.sessao and hasattr(self, "pedidos") and self.dono.get() == self.cliente:
                if not self.inst.isConnected():
                    raise FalhaRobo("Conexão perdida; parada e restauração não confirmadas pelo robô")
                if self.estado.get() == "DESARMADO":
                    return
                self.contador += 1
                self.pedidos.set(["1", self.sessao, self.cliente, str(self.contador), "PARAR", "0", "0", "0", "0", "0"])
                self.inst.flush()
                limite = time.monotonic() + 5
                while time.monotonic() < limite:
                    if not self.inst.isConnected() or self.nonce.get() != self.sessao:
                        raise FalhaRobo("Sessão encerrada sem confirmação da restauração")
                    estado = self.estado.get()
                    if estado == "DESARMADO":
                        return
                    if estado == "FALHA":
                        raise FalhaRobo("Robô encerrou em falha: " + self.mensagem.get())
                    time.sleep(0.01)
                raise FalhaRobo("Sem confirmação de parada e restauração do controlador")
        finally:
            self.fim.set()
            if self.thread: self.thread.join(timeout=1)
            for recurso in reversed(self.recursos):
                try: recurso.close()
                except Exception: pass
            self.inst.stopClient()
            import ntcore
            ntcore.NetworkTableInstance.destroy(self.inst)

    def __enter__(self): return self
    def __exit__(self, *_): self.close()


class Registro:
    def __init__(self, pasta, config):
        self.pasta = Path(pasta)
        self.pasta.mkdir(parents=True, exist_ok=False)
        self.config = config
        self.ensaios = []
        self.ultimas = []
        self.veredito = "EM_ANDAMENTO"
        self.ganhos_aprovados = None
        (self.pasta / "configuracao.json").write_text(json.dumps(asdict(config), indent=2), encoding="utf-8")

    def ensaio(self, amostras, metricas):
        self.ultimas = amostras
        numero = len(self.ensaios) + 1
        nome = f"ensaio-{numero:04d}"
        with (self.pasta / (nome + ".csv")).open("w", newline="", encoding="utf-8") as f:
            campos = list(asdict(amostras[0]))
            escritor = csv.DictWriter(f, fieldnames=campos)
            escritor.writeheader()
            escritor.writerows(asdict(a) for a in amostras)
        resumo = {"arquivo": nome + ".csv", "ganhos": {"p": amostras[0].p, "i": amostras[0].i, "d": amostras[0].d},
                  "alvo": amostras[0].alvo,
                  "tipo": "avaliacao" if metricas is not None else "preparacao",
                  "metricas": asdict(metricas) if metricas is not None else None}
        self.ensaios.append(resumo)
        (self.pasta / (nome + ".json")).write_text(json.dumps(resumo, indent=2), encoding="utf-8")
        self._relatorio(amostras)

    def finalizar(self, resultado, aceitacao):
        dados = asdict(resultado)
        dados["candidato"] = dados.pop("melhor")
        self.ganhos_aprovados = asdict(resultado.melhor) if aceitacao["aprovado"] else None
        dados["ganhos_aprovados"] = self.ganhos_aprovados
        self.veredito = "APROVADO NOS ENSAIOS" if aceitacao["aprovado"] else "INCONCLUSIVO — SEM GANHOS APROVADOS"
        dados.update({"backend": self.config.backend, "unidade": self.config.unidade,
                      "protocolo": 1, "politica_ao_desarmar": "restaurar_parametros_originais_sem_gravar_flash",
                      "aceitacao": aceitacao,
                      "uso": "Só use no projeto ganhos com aceitacao.aprovado=true; não transferir entre backends/unidades."})
        (self.pasta / "melhor-pid.json").write_text(json.dumps(dados, indent=2), encoding="utf-8")
        self._relatorio(self.ultimas)

    def concluir_ensaio(self):
        self.veredito = "ENSAIO CONCLUÍDO — SEM OTIMIZAÇÃO OU GANHOS APROVADOS"
        self._relatorio(self.ultimas)

    def falhar(self, erro):
        self.veredito = "FALHA — SEM GANHOS APROVADOS"
        self.ganhos_aprovados = None
        (self.pasta / "falha.json").write_text(json.dumps({"erro": str(erro),
            "tipo": type(erro).__name__, "nenhum_resultado_aprovado": True}, ensure_ascii=False, indent=2), encoding="utf-8")
        arquivo = self.pasta / "melhor-pid.json"
        if arquivo.exists():
            dados = json.loads(arquivo.read_text(encoding="utf-8"))
            dados["ganhos_aprovados"] = None
            dados["aceitacao"] = {"aprovado": False, "estado": "falha_sessao", "motivo": str(erro)}
            arquivo.write_text(json.dumps(dados, indent=2), encoding="utf-8")
        self._relatorio(self.ultimas)

    def _relatorio(self, amostras):
        status = (f'<div role="status"><h2>{html.escape(self.veredito)}</h2>'
                  f'<p>Ganhos aprovados: {html.escape(str(self.ganhos_aprovados))}</p></div>')
        if not amostras:
            (self.pasta / "relatorio.html").write_text('<!doctype html><meta charset="utf-8"><title>AutoPrecision</title>'
                + status + '<p>Nenhum ensaio completo foi registrado.</p>', encoding="utf-8")
            return
        def linha(valores, cor):
            pontos = " ".join(f"{20 + 760 * a.tempo / tempo:.2f},{220 - 200 * (v - minimo) / amplitude:.2f}"
                              for a, v in zip(amostras, valores))
            return f'<polyline fill="none" stroke="{cor}" stroke-width="2" points="{pontos}"/>'
        minimo = min(min(a.posicao, a.referencia, a.alvo) for a in amostras)
        maximo = max(max(a.posicao, a.referencia, a.alvo) for a in amostras)
        amplitude, tempo = max(maximo - minimo, 1e-9), max(amostras[-1].tempo, 1e-9)
        linhas = linha([a.posicao for a in amostras], "#2166d1")
        linhas += linha([a.referencia for a in amostras], "#d95f02")
        linhas += linha([a.alvo for a in amostras], "#2e8540")
        tabela = ""
        for i, e in enumerate(self.ensaios, 1):
            m = e["metricas"]
            nota = f'{m["pontuacao"]:.5g}' if m else "preparação"
            estabilizou = str(m["estabilizou"]) if m else "—"
            tabela += (f'<tr><td><a href="{e["arquivo"]}">{i}</a></td><td>{html.escape(str(e["ganhos"]))}</td>'
                       f'<td>{e["alvo"]:.4g}</td><td>{nota}</td><td>{estabilizou}</td></tr>')
        pagina = f'''<!doctype html><meta charset="utf-8"><title>AutoPrecision</title>
        <style>body{{font:16px system-ui;max-width:1000px;margin:36px auto;padding:20px}}td,th{{padding:8px;border-bottom:1px solid #ccc}}svg{{width:100%;background:#f5f7fa}}table{{border-collapse:collapse}}</style>
        <h1>AutoPrecision</h1>{status}<p>Backend: {html.escape(self.config.backend)} · Unidade: {html.escape(self.config.unidade)}</p>
        <p>Último ensaio — azul: posição; laranja: referência suave; verde: alvo.</p>
        <svg viewBox="0 0 800 240" role="img" aria-label="Posição, referência e alvo do último ensaio">{linhas}</svg>
        <p>Tempo: 0–{tempo:.3g}s. Posição: {minimo:.3g}–{maximo:.3g}.</p>
        <table><tr><th>Ensaio/CSV</th><th>PID</th><th>Alvo</th><th>Pontuação ↓</th><th>Estabilizou</th></tr>{tabela}</table>'''
        (self.pasta / "relatorio.html").write_text(pagina, encoding="utf-8")
