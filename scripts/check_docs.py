"""Confere a documentação Obsidian/GitHub sem dependências externas."""
import hashlib
import json
from pathlib import Path
import re
from urllib.parse import unquote, urlsplit

ROOT = Path(__file__).resolve().parents[1]
VAULT = ROOT


def exigir(condicao, mensagem):
    if not condicao:
        raise ValueError(mensagem)


def destino_local(texto, origem):
    url = urlsplit(texto.strip("<>"))
    if url.scheme or url.netloc:
        exigir(url.scheme in ("http", "https", "mailto"), "Esquema de link não permitido: " + texto)
        return None
    destino = (origem.parent / unquote(url.path)).resolve() if url.path else origem.resolve()
    exigir(destino.is_relative_to(ROOT), "Link fora do repositório: " + texto)
    exigir(destino.exists(), f"Link ausente em {origem.relative_to(ROOT)}: {texto}")
    if url.fragment and destino.suffix == ".md":
        titulos = re.findall(r"^#{1,6}\s+(.+?)\s*$", destino.read_text(encoding="utf-8"), re.M)
        slugs = {re.sub(r"[^\w\- ]", "", t.casefold()).replace(" ", "-") for t in titulos}
        exigir(unquote(url.fragment) in slugs, "Seção ausente: " + texto)
    return destino


def verificar():
    manifesto = json.loads((VAULT / "fontes-manifesto.json").read_text(encoding="utf-8"))
    notas = [VAULT / p for p in manifesto["notes_authored"]]
    notas += [VAULT / p for p in ("98 - Modelos/Ficha de mecanismo.md",
              "98 - Modelos/Registro de teste.md", "99 - Anexos/Leia os anexos.md", "README.md")]
    links = 0
    for nota in notas:
        texto = nota.read_text(encoding="utf-8")
        exigir(texto.count("```") % 2 == 0, "Bloco de código incompleto: " + str(nota))
        texto = re.sub(r"```.*?```", "", texto, flags=re.S)
        exigir("[[" not in texto, "Use links Markdown navegáveis no GitHub: " + str(nota))
        for link in re.findall(r"!?\[[^\]]*\]\((<[^>]+>|[^\s)]+)(?:\s+\"[^\"]*\")?\)", texto):
            if destino_local(link, nota) is not None:
                links += 1

    for config in (VAULT / ".obsidian").glob("*.json"):
        json.loads(config.read_text(encoding="utf-8"))
    modelos = json.loads((VAULT / ".obsidian/templates.json").read_text(encoding="utf-8"))
    exigir((VAULT / modelos["folder"]).resolve().is_relative_to(VAULT), "Modelos fora do vault")
    exigir((VAULT / modelos["folder"]).is_dir(), "Pasta de modelos ausente")
    marcadores = json.loads((VAULT / ".obsidian/bookmarks.json").read_text(encoding="utf-8"))
    for item in marcadores["items"]:
        caminho = (VAULT / item["path"]).resolve()
        exigir(caminho.is_relative_to(VAULT) and caminho.is_file(), "Marcador inválido: " + item["path"])
    exigir(not list(VAULT.rglob("workspace*.json")), "Estado pessoal do Obsidian não deve ser publicado")

    canvas = json.loads((VAULT / "Mapa da LuverLib.canvas").read_text(encoding="utf-8"))
    ids = [n["id"] for n in canvas["nodes"]]
    exigir(len(ids) == len(set(ids)), "IDs duplicados no Canvas")
    for no in canvas["nodes"]:
        exigir(all(isinstance(no[k], (int, float)) for k in ("x", "y", "width", "height")), "Geometria inválida no Canvas")
        exigir(no["width"] > 0 and no["height"] > 0, "Dimensões inválidas no Canvas")
        if no["type"] == "file":
            arquivo = (VAULT / no["file"]).resolve()
            exigir(arquivo.is_relative_to(VAULT) and arquivo.is_file(), "Arquivo do Canvas ausente: " + no["file"])
    for aresta in canvas["edges"]:
        exigir(aresta["fromNode"] in ids and aresta["toNode"] in ids, "Aresta sem nó no Canvas")

    fontes = VAULT / "99 - Anexos/Fontes"
    for relativo, esperado in manifesto["fontes"].items():
        arquivo = (fontes / relativo).resolve()
        exigir(arquivo.is_relative_to(fontes), "Fonte fora do snapshot: " + relativo)
        exigir(arquivo.is_file(), "Fonte anexada ausente: " + relativo)
        exigir(hashlib.sha256(arquivo.read_bytes()).hexdigest() == esperado, "Fonte anexada alterada: " + relativo)
    resumo = {"notas_verificadas": len(notas), "links_locais": links,
              "fontes_com_hash": len(manifesto["fontes"]), "nos_canvas": len(ids),
              "arestas_canvas": len(canvas["edges"]), "estado": "aprovado"}
    print(json.dumps(resumo, ensure_ascii=False, indent=2))
    return resumo


if __name__ == "__main__":
    verificar()
