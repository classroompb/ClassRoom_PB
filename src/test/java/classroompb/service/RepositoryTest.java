package classroompb.service;

import org.example.classroompb.model.*;
import org.example.classroompb.repository.*;
import org.junit.jupiter.api.*;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RepositoryTest {

    private static final String ARQUIVO_TESTE_PERIODO = "dados/teste-periodos.json";
    private static final String ARQUIVO_TESTE_DISCIPLINA = "dados/teste-disciplinas.json";

    @AfterEach
    void limpar() {
        new File(ARQUIVO_TESTE_PERIODO).delete();
        new File(ARQUIVO_TESTE_DISCIPLINA).delete();
    }

    // ===== PeriodoLetivoRepository =====

    @Test
    @Order(1)
    void deveSalvarECarregarPeriodoLetivo() {
        PeriodoLetivoRepository repo = new PeriodoLetivoRepository(ARQUIVO_TESTE_PERIODO);

        PeriodoLetivo p = new PeriodoLetivo("2024.1");
        repo.salvarTodos(List.of(p));

        List<PeriodoLetivo> carregados = repo.carregarTodos();
        assertEquals(1, carregados.size());
        assertEquals("2024.1", carregados.get(0).getIdentificador());
    }






    @Test
    @Order(2)
    void deveRetornarListaVaziaQuandoArquivoNaoExiste() {
        PeriodoLetivoRepository repo = new PeriodoLetivoRepository(ARQUIVO_TESTE_PERIODO);
        List<PeriodoLetivo> lista = repo.carregarTodos();
        assertNotNull(lista);
        assertTrue(lista.isEmpty());
    }

    @Test
    @Order(3)
    void deveSalvarMultiplosPeriodos() {
        PeriodoLetivoRepository repo = new PeriodoLetivoRepository(ARQUIVO_TESTE_PERIODO);

        PeriodoLetivo p1 = new PeriodoLetivo("2024.1");
        PeriodoLetivo p2 = new PeriodoLetivo("2024.2");
        repo.salvarTodos(List.of(p1, p2));

        List<PeriodoLetivo> carregados = repo.carregarTodos();
        assertEquals(2, carregados.size());
    }

    @Test
    @Order(4)
    void devePersistirStatusDoPeriodo() {
        PeriodoLetivoRepository repo = new PeriodoLetivoRepository(ARQUIVO_TESTE_PERIODO);

        PeriodoLetivo p = new PeriodoLetivo("2024.1");
        p.ativar();
        repo.salvarTodos(List.of(p));

        List<PeriodoLetivo> carregados = repo.carregarTodos();
        assertEquals(StatusPeriodoLetivo.ATIVO, carregados.get(0).getStatus());
    }

    @Test
    @Order(5)
    void deveSobreescreverAoSalvarNovamente() {
        PeriodoLetivoRepository repo = new PeriodoLetivoRepository(ARQUIVO_TESTE_PERIODO);

        repo.salvarTodos(List.of(new PeriodoLetivo("2024.1"), new PeriodoLetivo("2024.2")));
        repo.salvarTodos(List.of(new PeriodoLetivo("2025.1")));

        List<PeriodoLetivo> carregados = repo.carregarTodos();
        assertEquals(1, carregados.size());
        assertEquals("2025.1", carregados.get(0).getIdentificador());
    }

    // ===== DisciplinaRepository (Fake) =====

    @Test
    @Order(6)
    void deveSalvarECarregarDisciplinaViaFake() {
        var repo = new DisciplinaRepository() {
            private List<Disciplina> lista = new java.util.ArrayList<>();
            @Override public List<Disciplina> carregarTodos() { return new java.util.ArrayList<>(lista); }
            @Override public void salvarTodos(List<Disciplina> d) { lista.clear(); lista.addAll(d); }
        };

        Disciplina d = new Disciplina("CC101", "Programação I", 60, 4);
        repo.salvarTodos(List.of(d));

        List<Disciplina> carregados = repo.carregarTodos();
        assertEquals(1, carregados.size());
        assertEquals("CC101", carregados.get(0).getCodigo());
    }

    @Test
    @Order(7)
    void deveRetornarListaVaziaFakeDisciplina() {
        var repo = new DisciplinaRepository() {
            private List<Disciplina> lista = new java.util.ArrayList<>();
            @Override public List<Disciplina> carregarTodos() { return new java.util.ArrayList<>(lista); }
            @Override public void salvarTodos(List<Disciplina> d) { lista.clear(); lista.addAll(d); }
        };

        assertTrue(repo.carregarTodos().isEmpty());
    }

    @Test
    @Order(8)
    void deveSalvarMultiplasDisciplinas() {
        var repo = new DisciplinaRepository() {
            private List<Disciplina> lista = new java.util.ArrayList<>();
            @Override public List<Disciplina> carregarTodos() { return new java.util.ArrayList<>(lista); }
            @Override public void salvarTodos(List<Disciplina> d) { lista.clear(); lista.addAll(d); }
        };

        repo.salvarTodos(List.of(
            new Disciplina("CC101", "Programação I", 60, 4),
            new Disciplina("CC102", "Programação II", 60, 4)
        ));

        assertEquals(2, repo.carregarTodos().size());
    }
}