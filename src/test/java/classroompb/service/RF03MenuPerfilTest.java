package classroompb.service;

import org.example.classroompb.model.*;
import org.example.classroompb.repository.UsuarioRepository;
import org.example.classroompb.service.UsuarioService;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RF03MenuPerfilTest {

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
    void deveRetornarMenuAluno() {
        String menu = service.getMenuPorPerfil(TipoUsuario.ALUNO);
        assertTrue(menu.contains("MENU ALUNO"));
        assertTrue(menu.contains("matrícula"));
    }

    @Test
    @Order(2)
    void deveRetornarMenuProfessor() {
        String menu = service.getMenuPorPerfil(TipoUsuario.PROFESSOR);
        assertTrue(menu.contains("MENU PROFESSOR"));
        assertTrue(menu.contains("frequência"));
    }

    @Test
    @Order(3)
    void deveRetornarMenuCoordenador() {
        String menu = service.getMenuPorPerfil(TipoUsuario.COORDENADOR);
        assertTrue(menu.contains("MENU COORDENADOR"));
        assertTrue(menu.contains("disciplinas"));
    }

    @Test
    @Order(4)
    void deveRetornarMenuAdministrador() {
        String menu = service.getMenuPorPerfil(TipoUsuario.ADMINISTRADOR);
        assertTrue(menu.contains("MENU ADMINISTRADOR"));
        assertTrue(menu.contains("usuários"));
    }
}