package classroompb.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.example.classroompb.model.*;
import org.example.classroompb.repository.*;
import org.example.classroompb.service.*;
import org.junit.jupiter.api.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RF14AlterarCancelarTurmaTest {

    private TurmaService turmaService;
    private UsuarioService usuarioService;
    private DisciplinaService disciplinaService;
    private PeriodoLetivoService periodoLetivoService;

    static class TurmaRepositorioFake extends TurmaRepository {
        private final List<Turma> lista = new java.util.ArrayList<>();

        @Override
        public List<Turma> carregarTodos() {
            return new java.util.ArrayList<>(lista);
        }

        @Override
        public void salvarTodos(List<Turma> turmas) {
            lista.clear();
            lista.addAll(turmas);
        }
    }

    static class UsuarioRepositorioFake extends UsuarioRepository {
        private final List<Usuario> lista = new java.util.ArrayList<>();

        @Override
        public List<Usuario> carregarTodos() {
            return new java.util.ArrayList<>(lista);
        }

        @Override
        public void salvarTodos(List<Usuario> usuarios) {
            lista.clear();
            lista.addAll(usuarios);
        }
    }

    static class DisciplinaRepositorioFake extends DisciplinaRepository {
        private final List<Disciplina> lista = new java.util.ArrayList<>();

        @Override
        public List<Disciplina> carregarTodos() {
            return new java.util.ArrayList<>(lista);
        }

        @Override
        public void salvarTodos(List<Disciplina> disciplinas) {
            lista.clear();
            lista.addAll(disciplinas);
        }
    }

    static class PeriodoLetivoRepositorioFake extends PeriodoLetivoRepository {
        private final List<PeriodoLetivo> lista = new java.util.ArrayList<>();

        @Override
        public List<PeriodoLetivo> carregarTodos() {
            return new java.util.ArrayList<>(lista);
        }

        @Override
        public void salvarTodos(List<PeriodoLetivo> periodos) {
            lista.clear();
            lista.addAll(periodos);
        }
    }

    private PeriodoLetivo periodo;
    private Turma turma;

    @BeforeEach
    void setUp() {
        var usuarioRepo = new UsuarioRepositorioFake();
        var disciplinaRepo = new DisciplinaRepositorioFake();
        var periodoRepo = new PeriodoLetivoRepositorioFake();
        var turmaRepo = new TurmaRepositorioFake();

        usuarioService = new UsuarioService(usuarioRepo);
        disciplinaService = new DisciplinaService(disciplinaRepo);
        periodoLetivoService = new PeriodoLetivoService(periodoRepo);
        turmaService = new TurmaService(turmaRepo, usuarioService, disciplinaService);

        usuarioService.cadastrar("PROFESSOR", "Prof. João", "P001", "joao@example.com", "1234");
        usuarioService.cadastrar("PROFESSOR", "Prof. Maria", "P002", "maria@example.com", "1234");

        disciplinaService.cadastrar("CC101", "Programação I", 60, 4);

        periodoLetivoService.cadastrar("2024.1");
        periodo = periodoLetivoService.buscarPorIdentificador("2024.1");
        // Período INATIVO por padrão — permite alteração

        turma = turmaService.ofertarTurma("CC101", "P001", periodo, 30, "08:00-10:00", "Sala A1");
    }

    // ===== ALTERAR TURMA =====

    @Test
    @Order(1)
    void deveAlterarSalaComSucesso() {
        Turma alterada = turmaService.alterarTurma(turma.getCodigo(), null, null, null, "Sala B2");
        assertEquals("Sala B2", alterada.getSala());
    }

    @Test
    @Order(2)
    void deveAlterarHorarioComSucesso() {
        Turma alterada =
                turmaService.alterarTurma(turma.getCodigo(), null, null, "14:00-16:00", null);
        assertEquals("14:00-16:00", alterada.getHorario());
    }

    @Test
    @Order(3)
    void deveAlterarLimiteVagasComSucesso() {
        Turma alterada = turmaService.alterarTurma(turma.getCodigo(), null, 50, null, null);
        assertEquals(50, alterada.getLimiteVagas());
    }

    @Test
    @Order(4)
    void deveAlterarProfessorComSucesso() {
        Turma alterada = turmaService.alterarTurma(turma.getCodigo(), "P002", null, null, null);
        assertEquals("P002", alterada.getProfessor().getMatricula());
    }

    @Test
    @Order(5)
    void deveRejeitarAlteracaoComTurmaInexistente() {
        assertThrows(
                IllegalArgumentException.class,
                () -> turmaService.alterarTurma("INVALIDO", null, null, null, "Sala X"));
    }

    @Test
    @Order(6)
    void deveRejeitarAlteracaoComPeriodoAtivo() {
        periodo.ativar();
        assertThrows(
                IllegalStateException.class,
                () -> turmaService.alterarTurma(turma.getCodigo(), null, null, null, "Sala X"));
    }

    @Test
    @Order(7)
    void deveRejeitarAlteracaoComPeriodoEncerrado() {
        periodo.encerrar();
        assertThrows(
                IllegalStateException.class,
                () -> turmaService.alterarTurma(turma.getCodigo(), null, null, null, "Sala X"));
    }

    @Test
    @Order(8)
    void deveRejeitarAlteracaoProfessorComChoqueHorario() {
        // P002 já tem turma no mesmo horário
        turmaService.ofertarTurma("CC101", "P002", periodo, 20, "08:00-10:00", "Sala B1");

        assertThrows(
                IllegalArgumentException.class,
                () -> turmaService.alterarTurma(turma.getCodigo(), "P002", null, null, null));
    }

    @Test
    @Order(9)
    void deveRejeitarAlteracaoComProfessorInexistente() {
        assertThrows(
                IllegalArgumentException.class,
                () -> turmaService.alterarTurma(turma.getCodigo(), "INVALIDO", null, null, null));
    }

    // ===== CANCELAR TURMA =====

    @Test
    @Order(10)
    void deveCancelarTurmaComSucesso() {
        turmaService.cancelarTurma(turma.getCodigo());
        assertNull(turmaService.buscarPorCodigo(turma.getCodigo()));
    }

    @Test
    @Order(11)
    void deveRejeitarCancelamentoComTurmaInexistente() {
        assertThrows(IllegalArgumentException.class, () -> turmaService.cancelarTurma("INVALIDO"));
    }

    @Test
    @Order(12)
    void deveRejeitarCancelamentoComPeriodoAtivo() {
        periodo.ativar();
        assertThrows(
                IllegalStateException.class, () -> turmaService.cancelarTurma(turma.getCodigo()));
    }

    @Test
    @Order(13)
    void deveRejeitarCancelamentoComPeriodoEncerrado() {
        periodo.encerrar();
        assertThrows(
                IllegalStateException.class, () -> turmaService.cancelarTurma(turma.getCodigo()));
    }
}
