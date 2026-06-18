package org.example.classroompb.cli;

import org.example.classroompb.model.Curso;
import org.example.classroompb.model.Disciplina;
import org.example.classroompb.model.ItemListaEspera;
import org.example.classroompb.model.PeriodoLetivo;
import org.example.classroompb.model.RegistroFrequencia;
import org.example.classroompb.model.TipoUsuario;
import org.example.classroompb.model.Turma;
import org.example.classroompb.model.Usuario;
import org.example.classroompb.repository.CursoRepository;
import org.example.classroompb.repository.UsuarioRepository;
import org.example.classroompb.repository.DisciplinaRepository;
import org.example.classroompb.repository.PeriodoLetivoRepository;
import org.example.classroompb.repository.TurmaRepository;
import org.example.classroompb.repository.FrequenciaRepository;
import org.example.classroompb.service.CursoService;
import org.example.classroompb.service.UsuarioService;
import org.example.classroompb.service.DisciplinaService;
import org.example.classroompb.service.PeriodoLetivoService;
import org.example.classroompb.service.TurmaService;
import org.example.classroompb.service.FrequenciaService;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class CLI {

    private final UsuarioService usuarioService;
    private final CursoService cursoService;
    private final DisciplinaService disciplinaService;
    private final PeriodoLetivoService periodoLetivoService;
    private final TurmaService turmaService;
    private final FrequenciaService frequenciaService;
    private final Scanner scanner;
    private Usuario usuarioLogado;

    public CLI() {
        this.usuarioService = new UsuarioService(new UsuarioRepository());
        this.cursoService = new CursoService(new CursoRepository());
        this.disciplinaService = new DisciplinaService(new DisciplinaRepository());
        this.periodoLetivoService = new PeriodoLetivoService(new PeriodoLetivoRepository());
        this.turmaService = new TurmaService(new TurmaRepository(), usuarioService, disciplinaService);
        this.frequenciaService = new FrequenciaService(new FrequenciaRepository(), usuarioService, turmaService);
        this.scanner = new Scanner(System.in);
    }

    public void iniciar() {
        System.out.println("==============================");
        System.out.println("   Bem-vindo ao ClassRoomPB  ");
        System.out.println("==============================");

        boolean rodando = true;
        while (rodando) {
            if (usuarioLogado == null) {
                rodando = menuInicial();
            } else {
                menuUsuario();
            }
        }

        System.out.println("Sistema encerrado. Até logo!");
        scanner.close();
    }

    private boolean menuInicial() {
        System.out.println("\n1. Login");
        System.out.println("2. Cadastrar usuário");
        System.out.println("0. Sair");
        System.out.print("Escolha: ");
        String opcao = scanner.nextLine().trim();

        switch (opcao) {
            case "1" -> login();
            case "2" -> cadastrar();
            case "0" -> { return false; }
            default  -> System.out.println("Opção inválida.");
        }
        return true;
    }

    private void login() {
        System.out.print("Matrícula ou email: ");
        String identificador = scanner.nextLine().trim();
        System.out.print("Senha: ");
        String senha = scanner.nextLine().trim();

        try {
            usuarioLogado = usuarioService.login(identificador, senha);
            System.out.println("\nLogin realizado com sucesso! Bem-vindo(a), " + usuarioLogado.getNome() + ".");
        } catch (IllegalArgumentException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void cadastrar() {
        System.out.print("Tipo (ALUNO/PROFESSOR/COORDENADOR/ADMINISTRADOR): ");
        String tipo = scanner.nextLine().trim();
        System.out.print("Nome: ");
        String nome = scanner.nextLine().trim();
        System.out.print("Matrícula: ");
        String matricula = scanner.nextLine().trim();
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Senha: ");
        String senha = scanner.nextLine().trim();

        try {
            Usuario u = usuarioService.cadastrar(tipo, nome, matricula, email, senha);
            System.out.println("Usuário cadastrado com sucesso: " + u);
        } catch (IllegalArgumentException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void menuUsuario() {
        // RF03 - exibe menu conforme perfil
        System.out.println(usuarioService.getMenuPorPerfil(usuarioLogado.getTipo()));
        System.out.print("Escolha: ");
        String opcao = scanner.nextLine().trim();

        if (opcao.equals("0")) {
            System.out.println("Logout realizado.");
            usuarioLogado = null;
            return;
        }

        // RF05 - Administrador cadastra cursos (opção 2 do menu admin)
        if (usuarioLogado.getTipo() == TipoUsuario.ADMINISTRADOR && opcao.equals("2")) {
            cadastrarCurso();
            return;
        }
        
        // RF06 - Coordenador cadastra disciplinas
        if (usuarioLogado.getTipo() == TipoUsuario.COORDENADOR && opcao.equals("1")) {
            cadastrarDisciplina();
            return;
        }

        // RF10/RF11 - Coordenador oferta turmas
        if (usuarioLogado.getTipo() == TipoUsuario.COORDENADOR && opcao.equals("2")) {
            ofertarTurma();
            return;
        }

        // RF08 - Coordenador cadastra periodos letivos
        if (usuarioLogado.getTipo() == TipoUsuario.COORDENADOR && opcao.equals("3")) {
            cadastrarPeriodoLetivo();
            return;
        }

        // RF09 - Coordenador ativa periodos letivos
        if (usuarioLogado.getTipo() == TipoUsuario.COORDENADOR && opcao.equals("4")) {
            ativarPeriodoLetivo();
            return;
        }

        // RF09 - Coordenador encerra periodos letivos
        if (usuarioLogado.getTipo() == TipoUsuario.COORDENADOR && opcao.equals("5")) {
            encerrarPeriodoLetivo();
            return;
        }

        // RF14 - Coordenador altera ou cancela turmas
        if (usuarioLogado.getTipo() == TipoUsuario.COORDENADOR && opcao.equals("6")) {
            gerenciarTurma();
            return;
        }

        // RF07 - Coordenador adiciona pre-requisito em disciplina
        if (usuarioLogado.getTipo() == TipoUsuario.COORDENADOR && opcao.equals("10")) {
            adicionarPreRequisito();
            return;
        }

        // RF22 - Coordenador configura datas do período letivo (prazo de cancelamento)
        if (usuarioLogado.getTipo() == TipoUsuario.COORDENADOR && opcao.equals("7")) {
            configurarDatasPeriodoLetivo();
            return;
        }

        // RF26 - Coordenador visualiza a lista de espera das turmas
        if (usuarioLogado.getTipo() == TipoUsuario.COORDENADOR && opcao.equals("8")) {
            visualizarListaEsperaTurmas();
            return;
        }

        // RF27 - Professor registra presença/falta do aluno
        if (usuarioLogado.getTipo() == TipoUsuario.PROFESSOR && opcao.equals("2")) {
            registrarFrequencia();
            return;
        }

        // RF16/RF21 - Aluno solicita matrícula (ou entra em fila se sem vagas)
        if (usuarioLogado.getTipo() == TipoUsuario.ALUNO && opcao.equals("2")) {
            solicitarMatriculaEmTurma();
            return;
        }

        // RF23 - Aluno acompanha matrícula e lista de espera
        if (usuarioLogado.getTipo() == TipoUsuario.ALUNO && opcao.equals("3")) {
            acompanharMatriculaEEspera();
            return;
        }

        // RF22/RF23 - Aluno cancela matrícula (com validação de prazo)
        if (usuarioLogado.getTipo() == TipoUsuario.ALUNO && opcao.equals("6")) {
            cancelarMatriculaEmTurma();
            return;
        }

        System.out.println("Funcionalidade em desenvolvimento.");
    }

    private void cadastrarCurso() {
        System.out.print("Código do curso: ");
        String codigo = scanner.nextLine().trim();
        System.out.print("Nome do curso: ");
        String nome = scanner.nextLine().trim();

        try {
            Curso c = cursoService.cadastrar(codigo, nome);
            System.out.println("Curso cadastrado com sucesso: " + c);
        } catch (IllegalArgumentException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    // RF27 - Professor registra presença/falta de um aluno de uma turma numa data
    private void registrarFrequencia() {
        System.out.print("Código da turma: ");
        String codigoTurma = scanner.nextLine().trim();
        System.out.print("Matrícula do aluno: ");
        String matriculaAluno = scanner.nextLine().trim();
        System.out.print("Data da aula (AAAA-MM-DD): ");
        String dataTexto = scanner.nextLine().trim();
        System.out.print("Presença? (P = presente / F = falta): ");
        String status = scanner.nextLine().trim();

        LocalDate data;
        try {
            data = LocalDate.parse(dataTexto);
        } catch (DateTimeParseException e) {
            System.out.println("Erro: data inválida. Use o formato AAAA-MM-DD (ex: 2026-06-17).");
            return;
        }

        boolean presente;
        if (status.equalsIgnoreCase("P")) {
            presente = true;
        } else if (status.equalsIgnoreCase("F")) {
            presente = false;
        } else {
            System.out.println("Erro: informe P (presente) ou F (falta).");
            return;
        }

        try {
            RegistroFrequencia registro =
                    frequenciaService.registrarFrequencia(codigoTurma, matriculaAluno, data, presente);
            System.out.println("Frequência registrada: " + registro);
            System.out.println("Resumo deste aluno na turma -> presenças: "
                    + frequenciaService.contarPresencas(codigoTurma, matriculaAluno)
                    + " | faltas: " + frequenciaService.contarFaltas(codigoTurma, matriculaAluno));
        } catch (IllegalArgumentException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void cadastrarDisciplina() {
    	 System.out.print("Código da disciplina:  ");
         String codigo = scanner.nextLine().trim();
         System.out.print("Nome da disciplina:  ");
         String nome = scanner.nextLine().trim();
         System.out.print("Carga horária da disciplina:  ");
         int cargaHora = Integer.parseInt(scanner.nextLine().trim());
         System.out.print("Créditos da disciplina:  ");
         int creditos = Integer.parseInt(scanner.nextLine().trim());
         System.out.println();
         try {
        	 disciplinaService.cadastrar(codigo, nome, cargaHora, creditos);
        	 System.out.println("Disciplina cadastrada com sucesso.");
         } catch (IllegalArgumentException e) {
        	 System.out.println("Erro: " + e.getMessage());
         }
         
         System.out.println();
         System.out.println();
    }

    private void adicionarPreRequisito() {
        System.out.print("Código da disciplina: ");
        String codigoDisciplina = scanner.nextLine().trim();
        System.out.print("Código da disciplina pré-requisito: ");
        String codigoPreRequisito = scanner.nextLine().trim();

        try {
            disciplinaService.adicionarPreRequisito(codigoDisciplina, codigoPreRequisito);
            System.out.println("Pré-requisito adicionado com sucesso.");
        } catch (IllegalArgumentException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void ofertarTurma() {
        System.out.print("Código da disciplina: ");
        String codigoDisciplina = scanner.nextLine().trim();
        System.out.print("Matrícula do professor: ");
        String matriculaProfessor = scanner.nextLine().trim();
        System.out.print("Identificador do período letivo (ex: 2026.1): ");
        String identificadorPeriodo = scanner.nextLine().trim();
        System.out.print("Limite de vagas: ");
        String limiteTexto = scanner.nextLine().trim();
        System.out.print("Horário (ex: 08:00-10:00): ");
        String horario = scanner.nextLine().trim();
        System.out.print("Sala: ");
        String sala = scanner.nextLine().trim();

        try {
            int limiteVagas = Integer.parseInt(limiteTexto);
            PeriodoLetivo periodo = periodoLetivoService.buscarPorIdentificador(identificadorPeriodo);
            if (periodo == null) {
                throw new IllegalArgumentException("Período letivo não encontrado: " + identificadorPeriodo);
            }

            // Normalizar formato do horário (ex: 8:00-10:00 → 08:00-10:00)
            String horarioNormalizado = normalizarHorario(horario);

            Turma turma = turmaService.ofertarTurma(
                    codigoDisciplina,
                    matriculaProfessor,
                    periodo,
                    limiteVagas,
                    horarioNormalizado,
                    sala
            );
            System.out.println("Turma ofertada com sucesso: " + turma);
        } catch (NumberFormatException e) {
            System.out.println("Erro: limite de vagas deve ser um número inteiro.");
        } catch (IllegalArgumentException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void cadastrarPeriodoLetivo() {
        System.out.print("Identificador do período letivo (ex: 2026.1): ");
        String identificador = scanner.nextLine().trim();

        try {
            PeriodoLetivo periodo = periodoLetivoService.cadastrar(identificador);
            System.out.println("Período letivo cadastrado com sucesso: " + periodo);
            
            // RF22: Perguntar se deseja configurar datas agora
            System.out.print("\nDeseja configurar as datas agora? (s/n): ");
            String opcao = scanner.nextLine().trim().toLowerCase();
            if (opcao.equals("s")) {
                configurarDatasPeriodoLetivo();
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void ativarPeriodoLetivo() {
        System.out.print("Identificador do período letivo para ativar: ");
        String identificador = scanner.nextLine().trim();

        try {
            PeriodoLetivo periodo = periodoLetivoService.ativar(identificador);
            System.out.println("Período letivo ativado com sucesso: " + periodo);
        } catch (IllegalArgumentException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void encerrarPeriodoLetivo() {
        System.out.print("Identificador do período letivo para encerrar: ");
        String identificador = scanner.nextLine().trim();

        try {
            PeriodoLetivo periodo = periodoLetivoService.encerrar(identificador);
            System.out.println("Período letivo encerrado com sucesso: " + periodo);
        } catch (IllegalArgumentException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    /**
     * RF22 - Coordenador configura datas do período letivo
     * Permite definir datas de início, fim e prazo de cancelamento
     */
    private void configurarDatasPeriodoLetivo() {
        System.out.print("\nIdentificador do período letivo (ex: 2026.1): ");
        String identificador = scanner.nextLine().trim();

        try {
            PeriodoLetivo periodo = periodoLetivoService.buscarPorIdentificador(identificador);
            if (periodo == null) {
                System.out.println("Erro: Período letivo não encontrado: " + identificador);
                return;
            }

            System.out.println("\n=== Configurar Datas do Período " + identificador + " ===");
            System.out.println("Status atual: " + periodo.getStatus());
            System.out.println();

            // Data de início
            System.out.print("Data de início (formato: dd/MM/yyyy, ex: 01/03/2026): ");
            String dataInicioStr = scanner.nextLine().trim();
            java.time.LocalDate dataInicio = null;
            if (!dataInicioStr.isBlank()) {
                dataInicio = java.time.LocalDate.parse(dataInicioStr, 
                    java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                periodo.setDataInicio(dataInicio);
            }

            // Data de fim
            System.out.print("Data de fim (formato: dd/MM/yyyy, ex: 30/06/2026): ");
            String dataFimStr = scanner.nextLine().trim();
            java.time.LocalDate dataFim = null;
            if (!dataFimStr.isBlank()) {
                dataFim = java.time.LocalDate.parse(dataFimStr, 
                    java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                periodo.setDataFim(dataFim);
            }

            // Data limite de cancelamento (RF22)
            System.out.print("Data limite para cancelamento (formato: dd/MM/yyyy, ex: 30/05/2026): ");
            String dataLimiteStr = scanner.nextLine().trim();
            java.time.LocalDate dataLimite = null;
            if (!dataLimiteStr.isBlank()) {
                dataLimite = java.time.LocalDate.parse(dataLimiteStr, 
                    java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                periodo.setDataLimiteCancelamento(dataLimite);
            }

            System.out.println("\n📋 Período Letivo Configurado:");
            System.out.println("   Identificador: " + periodo.getIdentificador());
            if (dataInicio != null) {
                System.out.println("   Data de início: " + dataInicio);
            }
            if (dataFim != null) {
                System.out.println("   Data de fim: " + dataFim);
            }
            if (dataLimite != null) {
                System.out.println("   📍 Data limite de cancelamento (RF22): " + dataLimite);
            }
            System.out.println("   Status: " + periodo.getStatus());

        } catch (java.time.format.DateTimeParseException e) {
            System.out.println("Erro: Formato de data inválido. Use: dd/MM/yyyy (ex: 01/03/2026)");
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void gerenciarTurma() {
        System.out.println("\n1. Alterar turma");
        System.out.println("2. Cancelar turma");
        System.out.println("0. Voltar");
        System.out.print("Escolha: ");
        String opcao = scanner.nextLine().trim();

        switch (opcao) {
            case "1" -> alterarTurma();
            case "2" -> cancelarTurma();
            case "0" -> System.out.println("Voltando ao menu.");
            default -> System.out.println("Opção inválida.");
        }
    }

    private void alterarTurma() {
        System.out.print("Código da turma: ");
        String codigoTurma = scanner.nextLine().trim();
        System.out.print("Nova matrícula do professor (deixe vazio para manter): ");
        String novaMatriculaProfessor = scanner.nextLine().trim();
        System.out.print("Novo limite de vagas (deixe vazio para manter): ");
        String limiteTexto = scanner.nextLine().trim();
        System.out.print("Novo horário (ex: 08:00-10:00, deixe vazio para manter): ");
        String novoHorario = scanner.nextLine().trim();
        System.out.print("Nova sala (deixe vazio para manter): ");
        String novaSala = scanner.nextLine().trim();

        try {
            Integer novoLimiteVagas = limiteTexto.isBlank() ? null : Integer.parseInt(limiteTexto);
            Turma turma = turmaService.alterarTurma(
                    codigoTurma,
                    novaMatriculaProfessor.isBlank() ? null : novaMatriculaProfessor,
                    novoLimiteVagas,
                    novoHorario.isBlank() ? null : novoHorario,
                    novaSala.isBlank() ? null : novaSala
            );
            System.out.println("Turma alterada com sucesso: " + turma);
        } catch (NumberFormatException e) {
            System.out.println("Erro: limite de vagas deve ser um número inteiro.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void cancelarTurma() {
        System.out.print("Código da turma: ");
        String codigoTurma = scanner.nextLine().trim();

        try {
            turmaService.cancelarTurma(codigoTurma);
            System.out.println("Turma cancelada com sucesso.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    /**
     * RF26 - Coordenador visualiza a lista de espera de cada turma.
     * Permite consultar uma turma específica ou listar todas as turmas com fila.
     */
    private void visualizarListaEsperaTurmas() {
        System.out.println("\n=== LISTA DE ESPERA (RF26) ===");
        System.out.println("1. Visualizar lista de espera de uma turma");
        System.out.println("2. Listar todas as turmas com fila de espera");
        System.out.println("0. Voltar");
        System.out.print("Escolha: ");
        String opcao = scanner.nextLine().trim();

        switch (opcao) {
            case "1" -> visualizarListaEsperaDeUmaTurma();
            case "2" -> listarTurmasComListaEspera();
            case "0" -> System.out.println("Voltando ao menu.");
            default -> System.out.println("Opção inválida.");
        }
    }

    private void visualizarListaEsperaDeUmaTurma() {
        System.out.print("Código da turma: ");
        String codigoTurma = scanner.nextLine().trim();

        try {
            Turma turma = turmaService.obterTurma(codigoTurma);
            List<ItemListaEspera> fila = turmaService.visualizarListaDeEspera(codigoTurma);

            System.out.println("\n📋 Turma " + turma.getCodigo() + " - " + turma.getDisciplina().getNome());
            System.out.println("   Vagas: " + turma.getVagasDisponiveis() + "/" + turma.getLimiteVagas());
            System.out.println("   ─────────────────────────");

            if (fila.isEmpty()) {
                System.out.println("   Nenhum aluno na lista de espera.");
            } else {
                System.out.println("   Lista de espera (ordem de solicitação):");
                for (ItemListaEspera item : fila) {
                    System.out.println("   " + item);
                }
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void listarTurmasComListaEspera() {
        List<Turma> turmas = turmaService.listarTurmasComListaDeEspera();
        if (turmas.isEmpty()) {
            System.out.println("\nNenhuma turma possui alunos em lista de espera.");
            return;
        }

        System.out.println("\nTurmas com lista de espera:");
        for (Turma turma : turmas) {
            System.out.println("   • " + turma.getCodigo() + " - " + turma.getDisciplina().getNome()
                    + " | em espera: " + turma.getTotalEmEspera());
        }
    }

    // ==================== RF16/RF21/RF22/RF23 - ALUNO ====================

    /**
     * RF16/RF21 - Aluno solicita matrícula em uma turma.
     * Se houver vagas, matricula normalmente.
     * Se não houver vagas, adiciona automaticamente à lista de espera (RF21).
     */
    private void solicitarMatriculaEmTurma() {
        System.out.print("\nCódigo da turma: ");
        String codigoTurma = scanner.nextLine().trim();

        try {
            // Obter turma para exibir informações
            Turma turma = turmaService.obterTurma(codigoTurma);
            if (turma == null) {
                System.out.println("Erro: Turma não encontrada.");
                return;
            }

            System.out.println("\n📚 " + turma.getDisciplina().getNome());
            System.out.println("   Professor: " + turma.getProfessor().getNome());
            System.out.println("   Horário: " + turma.getHorario());
            System.out.println("   Sala: " + turma.getSala());
            System.out.println("   Vagas disponíveis: " + turma.getVagasDisponiveis() + "/" + turma.getLimiteVagas());

            // Solicitar matrícula
            turmaService.solicitarMatricula(codigoTurma, usuarioLogado.getMatricula());

            // Verificar se foi matriculado ou adicionado à fila
            int posicao = turmaService.obterPosicaoEmEspera(codigoTurma, usuarioLogado.getMatricula());

            if (posicao == -1) {
                System.out.println("\n✅ Matrícula realizada com sucesso!");
            } else {
                System.out.println("\n❌ Turma cheia!");
                System.out.println("✅ Você foi adicionado à lista de espera.");
                System.out.println("📍 Sua posição na fila: " + posicao);
                System.out.println("Você será promovido automaticamente quando uma vaga abrir.");
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    /**
     * RF23 - Aluno acompanha matrícula e lista de espera em uma turma.
     * Exibe posição na fila e informações da turma.
     */
    private void acompanharMatriculaEEspera() {
        System.out.print("\nCódigo da turma: ");
        String codigoTurma = scanner.nextLine().trim();

        try {
            Turma turma = turmaService.obterTurma(codigoTurma);
            if (turma == null) {
                System.out.println("Erro: Turma não encontrada.");
                return;
            }

            String matriculaAluno = usuarioLogado.getMatricula();
            int posicaoEspera = turmaService.obterPosicaoEmEspera(codigoTurma, matriculaAluno);

            System.out.println("\n📊 " + turma.getDisciplina().getNome());
            System.out.println("   Código: " + codigoTurma);
            System.out.println("   Professor: " + turma.getProfessor().getNome());
            System.out.println("   Horário: " + turma.getHorario());
            System.out.println("   Sala: " + turma.getSala());
            System.out.println("   ─────────────────────────");
            System.out.println("   Vagas: " + turma.getVagasDisponiveis() + "/" + turma.getLimiteVagas() + " disponíveis");
            System.out.println("   Matriculados: " + turma.getAlunoMatriculados().size());

            if (posicaoEspera > 0) {
                System.out.println("   ✅ Na lista de espera: SIM - Posição: " + posicaoEspera + " de " + turma.getAlunosEmEspera().size());
                System.out.println("   👥 Alunos à sua frente: " + (posicaoEspera - 1));
                System.out.println("   ─────────────────────────");
                System.out.println("\nDeseja desistir da lista de espera? (s/n)");
                String resposta = scanner.nextLine().trim().toLowerCase();
                if (resposta.equals("s")) {
                    turmaService.removerDaEspera(codigoTurma, matriculaAluno);
                    System.out.println("✅ Você foi removido da lista de espera.");
                }
            } else if (turma.getAlunoMatriculados().contains(matriculaAluno)) {
                System.out.println("   ✅ Status: MATRICULADO");
            } else {
                System.out.println("   ❌ Status: NÃO MATRICULADO");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    /**
     * RF22/RF23 - Aluno cancela matrícula em uma turma.
     * Valida se o cancelamento está dentro do prazo (RF22).
     * Se sim, remove aluno e promove automaticamente primeiro da fila (RF23).
     */
    private void cancelarMatriculaEmTurma() {
        System.out.print("\nCódigo da turma: ");
        String codigoTurma = scanner.nextLine().trim();

        try {
            Turma turma = turmaService.obterTurma(codigoTurma);
            if (turma == null) {
                System.out.println("Erro: Turma não encontrada.");
                return;
            }

            String matriculaAluno = usuarioLogado.getMatricula();

            System.out.println("\n⚠️  Validando prazo de cancelamento...");

            // Validar prazo usando o método do período letivo (RF22)
            PeriodoLetivo periodo = turma.getPeriodoLetivo();
            if (!periodo.permiteCancelamento()) {
                System.out.println("❌ Cancelamento não permitido (fora do prazo).");
                java.time.LocalDate dataLimite = periodo.getDataLimiteCancelamento();
                System.out.println("   Data limite: " + dataLimite);
                System.out.println("   Data de hoje: " + java.time.LocalDate.now());
                return;
            }

            // Cancelar matrícula (promove automaticamente da fila)
            turmaService.cancelarMatricula(codigoTurma, matriculaAluno);
            
            java.time.LocalDate dataLimite = periodo.getDataLimiteCancelamento();
            if (dataLimite != null) {
                System.out.println("\n✅ Matrícula cancelada com sucesso (prazo até: " + dataLimite + ")");
            } else {
                System.out.println("\n✅ Matrícula cancelada com sucesso");
            }

            // Verificar se havia alguém na fila para ser promovido
            int totalEmEspera = turmaService.consultarQuantidadeEmEspera(codigoTurma);
            if (totalEmEspera > 0) {
                System.out.println("\n🎯 PROMOÇÃO AUTOMÁTICA (RF23):");
                System.out.println("   ✅ Próximo aluno foi promovido da lista de espera");
                System.out.println("   📍 Alunos ainda em fila: " + totalEmEspera);
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    /**
     * Normaliza o formato do horário para HH:mm-HH:mm
     * Exemplo: "8:00-10:00" → "08:00-10:00"
     */
    private String normalizarHorario(String horario) {
        if (horario == null || horario.isBlank()) {
            return horario;
        }

        try {
            String[] partes = horario.split("-");
            if (partes.length != 2) {
                throw new IllegalArgumentException("Horário deve estar no formato HH:mm-HH:mm (ex: 08:00-10:00)");
            }

            String inicio = partes[0].trim();
            String fim = partes[1].trim();

            // Normalizar cada parte para HH:mm
            String inicioNormalizado = normalizarTempo(inicio);
            String fimNormalizado = normalizarTempo(fim);

            return inicioNormalizado + "-" + fimNormalizado;
        } catch (Exception e) {
            throw new IllegalArgumentException("Formato de horário inválido. Use: HH:mm-HH:mm (ex: 08:00-10:00)");
        }
    }

    /**
     * Normaliza um tempo para formato HH:mm
     * Exemplo: "8:00" → "08:00"
     */
    private String normalizarTempo(String tempo) {
        String[] partes = tempo.split(":");
        if (partes.length != 2) {
            throw new IllegalArgumentException("Tempo deve estar no formato HH:mm");
        }

        int hora = Integer.parseInt(partes[0].trim());
        int minuto = Integer.parseInt(partes[1].trim());

        if (hora < 0 || hora > 23 || minuto < 0 || minuto > 59) {
            throw new IllegalArgumentException("Hora e minuto devem estar em valores válidos");
        }

        return String.format("%02d:%02d", hora, minuto);
    }

    public static void main(String[] args) {
        new CLI().iniciar();
    }
}
