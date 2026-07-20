package classroompb.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import org.example.classroompb.dto.ReprovacaoDisciplinaDTO;
import org.example.classroompb.exception.AcessoNegadoException;
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

/** RF42 — o coordenador gera o relatório de reprovação por disciplina (RN11 nota, RN12 falta). */
class RF42RelatorioReprovacaoDisciplinaTest {

    private AvaliacaoService avaliacaoService;
    private TurmaService turmaService;
    private UsuarioService usuarioService;
    private DisciplinaService disciplinaService;
    private PeriodoLetivoService periodoLetivoService;

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

    private Turma turmaAlgoritmos;
    private Turma turmaBanco;

    @BeforeEach
    void setUp() {
        usuarioService = new UsuarioService(new UsuarioRepositorioFake());
        disciplinaService = new DisciplinaService(new DisciplinaRepositorioFake());
        periodoLetivoService = new PeriodoLetivoService(new PeriodoLetivoRepositorioFake());
        turmaService =
                new TurmaService(new TurmaRepositorioFake(), usuarioService, disciplinaService);
        avaliacaoService = new AvaliacaoService(new AvaliacaoRepositorioFake(), turmaService);

        usuarioService.cadastrar("COORDENADOR", "Coord", "C001", "coord@example.com", "1234");
        usuarioService.cadastrar("PROFESSOR", "Prof", "P001", "prof@example.com", "1234");
        for (int i = 1; i <= 5; i++) {
            usuarioService.cadastrar("ALUNO", "Aluno " + i, "A00" + i, "a" + i + "@x.com", "1234");
        }

        disciplinaService.cadastrar("D001", "Algoritmos", 60, 4);
        disciplinaService.cadastrar("D002", "Banco de Dados", 60, 4);
        periodoLetivoService.cadastrar("2026.1");
        PeriodoLetivo periodo = periodoLetivoService.buscarPorIdentificador("2026.1");

        turmaAlgoritmos =
                turmaService.ofertarTurma("D001", "P001", periodo, 10, "08:00-10:00", "Sala 1");
        turmaBanco =
                turmaService.ofertarTurma("D002", "P001", periodo, 10, "10:00-12:00", "Sala 2");
        for (int i = 1; i <= 3; i++) {
            turmaService.solicitarMatricula(turmaAlgoritmos.getCodigo(), "A00" + i);
        }
        turmaService.solicitarMatricula(turmaBanco.getCodigo(), "A004");
        turmaService.solicitarMatricula(turmaBanco.getCodigo(), "A005");
    }

    private void finalizar(String turma, String aluno, double nota, double frequencia) {
        avaliacaoService.lancarNota(turma, aluno, nota);
        avaliacaoService.registrarFrequencia(turma, aluno, frequencia);
        avaliacaoService.definirSituacaoFinal(turma, aluno);
    }

    @Test
    void separaReprovacaoPorNotaEPorFaltaEcalculaTaxa() {
        Usuario coordenador = usuarioService.buscarPorMatricula("C001");
        String algoritmos = turmaAlgoritmos.getCodigo();
        String banco = turmaBanco.getCodigo();

        // Algoritmos: 1 aprovado, 1 reprovado por nota, 1 reprovado por falta -> 2/3 reprovados
        finalizar(algoritmos, "A001", 8.0, 90.0); // aprovado
        finalizar(algoritmos, "A002", 2.0, 90.0); // reprovado por nota (RN11)
        finalizar(algoritmos, "A003", 9.0, 50.0); // reprovado por falta (RN12)

        // Banco de Dados: 2 aprovados -> 0 reprovados
        finalizar(banco, "A004", 7.5, 95.0);
        finalizar(banco, "A005", 8.0, 88.0);

        List<ReprovacaoDisciplinaDTO> relatorio =
                avaliacaoService.relatorioReprovacaoPorDisciplina(coordenador);

        assertEquals(2, relatorio.size());

        // Ordenado por nome: Algoritmos vem antes de Banco de Dados
        ReprovacaoDisciplinaDTO algo = relatorio.get(0);
        assertEquals("D001", algo.codigoDisciplina());
        assertEquals("Algoritmos", algo.nomeDisciplina());
        assertEquals(3, algo.totalAvaliados());
        assertEquals(1, algo.reprovadosPorNota());
        assertEquals(1, algo.reprovadosPorFalta());
        assertEquals(2, algo.totalReprovados());
        assertEquals(66.7, algo.taxaReprovacao(), 0.1);

        ReprovacaoDisciplinaDTO bd = relatorio.get(1);
        assertEquals("Banco de Dados", bd.nomeDisciplina());
        assertEquals(2, bd.totalAvaliados());
        assertEquals(0, bd.totalReprovados());
        assertEquals(0.0, bd.taxaReprovacao(), 0.001);
    }

    @Test
    void avaliacaoEmAndamentoNaoEntraNaConta() {
        Usuario coordenador = usuarioService.buscarPorMatricula("C001");
        String algoritmos = turmaAlgoritmos.getCodigo();

        // Só lança nota, não define situação final: fica EM_ANDAMENTO
        avaliacaoService.lancarNota(algoritmos, "A001", 2.0);

        List<ReprovacaoDisciplinaDTO> relatorio =
                avaliacaoService.relatorioReprovacaoPorDisciplina(coordenador);

        // Nenhuma disciplina com avaliação finalizada
        assertEquals(0, relatorio.size());
    }

    @Test
    void naoCoordenadorRecebeAcessoNegado() {
        Usuario professor = usuarioService.buscarPorMatricula("P001");
        Usuario aluno = usuarioService.buscarPorMatricula("A001");

        assertThrows(
                AcessoNegadoException.class,
                () -> avaliacaoService.relatorioReprovacaoPorDisciplina(professor));
        assertThrows(
                AcessoNegadoException.class,
                () -> avaliacaoService.relatorioReprovacaoPorDisciplina(aluno));
        assertThrows(
                AcessoNegadoException.class,
                () -> avaliacaoService.relatorioReprovacaoPorDisciplina(null));
    }
}
