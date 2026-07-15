package classroompb.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.example.classroompb.model.Disciplina;
import org.example.classroompb.model.PeriodoLetivo;
import org.example.classroompb.model.RegistroFrequencia;
import org.example.classroompb.model.Turma;
import org.example.classroompb.model.Usuario;
import org.example.classroompb.repository.DisciplinaRepository;
import org.example.classroompb.repository.FrequenciaRepository;
import org.example.classroompb.repository.PeriodoLetivoRepository;
import org.example.classroompb.repository.TurmaRepository;
import org.example.classroompb.repository.UsuarioRepository;
import org.example.classroompb.service.DisciplinaService;
import org.example.classroompb.service.FrequenciaService;
import org.example.classroompb.service.PeriodoLetivoService;
import org.example.classroompb.service.TurmaService;
import org.example.classroompb.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** RF27: O professor deve poder registrar presença/falta do aluno. */
class RF27RegistrarFrequenciaTest {

    private FrequenciaService frequenciaService;
    private TurmaService turmaService;
    private UsuarioService usuarioService;
    private DisciplinaService disciplinaService;
    private FrequenciaRepositorioFake frequenciaRepo;
    private Turma turma;

    private final LocalDate dia1 = LocalDate.of(2026, 6, 1);
    private final LocalDate dia2 = LocalDate.of(2026, 6, 8);

    static class FrequenciaRepositorioFake extends FrequenciaRepository {
        private final List<RegistroFrequencia> lista = new ArrayList<>();

        @Override
        public List<RegistroFrequencia> carregarTodos() {
            return new ArrayList<>(lista);
        }

        @Override
        public void salvarTodos(List<RegistroFrequencia> registros) {
            lista.clear();
            lista.addAll(registros);
        }
    }

    static class TurmaRepositorioFake extends TurmaRepository {
        private final List<Turma> lista = new ArrayList<>();

        @Override
        public List<Turma> carregarTodos() {
            return new ArrayList<>(lista);
        }

        @Override
        public void salvarTodos(List<Turma> turmas) {
            lista.clear();
            lista.addAll(turmas);
        }
    }

    static class UsuarioRepositorioFake extends UsuarioRepository {
        private final List<Usuario> lista = new ArrayList<>();

        @Override
        public List<Usuario> carregarTodos() {
            return new ArrayList<>(lista);
        }

        @Override
        public void salvarTodos(List<Usuario> usuarios) {
            lista.clear();
            lista.addAll(usuarios);
        }
    }

    static class DisciplinaRepositorioFake extends DisciplinaRepository {
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

    static class PeriodoLetivoRepositorioFake extends PeriodoLetivoRepository {
        private final List<PeriodoLetivo> lista = new ArrayList<>();

        @Override
        public List<PeriodoLetivo> carregarTodos() {
            return new ArrayList<>(lista);
        }

        @Override
        public void salvarTodos(List<PeriodoLetivo> periodos) {
            lista.clear();
            lista.addAll(periodos);
        }
    }

    @BeforeEach
    void setUp() {
        usuarioService = new UsuarioService(new UsuarioRepositorioFake());
        disciplinaService = new DisciplinaService(new DisciplinaRepositorioFake());
        var periodoLetivoService = new PeriodoLetivoService(new PeriodoLetivoRepositorioFake());
        turmaService =
                new TurmaService(new TurmaRepositorioFake(), usuarioService, disciplinaService);
        frequenciaRepo = new FrequenciaRepositorioFake();
        frequenciaService = new FrequenciaService(frequenciaRepo, usuarioService, turmaService);

        usuarioService.cadastrar("PROFESSOR", "Prof. Ana", "P001", "ana@example.com", "1234");
        usuarioService.cadastrar("ALUNO", "Aluno 1", "A001", "aluno1@example.com", "1234");
        usuarioService.cadastrar("ALUNO", "Aluno 2", "A002", "aluno2@example.com", "1234");
        usuarioService.cadastrar(
                "ALUNO", "Aluno 3 (nao matriculado)", "A003", "aluno3@example.com", "1234");

        disciplinaService.cadastrar("CC101", "Programacao I", 60, 4);
        periodoLetivoService.cadastrar("2026.1");
        PeriodoLetivo periodo = periodoLetivoService.buscarPorIdentificador("2026.1");

        turma = turmaService.ofertarTurma("CC101", "P001", periodo, 30, "08:00-10:00", "Sala A1");
        turmaService.solicitarMatricula(turma.getCodigo(), "A001");
        turmaService.solicitarMatricula(turma.getCodigo(), "A002");
    }

    @Test
    void deveRegistrarPresencaDeAlunoMatriculado() {
        RegistroFrequencia registro =
                frequenciaService.registrarPresenca(turma.getCodigo(), "A001", dia1);

        assertTrue(registro.isPresente());
        assertFalse(registro.isFalta());
        assertEquals("A001", registro.getMatriculaAluno());
        assertEquals(dia1, registro.getData());
        assertEquals(1, frequenciaService.contarPresencas(turma.getCodigo(), "A001"));
    }

