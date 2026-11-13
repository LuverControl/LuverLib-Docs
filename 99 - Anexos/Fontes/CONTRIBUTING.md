# Contribuir com a LuverLib

Use Java 17, WPILib 2026 e Python 3.13. Abra uma issue descrevendo a alteração,
crie uma branch e envie um pull request para `main`. Descreva o problema,
a mudança observável, as verificações e qualquer efeito em mecanismos reais.

Na raiz do projeto:

```sh
./gradlew --no-daemon check build
python3.13 -m pip install -r ferramentas/autoprecision/requirements.txt
python3.13 -m unittest discover -s ferramentas/autoprecision -p 'test_*.py' -v
```

O `check` também compila `examples/`. O CI executa o build e os testes Java
em Linux e Windows, verifica a CLI Python e valida os workflows. O check
`CI completo` precisa passar para integrar na `main`.

Mantenha as proteções de habilitação, parada, autorização local e limites
físicos. Testes de desktop validam o software em simulação; descreva
separadamente qualquer teste com roboRIO, sensores ou motores reais.

Não envie credenciais, arquivos `.env`, dados pessoais, logs de operadores
ou configurações locais. Atualizações de temporada precisam alinhar GradleRIO,
vendordeps, Python/pyntcore e os testes.

Os responsáveis revisam o pull request e integram por squash. As versões são
preparadas conforme a seção de entregas do README. Contribuições ao código da
equipe seguem a licença BSD 3-Clause do projeto; dependências de terceiros
mantêm suas próprias licenças.
