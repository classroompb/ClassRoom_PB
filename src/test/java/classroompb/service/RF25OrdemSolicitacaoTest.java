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

import static org.junit.jupiter.api.Assertions.*;

/**
 * RF25: A lista de espera deve respeitar a ordem de solicitação (FIFO).
 *
 * Verifica que a ordem é preservada na inserção, após remoções manuais e após
 * promoções automáticas (RF24).
 */
class RF25OrdemSolicitacaoTest {

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
        usuarioService.cadastrar("ALUNO", "Aluno 1", "A001", "aluno1@example.com", "1234");
        usuarioService.cadastrar("ALUNO", "Aluno 2", "A002", "aluno2@example.com", "1234");
        usuarioService.cadastrar("ALUNO", "Aluno 3", "A003", "aluno3@example.com", "1234");
        usuarioService.cadastrar("ALUNO", "Aluno 4", "A004", "aluno4@example.com", "1234");
        usuarioService.cadastrar("ALUNO", "Aluno 5", "A005", "aluno5@example.com", "1234");

        disciplinaService.cadastrar("CC101", "Programacao I", 60, 4);

        periodoLetivoService.cadastrar("2026.1");
        periodo = periodoLetivoService.buscarPorIdentificador("2026.1");
    }

    @Test
    void deveManterOrdemDeSolicitacaoNaInsercao() {
        Turma turma = turmaService.ofertarTurma("CC101", "P001", periodo, 1, "08:00-10:00", "Sala A1");
        turmaService.solicitarMatricula(turma.getCodigo(), "A001"); // matriculado

        turmaService.solicitarMatricula(turma.getCodigo(), "A003");
        turmaService.solicitarMatricula(turma.getCodigo(), "A002");
        turmaService.solicitarMatricula(turma.getCodigo(), "A004");

        List<String> fila = turmaService.listarListaDeEspera(turma.getCodigo());
        assertEquals(List.of("A003", "A002", "A004"), fila);
    }

    @Test
    void deveChamarSempreOPrimeiroDaOrdem() {
        Turma turma = turmaService.ofertarTurma("CC101", "P001", periodo, 1, "08:00-10:00", "Sala A1");
        turmaService.solicitarMatricula(turma.getCodigo(), "A001");
        turmaService.solicitarMatricula(turma.getCodigo(), "A002"); // 1º da fila
        turmaService.solicitarMatricula(turma.getCodigo(), "A003"); // 2º da fila
        turmaService.solicitarMatricula(turma.getCodigo(), "A004"); // 3º da fila

        // Libera vaga: deve chamar A002 (primeiro solicitante)
        turmaService.cancelarMatricula(turma.getCodigo(), "A001");

        Turma t = turmaService.obterTurma(turma.getCodigo());
        assertTrue(t.alunoJaMatriculado("A002"));
        assertEquals(List.of("A003", "A004"), turmaService.listarListaDeEspera(turma.getCodigo()));
    }

    @Test
    void deveManterOrdemAposRemocaoNoMeioDaFila() {
        Turma turma = turmaService.ofertarTurma("CC101", "P001", periodo, 1, "08:00-10:00", "Sala A1");
        turmaService.solicitarMatricula(turma.getCodigo(), "A001");
        turmaService.solicitarMatricula(turma.getCodigo(), "A002");
        turmaService.solicitarMatricula(turma.getCodigo(), "A003");
        turmaService.solicitarMatricula(turma.getCodigo(), "A004");

        // A003 desiste (posição 2 da fila)
        turmaService.removerDaEspera(turma.getCodigo(), "A003");

        assertEquals(List.of("A002", "A004"), turmaService.listarListaDeEspera(turma.getCodigo()));

        // Próxima chamada deve respeitar a nova ordem: A002 primeiro
        turmaService.cancelarMatricula(turma.getCodigo(), "A001");
        Turma t = turmaService.obterTurma(turma.getCodigo());
        assertTrue(t.alunoJaMatriculado("A002"));
        assertEquals(List.of("A004"), turmaService.listarListaDeEspera(turma.getCodigo()));
    }

    @Test
    void posicaoNaFilaDeveRefletirOrdemDeSolicitacao() {
        Turma turma = turmaService.ofertarTurma("CC101", "P001", periodo, 1, "08:00-10:00", "Sala A1");
        turmaService.solicitarMatricula(turma.getCodigo(), "A001");
        turmaService.solicitarMatricula(turma.getCodigo(), "A002");
        turmaService.solicitarMatricula(turma.getCodigo(), "A003");
        turmaService.solicitarMatricula(turma.getCodigo(), "A004");

        assertEquals(1, turmaService.obterPosicaoEmEspera(turma.getCodigo(), "A002"));
        assertEquals(2, turmaService.obterPosicaoEmEspera(turma.getCodigo(), "A003"));
        assertEquals(3, turmaService.obterPosicaoEmEspera(turma.getCodigo(), "A004"));
    }

    @Test
    void listaRetornadaDeveSerCopiaImutavelDoEstadoInterno() {
        Turma turma = turmaService.ofertarTurma("CC101", "P001", periodo, 1, "08:00-10:00", "Sala A1");
        turmaService.solicitarMatricula(turma.getCodigo(), "A001");
        turmaService.solicitarMatricula(turma.getCodigo(), "A002");

        List<String> fila = turmaService.listarListaDeEspera(turma.getCodigo());
        fila.clear(); // alterar a cópia não deve afetar a turma

        assertEquals(1, turmaService.consultarQuantidadeEmEspera(turma.getCodigo()));
    }
}