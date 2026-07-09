package classroompb.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import org.example.classroompb.model.Avaliacao;
import org.example.classroompb.model.Disciplina;
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
 * RF33 — o sistema define a situação final do aluno.
 *
 * <p>RN08 (frequência mínima 75%), RN09 (média &gt;= 7 aprova), RN10 (4 a 6,9 recuperação), RN11
 * (&lt; 4 reprova por nota), RN12 (falta prevalece sobre a nota).
 */
class RF33SituacaoFinalTest {

    private AvaliacaoService avaliacaoService;
    private TurmaService turmaService;
    private Turma turma;

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
        var periodoLetivoService = new PeriodoLetivoService(new PeriodoLetivoRepositorioFake());
        turmaService = new TurmaService(new TurmaRepositorioFake(), usuarioService, disciplinaService);
        avaliacaoService = new AvaliacaoService(new AvaliacaoRepositorioFake(), turmaService);

        usuarioService.cadastrar("PROFESSOR", "Prof. Ana", "P001", "ana@example.com", "1234");
        usuarioService.cadastrar("ALUNO", "Aluno 1", "A001", "aluno1@example.com", "1234");

        disciplinaService.cadastrar("CC101", "Programacao I", 60, 4);
        periodoLetivoService.cadastrar("2026.1");
        PeriodoLetivo periodo = periodoLetivoService.buscarPorIdentificador("2026.1");

        turma = turmaService.ofertarTurma("CC101", "P001", periodo, 5, "08:00-10:00", "Sala A1");
        turmaService.solicitarMatricula(turma.getCodigo(), "A001");
    }

    // ---- Via serviço (integra nota + frequência + persistência) ----

    @Test
    void deveAprovarComMediaAltaEFrequenciaOk() {
        avaliacaoService.lancarNota(turma.getCodigo(), "A001", 8.0);
        avaliacaoService.registrarFrequencia(turma.getCodigo(), "A001", 90.0);

        assertEquals(
                SituacaoFinal.APROVADO,
                avaliacaoService.definirSituacaoFinal(turma.getCodigo(), "A001"));
    }

    @Test
    void deveIrParaRecuperacaoComMediaIntermediaria() {
        avaliacaoService.lancarNota(turma.getCodigo(), "A001", 5.0);
        avaliacaoService.registrarFrequencia(turma.getCodigo(), "A001", 80.0);

        assertEquals(
                SituacaoFinal.RECUPERACAO,
                avaliacaoService.definirSituacaoFinal(turma.getCodigo(), "A001"));
    }

    @Test
    void deveReprovarPorNotaComMediaBaixa() {
        avaliacaoService.lancarNota(turma.getCodigo(), "A001", 2.0);
        avaliacaoService.registrarFrequencia(turma.getCodigo(), "A001", 95.0);

        assertEquals(
                SituacaoFinal.REPROVADO_POR_NOTA,
                avaliacaoService.definirSituacaoFinal(turma.getCodigo(), "A001"));
    }

    @Test
    void deveReprovarPorFaltaMesmoComMediaAlta() {
        // RN12: média 9,0 mas frequência 70% -> reprova por falta.
        avaliacaoService.lancarNota(turma.getCodigo(), "A001", 9.0);
        avaliacaoService.registrarFrequencia(turma.getCodigo(), "A001", 70.0);

        assertEquals(
                SituacaoFinal.REPROVADO_POR_FALTA,
                avaliacaoService.definirSituacaoFinal(turma.getCodigo(), "A001"));
    }

    @Test
    void devePersistirSituacaoNaAvaliacao() {
        avaliacaoService.lancarNota(turma.getCodigo(), "A001", 8.0);
        avaliacaoService.registrarFrequencia(turma.getCodigo(), "A001", 90.0);
        avaliacaoService.definirSituacaoFinal(turma.getCodigo(), "A001");

        Avaliacao avaliacao = avaliacaoService.buscarAvaliacao(turma.getCodigo(), "A001");
        assertEquals(SituacaoFinal.APROVADO, avaliacao.getSituacao());
    }

    @Test
    void deveRejeitarSituacaoSemNotas() {
        avaliacaoService.registrarFrequencia(turma.getCodigo(), "A001", 90.0);
        assertThrows(
                IllegalArgumentException.class,
                () -> avaliacaoService.definirSituacaoFinal(turma.getCodigo(), "A001"));
    }

    // ---- Regra pura de faixas (RN08-RN12), sem I/O ----

    @Test
    void faixaExata7AprovaEFaixa69Recupera() {
        assertEquals(SituacaoFinal.APROVADO, avaliacaoService.calcularSituacao(7.0, 75.0));
        assertEquals(SituacaoFinal.RECUPERACAO, avaliacaoService.calcularSituacao(6.9, 75.0));
    }

    @Test
    void faixaExata4RecuperaEFaixa39ReprovaPorNota() {
        assertEquals(SituacaoFinal.RECUPERACAO, avaliacaoService.calcularSituacao(4.0, 80.0));
        assertEquals(SituacaoFinal.REPROVADO_POR_NOTA, avaliacaoService.calcularSituacao(3.9, 80.0));
    }

    @Test
    void frequenciaExata75NaoReprovaPorFalta() {
        // RN08: 75% é o mínimo para aprovação, então 75% com média alta aprova.
        assertEquals(SituacaoFinal.APROVADO, avaliacaoService.calcularSituacao(8.0, 75.0));
        assertEquals(SituacaoFinal.REPROVADO_POR_FALTA, avaliacaoService.calcularSituacao(8.0, 74.9));
    }
}
