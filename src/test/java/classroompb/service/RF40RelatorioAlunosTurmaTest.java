package classroompb.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import org.example.classroompb.dto.AlunoMatriculadoDTO;
import org.example.classroompb.exception.AcessoNegadoException;
import org.example.classroompb.model.*;
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

class RF40RelatorioAlunosTurmaTest {

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
                new TurmaService(
                        new TurmaRepositorioFake(), usuarioService, disciplinaService);

        // Cadastra Coordenador, Professor e Alunos para os testes
        usuarioService.cadastrar(
                "COORDENADOR", "Coordenador Principal", "C001", "coord@example.com", "123");
        usuarioService.cadastrar("PROFESSOR", "Professor Silva", "P001", "prof@example.com", "123");
        usuarioService.cadastrar("ALUNO", "Aluno Um", "A001", "aluno1@example.com", "123");
        usuarioService.cadastrar("ALUNO", "Aluno Dois", "A002", "aluno2@example.com", "123");

        disciplinaService.cadastrar("D001", "Algoritmos", 60, 4);
        periodoLetivoService.cadastrar("2026.1");
    }

    @Test
    void deveGerarRelatorioDeAlunosPorTurmaComSucessoQuandoCoordenadorLogado() {
        // Arrange
        Usuario coordenador = usuarioService.buscarPorMatricula("C001");
        PeriodoLetivo periodo = periodoLetivoService.buscarPorIdentificador("2026.1");

        Turma turma =
                turmaService.ofertarTurma(
                        "D001", "P001", periodo, 10, "08:00-10:00", "Sala 101");
        turmaService.solicitarMatricula(turma.getCodigo(), "A001");
        turmaService.solicitarMatricula(turma.getCodigo(), "A002");

        // Act
        List<AlunoMatriculadoDTO> relatorio =
                turmaService.obterAlunosMatriculadosPorTurma(turma.getCodigo(), coordenador);

        // Assert
        assertNotNull(relatorio);
        assertEquals(2, relatorio.size());

        AlunoMatriculadoDTO dto1 = relatorio.get(0);
        assertEquals("A001", dto1.matricula());
        assertEquals("Aluno Um", dto1.nome());

        AlunoMatriculadoDTO dto2 = relatorio.get(1);
        assertEquals("A002", dto2.matricula());
        assertEquals("Aluno Dois", dto2.nome());
    }

    @Test
    void deveLancarAcessoNegadoExceptionQuandoUsuarioNaoForCoordenador() {
        // Arrange
        Usuario professor = usuarioService.buscarPorMatricula("P001");
        Usuario aluno = usuarioService.buscarPorMatricula("A001");
        PeriodoLetivo periodo = periodoLetivoService.buscarPorIdentificador("2026.1");

        Turma turma =
                turmaService.ofertarTurma(
                        "D001", "P001", periodo, 10, "08:00-10:00", "Sala 101");
        turmaService.solicitarMatricula(turma.getCodigo(), "A001");

        // Act & Assert
        // Testando com Professor
        assertThrows(
                AcessoNegadoException.class,
                () -> {
                    turmaService.obterAlunosMatriculadosPorTurma(turma.getCodigo(), professor);
                });

        // Testando com Aluno
        assertThrows(
                AcessoNegadoException.class,
                () -> {
                    turmaService.obterAlunosMatriculadosPorTurma(turma.getCodigo(), aluno);
                });

        // Testando com Usuário Nulo
        assertThrows(
                AcessoNegadoException.class,
                () -> {
                    turmaService.obterAlunosMatriculadosPorTurma(turma.getCodigo(), null);
                });
    }
}
