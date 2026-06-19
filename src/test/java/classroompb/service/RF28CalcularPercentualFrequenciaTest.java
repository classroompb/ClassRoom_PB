package classroompb.service;

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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RF28: O sistema deve calcular automaticamente o percentual de frequência.
 */
class RF28CalcularPercentualFrequenciaTest {

    private FrequenciaService frequenciaService;
    private TurmaService turmaService;
    private UsuarioService usuarioService;
    private DisciplinaService disciplinaService;
    private Turma turma;

    private final LocalDate dia1 = LocalDate.of(2026, 6, 1);
    private final LocalDate dia2 = LocalDate.of(2026, 6, 8);
    private final LocalDate dia3 = LocalDate.of(2026, 6, 15);
    private final LocalDate dia4 = LocalDate.of(2026, 6, 22);
    private final LocalDate dia5 = LocalDate.of(2026, 6, 29);

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
        var periodoLetivoService = new PeriodoLetivoService(new PeriodoLetivoRepositorioFake());
        turmaService = new TurmaService(new TurmaRepositorioFake(), usuarioService, disciplinaService);
        frequenciaService = new FrequenciaService(new FrequenciaRepositorioFake(), usuarioService, turmaService);

        usuarioService.cadastrar("PROFESSOR", "Prof. Ana", "P001", "ana@example.com", "1234");
        usuarioService.cadastrar("ALUNO", "Aluno 1", "A001", "aluno1@example.com", "1234");
        usuarioService.cadastrar("ALUNO", "Aluno 2", "A002", "aluno2@example.com", "1234");
        usuarioService.cadastrar("ALUNO", "Aluno 3 (nao matriculado)", "A003", "aluno3@example.com", "1234");

        disciplinaService.cadastrar("CC101", "Programacao I", 60, 4);
        periodoLetivoService.cadastrar("2026.1");
        PeriodoLetivo periodo = periodoLetivoService.buscarPorIdentificador("2026.1");

