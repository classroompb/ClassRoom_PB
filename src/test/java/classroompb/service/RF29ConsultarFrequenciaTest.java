package classroompb.service;

import org.example.classroompb.exception.AlunoNaoEncontradoException;
import org.example.classroompb.exception.AlunoNaoMatriculadoException;
import org.example.classroompb.exception.DisciplinaNaoEncontradaException;
import org.example.classroompb.exception.FrequenciaNaoRegistradaException;
import org.example.classroompb.model.Disciplina;
import org.example.classroompb.model.FrequenciaDisciplina;
import org.example.classroompb.model.PeriodoLetivo;
import org.example.classroompb.model.RegistroFrequencia;
import org.example.classroompb.model.Turma;
import org.example.classroompb.model.Usuario;
import org.example.classroompb.repository.DisciplinaRepository;
import org.example.classroompb.repository.FrequenciaRepository;
import org.example.classroompb.repository.PeriodoLetivoRepository;
import org.example.classroompb.repository.TurmaRepository;
import org.example.classroompb.repository.UsuarioRepository;
import org.example.classroompb.service.ConsultaFrequenciaService;
import org.example.classroompb.service.DisciplinaService;
import org.example.classroompb.service.FrequenciaService;
import org.example.classroompb.service.PeriodoLetivoService;
import org.example.classroompb.service.TurmaService;
import org.example.classroompb.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RF29: O aluno deve poder consultar sua frequência por disciplina.
 */
class RF29ConsultarFrequenciaTest {

    private ConsultaFrequenciaService consultaFrequenciaService;
    private FrequenciaService frequenciaService;
    private TurmaService turmaService;
    private UsuarioService usuarioService;
    private DisciplinaService disciplinaService;
    private PeriodoLetivoService periodoLetivoService;
    private Turma turmaCC101;
    private Turma turmaMAT101;

    private final LocalDate dia1 = LocalDate.of(2026, 6, 1);
    private final LocalDate dia2 = LocalDate.of(2026, 6, 8);
    private final LocalDate dia3 = LocalDate.of(2026, 6, 15);
    private final LocalDate dia4 = LocalDate.of(2026, 6, 22);

    // ==================== Repositórios Fake ====================

    static class FrequenciaRepositorioFake extends FrequenciaRepository {
        private final List<RegistroFrequencia> lista = new ArrayList<>();
        @Override public List<RegistroFrequencia> carregarTodos() { return new ArrayList<>(lista); }
        @Override public void salvarTodos(List<RegistroFrequencia> registros) { lista.clear(); lista.addAll(registros); }
    }

    static class TurmaRepositorioFake extends TurmaRepository {
        private final List<Turma> lista = new ArrayList<>();
        @Override public List<Turma> carregarTodos() { return new ArrayList<>(lista); }
        @Override public void salvarTodos(List<Turma> turmas) { lista.clear(); lista.addAll(turmas); }
    }

    static class UsuarioRepositorioFake extends UsuarioRepository {
        private final List<Usuario> lista = new ArrayList<>();
        @Override public List<Usuario> carregarTodos() { return new ArrayList<>(lista); }
        @Override public void salvarTodos(List<Usuario> usuarios) { lista.clear(); lista.addAll(usuarios); }
    }

    static class DisciplinaRepositorioFake extends DisciplinaRepository {
        private final List<Disciplina> lista = new ArrayList<>();
        @Override public List<Disciplina> carregarTodos() { return new ArrayList<>(lista); }
        @Override public void salvarTodos(List<Disciplina> disciplinas) { lista.clear(); lista.addAll(disciplinas); }
    }

    static class PeriodoLetivoRepositorioFake extends PeriodoLetivoRepository {
        private final List<PeriodoLetivo> lista = new ArrayList<>();
        @Override public List<PeriodoLetivo> carregarTodos() { return new ArrayList<>(lista); }
        @Override public void salvarTodos(List<PeriodoLetivo> periodos) { lista.clear(); lista.addAll(periodos); }
    }

    // ==================== Setup ====================

