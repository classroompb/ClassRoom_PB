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
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * RF23: O sistema deve manter lista de espera por turma.
 *
 * <p>Este requisito assegura que: 1. A lista de espera é mantida por turma (não globalmente) 2. A
 * ordem dos alunos na fila é preservada 3. Alunos podem ser consultados sobre sua posição 4. A
 * promoção automática ocorre quando há cancelamentos 5. O sistema permite gerenciamento completo da
 * lista
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RF23ListaEsperaManutencaoTest {

    private TurmaService turmaService;
    private UsuarioService usuarioService;
    private DisciplinaService disciplinaService;
    private PeriodoLetivoService periodoLetivoService;
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

        usuarioService = new UsuarioService(usuarioRepo);
        disciplinaService = new DisciplinaService(disciplinaRepo);
        periodoLetivoService = new PeriodoLetivoService(periodoRepo);
        turmaService = new TurmaService(turmaRepo, usuarioService, disciplinaService);

        usuarioService.cadastrar("PROFESSOR", "Prof. Ana", "P001", "ana@example.com", "1234");
        usuarioService.cadastrar("ALUNO", "Aluno 1", "A001", "aluno1@example.com", "1234");
        usuarioService.cadastrar("ALUNO", "Aluno 2", "A002", "aluno2@example.com", "1234");
        usuarioService.cadastrar("ALUNO", "Aluno 3", "A003", "aluno3@example.com", "1234");
        usuarioService.cadastrar("ALUNO", "Aluno 4", "A004", "aluno4@example.com", "1234");
        usuarioService.cadastrar("ALUNO", "Aluno 5", "A005", "aluno5@example.com", "1234");
        usuarioService.cadastrar("ALUNO", "Aluno 6", "A006", "aluno6@example.com", "1234");

        disciplinaService.cadastrar("CC101", "Programacao I", 60, 4);

        periodoLetivoService.cadastrar("2026.1");
        periodo = periodoLetivoService.buscarPorIdentificador("2026.1");
    }

    @Test
    @Order(1)
    void deveManterListaDeEsperaIndependentePorTurma() {
        // Arrange: Criar 2 turmas da mesma disciplina com 1 vaga cada
        Turma turma1 =
                turmaService.ofertarTurma("CC101", "P001", periodo, 1, "08:00-10:00", "Sala A1");
        Turma turma2 =
                turmaService.ofertarTurma("CC101", "P001", periodo, 1, "10:00-12:00", "Sala A2");

        // Act: Matricular em T1, colocar A002 e A003 na fila de T1
        turmaService.solicitarMatricula(turma1.getCodigo(), "A001");
        turmaService.solicitarMatricula(turma1.getCodigo(), "A002");
        turmaService.solicitarMatricula(turma1.getCodigo(), "A003");

        // Act: Matricular em T2, colocar A005 e A006 na fila de T2
        turmaService.solicitarMatricula(turma2.getCodigo(), "A004");
        turmaService.solicitarMatricula(turma2.getCodigo(), "A005");
        turmaService.solicitarMatricula(turma2.getCodigo(), "A006");

        // Assert: Verificar que cada turma tem sua própria fila
        assertEquals(2, turmaService.consultarQuantidadeEmEspera(turma1.getCodigo()));
        assertEquals(2, turmaService.consultarQuantidadeEmEspera(turma2.getCodigo()));

        // Assert: Verificar alunos corretos na fila de cada turma
        List<String> filaT1 = turmaService.obterTurma(turma1.getCodigo()).getAlunosEmEspera();
        List<String> filaT2 = turmaService.obterTurma(turma2.getCodigo()).getAlunosEmEspera();

        assertEquals("A002", filaT1.get(0));
        assertEquals("A003", filaT1.get(1));
        assertEquals("A005", filaT2.get(0));
        assertEquals("A006", filaT2.get(1));
    }

    @Test
    @Order(2)
    void deveConsultarPosicaoAlunoNaFila() {
        // Arrange: Turma com 1 vaga, 3 alunos na fila
        Turma turma =
                turmaService.ofertarTurma("CC101", "P001", periodo, 1, "08:00-10:00", "Sala A1");
        turmaService.solicitarMatricula(turma.getCodigo(), "A001"); // Matriculado
        turmaService.solicitarMatricula(turma.getCodigo(), "A002"); // Fila posição 1
        turmaService.solicitarMatricula(turma.getCodigo(), "A003"); // Fila posição 2
        turmaService.solicitarMatricula(turma.getCodigo(), "A004"); // Fila posição 3

        // Act & Assert: Consultar posições
        assertEquals(1, turmaService.obterPosicaoEmEspera(turma.getCodigo(), "A002"));
        assertEquals(2, turmaService.obterPosicaoEmEspera(turma.getCodigo(), "A003"));
        assertEquals(3, turmaService.obterPosicaoEmEspera(turma.getCodigo(), "A004"));
        assertEquals(
                -1,
                turmaService.obterPosicaoEmEspera(
                        turma.getCodigo(), "A001")); // Matriculado, não está na fila
        assertEquals(
                -1,
                turmaService.obterPosicaoEmEspera(turma.getCodigo(), "A005")); // Não está na turma
    }

    @Test
    @Order(3)
    void devePromoverAutomaticamentePrimeiroAlunoAoCancelarMatricula() {
        // Arrange: Turma com 1 vaga, A001 matriculado, A002-A004 na fila
        Turma turma =
                turmaService.ofertarTurma("CC101", "P001", periodo, 1, "08:00-10:00", "Sala A1");
        turmaService.solicitarMatricula(turma.getCodigo(), "A001");
        turmaService.solicitarMatricula(turma.getCodigo(), "A002");
        turmaService.solicitarMatricula(turma.getCodigo(), "A003");
        turmaService.solicitarMatricula(turma.getCodigo(), "A004");

        // Act: Cancelar matrícula de A001
        turmaService.cancelarMatricula(turma.getCodigo(), "A001");

        // Assert: A002 foi promovido automaticamente
        Turma turmaAtualizada = turmaService.obterTurma(turma.getCodigo());
        assertTrue(
                turmaAtualizada.alunoJaMatriculado("A002"),
                "A002 deve estar matriculado após promoção");
        assertEquals(2, turmaAtualizada.getTotalEmEspera(), "Devem restar 2 alunos na fila");
        assertEquals("A003", turmaAtualizada.getAlunosEmEspera().get(0));
        assertEquals("A004", turmaAtualizada.getAlunosEmEspera().get(1));
    }

    @Test
    @Order(4)
    void devePromoverEmCadeiaSucessivos() {
        // Arrange: Turma com 1 vaga, 4 alunos na fila
        Turma turma =
                turmaService.ofertarTurma("CC101", "P001", periodo, 1, "08:00-10:00", "Sala A1");
        turmaService.solicitarMatricula(turma.getCodigo(), "A001");
        turmaService.solicitarMatricula(turma.getCodigo(), "A002");
        turmaService.solicitarMatricula(turma.getCodigo(), "A003");
        turmaService.solicitarMatricula(turma.getCodigo(), "A004");
        turmaService.solicitarMatricula(turma.getCodigo(), "A005");

        // Act: Cancelar A001
        turmaService.cancelarMatricula(turma.getCodigo(), "A001");
        Turma turmaStep1 = turmaService.obterTurma(turma.getCodigo());

        // Assert: A002 foi promovido
        assertTrue(turmaStep1.alunoJaMatriculado("A002"));
        assertEquals(3, turmaStep1.getTotalEmEspera());

        // Act: Cancelar A002
        turmaService.cancelarMatricula(turma.getCodigo(), "A002");
        Turma turmaStep2 = turmaService.obterTurma(turma.getCodigo());

        // Assert: A003 foi promovido
        assertTrue(turmaStep2.alunoJaMatriculado("A003"));
        assertEquals(2, turmaStep2.getTotalEmEspera());
        assertEquals("A004", turmaStep2.getAlunosEmEspera().get(0));
        assertEquals("A005", turmaStep2.getAlunosEmEspera().get(1));
    }

    @Test
    @Order(5)
    void deveRemoverAlunoManualmenteDaListaDeEspera() {
        // Arrange: Turma com 4 alunos na fila
        Turma turma =
                turmaService.ofertarTurma("CC101", "P001", periodo, 1, "08:00-10:00", "Sala A1");
        turmaService.solicitarMatricula(turma.getCodigo(), "A001"); // Matriculado
        turmaService.solicitarMatricula(turma.getCodigo(), "A002"); // Fila posição 1
        turmaService.solicitarMatricula(turma.getCodigo(), "A003"); // Fila posição 2
        turmaService.solicitarMatricula(turma.getCodigo(), "A004"); // Fila posição 3

        assertEquals(3, turmaService.consultarQuantidadeEmEspera(turma.getCodigo()));

        // Act: Remover A003 da fila (desistência)
        turmaService.removerDaEspera(turma.getCodigo(), "A003");

        // Assert: A003 foi removido, A002 e A004 permanecem em ordem
        Turma turmaAtualizada = turmaService.obterTurma(turma.getCodigo());
        assertEquals(2, turmaAtualizada.getTotalEmEspera());
        assertEquals("A002", turmaAtualizada.getAlunosEmEspera().get(0));
        assertEquals("A004", turmaAtualizada.getAlunosEmEspera().get(1));
        assertEquals(-1, turmaService.obterPosicaoEmEspera(turma.getCodigo(), "A003"));
    }

    @Test
    @Order(6)
    void deveManterApenasUmaVezPorTurma() {
        // Arrange: Turma cheia
        Turma turma =
                turmaService.ofertarTurma("CC101", "P001", periodo, 1, "08:00-10:00", "Sala A1");
        turmaService.solicitarMatricula(turma.getCodigo(), "A001");

        // Act & Assert: A002 entra na fila
        turmaService.solicitarMatricula(turma.getCodigo(), "A002");
        assertEquals(1, turmaService.consultarQuantidadeEmEspera(turma.getCodigo()));

        // Act & Assert: A002 não pode entrar de novo
        assertThrows(
                IllegalArgumentException.class,
                () -> turmaService.solicitarMatricula(turma.getCodigo(), "A002"),
                "Aluno já está em lista de espera. (RF21)");
    }

    @Test
    @Order(7)
    void deveManterHistoricoCorretoAposPersistencia() {
        // Arrange: Criar turma e operações
        Turma turma =
                turmaService.ofertarTurma("CC101", "P001", periodo, 2, "08:00-10:00", "Sala A1");
        turmaService.solicitarMatricula(turma.getCodigo(), "A001");
        turmaService.solicitarMatricula(turma.getCodigo(), "A002");
        turmaService.solicitarMatricula(turma.getCodigo(), "A003");
        turmaService.solicitarMatricula(turma.getCodigo(), "A004");

        // Assert pós-operação
        Turma turmaFinal = turmaService.obterTurma(turma.getCodigo());
        assertEquals(2, turmaFinal.getTotalMatriculados());
        assertEquals(2, turmaFinal.getTotalEmEspera());
        assertEquals("A003", turmaFinal.getAlunosEmEspera().get(0));
        assertEquals("A004", turmaFinal.getAlunosEmEspera().get(1));
    }

    @Test
    @Order(8)
    void deveValidarLimitesDeListaEspera() {
        // Arrange: Turma com 1 vaga e múltiplos alunos na fila
        Turma turma =
                turmaService.ofertarTurma("CC101", "P001", periodo, 1, "08:00-10:00", "Sala A1");

        // Act: Tentar criar turma com 0 vagas deve falhar
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        turmaService.ofertarTurma(
                                "CC101", "P001", periodo, 0, "08:00-10:00", "Sala B1"),
                "Limite de vagas deve ser maior que zero.");

        // Assert: Turma anterior criada com sucesso tem limite válido
        Turma turmaVerificada = turmaService.obterTurma(turma.getCodigo());
        assertEquals(1, turmaVerificada.getLimiteVagas());
    }

    @Test
    @Order(9)
    void deveManterConsistenciaEntreVagasEFila() {
        // Arrange: Turma com 2 vagas
        Turma turma =
                turmaService.ofertarTurma("CC101", "P001", periodo, 2, "08:00-10:00", "Sala A1");

        // Act: Matricular 4 alunos (2 vagas, 2 na fila)
        turmaService.solicitarMatricula(turma.getCodigo(), "A001");
        turmaService.solicitarMatricula(turma.getCodigo(), "A002");
        turmaService.solicitarMatricula(turma.getCodigo(), "A003");
        turmaService.solicitarMatricula(turma.getCodigo(), "A004");

        Turma turmaVerificar = turmaService.obterTurma(turma.getCodigo());

        // Assert: Vagas + Matriculados + Espera = Total Esperado
        assertEquals(0, turmaVerificar.getVagasDisponiveis()); // Sem vagas
        assertEquals(2, turmaVerificar.getTotalMatriculados());
        assertEquals(2, turmaVerificar.getTotalEmEspera());
        assertEquals(2, turmaVerificar.getLimiteVagas());

        // Act: Cancelar um matriculado
        turmaService.cancelarMatricula(turma.getCodigo(), "A001");
        Turma turmaAposCancel = turmaService.obterTurma(turma.getCodigo());

        // Assert: Um da fila foi promovido
        assertEquals(0, turmaAposCancel.getVagasDisponiveis()); // Vagas foram preenchidas
        assertEquals(
                2, turmaAposCancel.getTotalMatriculados()); // A003 foi promovido no lugar de A001
        assertEquals(1, turmaAposCancel.getTotalEmEspera()); // A004 ainda na fila
    }
}
