# Regras de Estilo — ClassRoomPB

Conjunto de regras de estilo **objetivas e automatizáveis** para todo o projeto.
Cada regra indica a ferramenta capaz de validá-la automaticamente (Checkstyle,
PMD, Spotless, Google Java Format, SonarLint ou IntelliJ Inspections).

> Convenção de severidade sugerida: regras de formatação como `warning` no início
> da adoção, promovendo a `error` no CI quando o código já estiver conforme.

---

### Regra 01 — Nomes de classes em PascalCase

**Descrição**

Todo nome de classe, interface ou enum deve usar `PascalCase` (ex.: `FrequenciaService`).

**Motivação**

Padroniza a leitura e segue a convenção oficial da linguagem Java, evitando
divergência entre arquivos.

**Automatização**

* Checkstyle — `TypeName`
* PMD — `ClassNamingConventions`

**Exemplo correto**

```java
public class ConsultaFrequenciaService { }
```

**Exemplo incorreto**

```java
public class consulta_frequencia_service { }
```

---

### Regra 02 — Métodos e variáveis em camelCase

**Descrição**

Métodos, parâmetros e variáveis locais devem usar `camelCase`.

**Motivação**

Diferencia visualmente identificadores de membros de constantes e tipos.

**Automatização**

* Checkstyle — `MethodName`, `LocalVariableName`, `ParameterName`
* PMD — `MethodNamingConventions`

**Exemplo correto**

```java
public double calcularPercentualFrequencia(String codigoTurma) { }
```

**Exemplo incorreto**

```java
public double Calcular_Percentual_Frequencia(String CodigoTurma) { }
```

---

### Regra 03 — Constantes em UPPER_SNAKE_CASE

**Descrição**

Campos `static final` de tipos imutáveis devem usar `UPPER_SNAKE_CASE`.

**Motivação**

Sinaliza claramente valores constantes e compartilhados.

**Automatização**

* Checkstyle — `ConstantName`
* PMD — `FieldNamingConventions`

**Exemplo correto**

```java
public static final double FREQUENCIA_MINIMA = 75.0;
```

**Exemplo incorreto**

```java
public static final double frequenciaMinima = 75.0;
```

---

### Regra 04 — Pacotes em minúsculas

**Descrição**

Nomes de pacote devem conter apenas letras minúsculas, sem `_` ou caracteres maiúsculos.

**Motivação**

Mantém a organização de pacotes uniforme e compatível entre sistemas de arquivos.

**Automatização**

* Checkstyle — `PackageName`

**Exemplo correto**

```java
package org.example.classroompb.exception;
```

**Exemplo incorreto**

```java
package org.example.ClassRoomPB.Exception;
```

---

### Regra 05 — Proibir imports com asterisco

**Descrição**

Não usar imports do tipo `import pacote.*;`.

**Motivação**

Imports explícitos deixam claras as dependências da classe e evitam conflitos de nomes.

**Automatização**

* Checkstyle — `AvoidStarImport`
* PMD — `UnnecessaryImport` / SonarLint `java:S2208`

**Exemplo correto**

```java
import org.example.classroompb.model.Turma;
import org.example.classroompb.model.Aluno;
```

**Exemplo incorreto**

```java
import org.example.classroompb.model.*;
```

---

### Regra 06 — Proibir imports não utilizados

**Descrição**

Nenhum import declarado pode ficar sem uso.

**Motivação**

Reduz ruído e indica acoplamentos reais da classe.

**Automatização**

* Checkstyle — `UnusedImports`
* PMD — `UnusedImports` / SonarLint `java:S1128`

**Exemplo correto**

```java
import java.util.List;
List<String> codigos = List.of("CC101");
```

**Exemplo incorreto**

```java
import java.util.Map; // nunca utilizado
```

---

### Regra 07 — Ordem dos imports

**Descrição**

Imports devem ser ordenados alfabeticamente e agrupados (java, terceiros, projeto),
sem linhas em branco internas não previstas.

**Motivação**

Diffs menores e previsibilidade na localização de imports.

**Automatização**