    @BeforeEach
    void setUp() {
        usuarioService = new UsuarioService(new UsuarioRepositorioFake());
        disciplinaService = new DisciplinaService(new DisciplinaRepositorioFake());
        periodoLetivoService = new PeriodoLetivoService(new PeriodoLetivoRepositorioFake());
        turmaService = new TurmaService(new TurmaRepositorioFake(), usuarioService, disciplinaService);
        frequenciaService = new FrequenciaService(new FrequenciaRepositorioFake(), usuarioService, turmaService);
        consultaFrequenciaService = new ConsultaFrequenciaService(
                frequenciaService, turmaService, disciplinaService, usuarioService);

        usuarioService.cadastrar("PROFESSOR", "Prof. Ana", "P001", "ana@example.com", "1234");
        usuarioService.cadastrar("PROFESSOR", "Prof. Beto", "P002", "beto@example.com", "1234");
        usuarioService.cadastrar("ALUNO", "Aluno 1", "A001", "aluno1@example.com", "1234");
        usuarioService.cadastrar("ALUNO", "Aluno 2", "A002", "aluno2@example.com", "1234");
        usuarioService.cadastrar("ALUNO", "Aluno 3 (nao matriculado)", "A003", "aluno3@example.com", "1234");

        disciplinaService.cadastrar("CC101", "Programacao I", 60, 4);
        disciplinaService.cadastrar("MAT101", "Calculo I", 60, 4);
        disciplinaService.cadastrar("FIS101", "Fisica I", 60, 4);
        periodoLetivoService.cadastrar("2026.1");
        PeriodoLetivo periodo = periodoLetivoService.buscarPorIdentificador("2026.1");

        turmaCC101 = turmaService.ofertarTurma("CC101", "P001", periodo, 30, "08:00-10:00", "Sala A1");
        turmaMAT101 = turmaService.ofertarTurma("MAT101", "P002", periodo, 30, "10:00-12:00", "Sala A2");

        turmaService.solicitarMatricula(turmaCC101.getCodigo(), "A001");
        turmaService.solicitarMatricula(turmaCC101.getCodigo(), "A002");
        turmaService.solicitarMatricula(turmaMAT101.getCodigo(), "A001");
    }

    // ==================== Fluxo principal ====================

    @Test
    void deveConsultarFrequenciaDeUmaDisciplina() {
        frequenciaService.registrarPresenca(turmaCC101.getCodigo(), "A001", dia1);
        frequenciaService.registrarPresenca(turmaCC101.getCodigo(), "A001", dia2);
        frequenciaService.registrarFalta(turmaCC101.getCodigo(), "A001", dia3);

        FrequenciaDisciplina frequencia =
                consultaFrequenciaService.consultarPorDisciplina("A001", "CC101");

        assertEquals("CC101", frequencia.getCodigoDisciplina());
        assertEquals("Programacao I", frequencia.getNomeDisciplina());
        assertEquals(3, frequencia.getTotalAulas());
        assertEquals(2, frequencia.getTotalPresencas());
        assertEquals(1, frequencia.getTotalFaltas());
        assertEquals(66.67, frequencia.getPercentualFrequencia(), 0.01);
    }

    @Test
    void deveIndicarFrequenciaSuficienteAcimaDe75() {
        frequenciaService.registrarPresenca(turmaCC101.getCodigo(), "A001", dia1);
        frequenciaService.registrarPresenca(turmaCC101.getCodigo(), "A001", dia2);
        frequenciaService.registrarPresenca(turmaCC101.getCodigo(), "A001", dia3);
        frequenciaService.registrarFalta(turmaCC101.getCodigo(), "A001", dia4);

        FrequenciaDisciplina frequencia =
                consultaFrequenciaService.consultarPorDisciplina("A001", "CC101");

        assertEquals(75.0, frequencia.getPercentualFrequencia(), 0.01);
        assertTrue(frequencia.atingiuFrequenciaMinima());
    }

    @Test
    void deveIndicarFrequenciaInsuficienteAbaixoDe75() {
        frequenciaService.registrarPresenca(turmaCC101.getCodigo(), "A001", dia1);
        frequenciaService.registrarFalta(turmaCC101.getCodigo(), "A001", dia2);
        frequenciaService.registrarFalta(turmaCC101.getCodigo(), "A001", dia3);

        FrequenciaDisciplina frequencia =
                consultaFrequenciaService.consultarPorDisciplina("A001", "CC101");

        assertFalse(frequencia.atingiuFrequenciaMinima());
    }

    @Test
    void deveConsultarFrequenciaIndependentePorAluno() {
        frequenciaService.registrarPresenca(turmaCC101.getCodigo(), "A001", dia1);
        frequenciaService.registrarPresenca(turmaCC101.getCodigo(), "A001", dia2);

        frequenciaService.registrarFalta(turmaCC101.getCodigo(), "A002", dia1);
        frequenciaService.registrarPresenca(turmaCC101.getCodigo(), "A002", dia2);

        FrequenciaDisciplina freqA001 = consultaFrequenciaService.consultarPorDisciplina("A001", "CC101");
        FrequenciaDisciplina freqA002 = consultaFrequenciaService.consultarPorDisciplina("A002", "CC101");

        assertEquals(100.0, freqA001.getPercentualFrequencia(), 0.01);
        assertEquals(50.0, freqA002.getPercentualFrequencia(), 0.01);
    }

    @Test
    void deveConsultarFrequenciaSeparadaPorDisciplina() {
        frequenciaService.registrarPresenca(turmaCC101.getCodigo(), "A001", dia1);
        frequenciaService.registrarPresenca(turmaCC101.getCodigo(), "A001", dia2);

        frequenciaService.registrarFalta(turmaMAT101.getCodigo(), "A001", dia1);
        frequenciaService.registrarFalta(turmaMAT101.getCodigo(), "A001", dia2);

        FrequenciaDisciplina freqCC = consultaFrequenciaService.consultarPorDisciplina("A001", "CC101");
        FrequenciaDisciplina freqMAT = consultaFrequenciaService.consultarPorDisciplina("A001", "MAT101");

        assertEquals(100.0, freqCC.getPercentualFrequencia(), 0.01);
        assertEquals(0.0, freqMAT.getPercentualFrequencia(), 0.01);
    }

