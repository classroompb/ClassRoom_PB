# Como Usar RF21, RF22 e RF23 - Guia Prático

## Resumo das Funcionalidades

| RF | Descrição | Quem usa |
|---|---|---|
| **RF21** | Aluno entra em lista de espera quando não há vagas | Aluno |
| **RF22** | Aluno cancela matrícula dentro do prazo | Aluno |
| **RF23** | Sistema mantém lista de espera por turma | Aluno/Sistema |

---

## ✅ Pré-requisitos

Antes de usar, você precisa ter:

1. **Usuários cadastrados** (Alunos, Professor)
   - Menu > Cadastrar usuário > tipo "ALUNO"
   
2. **Disciplinas criadas** (Coordenador)
   - Menu Coordenador > Opção 1 > Cadastrar disciplina

3. **Períodos letivos** (Coordenador)
   - Menu Coordenador > Opção 3 > Cadastrar período letivo

4. **Turmas oferecidas** (Coordenador)
   - Menu Coordenador > Opção 2 > Ofertar turma

5. **Período letivo ativado** (Coordenador)
   - Menu Coordenador > Opção 4 > Ativar período

---

## 📋 Fluxo de Uso - Passo a Passo

### PASSO 1: Aluno faz Login
```
Menu Principal:
  1. Login ← SELECIONE
  2. Cadastrar usuário
  0. Sair

Matrícula ou email: 2026001
Senha: 1234

Login realizado com sucesso! Bem-vindo(a), João.
```

### PASSO 2: Aluno Solicita Matrícula (RF16 + RF21)
```
=== MENU ALUNO ===
1. Consultar disciplinas e turmas
2. Solicitar matrícula ← SELECIONE
3. Acompanhar matrícula e lista de espera
4. Consultar frequência e notas
5. Consultar histórico acadêmico
6. Cancelar matrícula
0. Sair

Escolha: 2
```

**CENÁRIO A: Há vagas disponíveis**
```
Código da turma: CC-2026.1-01
Matrícula de aluno: 2026001

✅ Aluno 2026001 matriculado com sucesso na turma CC-2026.1-01
```

**CENÁRIO B: SEM vagas (ATIVA RF21 - Lista de Espera)**
```
Código da turma: CC-2026.1-01
Matrícula de aluno: 2026002

❌ Turma cheia! 
✅ Aluno adicionado à lista de espera
📍 Sua posição na fila: 1

Você será notificado quando uma vaga abrir.
```

### PASSO 3: Aluno Consulta Posição na Fila (RF23)
```
=== MENU ALUNO ===
1. Consultar disciplinas e turmas
2. Solicitar matrícula
3. Acompanhar matrícula e lista de espera ← SELECIONE
4. Consultar frequência e notas
5. Consultar histórico acadêmico
6. Cancelar matrícula
0. Sair

Escolha: 3
```

```
Código da turma: CC-2026.1-01

📊 Turma CC-2026.1-01
   Disciplina: Programação I
   Professor: Prof. Ana
   Horário: 08:00-10:00
   Sala: Sala A1
   ────────────────────────
   Vagas: 1/2 disponíveis
   Matriculados: 1
   ✅ Na lista de espera: SIM - Posição: 1 de 3
   ────────────────────────
   Alunos à sua frente: 0
```

### PASSO 4: Aluno Cancela Matrícula (RF22 + RF23)
```
=== MENU ALUNO ===
1. Consultar disciplinas e turmas
2. Solicitar matrícula
3. Acompanhar matrícula e lista de espera
4. Consultar frequência e notas
5. Consultar histórico acadêmico
6. Cancelar matrícula ← SELECIONE
0. Sair

Escolha: 6
```

```
Código da turma: CC-2026.1-01
Matrícula do aluno: 2026001

⚠️  Validando prazo de cancelamento...
✅ Cancelamento permitido (prazo até: 2026-06-30)

Matrícula cancelada com sucesso!

🎯 PROMOÇÃO AUTOMÁTICA (RF23):
   ✅ Aluno 2026002 foi promovido da lista de espera
   📍 Próximo aluno em fila: 2026003 (posição 1)
```

---

## 🔧 Métodos de TurmaService Utilizados

### Para Matriculação (RF21 - Lista de Espera)
```java
// Solicita matrícula (entra em fila se sem vagas)
turmaService.solicitarMatricula(String codigoTurma, String matriculaAluno);
```

**O que acontece:**
- ✅ Se há vagas → Matricula o aluno normalmente
- ❌ Se sem vagas → Adiciona à lista de espera (RF21)

---

### Para Cancelamento (RF22)
```java
// Cancela matrícula (promove aluno da fila automaticamente)
turmaService.cancelarMatricula(String codigoTurma, String matriculaAluno);
```

**O que acontece:**
1. Valida se cancelamento está permitido (RF22)
2. Remove aluno da matrícula
3. **Promove automaticamente** primeiro da fila (RF23)
4. Salva dados

---

### Para Consultas (RF23)
```java
// Obter turma com informações atualizadas
Turma turma = turmaService.obterTurma(String codigoTurma);

// Consultar posição na fila (1-indexed, -1 se não está)
int posicao = turmaService.obterPosicaoEmEspera(
    String codigoTurma, 
    String matriculaAluno
);

// Consultar total em espera
int total = turmaService.consultarQuantidadeEmEspera(String codigoTurma);

// Remover aluno da fila (desistência)
turmaService.removerDaEspera(String codigoTurma, String matriculaAluno);
```

---

