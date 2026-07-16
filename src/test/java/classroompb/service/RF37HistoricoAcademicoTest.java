package classroompb.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.example.classroompb.model.Avaliacao;
import org.example.classroompb.model.Disciplina;
import org.example.classroompb.model.ItemHistoricoAcademico;
import org.example.classroompb.model.PeriodoLetivo;
import org.example.classroompb.model.SituacaoFinal;
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

/**
 * RF37 — o aluno deve poder consultar seu histórico acadêmico: disciplinas cursadas, notas, média
 * e situação por período. Depende do model {@code ItemHistoricoAcademico} (task D1).
 */
class RF37HistoricoAcademicoTest {

    private AvaliacaoService avaliacaoService;
    private TurmaService turmaService;
    private PeriodoLetivoService periodoLetivoService;
    private DisciplinaService disciplinaService;
    private UsuarioService usuarioService;

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
        usuarioService = new UsuarioService(new UsuarioRepositorioFake());
        disciplinaService = new DisciplinaService(new DisciplinaRepositorioFake());
        periodoLetivoService = new PeriodoLetivoService(new PeriodoLetivoRepositorioFake());
        turmaService =
                new TurmaService(new TurmaRepositorioFake(), usuarioService, disciplinaService);
        avaliacaoService = new AvaliacaoService(new AvaliacaoRepositorioFake(), turmaService);

        usuarioService.cadastrar("PROFESSOR", "Prof. Ana", "P001", "ana@example.com", "1234");
        usuarioService.cadastrar("ALUNO", "Aluno 1", "A001", "aluno1@example.com", "1234");

        disciplinaService.cadastrar("CC101", "Programacao I", 60, 4);
        disciplinaService.cadastrar("CC102", "Estrutura de Dados", 60, 4);

        periodoLetivoService.cadastrar("2025.2");
        periodoLetivoService.cadastrar("2026.1");
    }

    private Turma ofertarEMatricular(
            String codigoDisciplina, String identificadorPeriodo, String horario) {
        PeriodoLetivo periodo = periodoLetivoService.buscarPorIdentificador(identificadorPeriodo);
        Turma turma =
                turmaService.ofertarTurma(
                        codigoDisciplina, "P001", periodo, 5, horario, "Sala A1");
        turmaService.solicitarMatricula(turma.getCodigo(), "A001");
        return turma;
    }

    @Test
    void deveRetornarHistoricoVazioQuandoAlunoNaoTemAvaliacoes() {
        List<ItemHistoricoAcademico> historico =
                avaliacaoService.consultarHistoricoAcademico("A001");
        assertTrue(historico.isEmpty());
    }

    @Test
    void deveListarDisciplinaCursadaComNotasMediaESituacao() {
        Turma turma = ofertarEMatricular("CC101", "2025.2", "08:00-10:00");
        avaliacaoService.lancarNota(turma.getCodigo(), "A001", 8.0);
        avaliacaoService.lancarNota(turma.getCodigo(), "A001", 6.0);
        avaliacaoService.registrarFrequencia(turma.getCodigo(), "A001", 90.0);
        avaliacaoService.definirSituacaoFinal(turma.getCodigo(), "A001");

        List<ItemHistoricoAcademico> historico =
                avaliacaoService.consultarHistoricoAcademico("A001");

        assertEquals(1, historico.size());
        ItemHistoricoAcademico item = historico.get(0);
        assertEquals("2025.2", item.periodoLetivo());
        assertEquals("CC101", item.codigoDisciplina());
        assertEquals(List.of(8.0, 6.0), item.notas());
        assertEquals(7.0, item.media(), 0.0001);
        assertEquals(90.0, item.frequencia(), 0.0001);
        assertEquals(SituacaoFinal.APROVADO, item.situacao());
    }

    @Test
    void deveAgruparEOrdenarPorPeriodoLetivo() {
        Turma turmaAntiga = ofertarEMatricular("CC101", "2025.2", "08:00-10:00");
        avaliacaoService.lancarNota(turmaAntiga.getCodigo(), "A001", 9.0);
        avaliacaoService.registrarFrequencia(turmaAntiga.getCodigo(), "A001", 95.0);
        avaliacaoService.definirSituacaoFinal(turmaAntiga.getCodigo(), "A001");

        Turma turmaAtual = ofertarEMatricular("CC102", "2026.1", "10:00-12:00");
        avaliacaoService.lancarNota(turmaAtual.getCodigo(), "A001", 3.0);
        avaliacaoService.registrarFrequencia(turmaAtual.getCodigo(), "A001", 80.0);
        avaliacaoService.definirSituacaoFinal(turmaAtual.getCodigo(), "A001");

        List<ItemHistoricoAcademico> historico =
                avaliacaoService.consultarHistoricoAcademico("A001");

        assertEquals(2, historico.size());
        assertEquals("2025.2", historico.get(0).periodoLetivo());
        assertEquals(SituacaoFinal.APROVADO, historico.get(0).situacao());
        assertEquals("2026.1", historico.get(1).periodoLetivo());
        assertEquals(SituacaoFinal.REPROVADO_POR_NOTA, historico.get(1).situacao());
    }

    @Test
    void naoDeveMostrarDisciplinaDeOutroAluno() {
        usuarioService.cadastrar("ALUNO", "Aluno 2", "A002", "aluno2@example.com", "1234");
        Turma turma = ofertarEMatricular("CC101", "2025.2", "08:00-10:00");
        turmaService.solicitarMatricula(turma.getCodigo(), "A002");

        avaliacaoService.lancarNota(turma.getCodigo(), "A002", 10.0);
        avaliacaoService.registrarFrequencia(turma.getCodigo(), "A002", 100.0);
        avaliacaoService.definirSituacaoFinal(turma.getCodigo(), "A002");

        assertTrue(avaliacaoService.consultarHistoricoAcademico("A001").isEmpty());
        assertEquals(1, avaliacaoService.consultarHistoricoAcademico("A002").size());
    }

    @Test
    void deveRejeitarMatriculaVazia() {
        assertThrows(
                IllegalArgumentException.class,
                () -> avaliacaoService.consultarHistoricoAcademico(" "));
    }
}