* Spotless (`importOrder`)
* Checkstyle — `ImportOrder` / `CustomImportOrder`

**Exemplo correto**

```java
import java.util.List;

import org.example.classroompb.model.Turma;
```

**Exemplo incorreto**

```java
import org.example.classroompb.model.Turma;
import java.util.List;
```

---

### Regra 08 — Ordem dos membros da classe

**Descrição**

Membros devem seguir a ordem: campos `static`, campos de instância, construtores,
métodos. Métodos públicos antes dos privados.

**Motivação**

Estrutura previsível facilita navegação em qualquer classe do projeto.

**Automatização**

* Checkstyle — `DeclarationOrder`
* SonarLint — `java:S1213`

**Exemplo correto**

```java
public class Exemplo {
    private static final int MAX = 10;
    private int valor;
    public Exemplo() { }
    public int getValor() { return valor; }
    private void auxiliar() { }
}
```

**Exemplo incorreto**

```java
public class Exemplo {
    public Exemplo() { }
    private int valor;
    private static final int MAX = 10;
}
```

---

### Regra 09 — Tamanho máximo de método

**Descrição**

Métodos não podem ultrapassar 60 linhas (excluindo linhas em branco e comentários).

**Motivação**

Métodos curtos são mais testáveis e legíveis; tamanho excessivo indica múltiplas
responsabilidades.

**Automatização**

* Checkstyle — `MethodLength`
* PMD — `ExcessiveMethodLength`

**Exemplo correto**

```java
public boolean atingiuFrequenciaMinima() {
    return percentualFrequencia >= FREQUENCIA_MINIMA;
}
```

**Exemplo incorreto**

```java
public void processar() {
    // 120 linhas de lógica encadeada...
}
```

---

### Regra 10 — Tamanho máximo de classe

**Descrição**

Arquivos de classe não podem ultrapassar 400 linhas.

**Motivação**

Classes grandes acumulam responsabilidades e dificultam manutenção.

**Automatização**

* Checkstyle — `FileLength`
* PMD — `ExcessiveClassLength`

**Exemplo correto**

Uma classe de service focada (ex.: `ConsultaFrequenciaService`, ~150 linhas).

**Exemplo incorreto**

Uma classe `Util` com 1200 linhas e dezenas de métodos não relacionados.

---

### Regra 11 — Complexidade ciclomática máxima

**Descrição**

A complexidade ciclomática de um método não pode exceder 10.

**Motivação**

Limita ramificações, reduzindo a probabilidade de defeitos e o esforço de teste.

**Automatização**

* Checkstyle — `CyclomaticComplexity`
* PMD — `CyclomaticComplexity` / SonarLint `java:S3776`

**Exemplo correto**

```java
public String classificar(double media) {
    if (media >= 7.0) return "APROVADO";
    if (media >= 4.0) return "RECUPERACAO";
    return "REPROVADO";
}
```

**Exemplo incorreto**

```java
public String classificar(...) {
    if (...) { if (...) { if (...) { if (...) { /* muitos ramos aninhados */ } } } }
}
```

---

### Regra 12 — Proibir números mágicos

**Descrição**

Valores numéricos literais (exceto -1, 0, 1) não podem aparecer no meio do código;
devem ser constantes nomeadas.

**Motivação**

Constantes dão significado ao número e centralizam a manutenção (ex.: a RN08, 75%).

**Automatização**

* Checkstyle — `MagicNumber`
* PMD — `AvoidLiteralsInIfCondition` / SonarLint `java:S109`

**Exemplo correto**

```java
private static final double FREQUENCIA_MINIMA = 75.0;
return percentual >= FREQUENCIA_MINIMA;
```

**Exemplo incorreto**

```java
return percentual >= 75.0;
```

---

### Regra 13 — Strings mágicas de domínio devem virar enum

**Descrição**

Conjuntos fechados de valores textuais (tipo de usuário, status) devem ser modelados
como `enum`, não como `String`.

**Motivação**

Garante verificação em tempo de compilação e evita erros de digitação.

**Automatização**

