---
title: "API — Controles"
tags:
  - luverlib
  - api
revisado: 2026-11-13
---

# API — Controles

Leia primeiro [Controle Xbox](../03%20-%20Modulos/Controle%20Xbox.md). Esta referência lista as classes e suas assinaturas públicas declaradas no código consultado. Corpos de métodos foram omitidos. Records também geram acessores com os nomes de seus componentes.


## ControleXbox


Fornece leituras, perfis e triggers dos controles Xbox.


Fonte: [ControleXbox](../99%20-%20Anexos/Fontes/src/main/java/frc/robot/luverlib/controles/ControleXbox.java)


```java
public ControleXbox(int porta);
public ControleXbox(int porta, EventLoop eventos);
public void definirPerfil(Analogico analogico, PerfilEixo perfil);
public double valorAnalogico(Analogico analogico);
public double analogicoEsquerdoX();
public double analogicoEsquerdoY();
public double analogicoDireitoX();
public double analogicoDireitoY();
public double valorGatilhoEsquerdo();
public double valorGatilhoDireito();
public Trigger botao(Botao botao);
public Trigger botaoA();
public Trigger botaoB();
public Trigger botaoX();
public Trigger botaoY();
public Trigger botaoLB();
public Trigger botaoRB();
public Trigger botaoVoltar();
public Trigger botaoIniciar();
public Trigger cliqueAnalogicoEsquerdo();
public Trigger cliqueAnalogicoDireito();
public Trigger gatilhoEsquerdo();
public Trigger gatilhoDireito();
public Trigger gatilhoEsquerdo(double limiar);
public Trigger gatilhoDireito(double limiar);
public Trigger direcional(Direcao direcao);
public Trigger direcionalCima();
public Trigger direcionalDireita();
public Trigger direcionalBaixo();
public Trigger direcionalEsquerda();
public boolean estaConectado();
public void definirVibracao(double esquerda, double direita);
public void pararVibracao();
public XboxController obterDispositivo();
```


## PerfilEixo


Define e aplica zona morta, curva, limite e inversão de um analógico.


Fonte: [PerfilEixo](../99%20-%20Anexos/Fontes/src/main/java/frc/robot/luverlib/controles/PerfilEixo.java)


```java
public record PerfilEixo( double zonaMorta, double expoente, double limiteSaida, boolean invertido);
public PerfilEixo;
public double aplicar(double entrada);
```


## VariaveisControle


Mantém referências de compatibilidade aos ajustes em Constants.Controle e Constants.Tracao.


Fonte: [VariaveisControle](../99%20-%20Anexos/Fontes/src/main/java/frc/robot/luverlib/controles/VariaveisControle.java)


Ajustes e valores: [Referencia completa de Constants](../02%20-%20Entenda%20o%20projeto/Referencia%20completa%20de%20Constants.md).

---

← [Página inicial](../00%20-%20Comece%20aqui.md) · [Fontes e versão deste guia](Fontes%20e%20versao.md)
