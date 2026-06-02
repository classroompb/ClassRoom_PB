package org.example.classroompb.cli;

import org.example.classroompb.model.*;
import org.example.classroompb.repository.*;
import org.example.classroompb.service.*;

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
        System.out.println(usuarioService.getMenuPorPerfil(usuarioLogado.getTipo()));
        System.out.print("Escolha: ");
        String opcao = scanner.nextLine().trim();

        if (opcao.equals("0")) {
            System.out.println("Logout realizado.");
            usuarioLogado = null;
            return;
        }

        if (usuarioLogado.getTipo() == TipoUsuario.ADMINISTRADOR && opcao.equals("2")) {
            cadastrarCurso();
            return;
        }

        if (usuarioLogado.getTipo() == TipoUsuario.COORDENADOR) {
            switch (opcao) {
                case "1" -> cadastrarDisciplina();
                case "2" -> ofertarTurma();
                case "3" -> cadastrarPeriodoLetivo();
                case "4" -> ativarPeriodoLetivo();
                case "5" -> encerrarPeriodoLetivo();
                case "6" -> alterarTurma();
                case "7" -> cancelarTurma();
                default  -> System.out.println("Funcionalidade em desenvolvimento.");
            }
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
        System.out.print("Código da disciplina: ");
        String codigo = scanner.nextLine().trim();
        System.out.print("Nome da disciplina: ");
        String nome = scanner.nextLine().trim();
        System.out.print("Carga horária: ");
        int cargaHora = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Créditos: ");
        int creditos = Integer.parseInt(scanner.nextLine().trim());

        try {
            disciplinaService.cadastrar(codigo, nome, cargaHora, creditos);
            System.out.println("Disciplina cadastrada com sucesso.");
        } catch (IllegalArgumentException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void ofertarTurma() {
        System.out.print("Código da disciplina: ");
        String codigoDisciplina = scanner.nextLine().trim();
        System.out.print("Matrícula do professor: ");
        String matriculaProfessor = scanner.nextLine().trim();
        System.out.print("Período letivo (ex: 2026.1): ");
        String identificadorPeriodo = scanner.nextLine().trim();
        System.out.print("Limite de vagas: ");
        int vagas = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Horário (ex: 08:00-10:00): ");
        String horario = scanner.nextLine().trim();
        System.out.print("Sala: ");
        String sala = scanner.nextLine().trim();

        try {
            PeriodoLetivo periodo = periodoLetivoService.buscarPorIdentificador(identificadorPeriodo);
            if (periodo == null) {
                System.out.println("Erro: Período letivo não encontrado.");
                return;
            }
            Turma turma = turmaService.ofertarTurma(codigoDisciplina, matriculaProfessor, periodo, vagas, horario, sala);
            System.out.println("Turma ofertada com sucesso: " + turma);
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void alterarTurma() {
        System.out.print("Código da turma: ");
        String codigoTurma = scanner.nextLine().trim();
        System.out.print("Nova matrícula do professor (Enter para manter): ");
        String novoProfessor = scanner.nextLine().trim();
        System.out.print("Novo limite de vagas (0 para manter): ");
        String vagasStr = scanner.nextLine().trim();
        System.out.print("Novo horário (Enter para manter): ");
        String novoHorario = scanner.nextLine().trim();
        System.out.print("Nova sala (Enter para manter): ");
        String novaSala = scanner.nextLine().trim();

        try {
        	Integer novasVagas = (vagasStr.isEmpty() || vagasStr.equals("0")) ? null : Integer.parseInt(vagasStr);
            Turma turma = turmaService.alterarTurma(
                codigoTurma,
                novoProfessor.isEmpty() ? null : novoProfessor,
                novasVagas,
                novoHorario.isEmpty() ? null : novoHorario,
                novaSala.isEmpty() ? null : novaSala
            );
            System.out.println("Turma alterada com sucesso: " + turma);
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void cancelarTurma() {
        System.out.print("Código da turma: ");
        String codigoTurma = scanner.nextLine().trim();

        try {
            turmaService.cancelarTurma(codigoTurma);
            System.out.println("Turma cancelada com sucesso.");
        } catch (Exception e) {
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

    public static void main(String[] args) {
        new CLI().iniciar();
    }
}