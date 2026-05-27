package org.example.classroompb.service;

import org.example.classroompb.model.*;
import org.example.classroompb.repository.UsuarioRepository;

import java.util.List;

public class UsuarioService {

    private final UsuarioRepository repository;
    private List<Usuario> usuarios;

    public UsuarioService(UsuarioRepository repository) {
        this.repository = repository;
        this.usuarios = repository.carregarTodos();
    }

    // RF01 - Cadastro de usuários
    public Usuario cadastrar(String tipo, String nome, String matricula, String email, String senha) {
        validarCampos(nome, matricula, email, senha);

        // RF04 - Impedir cadastro duplicado
        if (buscarPorMatricula(matricula) != null) {
            throw new IllegalArgumentException("Já existe um usuário com a matrícula: " + matricula);
        }
        if (buscarPorEmail(email) != null) {
            throw new IllegalArgumentException("Já existe um usuário com o email: " + email);
        }

        Usuario usuario = switch (tipo.toUpperCase()) {
            case "ALUNO"          -> new Aluno(nome, matricula, email, senha);
            case "PROFESSOR"      -> new Professor(nome, matricula, email, senha);
            case "COORDENADOR"    -> new Coordenador(nome, matricula, email, senha);
            case "ADMINISTRADOR"  -> new Administrador(nome, matricula, email, senha);
            default -> throw new IllegalArgumentException("Tipo de usuário inválido: " + tipo +
                    ". Use: ALUNO, PROFESSOR, COORDENADOR ou ADMINISTRADOR.");
        };

        usuarios.add(usuario);
        repository.salvarTodos(usuarios);
        return usuario;
    }

    // RF02 - Login com matrícula/email e senha
    public Usuario login(String identificador, String senha) {
        if (identificador == null || identificador.isBlank()) {
            throw new IllegalArgumentException("Informe a matrícula ou email.");
        }
        if (senha == null || senha.isBlank()) {
            throw new IllegalArgumentException("Informe a senha.");
        }

        Usuario usuario = buscarPorMatricula(identificador);
        if (usuario == null) {
            usuario = buscarPorEmail(identificador);
        }

        if (usuario == null || !usuario.getSenha().equals(senha)) {
            throw new IllegalArgumentException("Matrícula/email ou senha incorretos.");
        }

        return usuario;
    }

    // RF03 - Retorna menu de funcionalidades conforme o perfil
    public String getMenuPorPerfil(TipoUsuario tipo) {
        return switch (tipo) {
            case ALUNO -> """
                    === MENU ALUNO ===
                    1. Consultar disciplinas e turmas
                    2. Solicitar matrícula
                    3. Acompanhar matrícula e lista de espera
                    4. Consultar frequência e notas
                    5. Consultar histórico acadêmico
                    6. Cancelar matrícula
                    0. Sair
                    """;
            case PROFESSOR -> """
                    === MENU PROFESSOR ===
                    1. Visualizar turmas
                    2. Registrar frequência
                    3. Lançar notas
                    4. Acompanhar alunos
                    5. Alterar notas (antes do fechamento)
                    0. Sair
                    """;
            case COORDENADOR -> """
                    === MENU COORDENADOR ===
                    1. Cadastrar disciplinas
                    2. Ofertar turmas
                    3. Cadastrar períodos letivos
                    4. Ativar período letivo
                    5. Encerrar período letivo
                    6. Gerenciar vagas e horários
                    7. Aprovar/cancelar matrículas
                    8. Visualizar listas de espera
                    9. Gerar relatórios acadêmicos
                    0. Sair
                    """;
            case ADMINISTRADOR -> """
                    === MENU ADMINISTRADOR ===
                    1. Gerenciar usuários
                    2. Cadastrar cursos
                    3. Configurar períodos letivos
                    4. Manter dados básicos
                    5. Gerar relatórios gerais
                    0. Sair
                    """;
        };
    }

    public List<Usuario> listarTodos() {
        return usuarios;
    }

    public Usuario buscarPorMatricula(String matricula) {
        return usuarios.stream()
                .filter(u -> u.getMatricula().equalsIgnoreCase(matricula))
                .findFirst().orElse(null);
    }

    public Usuario buscarPorEmail(String email) {
        return usuarios.stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .findFirst().orElse(null);
    }

    // RF04 - Helpers para verificar duplicidade sem lançar exceção
    public boolean matriculaJaExiste(String matricula) {
        return matricula != null && buscarPorMatricula(matricula) != null;
    }

    public boolean emailJaExiste(String email) {
        return email != null && buscarPorEmail(email) != null;
    }

    private void validarCampos(String nome, String matricula, String email, String senha) {
        if (nome == null || nome.isBlank())
            throw new IllegalArgumentException("Nome não pode ser vazio.");
        if (matricula == null || matricula.isBlank())
            throw new IllegalArgumentException("Matrícula não pode ser vazia.");
        if (email == null || email.isBlank() || !email.contains("@"))
            throw new IllegalArgumentException("Email inválido.");
        if (senha == null || senha.length() < 4)
            throw new IllegalArgumentException("Senha deve ter no mínimo 4 caracteres.");
    }
}
