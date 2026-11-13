---
title: "Ajustar PID com AutoPrecision"
tags:
  - luverlib
  - tutorial
revisado: 2026-11-13
---

# Ajustar PID com AutoPrecision

## Passo 1 — entenda o mecanismo

Defina zero/referência, unidade de posição, faixa física, redução, slot e limites medidos. Declare os parâmetros em `Constants`. Leia a [explicação de cada campo](../05%20-%20Referencia/Configuracao%20AutoPrecision%20campo%20a%20campo.md) antes de construir a configuração. Integre o motor já existente como em [AutoPrecision](../03%20-%20Modulos/AutoPrecision.md).

## Passo 2 — prepare o PC

Linux/macOS, a partir da raiz do projeto:

```sh
cd ferramentas/autoprecision
python3.13 -m venv .venv
source .venv/bin/activate
python -m pip install -r requirements.txt
python autoprecision.py --help
```

Windows, PowerShell, dentro de `ferramentas/autoprecision`:

```powershell
py -3.13 -m venv .venv
.venv\Scripts\Activate.ps1
python -m pip install -r requirements.txt
python autoprecision.py --help
```

Use o ambiente virtual para não misturar versões de NT4. `requirements.txt` fixa `pyntcore==2026.2.1`.

## Passo 3 — aprenda pela simulação

O comando abaixo usa uma bancada simulada no PC, sem se conectar ao robô. Os ganhos encontrados pertencem ao modelo simulado, não ao SPARK real:

```sh
python autoprecision.py simular --alvos=-0.20,0.20 --duracao 4 --avaliacoes 18 --confirmacoes 2
```

## Passo 4 — confira a comunicação sem mover

Com o servidor Java integrado e a rede do robô acessível, substitua `IP_DO_ROBORIO` pelo endereço real:

```sh
python autoprecision.py monitorar --host IP_DO_ROBORIO --nome Mecanismo --segundos 15
```

`Mecanismo` deve ser o mesmo nome usado no construtor Java. O monitor não arma, não toma posse nem mantém heartbeat de uma sessão de movimento. Para a conexão usual NT4, a CLI usa a porta 5810; a rede precisa permitir o acesso.

## Passo 5 — prepare o ensaio real

Em Test habilitado, mantenha a autorização local. Escolha alvos estritamente dentro da faixa física e duração compatível com a configuração. A sintaxe é:

```sh
# Sintaxe didática: substitua IP, nome e os alvos pelos valores verificados do mecanismo.
python autoprecision.py ensaio --host IP_DO_ROBORIO --nome Mecanismo --alvos=0.10 --duracao 4
python autoprecision.py otimizar --host IP_DO_ROBORIO --nome Mecanismo --alvos=-0.20,0.20 --duracao 4 --avaliacoes 18 --confirmacoes 2
```

Os alvos numéricos acima são exemplos de escrita, não posições aprovadas para hardware. Use `--alvos=...` com `=` quando houver negativos. Omitir `--ganhos` usa os ganhos iniciais definidos pelo robô.

A otimização precisa de ao menos dois alvos distintos, separados por quatro tolerâncias de posição e com margem no deslocamento permitido. Cada avaliação com dois alvos faz preparação, ida e retorno: três movimentos. Com 18 avaliações de até 4 segundos por movimento, o orçamento pode chegar a 54 movimentos e 216 segundos, além da comunicação/configuração.

## Passo 6 — leia os arquivos

O padrão é `~/Downloads/luverlib-AAAA-MM-DD/autoprecision/sessoes/HORARIO/`. `--saida` permite indicar uma pasta nova.

| Arquivo | O que conferir |
| --- | --- |
| `configuracao.json` | Unidade, backend e limites usados |
| `ensaio-NNNN.csv` e `.json` | Amostras e métricas de cada movimento |
| `relatorio.html` | Gráficos, histórico e veredito |
| `melhor-pid.json` | `aceitacao.aprovado` e `ganhos_aprovados` |
| `falha.json` | Motivo do erro, quando houver |

Código de saída 0 indica otimização aprovada, 2 indica inconclusiva e 1 indica erro. Um ensaio isolado apenas coleta métricas. A CLI exige confirmação do desarme antes de finalizar uma aprovação.

## Passo 7 — incorpore o que foi revisado

Depois de desarmar, os parâmetros temporários do ensaio são restaurados. Revise o resultado e salve os ganhos aprovados em `Constants` junto às condições do mecanismo. Preserve backend, slot, unidades, redução, feedforward, rampas e política de compensação usados na avaliação. O backend desta versão ensaia sem compensação de tensão.

O arquivo original [ExemploAutoPrecisionNeo.java](../99%20-%20Anexos/Fontes/examples/ExemploAutoPrecisionNeo.java) mostra `aplicarGanhosAprovados(...)` e o uso de perfil na operação. A aplicação exige robô desabilitado e nenhuma sessão ativa. Persistir em flash deve ser uma escolha explícita. Registre e valide a operação física depois da incorporação.

O **AutoPrecision da LuverLib ajusta automaticamente o PID de posição**, fazendo movimentos de teste e medindo quais ganhos deixam o mecanismo mais rápido, preciso e suave. Na versão atual, funciona com **NEO + SPARK MAX**. Conferi a documentação do seu projeto (Projects/LuverLib/README.md:223) e a implementação.

O processo funciona assim:

1. **Você configura o mecanismo:** redução, posição do encoder, faixa de movimento, limites de potência, velocidade, corrente e temperatura, além dos ganhos iniciais e tolerâncias. Ele precisa desses dados para conduzir os ensaios.
2. **Você autoriza os movimentos:** habilita o modo **Test** na Driver Station e mantém um botão local pressionado.
3. **O PC solicita os testes:** um programa Python conversa com o roboRIO por NetworkTables. O Java gera uma trajetória com aceleração e desaceleração limitadas, e o **SPARK MAX executa o PID internamente**.
4. **Ele compara os resultados:** mede erro de posição, quanto passou do alvo, tempo para estabilizar, mudanças bruscas na saída, saturação e esforço do motor.
5. **Ele ajusta os ganhos:** experimenta aumentar e diminuir **P, I e D**, um de cada vez, e mantém as combinações com melhor pontuação. Depois repete os testes da configuração inicial e da candidata para confirmar a melhora. Essa busca está no otimizador Python (Projects/LuverLib/ferramentas/autoprecision/otimizador.py:193).

Por exemplo, num braço, você define duas posições permitidas. Ele faz movimentos de ida e volta e procura ganhos que cheguem ao destino com pouco erro e pouca oscilação. A aprovação exige melhora confirmada e estabilização dentro das tolerâncias em todos os alvos das confirmações.

**Ao terminar, ele gera gráficos, medições e um arquivo com os ganhos aprovados.** O controlador restaura seus parâmetros anteriores; incorporar os novos ganhos ao código é uma etapa posterior.

Se o botão for solto, o robô sair de Test, faltar comunicação com o supervisor ou algum limite for violado, o ensaio é interrompido.

Você também pode pedir a um agente como eu para operar essa ferramenta pelo PC. A busca matemática já existe no AutoPrecision; a IA pode preparar e acompanhar a sessão. A documentação atual registra validação em simulação, com calibração física ainda dependente do mecanismo real.

---

← [Página inicial](../00%20-%20Comece%20aqui.md) · [Fontes e versão deste guia](../05%20-%20Referencia/Fontes%20e%20versao.md)
