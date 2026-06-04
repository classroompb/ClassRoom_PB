package org.example.classroompb.cli;

import org.example.classroompb.model.Curso;
import org.example.classroompb.model.Disciplina;
import org.example.classroompb.model.PeriodoLetivo;
import org.example.classroompb.model.TipoUsuario;
import org.example.classroompb.model.Turma;
import org.example.classroompb.model.Usuario;
import org.example.classroompb.repository.CursoRepository;
import org.example.classroompb.repository.UsuarioRepository;
import org.example.classroompb.repository.DisciplinaRepository;
import org.example.classroompb.repository.PeriodoLetivoRepository;
import org.example.classroompb.repository.TurmaRepository;
import org.example.classroompb.service.CursoService;
import org.example.classroompb.service.UsuarioService;
import org.example.classroompb.service.DisciplinaService;
import org.example.classroompb.service.PeriodoLetivoService;
import org.example.classroompb.service.TurmaService;

import java.util.Scanner;

public class CLI {

    private final UsuarioService usuarioService;
    private final CursoService cursoService;
    private final DisciplinaService disciplinaService;
    private final PeriodoLetivoService periodoLetivoService;
    private final TurmaService turmaService;
    private final Scanner scanner;
    private Usuario usuarioLogado;

    public CLI() {
        this.usuarioService = new UsuarioService(new UsuarioRepository());
        this.cursoService = new CursoService(new CursoRepository());
        this.disciplinaService = new DisciplinaService(new DisciplinaRepository());
        this.periodoLetivoService = new PeriodoLetivoService(new PeriodoLetivoRepository());
        this.turmaService = new TurmaService(new TurmaRepository(), usuarioService, disciplinaService);
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

        // RF15 - Aluno consulta disciplinas/turmas disponiveis
        if (usuarioLogado.getTipo() == TipoUsuario.ALUNO && opcao.equals("1")) {
            consultarTurmasDisponiveis();
            return;
        }

        // RF16/RF17 - Aluno solicita matricula com verificacao de vagas
        if (usuarioLogado.getTipo() == TipoUsuario.ALUNO && opcao.equals("2")) {
            solicitarMatricula();
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

            Turma turma = turmaService.ofertarTurma(
                    codigoDisciplina,
                    matriculaProfessor,
                    periodo,
                    limiteVagas,
                    horario,
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

    private void consultarTurmasDisponiveis() {
        System.out.print("Filtrar por periodo letivo? Informe o identificador ou deixe vazio: ");
        String identificadorPeriodo = scanner.nextLine().trim();

        try {
            var turmas = identificadorPeriodo.isBlank()
                    ? turmaService.consultarTurmasDisponiveis()
                    : turmaService.consultarTurmasDisponiveisPorPeriodo(identificadorPeriodo);

            if (turmas.isEmpty()) {
                System.out.println("Nenhuma turma disponivel encontrada.");
                return;
            }

            System.out.println("\nTurmas disponiveis:");
            for (Turma turma : turmas) {
                System.out.println(turma);
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void solicitarMatricula() {
        System.out.print("Codigo da turma: ");
        String codigoTurma = scanner.nextLine().trim();

        try {
            if (!turmaService.verificarVagasDisponiveis(codigoTurma)) {
                System.out.println("Nao ha vagas disponiveis para esta turma.");
                return;
            }

            turmaService.solicitarMatricula(codigoTurma, usuarioLogado.getMatricula());
            System.out.println("Matricula solicitada com sucesso.");
        } catch (IllegalArgumentException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new CLI().iniciar();
    }
}