    @Test
    void deveRegistrarFaltaDeAlunoMatriculado() {
        RegistroFrequencia registro =
                frequenciaService.registrarFalta(turma.getCodigo(), "A001", dia1);

        assertTrue(registro.isFalta());
        assertFalse(registro.isPresente());
        assertEquals(1, frequenciaService.contarFaltas(turma.getCodigo(), "A001"));
    }

    @Test
    void deveRegistrarViaMetodoGenericoComBooleano() {
        frequenciaService.registrarFrequencia(turma.getCodigo(), "A002", dia1, false);

        assertEquals(1, frequenciaService.contarFaltas(turma.getCodigo(), "A002"));
        assertEquals(0, frequenciaService.contarPresencas(turma.getCodigo(), "A002"));
    }

    @Test
    void deveRejeitarTurmaInexistente() {
        assertThrows(
                IllegalArgumentException.class,
                () -> frequenciaService.registrarPresenca("TURMA-INEXISTENTE", "A001", dia1));
    }

    @Test
    void deveRejeitarAlunoInexistente() {
        assertThrows(
                IllegalArgumentException.class,
                () -> frequenciaService.registrarPresenca(turma.getCodigo(), "A999", dia1));
    }

    @Test
    void deveRejeitarAlunoNaoMatriculadoNaTurma() {
        // A003 existe como aluno, mas não está matriculado na turma.
        assertThrows(
                IllegalArgumentException.class,
                () -> frequenciaService.registrarPresenca(turma.getCodigo(), "A003", dia1));
    }

    @Test
    void deveRejeitarDataNula() {
        assertThrows(
                IllegalArgumentException.class,
                () -> frequenciaService.registrarPresenca(turma.getCodigo(), "A001", null));
    }

    @Test
    void deveRejeitarCodigoTurmaVazio() {
        assertThrows(
                IllegalArgumentException.class,
                () -> frequenciaService.registrarPresenca("  ", "A001", dia1));
    }

    @Test
    void deveRejeitarMatriculaVazia() {
        assertThrows(
                IllegalArgumentException.class,
                () -> frequenciaService.registrarPresenca(turma.getCodigo(), "  ", dia1));
    }

    @Test
    void deveRejeitarRegistroDuplicadoNaMesmaData() {
        frequenciaService.registrarPresenca(turma.getCodigo(), "A001", dia1);

        assertThrows(
                IllegalArgumentException.class,
                () -> frequenciaService.registrarFalta(turma.getCodigo(), "A001", dia1));
    }

    @Test
    void devePermitirRegistrosEmDatasDiferentes() {
        frequenciaService.registrarPresenca(turma.getCodigo(), "A001", dia1);
        frequenciaService.registrarFalta(turma.getCodigo(), "A001", dia2);

        assertEquals(1, frequenciaService.contarPresencas(turma.getCodigo(), "A001"));
        assertEquals(1, frequenciaService.contarFaltas(turma.getCodigo(), "A001"));
        assertEquals(2, frequenciaService.listarPorAluno(turma.getCodigo(), "A001").size());
    }

    @Test
    void deveListarRegistrosPorTurma() {
        frequenciaService.registrarPresenca(turma.getCodigo(), "A001", dia1);
        frequenciaService.registrarFalta(turma.getCodigo(), "A002", dia1);

        assertEquals(2, frequenciaService.listarPorTurma(turma.getCodigo()).size());
    }

    @Test
    void deveListarRegistrosPorAluno() {
        frequenciaService.registrarPresenca(turma.getCodigo(), "A001", dia1);
        frequenciaService.registrarPresenca(turma.getCodigo(), "A002", dia1);

        assertEquals(1, frequenciaService.listarPorAluno(turma.getCodigo(), "A001").size());
    }

    @Test
    void jaRegistradoRefleteOEstado() {
        assertFalse(frequenciaService.jaRegistrado(turma.getCodigo(), "A001", dia1));

        frequenciaService.registrarPresenca(turma.getCodigo(), "A001", dia1);

        assertTrue(frequenciaService.jaRegistrado(turma.getCodigo(), "A001", dia1));
    }

    @Test
    void devePersistirRegistrosEntreInstancias() {
        frequenciaService.registrarPresenca(turma.getCodigo(), "A001", dia1);
        frequenciaService.registrarFalta(turma.getCodigo(), "A002", dia1);

        // Nova instância sobre o mesmo repositório deve recarregar os registros salvos.
        FrequenciaService recarregado =
                new FrequenciaService(frequenciaRepo, usuarioService, turmaService);

        assertEquals(2, recarregado.listarPorTurma(turma.getCodigo()).size());
        assertTrue(recarregado.jaRegistrado(turma.getCodigo(), "A001", dia1));
    }
}
