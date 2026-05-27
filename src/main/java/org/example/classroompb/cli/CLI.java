package org.example.classroompb.cli;

import org.example.classroompb.model.Curso;
import org.example.classroompb.model.Disciplina;
import org.example.classroompb.model.PeriodoLetivo;
import org.example.classroompb.model.TipoUsuario;
import org.example.classroompb.model.Usuario;
import org.example.classroompb.repository.CursoRepository;
import org.example.classroompb.repository.UsuarioRepository;
import org.example.classroompb.repository.DisciplinaRepository;
import org.example.classroompb.repository.PeriodoLetivoRepository;
import org.example.classroompb.service.CursoService;
import org.example.classroompb.service.UsuarioService;
import org.example.classroompb.service.DisciplinaService;
import org.example.classroompb.service.PeriodoLetivoService;

import java.util.Scanner;

public class CLI {

    private final UsuarioService usuarioService;
    private final CursoService cursoService;
    private final DisciplinaService disciplinaService;
    private final PeriodoLetivoService periodoLetivoService;
    private final Scanner scanner;
    private Usuario usuarioLogado;

    public CLI() {
        this.usuarioService = new UsuarioService(new UsuarioRepository());
        this.cursoService = new CursoService(new CursoRepository());
        this.disciplinaService = new DisciplinaService(new DisciplinaRepository());
        this.periodoLetivoService = new PeriodoLetivoService(new PeriodoLetivoRepository());
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
