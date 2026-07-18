package classroompb.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.example.classroompb.model.Avaliacao;
import org.example.classroompb.model.Curso;
import org.example.classroompb.model.Disciplina;
import org.example.classroompb.model.HistoricoAlunoCurso;
import org.example.classroompb.model.PeriodoLetivo;
import org.example.classroompb.model.SituacaoFinal;
import org.example.classroompb.model.Turma;
import org.example.classroompb.model.Usuario;
import org.example.classroompb.repository.AvaliacaoRepository;
import org.example.classroompb.repository.CursoRepository;
import org.example.classroompb.repository.DisciplinaRepository;
import org.example.classroompb.repository.PeriodoLetivoRepository;
import org.example.classroompb.repository.TurmaRepository;
import org.example.classroompb.repository.UsuarioRepository;
import org.example.classroompb.service.AvaliacaoService;
import org.example.classroompb.service.CursoService;
import org.example.classroompb.service.DisciplinaService;
import org.example.classroompb.service.PeriodoLetivoService;
import org.example.classroompb.service.TurmaService;
import org.example.classroompb.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * RF39 — o coordenador deve poder consultar o histórico dos alunos do curso.
 *
 * <p>Cobre o vínculo Aluno-Curso (novo, feito via {@code UsuarioService.vincularAlunoACurso}), a
 * listagem de alunos por curso e a montagem do histórico agrupado (reaproveitando o RF37).
 */
class RF39HistoricoAlunosCursoTest {

    private UsuarioService usuarioService;
    private CursoService cursoService;
    private DisciplinaService disciplinaService;
    private PeriodoLetivoService periodoLetivoService;
    private TurmaService turmaService;
    private AvaliacaoService avaliacaoService;

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

    static class CursoRepositorioFake extends CursoRepository {
        private final List<Curso> lista = new ArrayList<>();

        @Override
        public List<Curso> carregarTodos() {
            return new ArrayList<>(lista);
        }

