package classroompb.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import org.example.classroompb.dto.OcupacaoVagasDTO;
import org.example.classroompb.exception.AcessoNegadoException;
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

/** RF41 — o coordenador gera o relatório de ocupação de vagas das turmas. */
class RF41RelatorioOcupacaoVagasTest {

    private TurmaService turmaService;
    private UsuarioService usuarioService;
    private DisciplinaService disciplinaService;
    private PeriodoLetivoService periodoLetivoService;

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

        usuarioService.cadastrar("COORDENADOR", "Coord", "C001", "coord@example.com", "1234");
        usuarioService.cadastrar("PROFESSOR", "Prof Silva", "P001", "prof@example.com", "1234");
        usuarioService.cadastrar("ALUNO", "Aluno 1", "A001", "a1@example.com", "1234");
        usuarioService.cadastrar("ALUNO", "Aluno 2", "A002", "a2@example.com", "1234");
        usuarioService.cadastrar("ALUNO", "Aluno 3", "A003", "a3@example.com", "1234");
        usuarioService.cadastrar("ALUNO", "Aluno 4", "A004", "a4@example.com", "1234");

        disciplinaService.cadastrar("D001", "Algoritmos", 60, 4);
        periodoLetivoService.cadastrar("2026.1");
    }

    @Test
    void deveContarVagasMatriculadosLivresEEsperaPorTurma() {
        Usuario coordenador = usuarioService.buscarPorMatricula("C001");
        PeriodoLetivo periodo = periodoLetivoService.buscarPorIdentificador("2026.1");

        // Turma com 2 vagas: 2 matriculados, 1 na lista de espera.
        Turma turma =
                turmaService.ofertarTurma("D001", "P001", periodo, 2, "08:00-10:00", "Sala 1");
        turmaService.solicitarMatricula(turma.getCodigo(), "A001");
        turmaService.solicitarMatricula(turma.getCodigo(), "A002");
        turmaService.solicitarMatricula(turma.getCodigo(), "A003"); // sem vaga -> espera

        List<OcupacaoVagasDTO> relatorio = turmaService.relatorioOcupacaoVagas(coordenador);

        assertNotNull(relatorio);
        assertEquals(1, relatorio.size());
        OcupacaoVagasDTO item = relatorio.get(0);
        assertEquals(turma.getCodigo(), item.codigoTurma());
        assertEquals("Algoritmos", item.nomeDisciplina());
        assertEquals("Prof Silva", item.nomeProfessor());
        assertEquals("2026.1", item.periodoLetivo());
        assertEquals(2, item.limiteVagas());
        assertEquals(2, item.matriculados());
        assertEquals(0, item.vagasDisponiveis());
        assertEquals(1, item.emEspera());
    }

    @Test
    void turmaSemMatriculaMostraTodasAsVagasLivres() {
        Usuario coordenador = usuarioService.buscarPorMatricula("C001");
        PeriodoLetivo periodo = periodoLetivoService.buscarPorIdentificador("2026.1");

        Turma turma =
                turmaService.ofertarTurma("D001", "P001", periodo, 5, "10:00-12:00", "Sala 2");

        List<OcupacaoVagasDTO> relatorio = turmaService.relatorioOcupacaoVagas(coordenador);

        OcupacaoVagasDTO item = relatorio.get(0);
        assertEquals(turma.getCodigo(), item.codigoTurma());
        assertEquals(5, item.limiteVagas());
        assertEquals(0, item.matriculados());
        assertEquals(5, item.vagasDisponiveis());
        assertEquals(0, item.emEspera());
    }

    @Test
    void semTurmasRetornaListaVazia() {
        Usuario coordenador = usuarioService.buscarPorMatricula("C001");
        assertEquals(0, turmaService.relatorioOcupacaoVagas(coordenador).size());
    }

    @Test
    void naoCoordenadorRecebeAcessoNegado() {
        Usuario professor = usuarioService.buscarPorMatricula("P001");
        Usuario aluno = usuarioService.buscarPorMatricula("A001");

        assertThrows(
                AcessoNegadoException.class, () -> turmaService.relatorioOcupacaoVagas(professor));
        assertThrows(AcessoNegadoException.class, () -> turmaService.relatorioOcupacaoVagas(aluno));
        assertThrows(AcessoNegadoException.class, () -> turmaService.relatorioOcupacaoVagas(null));
    }
}
