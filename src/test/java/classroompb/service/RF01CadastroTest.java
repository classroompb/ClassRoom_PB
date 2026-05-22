package classroompb.service;

import org.example.classroompb.model.*;
import org.example.classroompb.repository.UsuarioRepository;
import org.example.classroompb.service.UsuarioService;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RF01CadastroTest {

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
    void deveCadastrarAluno() {
        Usuario u = service.cadastrar("ALUNO", "João", "2026001", "joao@email.com", "1234");
        assertNotNull(u);
        assertInstanceOf(Aluno.class, u);
        assertEquals(TipoUsuario.ALUNO, u.getTipo());
        assertEquals("João", u.getNome());
    }

    @Test
    @Order(2)
    void deveCadastrarProfessor() {
        Usuario u = service.cadastrar("PROFESSOR", "Maria", "P001", "maria@email.com", "1234");
        assertInstanceOf(Professor.class, u);
        assertEquals(TipoUsuario.PROFESSOR, u.getTipo());
    }

    @Test
    @Order(3)
    void deveCadastrarCoordenador() {
        Usuario u = service.cadastrar("COORDENADOR", "Carlos", "C001", "carlos@email.com", "1234");
        assertInstanceOf(Coordenador.class, u);
        assertEquals(TipoUsuario.COORDENADOR, u.getTipo());
    }

    @Test
    @Order(4)
    void deveCadastrarAdministrador() {
        Usuario u = service.cadastrar("ADMINISTRADOR", "Ana", "A001", "ana@email.com", "1234");
        assertInstanceOf(Administrador.class, u);
        assertEquals(TipoUsuario.ADMINISTRADOR, u.getTipo());
    }

    @Test
    @Order(5)
    void deveLancarExcecaoParaTipoInvalido() {
        assertThrows(IllegalArgumentException.class, () ->
                service.cadastrar("DIRETOR", "X", "X001", "x@email.com", "1234"));
    }

    @Test
    @Order(6)
    void deveLancarExcecaoParaNomeVazio() {
        assertThrows(IllegalArgumentException.class, () ->
                service.cadastrar("ALUNO", "", "2026002", "a@email.com", "1234"));
    }

    @Test
    @Order(7)
    void deveLancarExcecaoParaEmailInvalido() {
        assertThrows(IllegalArgumentException.class, () ->
                service.cadastrar("ALUNO", "Nome", "2026003", "emailsemarroba", "1234"));
    }

    @Test
    @Order(8)
    void deveLancarExcecaoParaSenhaCurta() {
        assertThrows(IllegalArgumentException.class, () ->
                service.cadastrar("ALUNO", "Nome", "2026004", "b@email.com", "123"));
    }

    @Test
    @Order(9)
    void devePersistirUsuarioAposCadastro() {
        service.cadastrar("ALUNO", "João", "2026001", "joao@email.com", "1234");
        List<Usuario> lista = service.listarTodos();
        assertEquals(1, lista.size());
        assertEquals("João", lista.get(0).getNome());
    }
}