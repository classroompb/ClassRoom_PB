# Guia de Estilo — ClassRoomPB (Team4)

> Baseado no Cap. 8 de *Engenharia de Software no Google* (Winters, Manshreck &
> Wright, O'Reilly, 2020). A ideia central: **"Style is not about taste; it's
> about consistency."** A base de código deve parecer escrita por uma única voz.

Este guia não é preferência pessoal de ninguém da equipe. É infraestrutura: ele
existe para reduzir atrito em code review, acelerar a leitura (gastamos ~80% do
tempo lendo código, não escrevendo) e deixar as ferramentas trabalharem por nós.

---

## Como rodar (o dia a dia)

O projeto traz o **Maven Wrapper** — não precisa ter Maven instalado, só o JDK 17+.

```bash
./mvnw spotless:check     # verifica a formatação (não altera nada)
./mvnw spotless:apply     # formata os arquivos automaticamente
./mvnw checkstyle:check   # roda o linter (nomes, chaves, imports...)
./mvnw test               # compila e roda os testes JUnit
```

No Windows/PowerShell use `.\mvnw.cmd ...`.

Antes de abrir um PR: rode `./mvnw spotless:apply` e `./mvnw test`.

---

## As duas camadas (e a divisão de trabalho)

O capítulo separa as regras em categorias. Aqui elas viram **duas ferramentas**,
e cada uma cuida de uma coisa — uma **não** duplica a outra.

| Camada | Ferramenta | Cuida de | Natureza |
|---|---|---|---|
| **1. Formatação** | Spotless + google-java-format | indentação, espaços, quebra de linha, posição de chaves, ordem/limpeza de imports | mecânica — **a ferramenta decide** |
| **2. Linter** | Checkstyle | nomes, chaves obrigatórias, import estrela, `==` em String, fall-through | julgamento — regras que o formatter não enxerga |

> **Por que não deixar o Checkstyle cuidar de indentação também?** Porque aí as
> duas ferramentas brigariam. Formatação tem um dono só: o Spotless.

---

## Camada 1 — Formatação

**Regra (must):** todo arquivo `.java` passa pelo `google-java-format`, estilo
**AOSP (4 espaços)**.

- **Por que google-java-format?** É a ferramenta que o próprio capítulo cita
  como "solução definitiva para formatação" em Java. Acaba o debate de
  tabs vs. espaços, chaves, etc. — em review e fora dele.
- **Por que AOSP (4 espaços) e não o Google canônico (2 espaços)?** O código do
  ClassRoomPB já era todo em 4 espaços. O capítulo diz: *consistência local
  supera a global em conflito* — não vale reformatar tudo para 2 espaços só
  para seguir o guia ao pé da letra. 4 espaços mantém o diff pequeno e o
  histórico legível.

---

## Camada 2 — Linter (Checkstyle)

Config em [`config/checkstyle/checkstyle.xml`](config/checkstyle/checkstyle.xml).
Cada regra tem um porquê — sem "porque sim":

| Regra | Nível | Por quê |
|---|---|---|
| `TypeName`, `MethodName`, `MemberName`, `ConstantName`… | must | Nome previsível = código previsível. `Curso`, `cadastrar`, `MAX_VAGAS`. |
| `AvoidStarImport` | must | `import x.*` esconde dependências e atrapalha análise estática. |
| `UnusedImports`, `RedundantImport` | must | Import morto é ruído que confunde quem lê. |
| `NeedBraces` | must | `if (x) faz();` sem chaves é bug clássico ao adicionar a 2ª linha. Sempre `{ }`. |
| `EmptyBlock` | should | Bloco vazio quase sempre é erro; se for intencional, comente o porquê. |
| `FallThrough` | must | Queda silenciosa entre `case`s é bug difícil de ver. |
| `StringLiteralEquality` | must | `==` em String compara referência, não conteúdo. Use `.equals`. |
| `OneStatementPerLine` | should | Uma instrução por linha — diffs e debug mais limpos. |

`severity = warning` e `failOnViolation = false` **na Fase 1** (ver rollout): o
linter avisa, mas ainda não quebra o build, porque o repo inteiro ainda não foi
migrado.

---

## Os três níveis (não confundir)

Direto do capítulo — misturar os três gera guia que ninguém segue:

- **must** — obrigatório, sem exceção sem aprovação.
- **should** — orientação forte; desvie só com bom motivo, documentado no PR.
- **may** — recomendação, fica a critério.

---

## Rollout — este guia é um documento vivo

O capítulo é explícito: **não force migração parcial**, e reformatar tudo é uma
*Large-Scale Change* deliberada. Por isso a adoção é em fases:

- **Fase 1 (atual):** ferramenta instalada e documentada; arquivos da Release 1
  (parte do Danilo) já migrados como referência. `spotless`/`checkstyle` rodam
  sob demanda, em modo aviso.
- **Fase 2 (coordenada com o time, após fechar a Sprint de Frequência):**
  rodar `./mvnw spotless:apply` no repositório inteiro num único commit de
  migração, e então **tornar a checagem bloqueante** — descomentar o
  `<execution><goal>check</goal>` do Spotless no `pom.xml` e ligar
  `failOnViolation=true` no Checkstyle. A partir daí o `./mvnw verify` rejeita
  código fora do padrão, como o capítulo recomenda.
- **Fase 3 (ideal):** rodar no CI (GitHub Actions), para que o estilo seja
  verificado em todo push/PR — não dependa de lembrar de rodar local.

Mudou de ideia sobre uma regra? Abra um PR alterando este guia + a config,
com a justificativa. O guia evolui com a linguagem e com o time.
