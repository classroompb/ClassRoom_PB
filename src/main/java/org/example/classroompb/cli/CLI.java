package org.example.classroompb.cli;

import org.example.classroompb.model.Avaliacao;
import org.example.classroompb.model.Curso;
import org.example.classroompb.model.Disciplina;
import org.example.classroompb.model.ItemListaEspera;
import org.example.classroompb.model.PeriodoLetivo;
import org.example.classroompb.model.SituacaoFinal;
import org.example.classroompb.model.TipoUsuario;
import org.example.classroompb.model.Turma;
import org.example.classroompb.model.Usuario;
import org.example.classroompb.repository.AvaliacaoRepository;
import org.example.classroompb.repository.CursoRepository;
import org.example.classroompb.repository.UsuarioRepository;
import org.example.classroompb.repository.DisciplinaRepository;
import org.example.classroompb.repository.PeriodoLetivoRepository;
import org.example.classroompb.repository.TurmaRepository;
import org.example.classroompb.service.AvaliacaoService;
import org.example.classroompb.service.CursoService;
import org.example.classroompb.service.UsuarioService;
import org.example.classroompb.service.DisciplinaService;
import org.example.classroompb.service.PeriodoLetivoService;
import org.example.classroompb.service.TurmaService;

import java.util.List;
import java.util.Scanner;
import java.util.function.Predicate;

public class CLI {

    private final UsuarioService usuarioService;
    private final CursoService cursoService;
    private final DisciplinaService disciplinaService;
    private final PeriodoLetivoService periodoLetivoService;
    private final TurmaService turmaService;
    private final AvaliacaoService avaliacaoService;
    private final Scanner scanner;
    private Usuario usuarioLogado;

