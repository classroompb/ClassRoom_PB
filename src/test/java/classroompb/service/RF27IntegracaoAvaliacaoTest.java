package classroompb.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.example.classroompb.model.Avaliacao;
import org.example.classroompb.model.Disciplina;
import org.example.classroompb.model.PeriodoLetivo;
import org.example.classroompb.model.RegistroFrequencia;
import org.example.classroompb.model.SituacaoFinal;
import org.example.classroompb.model.Turma;
import org.example.classroompb.model.Usuario;
import org.example.classroompb.repository.AvaliacaoRepository;
import org.example.classroompb.repository.DisciplinaRepository;
import org.example.classroompb.repository.FrequenciaRepository;
import org.example.classroompb.repository.PeriodoLetivoRepository;
import org.example.classroompb.repository.TurmaRepository;
import org.example.classroompb.repository.UsuarioRepository;
import org.example.classroompb.service.AvaliacaoService;
import org.example.classroompb.service.DisciplinaService;
import org.example.classroompb.service.FrequenciaService;
import org.example.classroompb.service.PeriodoLetivoService;
import org.example.classroompb.service.TurmaService;
import org.example.classroompb.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Integração do RF27 (registrar presença/falta) com o RF33 (situação final).
 *
 * <p>Antes essas duas partes não se falavam: o RF27 gravava presença aula por aula e o
 * AvaliacaoService usava um percentual digitado à mão. Eram duas fontes de verdade para a mesma
 * frequência. Agora, quando o aluno tem registros de aula, é deles que sai o percentual que
 * alimenta a RN08 e a RN12.
 */
class RF27IntegracaoAvaliacaoTest {

    private AvaliacaoService avaliacaoService;
    private FrequenciaService frequenciaService;
    private TurmaService turmaService;
    private Turma turma;

    private static final LocalDate AULA_1 = LocalDate.of(2026, 3, 2);
    private static final LocalDate AULA_2 = LocalDate.of(2026, 3, 4);
    private static final LocalDate AULA_3 = LocalDate.of(2026, 3, 9);
    private static final LocalDate AULA_4 = LocalDate.of(2026, 3, 11);

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

    static class FrequenciaRepositorioFake extends FrequenciaRepository {
        private final List<RegistroFrequencia> lista = new ArrayList<>();

        @Override
        public List<RegistroFrequencia> carregarTodos() {
            return new ArrayList<>(lista);
        }

        @Override
        public void salvarTodos(List<RegistroFrequencia> registros) {
            lista.clear();
            lista.addAll(registros);
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
        turmaService =
                new TurmaService(new TurmaRepositorioFake(), usuarioService, disciplinaService);
        frequenciaService =
                new FrequenciaService(
                        new FrequenciaRepositorioFake(), usuarioService, turmaService);
        avaliacaoService =
                new AvaliacaoService(
                        new AvaliacaoRepositorioFake(), turmaService, frequenciaService);

        usuarioService.cadastrar("PROFESSOR", "Prof. Ana", "P001", "ana@example.com", "1234");
        usuarioService.cadastrar("ALUNO", "Aluno 1", "A001", "aluno1@example.com", "1234");

        disciplinaService.cadastrar("CC101", "Programacao I", 60, 4);
        periodoLetivoService.cadastrar("2026.1");
        PeriodoLetivo periodo = periodoLetivoService.buscarPorIdentificador("2026.1");

        turma = turmaService.ofertarTurma("CC101", "P001", periodo, 5, "08:00-10:00", "Sala A1");
        turmaService.solicitarMatricula(turma.getCodigo(), "A001");
    }

    // ---- Cálculo do percentual a partir dos registros ----

    @Test
    void percentualSaiDosRegistrosDeAula() {
        frequenciaService.registrarPresenca(turma.getCodigo(), "A001", AULA_1);
        frequenciaService.registrarPresenca(turma.getCodigo(), "A001", AULA_2);
        frequenciaService.registrarPresenca(turma.getCodigo(), "A001", AULA_3);
        frequenciaService.registrarFalta(turma.getCodigo(), "A001", AULA_4);

        // 3 presenças em 4 aulas = 75%
        assertEquals(
                75.0, frequenciaService.calcularPercentualFrequencia(turma.getCodigo(), "A001"));
    }

    @Test
    void semRegistroNaoDaParaAfirmarFrequencia() {
        assertFalse(frequenciaService.temRegistros(turma.getCodigo(), "A001"));
        assertThrows(
                IllegalArgumentException.class,
                () -> frequenciaService.calcularPercentualFrequencia(turma.getCodigo(), "A001"));
    }