* SonarLint — `java:S1192` (literais repetidos)
* PMD — `AvoidDuplicateLiterals`

**Exemplo correto**

```java
public enum TipoUsuario { ALUNO, PROFESSOR, COORDENADOR, ADMINISTRADOR }
```

**Exemplo incorreto**

```java
if (usuario.getTipo().equals("ALUNO")) { }
```

---

### Regra 14 — Visibilidade restrita dos atributos

**Descrição**

Atributos de instância devem ser `private` (ou `protected` quando há herança real).

**Motivação**

Encapsulamento: o estado só muda por métodos controlados.

**Automatização**

* Checkstyle — `VisibilityModifier`
* PMD — `DefaultPackageOrPublicMembers` / SonarLint `java:S1104`

**Exemplo correto**

```java
private String codigoTurma;
public String getCodigoTurma() { return codigoTurma; }
```

**Exemplo incorreto**

```java
public String codigoTurma;
```

---

### Regra 15 — Imutabilidade com `final` quando possível

**Descrição**

Campos atribuídos apenas no construtor e parâmetros não reatribuídos devem ser `final`.

**Motivação**

Deixa explícita a intenção de imutabilidade e previne reatribuições acidentais.

**Automatização**

* Checkstyle — `FinalLocalVariable`, `FinalParameters`
* SonarLint — `java:S3008` / `java:S1488`

**Exemplo correto**

```java
private final FrequenciaService frequenciaService;
```

**Exemplo incorreto**

```java
private FrequenciaService frequenciaService; // nunca reatribuído após o construtor
```

---

### Regra 16 — Tratamento de exceções sem bloco vazio silencioso

**Descrição**

Blocos `catch` não podem ser vazios; devem tratar, relançar ou conter comentário
justificando o descarte.

**Motivação**

Exceções engolidas escondem defeitos e dificultam diagnóstico.

**Automatização**

* Checkstyle — `EmptyCatchBlock`
* PMD — `EmptyCatchBlock` / SonarLint `java:S108`

**Exemplo correto**

```java
} catch (FrequenciaNaoRegistradaException ignorada) {
    // Disciplina sem registros ainda não compõe o relatório consolidado.
}
```

**Exemplo incorreto**

```java
} catch (Exception e) {
}
```

---

### Regra 17 — Não capturar `Exception`/`Throwable` genéricos

**Descrição**

Preferir capturar exceções específicas em vez de `Exception` ou `Throwable`.

**Motivação**

Captura genérica mascara erros inesperados e dificulta o tratamento correto.

**Automatização**

* PMD — `AvoidCatchingGenericException`
* SonarLint — `java:S2221`

**Exemplo correto**

```java
} catch (DisciplinaNaoEncontradaException e) {
    System.out.println("Erro: " + e.getMessage());
}
```

**Exemplo incorreto**

```java
} catch (Exception e) {
    System.out.println("Erro: " + e.getMessage());
}
```

---

### Regra 18 — Indentação e formatação automáticas

**Descrição**

Indentação de 4 espaços, sem tabs, com formatação aplicada por ferramenta.

**Motivação**

Elimina debates de formatação e mantém diffs limpos.

**Automatização**

* Spotless + Google Java Format
* Checkstyle — `Indentation`, `FileTabCharacter`

**Exemplo correto**

```java
public void exemplo() {
    if (ativo) {
        executar();
    }
}
```

**Exemplo incorreto**

```java
public void exemplo() {
		if (ativo) {
	executar();
  }
}
```

---

### Regra 19 — Chaves obrigatórias em blocos de controle

**Descrição**

`if`, `else`, `for`, `while` devem sempre usar chaves, mesmo com uma única instrução.

**Motivação**

Evita erros ao adicionar linhas a blocos sem chaves.

**Automatização**

* Checkstyle — `NeedBraces`
* SonarLint — `java:S121`

**Exemplo correto**

```java
if (registros.isEmpty()) {
    continue;
}
```

**Exemplo incorreto**

```java
if (registros.isEmpty()) continue;
```

---

### Regra 20 — JavaDoc em tipos e métodos públicos

**Descrição**

