package org.example.classroompb.cli;

import org.example.classroompb.model.Usuario;
import org.example.classroompb.repository.UsuarioRepository;
import org.example.classroompb.service.UsuarioService;

import java.util.Scanner;

public class CLI {

    private final UsuarioService usuarioService;
    private final Scanner scanner;
    private Usuario usuarioLogado;

    public CLI() {
        this.usuarioService = new UsuarioService(new UsuarioRepository());
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
        } else {
            System.out.println("Funcionalidade em desenvolvimento.");
        }
    }

    public static void main(String[] args) {
        new CLI().iniciar();
    }
}
