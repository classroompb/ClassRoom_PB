package classroompb.service;

import org.example.classroompb.model.*;
import org.example.classroompb.repository.UsuarioRepository;
import org.example.classroompb.service.UsuarioService;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RF02LoginTest {

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
    void deveRealizarLoginComMatricula() {
        service.cadastrar("ALUNO", "João", "2026001", "joao@email.com", "1234");
        Usuario logado = service.login("2026001", "1234");
        assertNotNull(logado);
        assertEquals("João", logado.getNome());
    }

    @Test
    @Order(2)
    void deveRealizarLoginComEmail() {
        service.cadastrar("ALUNO", "João", "2026001", "joao@email.com", "1234");
        Usuario logado = service.login("joao@email.com", "1234");
        assertNotNull(logado);
        assertEquals("2026001", logado.getMatricula());
    }

    @Test
    @Order(3)
    void deveRejeitarSenhaErrada() {
        service.cadastrar("ALUNO", "João", "2026001", "joao@email.com", "1234");
        assertThrows(IllegalArgumentException.class, () ->
                service.login("2026001", "senhaerrada"));
    }

    @Test
    @Order(4)
    void deveRejeitarUsuarioInexistente() {
        assertThrows(IllegalArgumentException.class, () ->
                service.login("naoexiste", "1234"));
    }

    @Test
    @Order(5)
    void deveRejeitarLoginComIdentificadorVazio() {
        assertThrows(IllegalArgumentException.class, () ->
                service.login("", "1234"));
    }

    @Test
    @Order(6)
    void deveRejeitarLoginComSenhaVazia() {
        service.cadastrar("ALUNO", "João", "2026001", "joao@email.com", "1234");
        assertThrows(IllegalArgumentException.class, () ->
                service.login("2026001", ""));
    }
}