        turma = turmaService.ofertarTurma("CC101", "P001", periodo, 30, "08:00-10:00", "Sala A1");
        turmaService.solicitarMatricula(turma.getCodigo(), "A001");
        turmaService.solicitarMatricula(turma.getCodigo(), "A002");
    }

    // ==================== Cálculo do percentual ====================

    @Test
    void deveCalcular100PorCentoQuandoTodasPresencas() {
        frequenciaService.registrarPresenca(turma.getCodigo(), "A001", dia1);
        frequenciaService.registrarPresenca(turma.getCodigo(), "A001", dia2);
        frequenciaService.registrarPresenca(turma.getCodigo(), "A001", dia3);

        double percentual = frequenciaService.calcularPercentualFrequencia(turma.getCodigo(), "A001");

        assertEquals(100.0, percentual, 0.01);
    }

    @Test
    void deveCalcular0PorCentoQuandoTodasFaltas() {
        frequenciaService.registrarFalta(turma.getCodigo(), "A001", dia1);
        frequenciaService.registrarFalta(turma.getCodigo(), "A001", dia2);

        double percentual = frequenciaService.calcularPercentualFrequencia(turma.getCodigo(), "A001");

        assertEquals(0.0, percentual, 0.01);
    }

    @Test
    void deveCalcularPercentualParcial() {
        // 3 presenças + 2 faltas = 60%
        frequenciaService.registrarPresenca(turma.getCodigo(), "A001", dia1);
        frequenciaService.registrarPresenca(turma.getCodigo(), "A001", dia2);
        frequenciaService.registrarPresenca(turma.getCodigo(), "A001", dia3);
        frequenciaService.registrarFalta(turma.getCodigo(), "A001", dia4);
        frequenciaService.registrarFalta(turma.getCodigo(), "A001", dia5);

        double percentual = frequenciaService.calcularPercentualFrequencia(turma.getCodigo(), "A001");

        assertEquals(60.0, percentual, 0.01);
    }

    @Test
    void deveCalcularPercentualComUmUnicoRegistroPresenca() {
        frequenciaService.registrarPresenca(turma.getCodigo(), "A001", dia1);

        double percentual = frequenciaService.calcularPercentualFrequencia(turma.getCodigo(), "A001");

        assertEquals(100.0, percentual, 0.01);
    }

    @Test
    void deveCalcularPercentualComUmUnicoRegistroFalta() {
        frequenciaService.registrarFalta(turma.getCodigo(), "A001", dia1);

        double percentual = frequenciaService.calcularPercentualFrequencia(turma.getCodigo(), "A001");

        assertEquals(0.0, percentual, 0.01);
    }

    @Test
    void deveCalcularPercentualIndependentePorAluno() {
        // A001: 2 presenças, 1 falta = 66.67%
        frequenciaService.registrarPresenca(turma.getCodigo(), "A001", dia1);
        frequenciaService.registrarPresenca(turma.getCodigo(), "A001", dia2);
        frequenciaService.registrarFalta(turma.getCodigo(), "A001", dia3);

        // A002: 1 presença, 2 faltas = 33.33%
        frequenciaService.registrarPresenca(turma.getCodigo(), "A002", dia1);
        frequenciaService.registrarFalta(turma.getCodigo(), "A002", dia2);
        frequenciaService.registrarFalta(turma.getCodigo(), "A002", dia3);

        double percentualA001 = frequenciaService.calcularPercentualFrequencia(turma.getCodigo(), "A001");
        double percentualA002 = frequenciaService.calcularPercentualFrequencia(turma.getCodigo(), "A002");

        assertEquals(66.67, percentualA001, 0.01);
        assertEquals(33.33, percentualA002, 0.01);
    }

    // ==================== Validações ====================

    @Test
    void deveRejeitarTurmaInexistente() {
        assertThrows(IllegalArgumentException.class,
                () -> frequenciaService.calcularPercentualFrequencia("TURMA-INEXISTENTE", "A001"));
    }

    @Test
    void deveRejeitarAlunoInexistente() {
        assertThrows(IllegalArgumentException.class,
                () -> frequenciaService.calcularPercentualFrequencia(turma.getCodigo(), "A999"));
    }

    @Test
    void deveRejeitarAlunoNaoMatriculado() {
        assertThrows(IllegalArgumentException.class,
                () -> frequenciaService.calcularPercentualFrequencia(turma.getCodigo(), "A003"));
    }

    @Test
    void deveRejeitarQuandoNaoHaRegistros() {
        // A001 está matriculado mas não tem nenhum registro de frequência.
        assertThrows(IllegalArgumentException.class,
                () -> frequenciaService.calcularPercentualFrequencia(turma.getCodigo(), "A001"));
    }

    @Test
    void deveRejeitarCodigoTurmaVazio() {
        assertThrows(IllegalArgumentException.class,
                () -> frequenciaService.calcularPercentualFrequencia("  ", "A001"));
    }

    @Test
    void deveRejeitarMatriculaVazia() {
        assertThrows(IllegalArgumentException.class,
                () -> frequenciaService.calcularPercentualFrequencia(turma.getCodigo(), "  "));
    }

    // ==================== Resumo textual ====================

    @Test
    void deveGerarResumoFormatado() {
        frequenciaService.registrarPresenca(turma.getCodigo(), "A001", dia1);
        frequenciaService.registrarPresenca(turma.getCodigo(), "A001", dia2);
        frequenciaService.registrarFalta(turma.getCodigo(), "A001", dia3);

        String resumo = frequenciaService.gerarResumoFrequencia(turma.getCodigo(), "A001");

        assertTrue(resumo.contains("A001"));
        assertTrue(resumo.contains("2/3"));
        assertTrue(resumo.contains("66,7%") || resumo.contains("66.7%"));
    }

    @Test
    void deveGerarResumo100PorCento() {
        frequenciaService.registrarPresenca(turma.getCodigo(), "A001", dia1);
        frequenciaService.registrarPresenca(turma.getCodigo(), "A001", dia2);

        String resumo = frequenciaService.gerarResumoFrequencia(turma.getCodigo(), "A001");

        assertTrue(resumo.contains("2/2"));
        assertTrue(resumo.contains("100,0%") || resumo.contains("100.0%"));
    }
}