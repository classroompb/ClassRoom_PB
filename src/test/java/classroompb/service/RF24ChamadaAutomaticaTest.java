package classroompb.service;

import static org.junit.jupiter.api.Assertions.*;

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

/**
 * RF24: Quando uma vaga for liberada, o sistema deve chamar automaticamente o próximo aluno da
 * lista de espera.
 *
 * <p>Cobre as duas formas de liberação de vaga: - cancelamento de matrícula; - aumento do limite de
 * vagas da turma (RF14) liberando vagas.
 */
class RF24ChamadaAutomaticaTest {

    private TurmaService turmaService;
    private PeriodoLetivo periodo;

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

        var usuarioService = new UsuarioService(usuarioRepo);
        var disciplinaService = new DisciplinaService(disciplinaRepo);
        var periodoLetivoService = new PeriodoLetivoService(periodoRepo);
        turmaService = new TurmaService(turmaRepo, usuarioService, disciplinaService);

        usuarioService.cadastrar("PROFESSOR", "Prof. Ana", "P001", "ana@example.com", "1234");
        usuarioService.cadastrar("ALUNO", "Aluno 1", "A001", "aluno1@example.com", "1234");
        usuarioService.cadastrar("ALUNO", "Aluno 2", "A002", "aluno2@example.com", "1234");
        usuarioService.cadastrar("ALUNO", "Aluno 3", "A003", "aluno3@example.com", "1234");
        usuarioService.cadastrar("ALUNO", "Aluno 4", "A004", "aluno4@example.com", "1234");

        disciplinaService.cadastrar("CC101", "Programacao I", 60, 4);

        periodoLetivoService.cadastrar("2026.1");
        periodo = periodoLetivoService.buscarPorIdentificador("2026.1");
    }

    @Test
    void deveChamarProximoAutomaticamenteAoCancelarMatricula() {
        // Arrange: turma com 1 vaga, A001 matriculado e A002, A003 na fila
        Turma turma =
                turmaService.ofertarTurma("CC101", "P001", periodo, 1, "08:00-10:00", "Sala A1");
        turmaService.solicitarMatricula(turma.getCodigo(), "A001");
        turmaService.solicitarMatricula(turma.getCodigo(), "A002");
        turmaService.solicitarMatricula(turma.getCodigo(), "A003");

        // Act: libera a vaga cancelando A001
        turmaService.cancelarMatricula(turma.getCodigo(), "A001");

        // Assert: A002 (próximo da fila) foi chamado automaticamente
        Turma atualizada = turmaService.obterTurma(turma.getCodigo());
        assertTrue(atualizada.alunoJaMatriculado("A002"));
        assertFalse(atualizada.alunoJaEmEspera("A002"));
        assertEquals(0, atualizada.getVagasDisponiveis());
        assertEquals(1, atualizada.getTotalEmEspera());
    }

    @Test
    void naoDeveChamarNinguemQuandoFilaVazia() {
        // Arrange: turma com 1 vaga, sem fila
        Turma turma =
                turmaService.ofertarTurma("CC101", "P001", periodo, 1, "08:00-10:00", "Sala A1");
        turmaService.solicitarMatricula(turma.getCodigo(), "A001");

        // Act: cancela liberando vaga, mas não há ninguém na fila
        turmaService.cancelarMatricula(turma.getCodigo(), "A001");

        // Assert: vaga fica disponível e ninguém é matriculado
        Turma atualizada = turmaService.obterTurma(turma.getCodigo());
        assertEquals(1, atualizada.getVagasDisponiveis());
        assertEquals(0, atualizada.getTotalMatriculados());
    }

    @Test
    void deveChamarProximosAoAumentarLimiteDeVagas() {
        // Arrange: turma com 1 vaga, A001 matriculado, A002 e A003 na fila
        Turma turma =
                turmaService.ofertarTurma("CC101", "P001", periodo, 1, "08:00-10:00", "Sala A1");
        turmaService.solicitarMatricula(turma.getCodigo(), "A001");
        turmaService.solicitarMatricula(turma.getCodigo(), "A002");
        turmaService.solicitarMatricula(turma.getCodigo(), "A003");

        // Act: aumenta o limite para 3 (libera 2 vagas)
        turmaService.alterarTurma(turma.getCodigo(), null, 3, null, null);

        // Assert: A002 e A003 chamados automaticamente, na ordem
        Turma atualizada = turmaService.obterTurma(turma.getCodigo());
        assertTrue(atualizada.alunoJaMatriculado("A002"));
        assertTrue(atualizada.alunoJaMatriculado("A003"));
        assertEquals(3, atualizada.getTotalMatriculados());
        assertEquals(0, atualizada.getTotalEmEspera());
        assertEquals(0, atualizada.getVagasDisponiveis());
    }

    @Test
    void deveChamarProximoExplicitamenteAoLiberarVaga() {
        // Arrange: turma com 2 vagas, ambas ocupadas, A003 na fila
        Turma turma =
                turmaService.ofertarTurma("CC101", "P001", periodo, 2, "08:00-10:00", "Sala A1");
        turmaService.solicitarMatricula(turma.getCodigo(), "A001");
        turmaService.solicitarMatricula(turma.getCodigo(), "A002");
        turmaService.solicitarMatricula(turma.getCodigo(), "A003");

        // Sem vagas: chamada não promove ninguém
        assertTrue(turmaService.chamarProximosDaListaDeEspera(turma.getCodigo()).isEmpty());

        // Act: aumenta limite criando 1 vaga e chama explicitamente
        Turma t = turmaService.obterTurma(turma.getCodigo());
        t.ajustarLimiteVagas(3);
        List<String> promovidos = turmaService.chamarProximosDaListaDeEspera(turma.getCodigo());

        // Assert
        assertEquals(1, promovidos.size());
        assertEquals("A003", promovidos.get(0));
    }

    @Test
    void deveLancarErroAoChamarFilaDeTurmaInexistente() {
        assertThrows(
                IllegalArgumentException.class,
                () -> turmaService.chamarProximosDaListaDeEspera("INEXISTENTE"));
    }
}
