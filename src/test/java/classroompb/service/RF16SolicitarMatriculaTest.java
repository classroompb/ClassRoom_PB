package classroompb.service;

import org.example.classroompb.model.Disciplina;
import org.example.classroompb.model.PeriodoLetivo;
import org.example.classroompb.model.Turma;
import org.example.classroompb.model.Usuario;
import org.example.classroompb.repository.DisciplinaRepository;
import org.example.classroompb.repository.PeriodoLetivoRepository;
import org.example.classroompb.repository.TurmaRepository;
import org.example.classroompb.repository.UsuarioRepository;
import org.example.classroompb.service.DisciplinaService;
import org.example.classroompb.service.PeriodoLetivoService;
import org.example.classroompb.service.TurmaService;
import org.example.classroompb.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// ===== INICIO TESTES RF16: Solicitacao de matricula =====
class RF16SolicitarMatriculaTest {

    private TurmaService turmaService;
    private UsuarioService usuarioService;
    private DisciplinaService disciplinaService;
    private PeriodoLetivo periodo;

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

        usuarioService.cadastrar("PROFESSOR", "Prof. Ana", "P001", "ana@example.com", "1234");
        usuarioService.cadastrar("PROFESSOR", "Prof. Bia", "P002", "bia@example.com", "1234");
        usuarioService.cadastrar("ALUNO", "Aluno 1", "A001", "aluno1@example.com", "1234");

        disciplinaService.cadastrar("CC101", "Programacao I", 60, 4);
        disciplinaService.cadastrar("CC102", "Banco de Dados", 60, 4);

        periodoLetivoService.cadastrar("2026.1");
        periodo = periodoLetivoService.buscarPorIdentificador("2026.1");
    }

    @Test
    void deveSolicitarMatriculaEmTurma() {
        Turma turma = turmaService.ofertarTurma("CC101", "P001", periodo, 2, "08:00-10:00", "Sala A1");

        turmaService.solicitarMatricula(turma.getCodigo(), "A001");

        assertTrue(turma.alunoJaMatriculado("A001"));
        assertEquals(1, turma.getVagasDisponiveis());
    }

    @Test
    void deveRejeitarSolicitacaoDeAlunoInexistente() {
        Turma turma = turmaService.ofertarTurma("CC101", "P001", periodo, 2, "08:00-10:00", "Sala A1");

        assertThrows(IllegalArgumentException.class, () ->
                turmaService.solicitarMatricula(turma.getCodigo(), "A999"));
    }

    @Test
    void deveRejeitarSolicitacaoEmTurmaInexistente() {
        assertThrows(IllegalArgumentException.class, () ->
                turmaService.solicitarMatricula("TURMA-INEXISTENTE", "A001"));
    }

    @Test
    void deveRejeitarMatriculaDuplicadaNaMesmaTurma() {
        Turma turma = turmaService.ofertarTurma("CC101", "P001", periodo, 2, "08:00-10:00", "Sala A1");

        turmaService.solicitarMatricula(turma.getCodigo(), "A001");

        assertThrows(IllegalArgumentException.class, () ->
                turmaService.solicitarMatricula(turma.getCodigo(), "A001"));
    }

    @Test
    void deveRejeitarMatriculaComChoqueDeHorario() {
        Turma turma1 = turmaService.ofertarTurma("CC101", "P001", periodo, 2, "08:00-10:00", "Sala A1");
        Turma turma2 = turmaService.ofertarTurma("CC102", "P002", periodo, 2, "09:00-11:00", "Sala B1");

        turmaService.solicitarMatricula(turma1.getCodigo(), "A001");

        assertThrows(IllegalArgumentException.class, () ->
                turmaService.solicitarMatricula(turma2.getCodigo(), "A001"));
    }
}