## 📊 Exemplo Completo de Fluxo

### Setup Inicial (Coordenador)

```
1️⃣ Cadastrar Período Letivo
   Menu Coordenador > Opção 3
   Identificador: 2026.1
   
2️⃣ Cadastrar Disciplina
   Menu Coordenador > Opção 1
   Código: CC101
   Nome: Programação I
   Carga Horária: 60
   Créditos: 4
   
3️⃣ Ofertar Turma
   Menu Coordenador > Opção 2
   Disciplina: CC101
   Professor: P001
   Limite de Vagas: 2 ← IMPORTANTE: Só 2 vagas!
   Horário: 08:00-10:00
   Sala: Sala A1
   
   Resultado: Turma criada: CC101-2026.1-01
   
4️⃣ Ativar Período Letivo
   Menu Coordenador > Opção 4
   Identificador: 2026.1
```

### Fluxo de Alunos (4 alunos, 2 vagas)

```
ALUNO 1 (A001) - Login e Solicita Matrícula
   ✅ Matriculado com sucesso
   
ALUNO 2 (A002) - Login e Solicita Matrícula
   ✅ Matriculado com sucesso (2ª vaga preenchida)
   
ALUNO 3 (A003) - Login e Solicita Matrícula
   ❌ Sem vagas!
   📍 Adicionado à lista de espera - Posição 1/2
   
ALUNO 4 (A004) - Login e Solicita Matrícula
   ❌ Sem vagas!
   📍 Adicionado à lista de espera - Posição 2/2
```

### Cancelamento Ativa Promoção (RF22 + RF23)

```
ALUNO 1 (A001) - Cancela Matrícula
   ✅ Cancelamento permitido
   🎯 ALUNO 3 (A003) promovido AUTOMATICAMENTE!
   
Novo estado:
   Matriculados: A002, A003 (2 vagas)
   Fila de espera: A004 (posição 1)
```

---

## 🚨 Erros Comuns

### ❌ "Aluno já está matriculado"
```
Significa: O aluno já tem uma matrícula nesta turma
Solução: Use opção 6 para cancelar antes de solicitar novamente
```

### ❌ "Aluno já está em lista de espera"
```
Significa: O aluno já está na fila desta turma
Solução: Você não pode entrar na fila 2 vezes
Remover com: turmaService.removerDaEspera()
```

### ❌ "Conflito de horário"
```
Significa: O aluno já tem outra matrícula com horário conflitante
Solução: Escolha turma em horário diferente
```

### ❌ "Cancelamento não permitido (fora do prazo)"
```
Significa: Passou do prazo máximo de cancelamento (RF22)
Solução: Veja com Coordenador data limite
```

---

## 📊 Estados da Turma

```
Turma CC101-2026.1-01:
├─ Limite de Vagas: 2
├─ Vagas Disponíveis: 0
├─ Matriculados (2): [A001, A002]
└─ Lista de Espera (2): [A003, A004]
                         ↑ Próximo a ser promovido
```

---

## 🔄 Fluxo Automático de RF23

```
┌─────────────────────────┐
│  Aluno A001 matriculado │
│  Aluno A002 matriculado │
│  Turma CHEIA - 0 vagas  │
└─────────────┬───────────┘
              │
              ▼
┌─────────────────────────┐
│  Aluno A003 solicita    │ ← RF21: Entra em fila
│  → Adicionado à fila    │
│  Posição: 1             │
└─────────────┬───────────┘
              │
              ▼
┌─────────────────────────┐
│  Aluno A004 solicita    │ ← RF21: Entra em fila
│  → Adicionado à fila    │
│  Posição: 2             │
└─────────────┬───────────┘
              │
    ┌─────────┴─────────┐
    │                   │
    ▼                   ▼
┌──────────────┐  ┌──────────────────┐
│ A001 cancela │  │ A003 cancela     │
│  (dentro do  │  │  (desistência)   │
│   prazo)     │  │                  │
│   RF22 ✅    │  │  removerDaEspera │
└──────┬───────┘  └──────────────────┘
       │
       ▼
┌─────────────────────────┐
│  RF23: PROMOÇÃO         │ ← A003 promovido automaticamente
│  A003 → MATRICULADO     │   A004 agora em posição 1
│  Vagas: 1/2             │
│  Fila: [A004]           │
└─────────────────────────┘
```

---

## 💡 Boas Práticas

1. ✅ **Sempre consulte a posição antes de desistir**
   - Menu Aluno > Opção 3

2. ✅ **Cancele dentro do prazo permitido**
   - RF22: Cancelamento só dentro da janela

3. ✅ **Receba notificação de promoção**
   - RF23: Automático quando uma vaga abre

4. ✅ **Procure por outras turmas**
   - Menu Aluno > Opção 1: Consulta turmas disponíveis

---

## 🎯 Resumo de Funcionalidades

| Requisito | Menu | Opção | Função |
|-----------|------|-------|--------|
| **RF21** | ALUNO | 2 | Solicita matrícula (ou fila se cheio) |
| **RF22** | ALUNO | 6 | Cancela matrícula (com validação de prazo) |
| **RF23** | ALUNO | 3 | Consulta lista de espera e posição |
| **RF23** | Sistema | Auto | Promove aluno ao cancelar matrícula |

---

## 📝 Notas Importantes

- A **fila é FIFO** (First In, First Out)
- Cada **turma tem sua própria fila**
- A **promoção é automática** quando há cancelamento
- O sistema **persiste tudo** em disco automaticamente
- A lista de espera é **independente por turma**