        @Override
        public void salvarTodos(List<Curso> cursos) {
            lista.clear();
            lista.addAll(cursos);
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

    @BeforeEach
    void setUp() {
        usuarioService = new UsuarioService(new UsuarioRepositorioFake());
        cursoService = new CursoService(new CursoRepositorioFake());
        disciplinaService = new DisciplinaService(new DisciplinaRepositorioFake());
        periodoLetivoService = new PeriodoLetivoService(new PeriodoLetivoRepositorioFake());
        turmaService =
                new TurmaService(new TurmaRepositorioFake(), usuarioService, disciplinaService);
        avaliacaoService = new AvaliacaoService(new AvaliacaoRepositorioFake(), turmaService);

        cursoService.cadastrar("CC", "Ciência da Computação");
        cursoService.cadastrar("ADS", "Análise e Desenvolvimento de Sistemas");

        usuarioService.cadastrar("PROFESSOR", "Prof. Ana", "P001", "ana@example.com", "1234");
        usuarioService.cadastrar("COORDENADOR", "Coord. Bia", "K001", "bia@example.com", "1234");

        disciplinaService.cadastrar("CC101", "Programacao I", 60, 4);
        periodoLetivoService.cadastrar("2026.1");
    }

    private Turma ofertarEMatricular(String matriculaAluno) {
        PeriodoLetivo periodo = periodoLetivoService.buscarPorIdentificador("2026.1");
        Turma turma =
                turmaService.ofertarTurma("CC101", "P001", periodo, 5, "08:00-10:00", "Sala A1");
        turmaService.solicitarMatricula(turma.getCodigo(), matriculaAluno);
        return turma;
    }

    // ---- vincularAlunoACurso ----

    @Test
    void deveVincularAlunoACurso() {
        usuarioService.cadastrar("ALUNO", "Aluno 1", "A001", "aluno1@example.com", "1234");

        usuarioService.vincularAlunoACurso("A001", "CC");

        List<org.example.classroompb.model.Aluno> alunosDoCC = usuarioService.listarAlunosPorCurso("CC");
        assertEquals(1, alunosDoCC.size());
        assertEquals("A001", alunosDoCC.get(0).getMatricula());
        assertEquals("CC", alunosDoCC.get(0).getCodigoCurso());
    }

    @Test
    void deveRejeitarVinculoComAlunoInexistente() {
        assertThrows(
                IllegalArgumentException.class,
                () -> usuarioService.vincularAlunoACurso("A999", "CC"));
    }

    @Test
    void deveRejeitarVinculoComUsuarioQueNaoEAluno() {
        assertThrows(
                IllegalArgumentException.class,
                () -> usuarioService.vincularAlunoACurso("P001", "CC"));
    }

    @Test
    void deveRejeitarVinculoComCodigoDeCursoVazio() {
        usuarioService.cadastrar("ALUNO", "Aluno 1", "A001", "aluno1@example.com", "1234");

        assertThrows(
                IllegalArgumentException.class,
                () -> usuarioService.vincularAlunoACurso("A001", " "));
    }

    // ---- listarAlunosPorCurso ----

    @Test
    void deveListarApenasOsAlunosDoCursoInformado() {
        usuarioService.cadastrar("ALUNO", "Aluno 1", "A001", "aluno1@example.com", "1234");
        usuarioService.cadastrar("ALUNO", "Aluno 2", "A002", "aluno2@example.com", "1234");
        usuarioService.vincularAlunoACurso("A001", "CC");
        usuarioService.vincularAlunoACurso("A002", "ADS");

        List<org.example.classroompb.model.Aluno> alunosDoCC = usuarioService.listarAlunosPorCurso("CC");

        assertEquals(1, alunosDoCC.size());
        assertEquals("A001", alunosDoCC.get(0).getMatricula());
    }

    @Test
    void deveRetornarListaVaziaSeNenhumAlunoEstaVinculadoAoCurso() {
        usuarioService.cadastrar("ALUNO", "Aluno 1", "A001", "aluno1@example.com", "1234");

        assertTrue(usuarioService.listarAlunosPorCurso("CC").isEmpty());
    }

    @Test
    void deveRejeitarListagemComCodigoDeCursoVazio() {
        assertThrows(
                IllegalArgumentException.class, () -> usuarioService.listarAlunosPorCurso(""));
    }

    // ---- consultarHistoricoPorCurso ----

    @Test
    void deveMontarHistoricoDeCadaAlunoDoCurso() {
        usuarioService.cadastrar("ALUNO", "Aluno 1", "A001", "aluno1@example.com", "1234");
        usuarioService.cadastrar("ALUNO", "Aluno 2", "A002", "aluno2@example.com", "1234");
        usuarioService.vincularAlunoACurso("A001", "CC");
        usuarioService.vincularAlunoACurso("A002", "CC");

        Turma turma = ofertarEMatricular("A001");
        turmaService.solicitarMatricula(turma.getCodigo(), "A002");

        avaliacaoService.lancarNota(turma.getCodigo(), "A001", 8.0);
        avaliacaoService.registrarFrequencia(turma.getCodigo(), "A001", 90.0);
        avaliacaoService.definirSituacaoFinal(turma.getCodigo(), "A001");
        // Aluno 2 matriculado mas sem notas lançadas ainda

        List<org.example.classroompb.model.Aluno> alunosDoCurso =
                usuarioService.listarAlunosPorCurso("CC");
        List<HistoricoAlunoCurso> historicos =
                avaliacaoService.consultarHistoricoPorCurso(alunosDoCurso);

        assertEquals(2, historicos.size());

        HistoricoAlunoCurso historicoAluno1 =
                historicos.stream()
                        .filter(h -> h.matriculaAluno().equals("A001"))
                        .findFirst()
                        .orElseThrow();
        assertEquals("Aluno 1", historicoAluno1.nomeAluno());
        assertEquals(1, historicoAluno1.itens().size());
        assertEquals(SituacaoFinal.APROVADO, historicoAluno1.itens().get(0).situacao());

        HistoricoAlunoCurso historicoAluno2 =
                historicos.stream()
                        .filter(h -> h.matriculaAluno().equals("A002"))
                        .findFirst()
                        .orElseThrow();
        assertTrue(historicoAluno2.itens().isEmpty());
    }

    @Test
    void deveRetornarListaVaziaQuandoCursoNaoTemAlunosVinculados() {
        List<HistoricoAlunoCurso> historicos =
                avaliacaoService.consultarHistoricoPorCurso(usuarioService.listarAlunosPorCurso("CC"));

        assertTrue(historicos.isEmpty());
    }

    @Test
    void deveRejeitarListaNulaDeAlunos() {
        assertThrows(
                IllegalArgumentException.class,
                () -> avaliacaoService.consultarHistoricoPorCurso(null));
    }

    @Test
    void naoDeveMisturarHistoricoDeAlunoDeOutroCurso() {
        usuarioService.cadastrar("ALUNO", "Aluno 1", "A001", "aluno1@example.com", "1234");
        usuarioService.cadastrar("ALUNO", "Aluno 2", "A002", "aluno2@example.com", "1234");
        usuarioService.vincularAlunoACurso("A001", "CC");
        usuarioService.vincularAlunoACurso("A002", "ADS");

        Turma turma = ofertarEMatricular("A001");
        turmaService.solicitarMatricula(turma.getCodigo(), "A002");
        avaliacaoService.lancarNota(turma.getCodigo(), "A002", 10.0);
        avaliacaoService.registrarFrequencia(turma.getCodigo(), "A002", 100.0);
        avaliacaoService.definirSituacaoFinal(turma.getCodigo(), "A002");

        List<HistoricoAlunoCurso> historicosCC =
                avaliacaoService.consultarHistoricoPorCurso(usuarioService.listarAlunosPorCurso("CC"));

        assertEquals(1, historicosCC.size());
        assertEquals("A001", historicosCC.get(0).matriculaAluno());
    }
}