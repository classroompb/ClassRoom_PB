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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// ===== INICIO TESTES RF17: Verificacao de vagas disponiveis =====
class RF17VerificarVagasDisponiveisTest {

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
        usuarioService.cadastrar("ALUNO", "Aluno 1", "A001", "aluno1@example.com", "1234");

        disciplinaService.cadastrar("CC101", "Programacao I", 60, 4);

        periodoLetivoService.cadastrar("2026.1");
        periodo = periodoLetivoService.buscarPorIdentificador("2026.1");
    }

    @Test
    void deveVerificarQuandoHaVagasDisponiveis() {
        Turma turma = turmaService.ofertarTurma("CC101", "P001", periodo, 1, "08:00-10:00", "Sala A1");

        assertTrue(turmaService.verificarVagasDisponiveis(turma.getCodigo()));
        assertEquals(1, turmaService.consultarQuantidadeVagasDisponiveis(turma.getCodigo()));
    }

    @Test
    void deveVerificarQuandoNaoHaVagasDisponiveis() {
        Turma turma = turmaService.ofertarTurma("CC101", "P001", periodo, 1, "08:00-10:00", "Sala A1");

        turmaService.solicitarMatricula(turma.getCodigo(), "A001");

        assertFalse(turmaService.verificarVagasDisponiveis(turma.getCodigo()));
        assertEquals(0, turmaService.consultarQuantidadeVagasDisponiveis(turma.getCodigo()));
    }

    @Test
    void deveRejeitarVerificacaoDeTurmaInexistente() {
        assertThrows(IllegalArgumentException.class, () ->
                turmaService.verificarVagasDisponiveis("TURMA-INEXISTENTE"));
    }

    @Test
    void deveColocarAlunoEmListaDeEsperaQuandoTurmaNaoTemVagas() {
        usuarioService.cadastrar("ALUNO", "Aluno 2", "A002", "aluno2@example.com", "1234");
        Turma turma = turmaService.ofertarTurma("CC101", "P001", periodo, 1, "08:00-10:00", "Sala A1");

        turmaService.solicitarMatricula(turma.getCodigo(), "A001"); // ocupa a unica vaga

        // RF21: sem vagas, o aluno entra na lista de espera (nao lanca excecao)
        turmaService.solicitarMatricula(turma.getCodigo(), "A002");

        assertFalse(turmaService.verificarVagasDisponiveis(turma.getCodigo()));
        assertEquals(1, turmaService.obterPosicaoEmEspera(turma.getCodigo(), "A002"));
    }
}
