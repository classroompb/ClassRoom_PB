package classroompb.service;

import org.example.classroompb.model.Curso;
import org.example.classroompb.repository.CursoRepository;
import org.example.classroompb.service.CursoService;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RF05CadastroCursoTest {

    private CursoService service;

    static class RepositorioFake extends CursoRepository {
        private final java.util.List<Curso> lista = new java.util.ArrayList<>();

        @Override
        public java.util.List<Curso> carregarTodos() {
            return new java.util.ArrayList<>(lista);
        }

        @Override
        public void salvarTodos(java.util.List<Curso> cursos) {
            lista.clear();
            lista.addAll(cursos);
        }
    }

    @BeforeEach
    void setUp() {
        service = new CursoService(new RepositorioFake());
    }

    @Test
    @Order(1)
    void deveCadastrarCursoValido() {
        Curso c = service.cadastrar("CC", "Ciência da Computação");
        assertNotNull(c);
        assertEquals("CC", c.getCodigo());
        assertEquals("Ciência da Computação", c.getNome());
    }

    @Test
    @Order(2)
    void deveRejeitarCursoComCodigoVazio() {
        assertThrows(IllegalArgumentException.class, () ->
                service.cadastrar("", "Algum curso"));
    }

    @Test
    @Order(3)
    void deveRejeitarCursoComCodigoNulo() {
        assertThrows(IllegalArgumentException.class, () ->
                service.cadastrar(null, "Algum curso"));
    }

    @Test
    @Order(4)
    void deveRejeitarCursoComCodigoApenasEspacos() {
        assertThrows(IllegalArgumentException.class, () ->
                service.cadastrar("   ", "Algum curso"));
    }

    @Test
    @Order(5)
    void deveRejeitarCursoComNomeVazio() {
        assertThrows(IllegalArgumentException.class, () ->
                service.cadastrar("CC", ""));
    }

    @Test
    @Order(6)
    void deveRejeitarCursoComNomeNulo() {
        assertThrows(IllegalArgumentException.class, () ->
                service.cadastrar("CC", null));
    }

    @Test
    @Order(7)
    void deveRejeitarCursoComCodigoDuplicado() {
        service.cadastrar("CC", "Ciência da Computação");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                service.cadastrar("CC", "Outro Nome"));
        assertTrue(ex.getMessage().toLowerCase().contains("código")
                || ex.getMessage().toLowerCase().contains("codigo"));
    }

    @Test
    @Order(8)
    void deveRejeitarCursoComCodigoDuplicadoCaseInsensitive() {
        service.cadastrar("CC", "Ciência da Computação");
        assertThrows(IllegalArgumentException.class, () ->
                service.cadastrar("cc", "Computação"));
    }

    @Test
    @Order(9)
    void devePersistirCursoAposCadastro() {
        service.cadastrar("CC", "Ciência da Computação");
        service.cadastrar("ENG", "Engenharia de Software");

        List<Curso> lista = service.listarTodos();
        assertEquals(2, lista.size());
    }

    @Test
    @Order(10)
    void deveBuscarCursoPorCodigo() {
        service.cadastrar("CC", "Ciência da Computação");
        Curso c = service.buscarPorCodigo("CC");
        assertNotNull(c);
        assertEquals("Ciência da Computação", c.getNome());
    }

    @Test
    @Order(11)
    void buscarPorCodigoRetornaNuloParaInexistente() {
        assertNull(service.buscarPorCodigo("INEXISTENTE"));
    }

    @Test
    @Order(12)
    void codigoJaExisteRetornaFalseParaInexistente() {
        assertFalse(service.codigoJaExiste("XYZ"));
    }
}
