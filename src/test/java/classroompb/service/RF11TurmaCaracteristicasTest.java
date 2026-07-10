package classroompb.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.example.classroompb.model.*;
import org.example.classroompb.repository.*;
import org.example.classroompb.service.*;
import org.junit.jupiter.api.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RF11TurmaCaracteristicasTest {

    private TurmaService turmaService;
    private UsuarioService usuarioService;
    private DisciplinaService disciplinaService;
    private PeriodoLetivoService periodoLetivoService;

    static class TurmaRepositorioFake extends TurmaRepository {
        private final List<Turma> lista = new java.util.ArrayList<>();

        @Override
        public List<Turma> carregarTodos() {
            return new java.util.ArrayList<>(lista);
        }

        @Override
        public void salvarTodos(List<Turma> turmas) {
            lista.clear();
            lista.addAll(turmas);
        }
    }

    static class UsuarioRepositorioFake extends UsuarioRepository {
        private final List<Usuario> lista = new java.util.ArrayList<>();

        @Override
        public List<Usuario> carregarTodos() {
            return new java.util.ArrayList<>(lista);
        }

        @Override
        public void salvarTodos(List<Usuario> usuarios) {
            lista.clear();
            lista.addAll(usuarios);
        }
    }

    static class DisciplinaRepositorioFake extends DisciplinaRepository {
        private final List<Disciplina> lista = new java.util.ArrayList<>();

        @Override
        public List<Disciplina> carregarTodos() {
            return new java.util.ArrayList<>(lista);
        }

        @Override
        public void salvarTodos(List<Disciplina> disciplinas) {
            lista.clear();
            lista.addAll(disciplinas);
        }
    }

    static class PeriodoLetivoRepositorioFake extends PeriodoLetivoRepository {
        private final List<PeriodoLetivo> lista = new java.util.ArrayList<>();

        @Override
        public List<PeriodoLetivo> carregarTodos() {
            return new java.util.ArrayList<>(lista);
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

        // Cria dados básicos
        usuarioService.cadastrar("PROFESSOR", "Prof. João", "P001", "joao@example.com", "1234");
        usuarioService.cadastrar("PROFESSOR", "Prof. Maria", "P002", "maria@example.com", "1234");
        usuarioService.cadastrar("ALUNO", "Aluno 1", "A001", "aluno1@example.com", "1234");

        disciplinaService.cadastrar("CC101", "Programação I", 60, 4);

        periodoLetivoService.cadastrar("2024.1");
    }

    @Test
    @Order(1)
    void devePossuirProfessorResponsavel() {
        PeriodoLetivo periodo = periodoLetivoService.buscarPorIdentificador("2024.1");

        Turma turma =
                turmaService.ofertarTurma("CC101", "P001", periodo, 30, "08:00-10:00", "Sala A1");

        assertNotNull(turma.getProfessor());
        assertEquals("Prof. João", turma.getProfessor().getNome());
        assertEquals("P001", turma.getProfessor().getMatricula());
    }

    @Test
    @Order(2)
    void devePossuirLimiteDeVagas() {
        PeriodoLetivo periodo = periodoLetivoService.buscarPorIdentificador("2024.1");

        Turma turma =
                turmaService.ofertarTurma("CC101", "P001", periodo, 30, "08:00-10:00", "Sala A1");

        assertEquals(30, turma.getLimiteVagas());
        assertEquals(30, turma.getVagasDisponiveis());
    }

    @Test
    @Order(3)
    void devePossuirHorario() {
        PeriodoLetivo periodo = periodoLetivoService.buscarPorIdentificador("2024.1");

        Turma turma =
                turmaService.ofertarTurma("CC101", "P001", periodo, 30, "08:00-10:00", "Sala A1");

        assertNotNull(turma.getHorario());
        assertEquals("08:00-10:00", turma.getHorario());
    }

    @Test
    @Order(4)
    void devePossuirSala() {
        PeriodoLetivo periodo = periodoLetivoService.buscarPorIdentificador("2024.1");

        Turma turma =
                turmaService.ofertarTurma("CC101", "P001", periodo, 30, "08:00-10:00", "Sala A1");

        assertNotNull(turma.getSala());
        assertEquals("Sala A1", turma.getSala());
    }

    @Test
    @Order(5)
    void deveRN03NaoUltrapassarLimiteVagas() {
        PeriodoLetivo periodo = periodoLetivoService.buscarPorIdentificador("2024.1");
        usuarioService.cadastrar("ALUNO", "Aluno 2", "A002", "aluno2@example.com", "1234");
        usuarioService.cadastrar("ALUNO", "Aluno 3", "A003", "aluno3@example.com", "1234");

        Turma turma =
                turmaService.ofertarTurma("CC101", "P001", periodo, 2, "08:00-10:00", "Sala A1");

        turmaService.matricularAluno(turma.getCodigo(), "A001");
        turmaService.matricularAluno(turma.getCodigo(), "A002");

        // RN03: Terceiro aluno não deve conseguir se matricular
        assertThrows(
                IllegalArgumentException.class,
                () -> turmaService.matricularAluno(turma.getCodigo(), "A003"));
    }

    @Test
    @Order(6)
    void deveRN01NaoPermitirMatriculaDuplaDoAluno() {
        PeriodoLetivo periodo = periodoLetivoService.buscarPorIdentificador("2024.1");

        Turma turma =
                turmaService.ofertarTurma("CC101", "P001", periodo, 30, "08:00-10:00", "Sala A1");

        turmaService.matricularAluno(turma.getCodigo(), "A001");

        // RN01: Aluno não pode se matricular duas vezes
        assertThrows(
                IllegalArgumentException.class,
                () -> turmaService.matricularAluno(turma.getCodigo(), "A001"));
    }

    @Test
    @Order(7)
    void deveRN06NaoPermitirProfessorDoiHorarios() {
        PeriodoLetivo periodo = periodoLetivoService.buscarPorIdentificador("2024.1");

        turmaService.ofertarTurma("CC101", "P001", periodo, 30, "08:00-10:00", "Sala A1");

        // RN06: Professor não pode ter duas turmas no mesmo horário
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        turmaService.ofertarTurma(
                                "CC101", "P001", periodo, 25, "08:00-10:00", "Sala A2"));
    }

    @Test
    @Order(8)
    void deveRN06PermitirProfessorHorariosForados() {
        PeriodoLetivo periodo = periodoLetivoService.buscarPorIdentificador("2024.1");

        // Primeira turma: 08:00-10:00
        turmaService.ofertarTurma("CC101", "P001", periodo, 30, "08:00-10:00", "Sala A1");

        // Segunda turma mesmo professor mas diferente horário: 14:00-16:00 (não conflita)
        Turma turma2 =
                turmaService.ofertarTurma("CC101", "P001", periodo, 25, "14:00-16:00", "Sala A2");

        assertNotNull(turma2);
        assertEquals("14:00-16:00", turma2.getHorario());
    }

    @Test
    @Order(9)
    void deveRN06PermitirProfessorHorariosBackToBack() {
        PeriodoLetivo periodo = periodoLetivoService.buscarPorIdentificador("2024.1");

        // Primeira turma: 08:00-10:00
        turmaService.ofertarTurma("CC101", "P001", periodo, 30, "08:00-10:00", "Sala A1");

        // Cadastra segunda disciplina
        disciplinaService.cadastrar("CC102", "Programação II", 60, 4);

        // Segunda turma mesmo professor, horário back-to-back: 10:00-12:00 (termina onde começa)
        Turma turma2 =
                turmaService.ofertarTurma("CC102", "P001", periodo, 25, "10:00-12:00", "Sala A2");

        assertNotNull(turma2);
        assertEquals("10:00-12:00", turma2.getHorario());
    }

    @Test
    @Order(10)
    void deveRN02DetectarConflitosDeHorario() {
        PeriodoLetivo periodo = periodoLetivoService.buscarPorIdentificador("2024.1");
        usuarioService.cadastrar("ALUNO", "Aluno 2", "A002", "aluno2@example.com", "1234");

        Turma turma1 =
                turmaService.ofertarTurma("CC101", "P001", periodo, 30, "08:00-10:00", "Sala A1");

        // Cria outra disciplina
        disciplinaService.cadastrar("CC102", "Programação II", 60, 4);
        Turma turma2 =
                turmaService.ofertarTurma("CC102", "P002", periodo, 30, "09:00-11:00", "Sala A2");

        turmaService.matricularAluno(turma1.getCodigo(), "A001");

        // RN02: Aluno não pode se matricular em turmas com horários conflitantes
        // 08:00-10:00 conflita com 09:00-11:00
        assertThrows(
                IllegalArgumentException.class,
                () -> turmaService.matricularAluno(turma2.getCodigo(), "A001"));
    }

    @Test
    @Order(11)
    void deveRN02PermitirHorariosNaoConflitantes() {
        PeriodoLetivo periodo = periodoLetivoService.buscarPorIdentificador("2024.1");

        Turma turma1 =
                turmaService.ofertarTurma("CC101", "P001", periodo, 30, "08:00-10:00", "Sala A1");

        disciplinaService.cadastrar("CC102", "Programação II", 60, 4);
        Turma turma2 =
                turmaService.ofertarTurma("CC102", "P002", periodo, 30, "10:00-12:00", "Sala A2");

        turmaService.matricularAluno(turma1.getCodigo(), "A001");

        // RN02: Sem conflito - 08:00-10:00 e 10:00-12:00 não sobrepõem (back-to-back OK)
        turmaService.matricularAluno(turma2.getCodigo(), "A001");

        assertTrue(turma1.alunoJaMatriculado("A001"));
        assertTrue(turma2.alunoJaMatriculado("A001"));
    }

    @Test
    @Order(12)
    void deveValidarNotasEntre0E10() {
        // RN07: Notas devem estar entre 0 e 10

        // Válido
        turmaService.validarNota(0);
        turmaService.validarNota(7.5);
        turmaService.validarNota(10);

        // Inválido
        assertThrows(IllegalArgumentException.class, () -> turmaService.validarNota(-1));
        assertThrows(IllegalArgumentException.class, () -> turmaService.validarNota(10.1));
        assertThrows(IllegalArgumentException.class, () -> turmaService.validarNota(15));
    }

    @Test
    @Order(13)
    void deveValidarFrequenciaMinima75Porcento() {
        // RN08: Frequência mínima: 75%

        // 75 minutos de 100 = 75%
        assertTrue(turmaService.verificarAprovacaoFrequencia(75, 100));

        // 74 minutos de 100 = 74%
        assertFalse(turmaService.verificarAprovacaoFrequencia(74, 100));

        // 150 minutos de 200 = 75%
        assertTrue(turmaService.verificarAprovacaoFrequencia(150, 200));
    }

    @Test
    @Order(14)
    void deveCalcularSituacaoAlunoAprovado() {
        // RN09: Média >= 7.0 = Aprovado

        String situacao1 = turmaService.calcularSituacaoAluno(7.0, 80);
        assertEquals("APROVADO", situacao1);

        String situacao2 = turmaService.calcularSituacaoAluno(8.5, 85);
        assertEquals("APROVADO", situacao2);

        String situacao3 = turmaService.calcularSituacaoAluno(10, 100);
        assertEquals("APROVADO", situacao3);
    }

    @Test
    @Order(15)
    void deveCalcularSituacaoAlunoRecuperacao() {
        // RN10: 4.0 <= Média < 7.0 = Recuperação

        String situacao1 = turmaService.calcularSituacaoAluno(4.0, 80);
        assertEquals("RECUPERACAO", situacao1);

        String situacao2 = turmaService.calcularSituacaoAluno(5.5, 85);
        assertEquals("RECUPERACAO", situacao2);

        String situacao3 = turmaService.calcularSituacaoAluno(6.9, 90);
        assertEquals("RECUPERACAO", situacao3);
    }

    @Test
    @Order(16)
    void deveCalcularSituacaoAlunoReprovadoPorNota() {
        // RN11: Média < 4.0 = Reprovado por nota

        String situacao1 = turmaService.calcularSituacaoAluno(3.9, 80);
        assertEquals("REPROVADO_POR_NOTA", situacao1);

        String situacao2 = turmaService.calcularSituacaoAluno(0, 90);
        assertEquals("REPROVADO_POR_NOTA", situacao2);

        String situacao3 = turmaService.calcularSituacaoAluno(2.5, 95);
        assertEquals("REPROVADO_POR_NOTA", situacao3);
    }

    @Test
    @Order(17)
    void deveRN12ReprovacaoPorFaltaPrevalesce() {
        // RN12: Reprovação por falta prevalece sobre aprovação por nota

        String situacao1 = turmaService.calcularSituacaoAluno(8.5, 70); // Boa nota, mas falta < 75%
        assertEquals("REPROVADO_POR_FALTA", situacao1);

        String situacao2 =
                turmaService.calcularSituacaoAluno(9.0, 50); // Excelente nota, mas faltas altas
        assertEquals("REPROVADO_POR_FALTA", situacao2);

        String situacao3 =
                turmaService.calcularSituacaoAluno(10, 0); // Perfeito em nota, não compareceu
        assertEquals("REPROVADO_POR_FALTA", situacao3);
    }

    @Test
    @Order(18)
    void deveCancelarMatriculaDevolverVagas() {
        PeriodoLetivo periodo = periodoLetivoService.buscarPorIdentificador("2024.1");

        Turma turma =
                turmaService.ofertarTurma("CC101", "P001", periodo, 2, "08:00-10:00", "Sala A1");

        turmaService.matricularAluno(turma.getCodigo(), "A001");
        assertEquals(1, turma.getVagasDisponiveis());

        turmaService.cancelarMatricula(turma.getCodigo(), "A001");
        assertEquals(2, turma.getVagasDisponiveis());
    }

    @Test
    @Order(19)
    void deveTrabalharComMultiplasTurmasSameDisiplina() {
        PeriodoLetivo periodo = periodoLetivoService.buscarPorIdentificador("2024.1");

        // Cria 3 turmas da mesma disciplina com diferentes professores e horários
        Turma turma1 =
                turmaService.ofertarTurma("CC101", "P001", periodo, 30, "08:00-10:00", "Sala A1");
        Turma turma2 =
                turmaService.ofertarTurma("CC101", "P002", periodo, 25, "10:00-12:00", "Sala B1");

        // Verifica que os códigos são únicos
        assertNotEquals(turma1.getCodigo(), turma2.getCodigo());

        // Verifica que foram criadas corretamente
        List<Turma> turmasCC101 = turmaService.listarTurmasPorDisciplina("CC101");
        assertEquals(2, turmasCC101.size());
    }
}
