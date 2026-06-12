package classroompb.service;

import org.example.classroompb.model.Disciplina;
import org.example.classroompb.model.ItemListaEspera;
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

import static org.junit.jupiter.api.Assertions.*;

/**
 * RF26: O coordenador deve poder visualizar a lista de espera de cada turma,
 * com a posição, matrícula e nome dos alunos, respeitando a ordem (RF25).
 */
class RF26VisualizarListaEsperaTest {

    private TurmaService turmaService;
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

        var usuarioService = new UsuarioService(usuarioRepo);
        var disciplinaService = new DisciplinaService(disciplinaRepo);
        var periodoLetivoService = new PeriodoLetivoService(periodoRepo);
        turmaService = new TurmaService(turmaRepo, usuarioService, disciplinaService);

        usuarioService.cadastrar("PROFESSOR", "Prof. Ana", "P001", "ana@example.com", "1234");
        usuarioService.cadastrar("ALUNO", "Maria Silva", "A001", "maria@example.com", "1234");
        usuarioService.cadastrar("ALUNO", "Joao Souza", "A002", "joao@example.com", "1234");
        usuarioService.cadastrar("ALUNO", "Ana Lima", "A003", "ana2@example.com", "1234");

        disciplinaService.cadastrar("CC101", "Programacao I", 60, 4);

        periodoLetivoService.cadastrar("2026.1");
        periodo = periodoLetivoService.buscarPorIdentificador("2026.1");
    }

    @Test
    void deveVisualizarListaDeEsperaComPosicaoMatriculaENome() {
        Turma turma = turmaService.ofertarTurma("CC101", "P001", periodo, 1, "08:00-10:00", "Sala A1");
        turmaService.solicitarMatricula(turma.getCodigo(), "A001"); // matriculado
        turmaService.solicitarMatricula(turma.getCodigo(), "A002"); // fila 1
        turmaService.solicitarMatricula(turma.getCodigo(), "A003"); // fila 2

        List<ItemListaEspera> fila = turmaService.visualizarListaDeEspera(turma.getCodigo());

        assertEquals(2, fila.size());

        assertEquals(1, fila.get(0).posicao());
        assertEquals("A002", fila.get(0).matriculaAluno());
        assertEquals("Joao Souza", fila.get(0).nomeAluno());

        assertEquals(2, fila.get(1).posicao());
        assertEquals("A003", fila.get(1).matriculaAluno());
        assertEquals("Ana Lima", fila.get(1).nomeAluno());
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHaEspera() {
        Turma turma = turmaService.ofertarTurma("CC101", "P001", periodo, 5, "08:00-10:00", "Sala A1");
        turmaService.solicitarMatricula(turma.getCodigo(), "A001");

        List<ItemListaEspera> fila = turmaService.visualizarListaDeEspera(turma.getCodigo());
        assertTrue(fila.isEmpty());
    }

    @Test
    void deveLancarErroAoVisualizarTurmaInexistente() {
        assertThrows(IllegalArgumentException.class, () ->
                turmaService.visualizarListaDeEspera("INEXISTENTE"));
    }

    @Test
    void deveListarApenasTurmasComListaDeEspera() {
        // Turma 1: cheia, com fila
        Turma turma1 = turmaService.ofertarTurma("CC101", "P001", periodo, 1, "08:00-10:00", "Sala A1");
        turmaService.solicitarMatricula(turma1.getCodigo(), "A001");
        turmaService.solicitarMatricula(turma1.getCodigo(), "A002");

        // Turma 2: com vaga, sem fila
        Turma turma2 = turmaService.ofertarTurma("CC101", "P001", periodo, 5, "10:00-12:00", "Sala A2");
        turmaService.solicitarMatricula(turma2.getCodigo(), "A003");

        List<Turma> comEspera = turmaService.listarTurmasComListaDeEspera();

        assertEquals(1, comEspera.size());
        assertEquals(turma1.getCodigo(), comEspera.get(0).getCodigo());
    }

    @Test
    void visualizacaoDeveRefletirOrdemAposPromocaoAutomatica() {
        Turma turma = turmaService.ofertarTurma("CC101", "P001", periodo, 1, "08:00-10:00", "Sala A1");
        turmaService.solicitarMatricula(turma.getCodigo(), "A001");
        turmaService.solicitarMatricula(turma.getCodigo(), "A002");
        turmaService.solicitarMatricula(turma.getCodigo(), "A003");

        // Libera vaga -> A002 promovido; fila deve restar só A003 na posição 1
        turmaService.cancelarMatricula(turma.getCodigo(), "A001");

        List<ItemListaEspera> fila = turmaService.visualizarListaDeEspera(turma.getCodigo());
        assertEquals(1, fila.size());
        assertEquals(1, fila.get(0).posicao());
        assertEquals("A003", fila.get(0).matriculaAluno());
    }
}