    public CLI() {
        this.usuarioService = new UsuarioService(new UsuarioRepository());
        this.cursoService = new CursoService(new CursoRepository());
        this.disciplinaService = new DisciplinaService(new DisciplinaRepository());
        this.periodoLetivoService = new PeriodoLetivoService(new PeriodoLetivoRepository());
        this.turmaService = new TurmaService(new TurmaRepository(), usuarioService, disciplinaService);
        this.avaliacaoService = new AvaliacaoService(new AvaliacaoRepository(), turmaService);
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
        System.out.println("(digite 0 em qualquer campo para cancelar)");

        String tipo = lerTipoUsuario();
        if (tipo == null) { System.out.println("Cadastro cancelado."); return; }

        String nome = lerCampo("Nome: ", v -> !v.isBlank(), "Nome não pode ser vazio.");
        if (nome == null) { System.out.println("Cadastro cancelado."); return; }

        String matricula = lerCampo("Matrícula: ",
                v -> !v.isBlank() && !usuarioService.matriculaJaExiste(v),
                "Matrícula vazia ou já cadastrada.");
        if (matricula == null) { System.out.println("Cadastro cancelado."); return; }

        String email = lerCampo("Email: ",
                v -> v.contains("@") && !usuarioService.emailJaExiste(v),
                "Email inválido (precisa de @) ou já cadastrado.");
        if (email == null) { System.out.println("Cadastro cancelado."); return; }

        String senha = lerCampo("Senha: ",
                v -> v.length() >= 4, "Senha deve ter no mínimo 4 caracteres.");
        if (senha == null) { System.out.println("Cadastro cancelado."); return; }

        try {
            Usuario u = usuarioService.cadastrar(tipo, nome, matricula, email, senha);
            System.out.println("Usuário cadastrado com sucesso: " + u);
        } catch (IllegalArgumentException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    /**
     * Lê um campo repetindo o prompt até o valor passar na validação, para que um dado
     * inválido não descarte o que já foi preenchido. Retorna null se o usuário digitar "0"
     * (cancela o cadastro).
     */
    private String lerCampo(String rotulo, Predicate<String> valido, String msgErro) {
        while (true) {
            System.out.print(rotulo);
            String valor = scanner.nextLine().trim();
            if (valor.equals("0")) {
                return null;
            }
            if (valido.test(valor)) {
                return valor;
            }
            System.out.println("  " + msgErro);
        }
    }

    /**
     * Lê um número inteiro repetindo o prompt até ser válido (não trava com texto). Retorna null
     * se o usuário digitar "0" (cancela).
     */
    private Integer lerInteiro(String rotulo, java.util.function.IntPredicate valido, String msgErro) {
        while (true) {
            System.out.print(rotulo);
            String valor = scanner.nextLine().trim();
            if (valor.equals("0")) {
                return null;
            }
            try {
                int numero = Integer.parseInt(valor);
                if (valido.test(numero)) {
                    return numero;
                }
                System.out.println("  " + msgErro);
            } catch (NumberFormatException e) {
                System.out.println("  Digite um número inteiro válido.");
            }
        }
    }

    /**
     * Lê o tipo de usuário aceitando o número (1-4) ou o nome. Acelera o cadastro sem obrigar a
     * digitar a palavra inteira. Retorna o tipo canônico, ou null se cancelar (0).
     */
    private String lerTipoUsuario() {
        String entrada = lerCampo(
                "Tipo — 1) ALUNO  2) PROFESSOR  3) COORDENADOR  4) ADMINISTRADOR: ",
                v -> resolverTipo(v) != null,
                "Tipo inválido. Digite 1-4 ou o nome (ALUNO/PROFESSOR/COORDENADOR/ADMINISTRADOR).");
        return (entrada == null) ? null : resolverTipo(entrada);
    }

    /** Converte "1".."4" ou o nome do perfil no tipo canônico; null se não reconhecer. */
    private String resolverTipo(String valor) {
        return switch (valor.toUpperCase()) {
            case "1", "ALUNO" -> "ALUNO";
            case "2", "PROFESSOR" -> "PROFESSOR";
            case "3", "COORDENADOR" -> "COORDENADOR";
            case "4", "ADMINISTRADOR" -> "ADMINISTRADOR";
            default -> null;
        };
    }

    /** Valida o formato do horário (HH:mm-HH:mm) reusando a normalização, sem lançar. */
    private boolean horarioValido(String horario) {
        try {
            normalizarHorario(horario);
            return true;
        } catch (RuntimeException e) {
            return false;
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

        // RF31 - Professor lança notas dos alunos de uma turma
        if (usuarioLogado.getTipo() == TipoUsuario.PROFESSOR && opcao.equals("3")) {
            lancarNotas();
            return;
        }

        // RF32/RF33 - Professor acompanha alunos (média e situação final)
        if (usuarioLogado.getTipo() == TipoUsuario.PROFESSOR && opcao.equals("4")) {
            acompanharAlunos();
            return;
        }

        System.out.println("Funcionalidade em desenvolvimento.");
    }

    private void cadastrarCurso() {
        System.out.println("(digite 0 em qualquer campo para cancelar)");

        String codigo = lerCampo("Código do curso: ",
                v -> !v.isBlank() && !cursoService.codigoJaExiste(v),
                "Código vazio ou já cadastrado.");
        if (codigo == null) { System.out.println("Cadastro cancelado."); return; }

        String nome = lerCampo("Nome do curso: ", v -> !v.isBlank(), "Nome não pode ser vazio.");
        if (nome == null) { System.out.println("Cadastro cancelado."); return; }

        try {
            Curso c = cursoService.cadastrar(codigo, nome);
            System.out.println("Curso cadastrado com sucesso: " + c);
        } catch (IllegalArgumentException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }
    
    private void cadastrarDisciplina() {
        System.out.println("(digite 0 em qualquer campo para cancelar)");

        String codigo = lerCampo("Código da disciplina: ",
                v -> !v.isBlank() && !disciplinaService.jaExiste(v),
                "Código vazio ou já cadastrado.");
        if (codigo == null) { System.out.println("Cadastro cancelado."); return; }

        String nome = lerCampo("Nome da disciplina: ", v -> !v.isBlank(), "Nome não pode ser vazio.");
        if (nome == null) { System.out.println("Cadastro cancelado."); return; }

        Integer cargaHora = lerInteiro("Carga horária: ", n -> n > 0,
                "Carga horária deve ser maior que zero.");
        if (cargaHora == null) { System.out.println("Cadastro cancelado."); return; }

        Integer creditos = lerInteiro("Créditos: ", n -> n > 0, "Créditos deve ser maior que zero.");
        if (creditos == null) { System.out.println("Cadastro cancelado."); return; }

        try {
            disciplinaService.cadastrar(codigo, nome, cargaHora, creditos);
            System.out.println("Disciplina cadastrada com sucesso.");
        } catch (IllegalArgumentException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void adicionarPreRequisito() {
        System.out.println("(digite 0 em qualquer campo para cancelar)");

        String codigoDisciplina = lerCampo("Código da disciplina: ",
                v -> disciplinaService.jaExiste(v), "Disciplina não encontrada.");
        if (codigoDisciplina == null) { System.out.println("Operação cancelada."); return; }

        String codigoPreRequisito = lerCampo("Código da disciplina pré-requisito: ",
                v -> disciplinaService.jaExiste(v), "Disciplina pré-requisito não encontrada.");
        if (codigoPreRequisito == null) { System.out.println("Operação cancelada."); return; }

        try {
            disciplinaService.adicionarPreRequisito(codigoDisciplina, codigoPreRequisito);
            System.out.println("Pré-requisito adicionado com sucesso.");
        } catch (IllegalArgumentException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void ofertarTurma() {
        System.out.println("(digite 0 em qualquer campo para cancelar)");

        String codigoDisciplina = lerCampo("Código da disciplina: ",
                v -> disciplinaService.jaExiste(v), "Disciplina não encontrada.");
        if (codigoDisciplina == null) { System.out.println("Operação cancelada."); return; }

        String matriculaProfessor = lerCampo("Matrícula do professor: ",
                v -> {
                    Usuario u = usuarioService.buscarPorMatricula(v);
                    return u != null && u.getTipo() == TipoUsuario.PROFESSOR;
                },
                "Professor não encontrado (matrícula inexistente ou não é professor).");
        if (matriculaProfessor == null) { System.out.println("Operação cancelada."); return; }

        String identificadorPeriodo = lerCampo("Identificador do período letivo (ex: 2026.1): ",
                v -> periodoLetivoService.jaExiste(v), "Período letivo não encontrado.");
        if (identificadorPeriodo == null) { System.out.println("Operação cancelada."); return; }

        Integer limiteVagas = lerInteiro("Limite de vagas: ", n -> n > 0,
                "Limite de vagas deve ser maior que zero.");
        if (limiteVagas == null) { System.out.println("Operação cancelada."); return; }

        String horario = lerCampo("Horário (ex: 08:00-10:00): ", this::horarioValido,
                "Horário inválido. Use HH:mm-HH:mm (ex: 08:00-10:00).");
        if (horario == null) { System.out.println("Operação cancelada."); return; }

        String sala = lerCampo("Sala: ", v -> !v.isBlank(), "Sala não pode ser vazia.");
        if (sala == null) { System.out.println("Operação cancelada."); return; }

        try {
            PeriodoLetivo periodo = periodoLetivoService.buscarPorIdentificador(identificadorPeriodo);
            Turma turma = turmaService.ofertarTurma(codigoDisciplina, matriculaProfessor, periodo,
                    limiteVagas, normalizarHorario(horario), sala);
            System.out.println("Turma ofertada com sucesso: " + turma);
        } catch (IllegalArgumentException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void cadastrarPeriodoLetivo() {
        System.out.println("(digite 0 para cancelar)");

        String identificador = lerCampo("Identificador do período letivo (ex: 2026.1): ",
                v -> !v.isBlank() && !periodoLetivoService.jaExiste(v),
                "Identificador vazio ou já cadastrado.");
        if (identificador == null) { System.out.println("Cadastro cancelado."); return; }

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

    /**
     * RF31 - O professor lança as notas dos alunos de uma turma sua. Percorre os alunos
     * matriculados e registra a nota informada para cada um (Enter pula o aluno).
     */
    private void lancarNotas() {
        List<Turma> turmas = turmaService.listarTurmasPorProfessor(usuarioLogado.getMatricula());
        if (turmas.isEmpty()) {
            System.out.println("Você não é responsável por nenhuma turma.");
            return;
        }

        System.out.println("\nSuas turmas:");
        for (Turma t : turmas) {
            System.out.println("   " + t.getCodigo() + " - " + t.getDisciplina().getNome()
                    + " (" + t.getTotalMatriculados() + " aluno(s))");
        }

        System.out.print("Código da turma: ");
        String codigoTurma = scanner.nextLine().trim();

        Turma turma = turmaService.buscarPorCodigo(codigoTurma);
        if (turma == null) {
            System.out.println("Erro: Turma não encontrada.");
            return;
        }
        if (!turma.getProfessor().getMatricula().equalsIgnoreCase(usuarioLogado.getMatricula())) {
            System.out.println("Erro: você não é o professor responsável por esta turma.");
            return;
        }

        List<String> alunos = turma.getAlunoMatriculados();
        if (alunos.isEmpty()) {
            System.out.println("Nenhum aluno matriculado nesta turma.");
            return;
        }

        System.out.println("\nDigite a nota de cada aluno (0 a 10, Enter para pular):");
        for (String matricula : alunos) {
            Usuario aluno = usuarioService.buscarPorMatricula(matricula);
            String nome = (aluno != null) ? aluno.getNome() : matricula;
            System.out.print("   " + nome + " (" + matricula + "): ");
            String entrada = scanner.nextLine().trim();
            if (entrada.isEmpty()) {
                continue;
            }
            try {
                double nota = Double.parseDouble(entrada.replace(",", "."));
                avaliacaoService.lancarNota(codigoTurma, matricula, nota);
                System.out.println("      Nota " + nota + " lançada.");
            } catch (NumberFormatException e) {
                System.out.println("      Erro: nota inválida.");
            } catch (IllegalArgumentException e) {
                System.out.println("      Erro: " + e.getMessage());
            }
        }
    }

    /**
     * RF32/RF33 - O professor acompanha os alunos de uma turma: exibe as notas e a média
     * calculada (RF32) e, informada a frequência, define a situação final (RF33).
     */
    private void acompanharAlunos() {
        System.out.print("\nCódigo da turma: ");
        String codigoTurma = scanner.nextLine().trim();

        Turma turma = turmaService.buscarPorCodigo(codigoTurma);
        if (turma == null) {
            System.out.println("Erro: Turma não encontrada.");
            return;
        }
        if (!turma.getProfessor().getMatricula().equalsIgnoreCase(usuarioLogado.getMatricula())) {
            System.out.println("Erro: você não é o professor responsável por esta turma.");
            return;
        }

        List<String> alunos = turma.getAlunoMatriculados();
        if (alunos.isEmpty()) {
            System.out.println("Nenhum aluno matriculado nesta turma.");
            return;
        }

        System.out.println("\n=== Acompanhamento — " + turma.getDisciplina().getNome()
                + " (" + codigoTurma + ") ===");
        for (String matricula : alunos) {
            Usuario aluno = usuarioService.buscarPorMatricula(matricula);
            String nome = (aluno != null) ? aluno.getNome() : matricula;
            Avaliacao avaliacao = avaliacaoService.buscarAvaliacao(codigoTurma, matricula);

            System.out.println("\n" + nome + " (" + matricula + "):");
            if (avaliacao == null || avaliacao.getNotas().isEmpty()) {
                System.out.println("   Sem notas lançadas.");
                continue;
            }

            System.out.println("   Notas: " + avaliacao.getNotas());
            System.out.printf("   Média (RF32): %.2f%n",
                    avaliacaoService.calcularMedia(codigoTurma, matricula));

            System.out.print("   Frequência (%) para definir a situação [Enter para pular]: ");
            String entrada = scanner.nextLine().trim();
            if (entrada.isEmpty()) {
                continue;
            }
            try {
                double frequencia = Double.parseDouble(entrada.replace(",", "."));
                avaliacaoService.registrarFrequencia(codigoTurma, matricula, frequencia);
                SituacaoFinal situacao = avaliacaoService.definirSituacaoFinal(codigoTurma, matricula);
                System.out.println("   Situação final (RF33): " + situacao);
            } catch (NumberFormatException e) {
                System.out.println("   Erro: frequência inválida.");
            } catch (IllegalArgumentException e) {
                System.out.println("   Erro: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        new CLI().iniciar();
    }
}