    @Test
    void temRegistrosViraVerdadeDepoisDoPrimeiroLancamento() {
        assertFalse(frequenciaService.temRegistros(turma.getCodigo(), "A001"));
        frequenciaService.registrarPresenca(turma.getCodigo(), "A001", AULA_1);
        assertTrue(frequenciaService.temRegistros(turma.getCodigo(), "A001"));
    }

    // ---- A situação final passa a usar a frequência real ----

    @Test
    void registrosDeAulaPrevalecemSobreOPercentualDigitadoAMao() {
        avaliacaoService.lancarNota(turma.getCodigo(), "A001", 8.0);
        // Professor digitou 90% na mão, mas os registros dizem outra coisa.
        avaliacaoService.registrarFrequencia(turma.getCodigo(), "A001", 90.0);

        frequenciaService.registrarFalta(turma.getCodigo(), "A001", AULA_1);
        frequenciaService.registrarFalta(turma.getCodigo(), "A001", AULA_2);
        frequenciaService.registrarPresenca(turma.getCodigo(), "A001", AULA_3);
        frequenciaService.registrarPresenca(turma.getCodigo(), "A001", AULA_4);

        // 2 de 4 = 50%, abaixo dos 75% da RN08.
        SituacaoFinal situacao = avaliacaoService.definirSituacaoFinal(turma.getCodigo(), "A001");

        assertEquals(SituacaoFinal.REPROVADO_POR_FALTA, situacao);
        // O campo da avaliação foi corrigido para não continuar mentindo 90%.
        assertEquals(
                50.0, avaliacaoService.buscarAvaliacao(turma.getCodigo(), "A001").getFrequencia());
    }

    @Test
    void rn12ContinuaValendoQuandoAFrequenciaVemDosRegistros() {
        // Média 9, ou seja, aprovaria com folga se a falta não prevalecesse.
        avaliacaoService.lancarNota(turma.getCodigo(), "A001", 9.0);
        avaliacaoService.lancarNota(turma.getCodigo(), "A001", 9.0);

        frequenciaService.registrarPresenca(turma.getCodigo(), "A001", AULA_1);
        frequenciaService.registrarFalta(turma.getCodigo(), "A001", AULA_2);
        frequenciaService.registrarFalta(turma.getCodigo(), "A001", AULA_3);
        frequenciaService.registrarFalta(turma.getCodigo(), "A001", AULA_4);

        // 1 de 4 = 25%.
        assertEquals(
                SituacaoFinal.REPROVADO_POR_FALTA,
                avaliacaoService.definirSituacaoFinal(turma.getCodigo(), "A001"));
        assertEquals(9.0, avaliacaoService.calcularMedia(turma.getCodigo(), "A001"));
    }

    @Test
    void aprovaQuandoOsRegistrosMostramFrequenciaSuficiente() {
        avaliacaoService.lancarNota(turma.getCodigo(), "A001", 8.0);

        frequenciaService.registrarPresenca(turma.getCodigo(), "A001", AULA_1);
        frequenciaService.registrarPresenca(turma.getCodigo(), "A001", AULA_2);
        frequenciaService.registrarPresenca(turma.getCodigo(), "A001", AULA_3);
        frequenciaService.registrarFalta(turma.getCodigo(), "A001", AULA_4);

        // 75% exatos: a RN08 pede "mínimo 75%", então não reprova.
        assertEquals(
                SituacaoFinal.APROVADO,
                avaliacaoService.definirSituacaoFinal(turma.getCodigo(), "A001"));
    }

    // ---- Sem registros, o comportamento antigo continua ----

    @Test
    void semRegistrosDeAulaValeOPercentualInformadoAMao() {
        avaliacaoService.lancarNota(turma.getCodigo(), "A001", 8.0);
        avaliacaoService.registrarFrequencia(turma.getCodigo(), "A001", 90.0);

        assertEquals(
                SituacaoFinal.APROVADO,
                avaliacaoService.definirSituacaoFinal(turma.getCodigo(), "A001"));
        assertEquals(
                90.0, avaliacaoService.buscarAvaliacao(turma.getCodigo(), "A001").getFrequencia());
    }

    @Test
    void servicoMontadoSemFrequenciaServiceUsaOPercentualManual() {
        // Construtor antigo (dois argumentos): é o que os testes das outras RFs usam.
        var semFrequencia = new AvaliacaoService(new AvaliacaoRepositorioFake(), turmaService);
        semFrequencia.lancarNota(turma.getCodigo(), "A001", 9.0);
        semFrequencia.registrarFrequencia(turma.getCodigo(), "A001", 80.0);

        // Mesmo havendo registros ruins no outro serviço, este ignora e usa os 80%.
        frequenciaService.registrarFalta(turma.getCodigo(), "A001", AULA_1);

        assertEquals(
                SituacaoFinal.APROVADO,
                semFrequencia.definirSituacaoFinal(turma.getCodigo(), "A001"));
    }
}