Classes públicas e métodos públicos de service/model devem ter JavaDoc descrevendo
propósito e, quando houver, exceções lançadas (`@throws`).

**Motivação**

Documenta o contrato público e a rastreabilidade com as RFs/RNs do sistema.

**Automatização**

* Checkstyle — `JavadocType`, `JavadocMethod`
* SonarLint — `java:S1176`

**Exemplo correto**

```java
/**
 * RF29: Consulta a frequência de um aluno em uma disciplina específica.
 * @throws DisciplinaNaoEncontradaException se a disciplina não existir.
 */
public FrequenciaDisciplina consultarPorDisciplina(String matricula, String codigo) { }
```

**Exemplo incorreto**

```java
public FrequenciaDisciplina consultarPorDisciplina(String matricula, String codigo) { }
```

---

### Regra 21 — Proibir código morto e variáveis não usadas

**Descrição**

Não pode haver variáveis locais, parâmetros privados ou métodos privados sem uso.

**Motivação**

Código morto confunde o leitor e mascara intenções.

**Automatização**

* PMD — `UnusedLocalVariable`, `UnusedPrivateMethod`, `UnusedPrivateField`
* SonarLint — `java:S1068`, `java:S1144`

**Exemplo correto**

```java
int total = registros.size();
return total;
```

**Exemplo incorreto**

```java
int total = registros.size();
int naoUsado = 42; // nunca lido
return total;
```

---

### Regra 22 — Uso de `var` apenas com tipo evidente

**Descrição**

`var` é permitido somente quando o tipo é óbvio pelo lado direito (ex.: `new`/literal).
Proibido quando o inicializador é ambíguo.

**Motivação**

Mantém legibilidade sem esconder o tipo real do leitor.

**Automatização**

* SonarLint — `java:S6212`
* IntelliJ Inspection — "Local variable type can be omitted/explicit"

**Exemplo correto**

```java
var registros = new ArrayList<RegistroFrequencia>();
```

**Exemplo incorreto**

```java
var dados = servico.processar(entrada); // tipo não evidente
```

---

### Regra 23 — Organização e nomenclatura dos testes

**Descrição**

Classes de teste terminam em `Test`, ficam em `src/test/java` no mesmo pacote do alvo,
e métodos de teste têm nomes descritivos do comportamento.

**Motivação**

Padroniza a descoberta de testes e torna o relatório de execução legível.

**Automatização**

* Maven Surefire (padrão `*Test`)
* Checkstyle — `MethodName` aplicado a classes de teste

**Exemplo correto**

```java
@Test
void deveRejeitarAlunoNaoMatriculadoNaDisciplina() { }
```

**Exemplo incorreto**

```java
@Test
void teste1() { }
```

---

### Regra 24 — Cada teste deve conter ao menos uma asserção

**Descrição**

Todo método anotado com `@Test` deve conter pelo menos uma asserção JUnit
(`assertEquals`, `assertThrows`, etc.).

**Motivação**

Testes sem asserção passam sempre e dão falsa sensação de cobertura.

**Automatização**

* PMD — `JUnitTestsShouldIncludeAssert`
* SonarLint — `java:S2699`

**Exemplo correto**

```java
@Test
void deveCalcularPercentual() {
    assertEquals(75.0, frequencia.getPercentualFrequencia(), 0.01);
}
```

**Exemplo incorreto**

```java
@Test
void deveCalcularPercentual() {
    consultaFrequenciaService.consultarPorDisciplina("A001", "CC101");
}
```

---

### Regra 25 — Proibir duplicação de código

**Descrição**

Blocos duplicados acima de um limiar (ex.: 100 tokens) devem ser extraídos para
método/constante reutilizável.

**Motivação**

Duplicação multiplica o custo de correção e o risco de divergência.

**Automatização**

* PMD CPD (Copy/Paste Detector)
* SonarLint — regra de duplicação

**Exemplo correto**

```java
private Aluno obterAluno(String matricula) { /* validação centralizada */ }
```

**Exemplo incorreto**

```java
// mesmo bloco de validação copiado em três métodos diferentes
```