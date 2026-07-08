# Contribuindo — ClassRoomPB (Team4)

Padrao de codigo do time. As regras e a justificativa completa estao em
[STYLE_GUIDE.md](STYLE_GUIDE.md). Aqui fica so o "como fazer" do dia a dia.

## Setup (uma vez)

- **JDK 17+** instalado. Nao precisa de Maven: o projeto traz o wrapper (`mvnw`).
- Na IDE, instale o plugin **google-java-format** e ative o estilo **AOSP**
  (IntelliJ: `Settings > Plugins`; Eclipse: marketplace).
- Opcional: plugin **SonarLint** na IDE, pega problemas enquanto voce digita.

## Fluxo de cada commit

```bash
./mvnw spotless:apply     # formata o codigo automaticamente
./mvnw test               # compila e roda os testes JUnit
```

No Windows (PowerShell) use `.\mvnw.cmd ...`.

## Antes de abrir um Pull Request

```bash
./mvnw spotless:check     # confirma que esta formatado
./mvnw checkstyle:check   # confere nomes, imports, chaves, etc.
```

Passou nos dois? Abra o PR. Uma branch por dev por feature (ex.: `danilo-rf31`).

## Severidade (fase atual)

Hoje as checagens **avisam, mas nao bloqueiam** o build (Fase 1 do rollout).
Mesmo assim, rode `spotless:apply` antes de commitar. Quando o time promover
para bloqueante (Fase 2), PR fora do padrao para de passar no CI. O passo a
passo do rollout esta no [STYLE_GUIDE.md](STYLE_GUIDE.md).
