# RF27 — Professor registra presença/falta do aluno

Responsável: **Danilo Nascimento** · Release 2 (Sprint 4) · Branch `danilo-rf27`

## O que foi desenvolvido

O professor, no seu menu, escolhe **"2. Registrar frequência"** e informa turma,
matrícula do aluno, data da aula e o status (P = presente / F = falta). O sistema
valida e grava o registro, mostrando um resumo de presenças/faltas do aluno na turma.

Componentes criados:

| Camada | Arquivo |
|---|---|
| Model | `model/RegistroFrequencia.java` — turma, aluno, data, presente (Serializable) |
| Repository | `repository/FrequenciaRepository.java` — persistência `.dat` (padrão `TurmaRepository`) |
| Service | `service/FrequenciaService.java` — regras + consultas (contar/listar) |
| CLI | `cli/CLI.java` — fluxo `registrarFrequencia()` no menu do professor |
| Testes | `test/.../RF27RegistrarFrequenciaTest.java` — 15 testes JUnit |

## Regras de negócio (todas via `IllegalArgumentException`)

- Turma deve existir.
- Aluno deve existir e ser do tipo ALUNO.
- Aluno deve estar **matriculado** na turma.
- Data obrigatória (não nula).
- Não pode haver dois registros para o mesmo aluno/turma na **mesma data**.

## Testes

`Tests run: 250, Failures: 0, Errors: 0` na suíte completa — sendo **15 novos do RF27**
(presença, falta, método genérico, turma/aluno inexistente, aluno não matriculado,
data nula, campos vazios, duplicidade, datas diferentes, contagem, listagem,
persistência entre instâncias).

## Prints do terminal

Cenário executado no CLI real (transcrição completa em
[`prints-terminal-rf27.txt`](prints-terminal-rf27.txt)): coordenador cria disciplina,
período e turma; aluno se matricula; professor registra presença e falta e tem os dois
casos de erro barrados.

```
=== MENU PROFESSOR ===
1. Visualizar turmas
2. Registrar frequência
...

# Presença
Código da turma: CC101-2026.1-01
Matrícula do aluno: A001
Data da aula (AAAA-MM-DD): 2026-06-01
Presença? (P = presente / F = falta): P
Frequência registrada: RegistroFrequencia{turma='CC101-2026.1-01', aluno='A001', data=2026-06-01, status=PRESENTE}
Resumo deste aluno na turma -> presenças: 1 | faltas: 0

# Falta
Código da turma: CC101-2026.1-01
Matrícula do aluno: A001
Data da aula (AAAA-MM-DD): 2026-06-08
Presença? (P = presente / F = falta): F
Frequência registrada: RegistroFrequencia{turma='CC101-2026.1-01', aluno='A001', data=2026-06-08, status=FALTA}
Resumo deste aluno na turma -> presenças: 1 | faltas: 1

# Erro — registro duplicado na mesma data
Erro: Já existe registro de frequência para o aluno A001 na data 2026-06-01.

# Erro — aluno não matriculado na turma
Erro: Aluno não está matriculado na turma CC101-2026.1-01: A002
```
