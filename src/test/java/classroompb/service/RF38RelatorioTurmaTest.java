package classroompb.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.example.classroompb.model.Avaliacao;
import org.example.classroompb.model.Disciplina;
import org.example.classroompb.model.ItemRelatorioTurma;
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
 * RF38 — Professor deve emitir relatório da turma contendo alunos matriculados, notas, média,
 * frequência e situação final.
 */
class RF38RelatorioTurmaTest {

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
    }

    private Turma ofertarEMatricular(
            String codigoDisciplina, String identificadorPeriodo, String horario) {

        PeriodoLetivo periodo = periodoLetivoService.buscarPorIdentificador(identificadorPeriodo);

        Turma turma =
                turmaService.ofertarTurma(codigoDisciplina, "P001", periodo, 5, horario, "Sala A1");

        turmaService.solicitarMatricula(turma.getCodigo(), "A001");

        return turma;
    }

    @Test
    void deveRetornarRelatorioVazioQuandoTurmaNaoTemAvaliacoes() {

        Turma turma = ofertarEMatricular("CC101", "2025.2", "08:00-10:00");

        List<ItemRelatorioTurma> relatorio =
                avaliacaoService.emitirRelatorioTurma(turma.getCodigo());

        assertTrue(relatorio.isEmpty());
    }

    @Test
    void deveEmitirRelatorioComNotasMediaFrequenciaESituacao() {
        Turma turma = ofertarEMatricular("CC101", "2025.2", "08:00-10:00");

        avaliacaoService.lancarNota(turma.getCodigo(), "A001", 8.0);

        avaliacaoService.lancarNota(turma.getCodigo(), "A001", 6.0);

        avaliacaoService.registrarFrequencia(turma.getCodigo(), "A001", 90.0);

        avaliacaoService.definirSituacaoFinal(turma.getCodigo(), "A001");

        List<ItemRelatorioTurma> relatorio =
                avaliacaoService.emitirRelatorioTurma(turma.getCodigo());

        assertEquals(1, relatorio.size());

        ItemRelatorioTurma item = relatorio.get(0);

        assertEquals("A001", item.matriculaAluno());
        assertEquals(List.of(8.0, 6.0), item.notas());
        assertEquals(7.0, item.media(), 0.0001);
        assertEquals(90.0, item.frequencia(), 0.0001);
        assertEquals(SituacaoFinal.APROVADO, item.situacao());
    }

    @Test
    void deveRetornarSomenteAlunosDaTurmaInformada() {
        usuarioService.cadastrar("ALUNO", "Aluno 2", "A002", "aluno2@example.com", "1234");

        Turma turma1 = ofertarEMatricular("CC101", "2025.2", "08:00-10:00");

        Turma turma2 =
                turmaService.ofertarTurma(
                        "CC102",
                        "P001",
                        periodoLetivoService.buscarPorIdentificador("2025.2"),
                        5,
                        "10:00-12:00",
                        "Sala A2");

        turmaService.solicitarMatricula(turma2.getCodigo(), "A002");

        avaliacaoService.lancarNota(turma1.getCodigo(), "A001", 8.0);

        avaliacaoService.registrarFrequencia(turma1.getCodigo(), "A001", 90.0);

        avaliacaoService.definirSituacaoFinal(turma1.getCodigo(), "A001");

        avaliacaoService.lancarNota(turma2.getCodigo(), "A002", 10.0);

        avaliacaoService.registrarFrequencia(turma2.getCodigo(), "A002", 100.0);

        avaliacaoService.definirSituacaoFinal(turma2.getCodigo(), "A002");

        List<ItemRelatorioTurma> relatorio =
                avaliacaoService.emitirRelatorioTurma(turma1.getCodigo());

        assertEquals(1, relatorio.size());
        assertEquals("A001", relatorio.get(0).matriculaAluno());
    }

    @Test
    void deveRejeitarCodigoTurmaVazio() {
        assertThrows(
                IllegalArgumentException.class, () -> avaliacaoService.emitirRelatorioTurma(" "));
    }
}
