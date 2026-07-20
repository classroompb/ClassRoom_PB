package org.example.classroompb.service;

import java.util.List;
import java.util.stream.Collectors;
import org.example.classroompb.dto.RelatorioUsuariosDTO;
import org.example.classroompb.exception.AcessoNegadoException;
import org.example.classroompb.model.Administrador;
import org.example.classroompb.model.Aluno;
import org.example.classroompb.model.Coordenador;
import org.example.classroompb.model.Professor;
import org.example.classroompb.model.TipoUsuario;
import org.example.classroompb.model.Usuario;
import org.example.classroompb.repository.UsuarioRepository;

public class UsuarioService {

    private final UsuarioRepository repository;
    private List<Usuario> usuarios;

    public UsuarioService(UsuarioRepository repository) {
        this.repository = repository;
        this.usuarios = repository.carregarTodos();
    }

    // RF01 - Cadastro de usuários
    public Usuario cadastrar(
            String tipo, String nome, String matricula, String email, String senha) {
        validarCampos(nome, matricula, email, senha);

        // RF04 - Impedir cadastro duplicado
        if (buscarPorMatricula(matricula) != null) {
            throw new IllegalArgumentException(
                    "Já existe um usuário com a matrícula: " + matricula);
        }
        if (buscarPorEmail(email) != null) {
            throw new IllegalArgumentException("Já existe um usuário com o email: " + email);
        }

        Usuario usuario =
                switch (tipo.toUpperCase()) {
                    case "ALUNO" -> new Aluno(nome, matricula, email, senha);
                    case "PROFESSOR" -> new Professor(nome, matricula, email, senha);
                    case "COORDENADOR" -> new Coordenador(nome, matricula, email, senha);
                    case "ADMINISTRADOR" -> new Administrador(nome, matricula, email, senha);
                    default ->
                            throw new IllegalArgumentException(
                                    "Tipo de usuário inválido: "
                                            + tipo
                                            + ". Use: ALUNO, PROFESSOR, COORDENADOR ou ADMINISTRADOR.");
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
            case ALUNO ->
                    """
                    === MENU ALUNO ===
                    1. Consultar disciplinas e turmas
                    2. Solicitar matrícula
                    3. Acompanhar matrícula e lista de espera
                    4. Consultar frequência e notas
                    5. Consultar histórico acadêmico
                    6. Cancelar matrícula
                    0. Sair
                    """;
            case PROFESSOR ->
                    """
                    === MENU PROFESSOR ===
                    1. Visualizar turmas
                    2. Registrar frequência
                    3. Lançar notas
                    4. Acompanhar alunos
                    5. Alterar notas (antes do fechamento)
                    6. Fechar notas de uma turma
                    7. Emitir relatorio da turma
                    0. Sair
                    """;
            case COORDENADOR ->
                    """
                    === MENU COORDENADOR ===
                    1. Cadastrar disciplinas
                    2. Ofertar turmas
                    3. Cadastrar períodos letivos
                    4. Ativar período letivo
                    5. Encerrar período letivo
                    6. Gerenciar turmas (alterar/cancelar)
                    7. Configurar datas do período letivo (RF22)
                    8. Visualizar lista de espera das turmas (RF26)
                    9. Consultar histórico dos alunos do curso (RF39)
                    10. Adicionar pré-requisito em disciplina
                    11. Vincular aluno a um curso (RF39)
                    12. Relatório de alunos por turma (RF40)
                    13. Relatório de ocupação de vagas (RF41)
                    14. Relatório de reprovação por disciplina (RF42)
                    0. Sair
                    """;
            case ADMINISTRADOR ->
                    """
                    === MENU ADMINISTRADOR ===
                    1. Gerenciar usuários
                    2. Cadastrar cursos
                    3. Configurar períodos letivos
                    4. Manter dados básicos
                    5. Relatório geral de usuários (RF43)
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
                .findFirst()
                .orElse(null);
    }

    public Usuario buscarPorEmail(String email) {
        return usuarios.stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElse(null);
    }

    // RF39 - Vincula um aluno já cadastrado a um curso, para permitir a consulta do coordenador
    public void vincularAlunoACurso(String matriculaAluno, String codigoCurso) {
        if (codigoCurso == null || codigoCurso.isBlank()) {
            throw new IllegalArgumentException("Código do curso não pode ser vazio.");
        }
        Usuario usuario = buscarPorMatricula(matriculaAluno);
        if (usuario == null) {
            throw new IllegalArgumentException("Aluno não encontrado: " + matriculaAluno);
        }
        if (!(usuario instanceof Aluno aluno)) {
            throw new IllegalArgumentException("Usuário " + matriculaAluno + " não é um aluno.");
        }
        aluno.vincularCurso(codigoCurso);
        repository.salvarTodos(usuarios);
    }

    // RF39 - Lista os alunos vinculados a um curso, para o coordenador consultar o histórico deles
    public List<Aluno> listarAlunosPorCurso(String codigoCurso) {
        if (codigoCurso == null || codigoCurso.isBlank()) {
            throw new IllegalArgumentException("Código do curso não pode ser vazio.");
        }
        return usuarios.stream()
                .filter(u -> u.getTipo() == TipoUsuario.ALUNO)
                .map(u -> (Aluno) u)
                .filter(a -> codigoCurso.equalsIgnoreCase(a.getCodigoCurso()))
                .collect(Collectors.toList());
    }

    // RF04 - Helpers para verificar duplicidade sem lançar exceção
    public boolean matriculaJaExiste(String matricula) {
        return matricula != null && buscarPorMatricula(matricula) != null;
    }

    public boolean emailJaExiste(String email) {
        return email != null && buscarPorEmail(email) != null;
    }

    /**
     * RF43 - O administrador deve gerar relatório geral de usuários cadastrados. Conta quantos
     * usuários existem de cada perfil e o total. Só o administrador pode gerar.
     */
    public RelatorioUsuariosDTO relatorioGeralUsuarios(Usuario usuarioLogado) {
        if (usuarioLogado == null || usuarioLogado.getTipo() != TipoUsuario.ADMINISTRADOR) {
            throw new AcessoNegadoException(
                    "Acesso negado: apenas administradores podem gerar este relatório.");
        }

        int alunos = 0;
        int professores = 0;
        int coordenadores = 0;
        int administradores = 0;
        for (Usuario u : usuarios) {
            switch (u.getTipo()) {
                case ALUNO -> alunos++;
                case PROFESSOR -> professores++;
                case COORDENADOR -> coordenadores++;
                case ADMINISTRADOR -> administradores++;
            }
        }

        return new RelatorioUsuariosDTO(
                alunos,
                professores,
                coordenadores,
                administradores,
                alunos + professores + coordenadores + administradores);
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
