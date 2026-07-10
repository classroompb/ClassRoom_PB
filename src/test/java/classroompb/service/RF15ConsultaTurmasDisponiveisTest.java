package classroompb.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
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

class RF15ConsultaTurmasDisponiveisTest {

    private TurmaService turmaService;
    private UsuarioService usuarioService;
    private DisciplinaService disciplinaService;
    private PeriodoLetivoService periodoLetivoService;
    private PeriodoLetivo periodo2026_1;
    private PeriodoLetivo periodo2026_2;

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
        var usuarioRepo = new UsuarioRepositorioFake();
        var disciplinaRepo = new DisciplinaRepositorioFake();
        var periodoRepo = new PeriodoLetivoRepositorioFake();
        var turmaRepo = new TurmaRepositorioFake();

        usuarioService = new UsuarioService(usuarioRepo);
        disciplinaService = new DisciplinaService(disciplinaRepo);
        periodoLetivoService = new PeriodoLetivoService(periodoRepo);
        turmaService = new TurmaService(turmaRepo, usuarioService, disciplinaService);

        usuarioService.cadastrar("PROFESSOR", "Prof. Ana", "P001", "ana@example.com", "1234");
        usuarioService.cadastrar("PROFESSOR", "Prof. Bia", "P002", "bia@example.com", "1234");
        usuarioService.cadastrar("ALUNO", "Aluno 1", "A001", "aluno1@example.com", "1234");

        disciplinaService.cadastrar("CC101", "Programacao I", 60, 4);
        disciplinaService.cadastrar("CC102", "Banco de Dados", 60, 4);

        periodoLetivoService.cadastrar("2026.1");
        periodoLetivoService.cadastrar("2026.2");
        periodo2026_1 = periodoLetivoService.buscarPorIdentificador("2026.1");
        periodo2026_2 = periodoLetivoService.buscarPorIdentificador("2026.2");
    }

    @Test
    void deveConsultarApenasTurmasComVagasDisponiveis() {
        Turma turmaComVaga =
                turmaService.ofertarTurma(
                        "CC101", "P001", periodo2026_1, 2, "08:00-10:00", "Sala A1");
        Turma turmaSemVaga =
                turmaService.ofertarTurma(
                        "CC102", "P002", periodo2026_1, 1, "10:00-12:00", "Sala B1");

        turmaService.solicitarMatricula(turmaSemVaga.getCodigo(), "A001");

        List<Turma> disponiveis = turmaService.consultarTurmasDisponiveis();

        assertEquals(1, disponiveis.size());
        assertEquals(turmaComVaga.getCodigo(), disponiveis.get(0).getCodigo());
    }

    @Test
    void deveConsultarTurmasDisponiveisPorPeriodoLetivo() {
        Turma turma2026_1 =
                turmaService.ofertarTurma(
                        "CC101", "P001", periodo2026_1, 2, "08:00-10:00", "Sala A1");
        turmaService.ofertarTurma("CC102", "P002", periodo2026_2, 2, "10:00-12:00", "Sala B1");

        List<Turma> disponiveis = turmaService.consultarTurmasDisponiveisPorPeriodo("2026.1");

        assertEquals(1, disponiveis.size());
        assertEquals(turma2026_1.getCodigo(), disponiveis.get(0).getCodigo());
    }

    @Test
    void deveRejeitarConsultaPorPeriodoVazio() {
        assertThrows(
                IllegalArgumentException.class,
                () -> turmaService.consultarTurmasDisponiveisPorPeriodo(""));
    }
}
