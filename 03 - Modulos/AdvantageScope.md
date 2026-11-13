---
title: "AdvantageScope"
tags:
  - luverlib
  - modulo
revisado: 2026-11-13
---

# AdvantageScope

## O que é

O aplicativo AdvantageScope permite visualizar os dados do robô. A classe `AdvantageScope` da LuverLib publica números, textos, booleanos e estruturas WPILib por NT4. `GravacaoAdvantageScope` ajuda a registrar dados em `.wpilog`.

## O que configurar

Em `Constants.AdvantageScope`, revise `RAIZ_TABELA`, `INTERVALO_SEGUNDOS`, `ATUALIZACAO_AUTOMATICA` e `GRAVAR_JOYSTICKS`. Os nomes dos sinais devem representar o mecanismo e a grandeza. Informe unidades explícitas nos números.

## Como usar ao vivo

Crie o publicador uma vez. Registre fornecedores que leem dados reais; eles serão consultados pelo scheduler quando o automático estiver habilitado:

```java
package guia;

import edu.wpi.first.math.geometry.Pose2d;
import java.util.function.Supplier;
import frc.robot.luverlib.advantagescope.AdvantageScope;
import frc.robot.luverlib.motores.neo.MotorNeo;

public final class LogsExemplo implements AutoCloseable {
  private final AdvantageScope dados = new AdvantageScope();

  public LogsExemplo(MotorNeo motorExistente, Supplier<Pose2d> poseReal) {
    try {
      dados.registrarMotorNeo("Mecanismo", motorExistente);
      dados.registrarNumero("Mecanismo/RPM", "rpm", motorExistente::obterVelocidadeRPM);
      dados.registrarPose2d("Robo/Pose", poseReal);
      dados.registrarEstadoRobo();
    } catch (RuntimeException | Error falha) {
      try { dados.close(); } catch (RuntimeException | Error limpeza) { falha.addSuppressed(limpeza); }
      throw falha;
    }
  }

  @Override public void close() {
    dados.close();
  }
}
```

Conecte o aplicativo à rede NT do robô e procure a raiz configurada, normalmente `LuverLib`. Escolha os sinais publicados para os gráficos. Para geometrias, use os canais tipados de pose, rotação, velocidades e módulos swerve; a biblioteca publica também os schemas das estruturas.

Sem scheduler, use uma configuração com automático desativado e chame `atualizar()` no ciclo. Evite duas formas de atualização do mesmo publicador. `CanalAdvantageScope` também permite receber valores de eventos com `accept(valor)`; `estaValido()` e `obterQuantidadeFalhas()` ajudam a identificar problemas no sinal.

## Como gravar e consultar depois

Na inicialização do robô, antes dos registros, escolha uma única inicialização global de logs. Se o projeto ainda não faz isso, pode usar:

```java
GravacaoAdvantageScope.iniciar();
```

A WPILib administra o arquivo e o diretório padrão. Copie o `.wpilog` produzido pelo projeto e abra-o no aplicativo AdvantageScope para analisar os sinais ao longo do tempo. Se já há um `DataLog` próprio, `anexar(rede, log)` é a alternativa de captura; não duplique captura da mesma rede no mesmo log.

A gravação permite rever dados; não transforma o programa em replay determinístico. `AdvantageKit.json` estar presente nos vendordeps não significa que a aplicação usa uma arquitetura de replay AdvantageKit.

**Depois:** [Observar e registrar um teste](../04%20-%20Tutoriais/Observar%20e%20registrar%20um%20teste.md) · [API - AdvantageScope](../05%20-%20Referencia/API%20-%20AdvantageScope.md).

---

← [Página inicial](../00%20-%20Comece%20aqui.md) · [Fontes e versão deste guia](../05%20-%20Referencia/Fontes%20e%20versao.md)
