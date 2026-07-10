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

class RF13TurmaSemProfessorTest {

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
        var usuarioRepo = new UsuarioRepositorioFake();
        var disciplinaRepo = new DisciplinaRepositorioFake();
        var periodoRepo = new PeriodoLetivoRepositorioFake();
        var turmaRepo = new TurmaRepositorioFake();

        usuarioService = new UsuarioService(usuarioRepo);
        disciplinaService = new DisciplinaService(disciplinaRepo);
        periodoLetivoService = new PeriodoLetivoService(periodoRepo);
        turmaService = new TurmaService(turmaRepo, usuarioService, disciplinaService);

        usuarioService.cadastrar("PROFESSOR", "Prof. João", "P001", "joao@example.com", "1234");
        disciplinaService.cadastrar("CC101", "Programação I", 60, 4);
        periodoLetivoService.cadastrar("2024.1");
    }

    @Test
    void deveImpedirOfertaTurmaComProfessorInvalido() {
        PeriodoLetivo periodo = periodoLetivoService.buscarPorIdentificador("2024.1");
        Exception ex =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                turmaService.ofertarTurma(
                                        "CC101",
                                        "INVALIDO",
                                        periodo,
                                        30,
                                        "08:00-10:00",
                                        "Sala A1"));
        assertTrue(ex.getMessage().toLowerCase().contains("professor"));
    }

    @Test
    void deveImpedirOfertaTurmaComMatriculaProfessorVazia() {
        PeriodoLetivo periodo = periodoLetivoService.buscarPorIdentificador("2024.1");
        Exception ex =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                turmaService.ofertarTurma(
                                        "CC101", "", periodo, 30, "08:00-10:00", "Sala A1"));
        assertTrue(
                ex.getMessage().toLowerCase().contains("professor")
                        || ex.getMessage().toLowerCase().contains("vazia"));
    }

    @Test
    void deveImpedirOfertaTurmaComMatriculaProfessorNula() {
        PeriodoLetivo periodo = periodoLetivoService.buscarPorIdentificador("2024.1");
        Exception ex =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                turmaService.ofertarTurma(
                                        "CC101", null, periodo, 30, "08:00-10:00", "Sala A1"));
        assertTrue(
                ex.getMessage().toLowerCase().contains("professor")
                        || ex.getMessage().toLowerCase().contains("nulo"));
    }
}
