package classroompb.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.example.classroompb.model.*;
import org.example.classroompb.repository.DisciplinaRepository;
import org.example.classroompb.repository.PeriodoLetivoRepository;
import org.example.classroompb.repository.TurmaRepository;
import org.example.classroompb.repository.UsuarioRepository;
import org.example.classroompb.service.DisciplinaService;
import org.example.classroompb.service.PeriodoLetivoService;
import org.example.classroompb.service.TurmaService;
import org.example.classroompb.service.UsuarioService;
import org.junit.jupiter.api.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RF10TurmasOfertadasTest {

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

    @BeforeEach
    void setUp() {
        // Inicializa repositórios fake
        var usuarioRepo = new UsuarioRepositorioFake();
        var disciplinaRepo = new DisciplinaRepositorioFake();
        var periodoRepo = new PeriodoLetivoRepositorioFake();
        var turmaRepo = new TurmaRepositorioFake();

        // Inicializa serviços
        usuarioService = new UsuarioService(usuarioRepo);
        disciplinaService = new DisciplinaService(disciplinaRepo);
        periodoLetivoService = new PeriodoLetivoService(periodoRepo);
        turmaService = new TurmaService(turmaRepo, usuarioService, disciplinaService);

        // Cria dados de teste
        usuarioService.cadastrar("PROFESSOR", "Prof. João", "P001", "joao@example.com", "1234");
        usuarioService.cadastrar("PROFESSOR", "Prof. Maria", "P002", "maria@example.com", "1234");

        disciplinaService.cadastrar("CC101", "Programação I", 60, 4);
        disciplinaService.cadastrar("CC102", "Programação II", 60, 4);

        periodoLetivoService.cadastrar("2024.1");
        periodoLetivoService.cadastrar("2024.2");
    }

    @Test
    @Order(1)
    void deveOfertarTurmaComSucesso() {
        PeriodoLetivo periodo = periodoLetivoService.buscarPorIdentificador("2024.1");

        Turma turma =
                turmaService.ofertarTurma("CC101", "P001", periodo, 30, "08:00-10:00", "Sala A1");

        assertNotNull(turma);
        assertEquals("CC101", turma.getDisciplina().getCodigo());
        assertEquals("P001", turma.getProfessor().getMatricula());
        assertEquals(30, turma.getLimiteVagas());
        assertEquals("08:00-10:00", turma.getHorario());
        assertEquals("Sala A1", turma.getSala());
    }

    @Test
    @Order(2)
    void deveRejeitarTurmaComDisciplinaInvalida() {
        PeriodoLetivo periodo = periodoLetivoService.buscarPorIdentificador("2024.1");

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        turmaService.ofertarTurma(
                                "INVALIDA", "P001", periodo, 30, "08:00-10:00", "Sala A1"));
    }

    @Test
    @Order(3)
    void deveRejeitarTurmaComProfessorInvalido() {
        PeriodoLetivo periodo = periodoLetivoService.buscarPorIdentificador("2024.1");

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        turmaService.ofertarTurma(
                                "CC101", "INVALIDO", periodo, 30, "08:00-10:00", "Sala A1"));
    }

    @Test
    @Order(4)
    void deveRejeitarTurmaComLimiteVagasZero() {
        PeriodoLetivo periodo = periodoLetivoService.buscarPorIdentificador("2024.1");

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        turmaService.ofertarTurma(
                                "CC101", "P001", periodo, 0, "08:00-10:00", "Sala A1"));
    }

    @Test
    @Order(5)
    void deveRejeitarTurmaComLimiteVagasNegativo() {
        PeriodoLetivo periodo = periodoLetivoService.buscarPorIdentificador("2024.1");

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        turmaService.ofertarTurma(
                                "CC101", "P001", periodo, -5, "08:00-10:00", "Sala A1"));
    }

    @Test
    @Order(6)
    void deveRejeitarTurmaComHorarioVazio() {
        PeriodoLetivo periodo = periodoLetivoService.buscarPorIdentificador("2024.1");

        assertThrows(
                IllegalArgumentException.class,
                () -> turmaService.ofertarTurma("CC101", "P001", periodo, 30, "", "Sala A1"));
    }

    @Test
    @Order(7)
    void deveRejeitarTurmaComSalaVazia() {
        PeriodoLetivo periodo = periodoLetivoService.buscarPorIdentificador("2024.1");

        assertThrows(
                IllegalArgumentException.class,
                () -> turmaService.ofertarTurma("CC101", "P001", periodo, 30, "08:00-10:00", ""));
    }

    @Test
    @Order(8)
    void deveRejeitarTurmaComPeriodoNulo() {
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        turmaService.ofertarTurma(
                                "CC101", "P001", null, 30, "08:00-10:00", "Sala A1"));
    }

    @Test
    @Order(9)
    void devePersistirTurmaAposOferta() {
        PeriodoLetivo periodo = periodoLetivoService.buscarPorIdentificador("2024.1");

        turmaService.ofertarTurma("CC101", "P001", periodo, 30, "08:00-10:00", "Sala A1");
        turmaService.ofertarTurma("CC102", "P002", periodo, 25, "10:00-12:00", "Sala A2");

        List<Turma> turmas = turmaService.listarTodasAsTurmas();
        assertEquals(2, turmas.size());
    }

    @Test
    @Order(10)
    void deveBuscarTurmaPorCodigo() {
        PeriodoLetivo periodo = periodoLetivoService.buscarPorIdentificador("2024.1");

        Turma turmaOfertada =
                turmaService.ofertarTurma("CC101", "P001", periodo, 30, "08:00-10:00", "Sala A1");
        Turma turmaBuscada = turmaService.buscarPorCodigo(turmaOfertada.getCodigo());

        assertNotNull(turmaBuscada);
        assertEquals(turmaOfertada.getCodigo(), turmaBuscada.getCodigo());
    }

    @Test
    @Order(11)
    void deveListarTurmasPorDisciplina() {
        PeriodoLetivo periodo = periodoLetivoService.buscarPorIdentificador("2024.1");

        turmaService.ofertarTurma("CC101", "P001", periodo, 30, "08:00-10:00", "Sala A1");
        turmaService.ofertarTurma("CC101", "P002", periodo, 25, "14:00-16:00", "Sala B1");
        turmaService.ofertarTurma("CC102", "P001", periodo, 20, "10:00-12:00", "Sala A2");

        List<Turma> turmasCC101 = turmaService.listarTurmasPorDisciplina("CC101");
        assertEquals(2, turmasCC101.size());
    }

    @Test
    @Order(12)
    void deveListarTurmasPorProfessor() {
        PeriodoLetivo periodo = periodoLetivoService.buscarPorIdentificador("2024.1");

        turmaService.ofertarTurma("CC101", "P001", periodo, 30, "08:00-10:00", "Sala A1");
        turmaService.ofertarTurma("CC102", "P001", periodo, 25, "14:00-16:00", "Sala B1");
        turmaService.ofertarTurma("CC101", "P002", periodo, 20, "10:00-12:00", "Sala A2");

        List<Turma> turmasP001 = turmaService.listarTurmasPorProfessor("P001");
        assertEquals(2, turmasP001.size());
    }

    @Test
    @Order(13)
    void deveListarTurmasPorPeriodo() {
        PeriodoLetivo periodo1 = periodoLetivoService.buscarPorIdentificador("2024.1");
        PeriodoLetivo periodo2 = periodoLetivoService.buscarPorIdentificador("2024.2");

        turmaService.ofertarTurma("CC101", "P001", periodo1, 30, "08:00-10:00", "Sala A1");
        turmaService.ofertarTurma("CC101", "P002", periodo1, 25, "14:00-16:00", "Sala B1");
        turmaService.ofertarTurma("CC102", "P001", periodo2, 20, "10:00-12:00", "Sala A2");

        List<Turma> turmas2024_1 = turmaService.listarTurmasPorPeriodo(periodo1);
        assertEquals(2, turmas2024_1.size());

        List<Turma> turmas2024_2 = turmaService.listarTurmasPorPeriodo(periodo2);
        assertEquals(1, turmas2024_2.size());
    }

    @Test
    @Order(14)
    void deveRejeitarTurmaComCodigoDisciplinaVazio() {
        PeriodoLetivo periodo = periodoLetivoService.buscarPorIdentificador("2024.1");

        assertThrows(
                IllegalArgumentException.class,
                () -> turmaService.ofertarTurma("", "P001", periodo, 30, "08:00-10:00", "Sala A1"));
    }

    @Test
    @Order(15)
    void deveRejeitarTurmaComMatriculaProfessorVazia() {
        PeriodoLetivo periodo = periodoLetivoService.buscarPorIdentificador("2024.1");

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        turmaService.ofertarTurma(
                                "CC101", "", periodo, 30, "08:00-10:00", "Sala A1"));
    }
}
