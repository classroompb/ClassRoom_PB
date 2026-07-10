package classroompb.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.example.classroompb.model.*;
import org.example.classroompb.repository.*;
import org.example.classroompb.service.*;
import org.junit.jupiter.api.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RF19ChoqueHorarioTest {

    private TurmaService turmaService;
    private UsuarioService usuarioService;
    private DisciplinaService disciplinaService;
    private PeriodoLetivo periodo;

    static class TurmaRepositorioFake extends TurmaRepository {
        private final List<Turma> lista = new java.util.ArrayList<>();

        @Override
        public List<Turma> carregarTodos() {
            return new java.util.ArrayList<>(lista);
        }

        @Override
        public void salvarTodos(List<Turma> t) {
            lista.clear();
            lista.addAll(t);
        }
    }

    static class UsuarioRepositorioFake extends UsuarioRepository {
        private final List<Usuario> lista = new java.util.ArrayList<>();

        @Override
        public List<Usuario> carregarTodos() {
            return new java.util.ArrayList<>(lista);
        }

        @Override
        public void salvarTodos(List<Usuario> u) {
            lista.clear();
            lista.addAll(u);
        }
    }

    static class DisciplinaRepositorioFake extends DisciplinaRepository {
        private final List<Disciplina> lista = new java.util.ArrayList<>();

        @Override
        public List<Disciplina> carregarTodos() {
            return new java.util.ArrayList<>(lista);
        }

        @Override
        public void salvarTodos(List<Disciplina> d) {
            lista.clear();
            lista.addAll(d);
        }
    }

    static class PeriodoLetivoRepositorioFake extends PeriodoLetivoRepository {
        private final List<PeriodoLetivo> lista = new java.util.ArrayList<>();

        @Override
        public List<PeriodoLetivo> carregarTodos() {
            return new java.util.ArrayList<>(lista);
        }

        @Override
        public void salvarTodos(List<PeriodoLetivo> p) {
            lista.clear();
            lista.addAll(p);
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
        var periodoLetivoService = new PeriodoLetivoService(periodoRepo);
        turmaService = new TurmaService(turmaRepo, usuarioService, disciplinaService);

        usuarioService.cadastrar("PROFESSOR", "Prof. João", "P001", "joao@example.com", "1234");
        usuarioService.cadastrar("PROFESSOR", "Prof. Maria", "P002", "maria@example.com", "1234");
        usuarioService.cadastrar("ALUNO", "Aluno 1", "A001", "aluno1@example.com", "1234");

        disciplinaService.cadastrar("CC101", "Programação I", 60, 4);
        disciplinaService.cadastrar("CC102", "Banco de Dados", 60, 4);

        periodoLetivoService.cadastrar("2026.1");
        periodo = periodoLetivoService.buscarPorIdentificador("2026.1");
    }

    @Test
    @Order(1)
    void devePermitirMatriculaEmHorariosDistintos() {
        Turma t1 =
                turmaService.ofertarTurma("CC101", "P001", periodo, 30, "08:00-10:00", "Sala A1");
        Turma t2 =
                turmaService.ofertarTurma("CC102", "P002", periodo, 30, "10:00-12:00", "Sala A2");
        turmaService.solicitarMatricula(t1.getCodigo(), "A001");
        turmaService.solicitarMatricula(t2.getCodigo(), "A001");
        assertTrue(t1.alunoJaMatriculado("A001"));
        assertTrue(t2.alunoJaMatriculado("A001"));
    }

    @Test
    @Order(2)
    void deveBloquearMatriculaComChoqueDeHorario() {
        Turma t1 =
                turmaService.ofertarTurma("CC101", "P001", periodo, 30, "08:00-10:00", "Sala A1");
        Turma t2 =
                turmaService.ofertarTurma("CC102", "P002", periodo, 30, "09:00-11:00", "Sala A2");
        turmaService.solicitarMatricula(t1.getCodigo(), "A001");
        assertThrows(
                IllegalArgumentException.class,
                () -> turmaService.solicitarMatricula(t2.getCodigo(), "A001"));
    }

    @Test
    @Order(3)
    void deveBloquearMatriculaComHorarioExatamenteIgual() {
        Turma t1 =
                turmaService.ofertarTurma("CC101", "P001", periodo, 30, "08:00-10:00", "Sala A1");
        Turma t2 =
                turmaService.ofertarTurma("CC102", "P002", periodo, 30, "08:00-10:00", "Sala A2");
        turmaService.solicitarMatricula(t1.getCodigo(), "A001");
        assertThrows(
                IllegalArgumentException.class,
                () -> turmaService.solicitarMatricula(t2.getCodigo(), "A001"));
    }

    @Test
    @Order(4)
    void devePermitirMatriculaEmHorarioSequencial() {
        Turma t1 =
                turmaService.ofertarTurma("CC101", "P001", periodo, 30, "08:00-10:00", "Sala A1");
        Turma t2 =
                turmaService.ofertarTurma("CC102", "P002", periodo, 30, "10:00-12:00", "Sala A2");
        turmaService.solicitarMatricula(t1.getCodigo(), "A001");
        turmaService.solicitarMatricula(t2.getCodigo(), "A001");
        assertTrue(t2.alunoJaMatriculado("A001"));
    }
}