    @Test
    void deveAceitarCodigoDisciplinaSemDistincaoDeCaixa() {
        frequenciaService.registrarPresenca(turmaCC101.getCodigo(), "A001", dia1);

        FrequenciaDisciplina frequencia =
                consultaFrequenciaService.consultarPorDisciplina("A001", "cc101");

        assertEquals("CC101", frequencia.getCodigoDisciplina());
    }

    // ==================== Consulta de todas as disciplinas ====================

    @Test
    void deveConsultarTodasAsDisciplinasComRegistro() {
        frequenciaService.registrarPresenca(turmaCC101.getCodigo(), "A001", dia1);
        frequenciaService.registrarFalta(turmaMAT101.getCodigo(), "A001", dia1);

        List<FrequenciaDisciplina> frequencias =
                consultaFrequenciaService.consultarTodasAsDisciplinas("A001");

        assertEquals(2, frequencias.size());
    }

    @Test
    void deveIgnorarDisciplinasSemRegistroNaConsultaGeral() {
        // A001 está em CC101 e MAT101, mas só CC101 tem registros.
        frequenciaService.registrarPresenca(turmaCC101.getCodigo(), "A001", dia1);

        List<FrequenciaDisciplina> frequencias =
                consultaFrequenciaService.consultarTodasAsDisciplinas("A001");

        assertEquals(1, frequencias.size());
        assertEquals("CC101", frequencias.get(0).getCodigoDisciplina());
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHaRegistros() {
        List<FrequenciaDisciplina> frequencias =
                consultaFrequenciaService.consultarTodasAsDisciplinas("A001");

        assertTrue(frequencias.isEmpty());
    }

    @Test
    void deveRetornarListaVaziaParaAlunoSemMatriculas() {
        List<FrequenciaDisciplina> frequencias =
                consultaFrequenciaService.consultarTodasAsDisciplinas("A003");

        assertTrue(frequencias.isEmpty());
    }

    // ==================== Casos de erro ====================

    @Test
    void deveRejeitarAlunoInexistente() {
        assertThrows(AlunoNaoEncontradoException.class,
                () -> consultaFrequenciaService.consultarPorDisciplina("A999", "CC101"));
    }

    @Test
    void deveRejeitarProfessorComoAluno() {
        assertThrows(AlunoNaoEncontradoException.class,
                () -> consultaFrequenciaService.consultarPorDisciplina("P001", "CC101"));
    }

    @Test
    void deveRejeitarDisciplinaInexistente() {
        assertThrows(DisciplinaNaoEncontradaException.class,
                () -> consultaFrequenciaService.consultarPorDisciplina("A001", "ZZZ999"));
    }

    @Test
    void deveRejeitarAlunoNaoMatriculadoNaDisciplina() {
        // A001 não está matriculado em FIS101.
        assertThrows(AlunoNaoMatriculadoException.class,
                () -> consultaFrequenciaService.consultarPorDisciplina("A001", "FIS101"));
    }

    @Test
    void deveRejeitarQuandoNaoHaFrequenciaRegistrada() {
        // A001 está em CC101, mas nenhuma frequência foi lançada.
        assertThrows(FrequenciaNaoRegistradaException.class,
                () -> consultaFrequenciaService.consultarPorDisciplina("A001", "CC101"));
    }

    @Test
    void deveRejeitarMatriculaVazia() {
        assertThrows(IllegalArgumentException.class,
                () -> consultaFrequenciaService.consultarPorDisciplina("   ", "CC101"));
    }

    @Test
    void deveRejeitarCodigoDisciplinaVazio() {
        assertThrows(IllegalArgumentException.class,
                () -> consultaFrequenciaService.consultarPorDisciplina("A001", "   "));
    }

    @Test
    void deveRejeitarMatriculaNula() {
        assertThrows(IllegalArgumentException.class,
                () -> consultaFrequenciaService.consultarPorDisciplina(null, "CC101"));
    }

    @Test
    void excecoesDevemSerCompativeisComIllegalArgument() {
        // Garante que o CLI (que captura IllegalArgumentException) continua funcionando.
        assertThrows(IllegalArgumentException.class,
                () -> consultaFrequenciaService.consultarPorDisciplina("A999", "CC101"));
    }

    // ==================== Caso limite do DTO ====================

    @Test
    void dtoDeveRejeitarTotalDeAulasZero() {
        assertThrows(IllegalArgumentException.class,
                () -> new FrequenciaDisciplina("CC101", "Programacao I", List.of("T1"), 0, 0));
    }

    @Test
    void dtoDeveRejeitarPresencasMaioresQueAulas() {
        assertThrows(IllegalArgumentException.class,
                () -> new FrequenciaDisciplina("CC101", "Programacao I", List.of("T1"), 5, 3));
    }
}