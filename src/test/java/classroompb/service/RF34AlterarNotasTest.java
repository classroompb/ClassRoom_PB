package classroompb.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import org.example.classroompb.model.Avaliacao;
import org.example.classroompb.model.Disciplina;
import org.example.classroompb.model.PeriodoLetivo;
import org.example.classroompb.model.Turma;
import org.example.classroompb.model.Usuario;
import org.example.classroompb.repository.AvaliacaoRepository;
import org.example.classroompb.repository.DisciplinaRepository;
import org.example.classroompb.repository.PeriodoLetivoRepository;
import org.example.classroompb.repository.TurmaRepository;
import org.example.classroompb.repository.UsuarioRepository;
import org.example.classroompb.service.AvaliacaoService;
import org.example.classroompb.service.DisciplinaService;
import org.example.classroompb.service.PeriodoLetivoService;
import org.example.classroompb.service.TurmaService;
import org.example.classroompb.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** RF34 — o professor edita uma nota enquanto a turma não estiver fechada. */
class RF34AlterarNotasTest {

    private AvaliacaoService avaliacaoService;
    private TurmaService turmaService;
    private PeriodoLetivoService periodoLetivoService;
    private Turma turma;
    private PeriodoLetivo periodo;

    static class AvaliacaoRepositorioFake extends AvaliacaoRepository {
        private final List<Avaliacao> lista = new ArrayList<>();

        @Override
        public List<Avaliacao> carregarTodos() {
            return new ArrayList<>(lista);
        }

        @Override
        public void salvarTodos(List<Avaliacao> avaliacoes) {
            lista.clear();
            lista.addAll(avaliacoes);
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
        var usuarioService = new UsuarioService(new UsuarioRepositorioFake());
        var disciplinaService = new DisciplinaService(new DisciplinaRepositorioFake());
        periodoLetivoService = new PeriodoLetivoService(new PeriodoLetivoRepositorioFake());
        turmaService =
                new TurmaService(new TurmaRepositorioFake(), usuarioService, disciplinaService);
        avaliacaoService = new AvaliacaoService(new AvaliacaoRepositorioFake(), turmaService);

        usuarioService.cadastrar("PROFESSOR", "Prof. Ana", "P001", "ana@example.com", "1234");
        usuarioService.cadastrar("ALUNO", "Aluno 1", "A001", "aluno1@example.com", "1234");

        disciplinaService.cadastrar("CC101", "Programacao I", 60, 4);
        periodoLetivoService.cadastrar("2026.1");
        periodo = periodoLetivoService.buscarPorIdentificador("2026.1");

        turma = turmaService.ofertarTurma("CC101", "P001", periodo, 5, "08:00-10:00", "Sala A1");
        turmaService.solicitarMatricula(turma.getCodigo(), "A001");
    }

    @Test
    void deveAlterarNotaComSucessoEmTurmaAberta() {
        avaliacaoService.lancarNota(turma.getCodigo(), "A001", 8.5);

        avaliacaoService.alterarNota(turma.getCodigo(), "A001", 0, 9.5);

        Avaliacao avaliacao = avaliacaoService.buscarAvaliacao(turma.getCodigo(), "A001");
        assertEquals(1, avaliacao.getNotas().size());
        assertEquals(9.5, avaliacao.getNotas().get(0), 0.001);
    }

    @Test
    void deveRejeitarAlteracaoEmTurmaEncerrada() {
        avaliacaoService.lancarNota(turma.getCodigo(), "A001", 8.5);

        // Ativa e encerra o período letivo correspondente à turma
        periodo.ativar();
        periodo.encerrar();

        assertThrows(
                IllegalStateException.class,
                () -> avaliacaoService.alterarNota(turma.getCodigo(), "A001", 0, 9.5));
    }

    @Test
    void deveGarantirQueNotaNaoFoiAlteradaAposTentativaFalha() {
        avaliacaoService.lancarNota(turma.getCodigo(), "A001", 8.5);

        // Ativa e encerra o período letivo correspondente à turma
        periodo.ativar();
        periodo.encerrar();

        assertThrows(
                IllegalStateException.class,
                () -> avaliacaoService.alterarNota(turma.getCodigo(), "A001", 0, 9.5));

        // Garante que o valor da nota antiga permaneceu inalterado no repositório/entidade
        Avaliacao avaliacao = avaliacaoService.buscarAvaliacao(turma.getCodigo(), "A001");
        assertEquals(8.5, avaliacao.getNotas().get(0), 0.001);
    }

    @Test
    void deveRejeitarAlteracaoEmAvaliacaoFechada() {
        avaliacaoService.lancarNota(turma.getCodigo(), "A001", 8.5);
        avaliacaoService.buscarAvaliacao(turma.getCodigo(), "A001").fechar();

        assertThrows(
                IllegalArgumentException.class,
                () -> avaliacaoService.alterarNota(turma.getCodigo(), "A001", 0, 9.5));
    }

    @Test
    void deveRejeitarAlteracaoComNotaInvalida() {
        avaliacaoService.lancarNota(turma.getCodigo(), "A001", 8.5);

        assertThrows(
                IllegalArgumentException.class,
                () -> avaliacaoService.alterarNota(turma.getCodigo(), "A001", 0, -1.0));

        assertThrows(
                IllegalArgumentException.class,
                () -> avaliacaoService.alterarNota(turma.getCodigo(), "A001", 0, 11.0));
    }

    @Test
    void deveRejeitarAlteracaoComIndiceInvalido() {
        avaliacaoService.lancarNota(turma.getCodigo(), "A001", 8.5);

        assertThrows(
                IllegalArgumentException.class,
                () -> avaliacaoService.alterarNota(turma.getCodigo(), "A001", 1, 9.5));

        assertThrows(
                IllegalArgumentException.class,
                () -> avaliacaoService.alterarNota(turma.getCodigo(), "A001", -1, 9.5));
    }
}
