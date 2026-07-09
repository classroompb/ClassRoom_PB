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

/** RF32 — o sistema calcula automaticamente a média final do aluno. */
class RF32CalculoMediaTest {

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

    @Test
    void deveCalcularMediaDeUmaNota() {
        avaliacaoService.lancarNota(turma.getCodigo(), "A001", 8.0);
        assertEquals(8.0, avaliacaoService.calcularMedia(turma.getCodigo(), "A001"), 0.001);
    }

    @Test
    void deveCalcularMediaAritmeticaDeVariasNotas() {
        avaliacaoService.lancarNota(turma.getCodigo(), "A001", 6.0);
        avaliacaoService.lancarNota(turma.getCodigo(), "A001", 9.0);
        avaliacaoService.lancarNota(turma.getCodigo(), "A001", 7.5);
        // (6.0 + 9.0 + 7.5) / 3 = 7.5
        assertEquals(7.5, avaliacaoService.calcularMedia(turma.getCodigo(), "A001"), 0.001);
    }

    @Test
    void deveLidarComMediaNaoInteira() {
        avaliacaoService.lancarNota(turma.getCodigo(), "A001", 7.0);
        avaliacaoService.lancarNota(turma.getCodigo(), "A001", 8.0);
        // (7.0 + 8.0) / 2 = 7.5
        assertEquals(7.5, avaliacaoService.calcularMedia(turma.getCodigo(), "A001"), 0.001);
    }

    @Test
    void deveReprovarNaMediaComNotasBaixas() {
        avaliacaoService.lancarNota(turma.getCodigo(), "A001", 2.0);
        avaliacaoService.lancarNota(turma.getCodigo(), "A001", 3.0);
        assertEquals(2.5, avaliacaoService.calcularMedia(turma.getCodigo(), "A001"), 0.001);
    }

    @Test
    void deveRejeitarMediaSemAvaliacao() {
        assertThrows(
                IllegalArgumentException.class,
                () -> avaliacaoService.calcularMedia(turma.getCodigo(), "A001"));
    }
}
