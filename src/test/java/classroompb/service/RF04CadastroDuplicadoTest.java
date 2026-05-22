package classroompb.service;

import org.example.classroompb.model.Usuario;
import org.example.classroompb.repository.UsuarioRepository;
import org.example.classroompb.service.UsuarioService;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RF04CadastroDuplicadoTest {

    private UsuarioService service;

    static class RepositorioFake extends UsuarioRepository {
        private final java.util.List<Usuario> lista = new java.util.ArrayList<>();

        @Override
        public java.util.List<Usuario> carregarTodos() {
            return new java.util.ArrayList<>(lista);
        }

        @Override
        public void salvarTodos(java.util.List<Usuario> usuarios) {
            lista.clear();
            lista.addAll(usuarios);
        }
    }

    @BeforeEach
    void setUp() {
        service = new UsuarioService(new RepositorioFake());
    }

    @Test
    @Order(1)
    void deveRejeitarCadastroComMatriculaDuplicada() {
        service.cadastrar("ALUNO", "João", "2026001", "joao@email.com", "1234");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                service.cadastrar("ALUNO", "Outro João", "2026001", "outro@email.com", "1234"));

        assertTrue(ex.getMessage().toLowerCase().contains("matrícula")
                || ex.getMessage().toLowerCase().contains("matricula"));
    }

    @Test
    @Order(2)
    void deveRejeitarCadastroComEmailDuplicado() {
        service.cadastrar("ALUNO", "João", "2026001", "joao@email.com", "1234");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                service.cadastrar("PROFESSOR", "Maria", "P001", "joao@email.com", "1234"));

        assertTrue(ex.getMessage().toLowerCase().contains("email")
                || ex.getMessage().toLowerCase().contains("e-mail"));
    }

    @Test
    @Order(3)
    void deveRejeitarMatriculaDuplicadaIndependenteDoTipo() {
        service.cadastrar("ALUNO", "João", "2026001", "joao@email.com", "1234");
        assertThrows(IllegalArgumentException.class, () ->
                service.cadastrar("ADMINISTRADOR", "Admin", "2026001", "admin@email.com", "1234"));
    }

    @Test
    @Order(4)
    void deveRejeitarMatriculaDuplicadaComCaseDiferente() {
        service.cadastrar("ALUNO", "João", "abc123", "joao@email.com", "1234");
        assertThrows(IllegalArgumentException.class, () ->
                service.cadastrar("ALUNO", "João 2", "ABC123", "outro@email.com", "1234"));
    }

    @Test
    @Order(5)
    void deveRejeitarEmailDuplicadoComCaseDiferente() {
        service.cadastrar("ALUNO", "João", "2026001", "joao@email.com", "1234");
        assertThrows(IllegalArgumentException.class, () ->
                service.cadastrar("ALUNO", "João 2", "2026002", "JOAO@email.com", "1234"));
    }

    @Test
    @Order(6)
    void deveCadastrarUsuarioQuandoMatriculaEEmailNaoExistem() {
        service.cadastrar("ALUNO", "João", "2026001", "joao@email.com", "1234");
        Usuario u = service.cadastrar("ALUNO", "Maria", "2026002", "maria@email.com", "1234");
        assertNotNull(u);
        assertEquals(2, service.listarTodos().size());
    }

    @Test
    @Order(7)
    void matriculaJaExisteRetornaTrueQuandoCadastrado() {
        service.cadastrar("ALUNO", "João", "2026001", "joao@email.com", "1234");
        assertTrue(service.matriculaJaExiste("2026001"));
    }

    @Test
    @Order(8)
    void matriculaJaExisteRetornaFalseQuandoNaoCadastrado() {
        assertFalse(service.matriculaJaExiste("9999999"));
    }

    @Test
    @Order(9)
    void emailJaExisteRetornaTrueQuandoCadastrado() {
        service.cadastrar("ALUNO", "João", "2026001", "joao@email.com", "1234");
        assertTrue(service.emailJaExiste("joao@email.com"));
    }

    @Test
    @Order(10)
    void emailJaExisteRetornaFalseQuandoNaoCadastrado() {
        assertFalse(service.emailJaExiste("naoexiste@email.com"));
    }

    @Test
    @Order(11)
    void matriculaJaExisteRetornaFalseParaNulo() {
        assertFalse(service.matriculaJaExiste(null));
    }

    @Test
    @Order(12)
    void emailJaExisteRetornaFalseParaNulo() {
        assertFalse(service.emailJaExiste(null));
    }
}
