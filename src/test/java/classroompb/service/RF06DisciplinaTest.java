package classroompb.service;

import org.example.classroompb.model.Disciplina;
import org.example.classroompb.repository.DisciplinaRepository;
import org.example.classroompb.service.DisciplinaService;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RF06DisciplinaTest {

    private DisciplinaService service;

    // Repositório fake em memória — não escreve em disco
    static class RepositorioFake extends DisciplinaRepository {
        private final List<Disciplina> lista = new ArrayList<>();

        @Override
        public List<Disciplina> carregarTodos() {
            return new ArrayList<>(lista);
        }

        @Override
        public void salvarTodos(List<Disciplina> disciplinas) {
            lista.clear();
            lista.addAll(disciplinas);
        }
    }

    @BeforeEach
    void setUp() {
        service = new DisciplinaService(new RepositorioFake());
    }

    // --- RF07: atributos e criação ---

    @Test
    @Order(1)
    void deveCadastrarDisciplinaValida() {
        Disciplina d = service.cadastrar("ES2", "Engenharia de Software 2", 60, 4);
        assertNotNull(d);
        assertEquals("ES2", d.getCodigo());
        assertEquals("Engenharia de Software 2", d.getNome());
        assertEquals(60, d.getCargaHoraria());
        assertEquals(4, d.getCreditos());
    }

    @Test
    @Order(2)
    void disciplinaDeveComecarSemPreRequisitos() {
        Disciplina d = service.cadastrar("ES2", "Engenharia de Software 2", 60, 4);
        assertTrue(d.getPreRequisitos().isEmpty());
    }

    // --- RF06: validação de campos ---

    @Test
    @Order(3)
    void deveRejeitarCodigoVazio() {
        assertThrows(IllegalArgumentException.class, () ->
                service.cadastrar("", "Engenharia de Software 2", 60, 4));
    }

    @Test
    @Order(4)
    void deveRejeitarCodigoNulo() {
        assertThrows(IllegalArgumentException.class, () ->
                service.cadastrar(null, "Engenharia de Software 2", 60, 4));
    }

    @Test
    @Order(5)
    void deveRejeitarCodigoApenasEspacos() {
        assertThrows(IllegalArgumentException.class, () ->
                service.cadastrar("   ", "Engenharia de Software 2", 60, 4));
    }

    @Test
    @Order(6)
    void deveRejeitarNomeVazio() {
        assertThrows(IllegalArgumentException.class, () ->
                service.cadastrar("ES2", "", 60, 4));
    }

    @Test
    @Order(7)
    void deveRejeitarNomeNulo() {
        assertThrows(IllegalArgumentException.class, () ->
                service.cadastrar("ES2", null, 60, 4));
    }

    @Test
    @Order(8)
    void deveRejeitarCargaHorariaZero() {
        assertThrows(IllegalArgumentException.class, () ->
                service.cadastrar("ES2", "Engenharia de Software 2", 0, 4));
    }

    @Test
    @Order(9)
    void deveRejeitarCargaHorariaNegativa() {
        assertThrows(IllegalArgumentException.class, () ->
                service.cadastrar("ES2", "Engenharia de Software 2", -10, 4));
    }

    @Test
    @Order(10)
    void deveRejeitarCreditosZero() {
        assertThrows(IllegalArgumentException.class, () ->
                service.cadastrar("ES2", "Engenharia de Software 2", 60, 0));
    }

    @Test
    @Order(11)
    void deveRejeitarCreditosNegativos() {
        assertThrows(IllegalArgumentException.class, () ->
                service.cadastrar("ES2", "Engenharia de Software 2", 60, -1));
    }

    // --- RF06: duplicata ---

    @Test
    @Order(12)
    void deveRejeitarCodigoDuplicado() {
        service.cadastrar("ES2", "Engenharia de Software 2", 60, 4);
        assertThrows(IllegalArgumentException.class, () ->
                service.cadastrar("ES2", "Outro Nome", 30, 2));
    }

    @Test
    @Order(13)
    void deveRejeitarCodigoDuplicadoCaseInsensitive() {
        service.cadastrar("ES2", "Engenharia de Software 2", 60, 4);
        assertThrows(IllegalArgumentException.class, () ->
                service.cadastrar("es2", "Outro Nome", 30, 2));
    }

    // --- Busca e listagem ---

    @Test
    @Order(14)
    void deveBuscarDisciplinaPorCodigo() {
        service.cadastrar("CALC1", "Cálculo 1", 80, 6);
        Disciplina d = service.buscarPorCodigo("CALC1");
        assertNotNull(d);
        assertEquals("Cálculo 1", d.getNome());
    }

    @Test
    @Order(15)
    void buscarPorCodigoRetornaNuloParaInexistente() {
        assertNull(service.buscarPorCodigo("INEXISTENTE"));
    }

    @Test
    @Order(16)
    void buscarPorCodigoNuloRetornaNulo() {
        assertNull(service.buscarPorCodigo(null));
    }

    @Test
    @Order(17)
    void jaExisteRetornaFalseParaInexistente() {
        assertFalse(service.jaExiste("XYZ"));
    }

    @Test
    @Order(18)
    void deveListarTodasAsDisciplinas() {
        service.cadastrar("ES2", "Engenharia de Software 2", 60, 4);
        service.cadastrar("CALC1", "Cálculo 1", 80, 6);
        assertEquals(2, service.listarTodos().size());
    }

    // --- Pré-requisitos ---

    @Test
    @Order(19)
    void deveAdicionarPreRequisito() {
        service.cadastrar("CALC1", "Cálculo 1", 80, 6);
        service.cadastrar("CALC2", "Cálculo 2", 80, 6);
        service.adicionarPreRequisito("CALC2", "CALC1");

        Disciplina calc2 = service.buscarPorCodigo("CALC2");
        assertTrue(calc2.getPreRequisitos().contains("CALC1"));
    }

    @Test
    @Order(20)
    void deveRejeitarPreRequisitoComDisciplinaInexistente() {
        service.cadastrar("CALC1", "Cálculo 1", 80, 6);
        assertThrows(IllegalArgumentException.class, () ->
                service.adicionarPreRequisito("INEXISTENTE", "CALC1"));
    }

    @Test
    @Order(21)
    void deveRejeitarPreRequisitoInexistente() {
        service.cadastrar("CALC2", "Cálculo 2", 80, 6);
        assertThrows(IllegalArgumentException.class, () ->
                service.adicionarPreRequisito("CALC2", "INEXISTENTE"));
    }
}
