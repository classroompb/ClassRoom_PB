package classroompb.service;

import org.example.classroompb.exception.AlunoNaoEncontradoException;
import org.example.classroompb.model.Alerta;
import org.example.classroompb.model.Disciplina;
import org.example.classroompb.model.FrequenciaDisciplina;
import org.example.classroompb.model.PeriodoLetivo;
import org.example.classroompb.model.Turma;
import org.example.classroompb.model.Usuario;
import org.example.classroompb.repository.AlertaRepository;
import org.example.classroompb.repository.DisciplinaRepository;
import org.example.classroompb.repository.FrequenciaRepository;
import org.example.classroompb.repository.PeriodoLetivoRepository;
import org.example.classroompb.repository.TurmaRepository;
import org.example.classroompb.repository.UsuarioRepository;
import org.example.classroompb.service.AlertaService;
import org.example.classroompb.service.ConsultaFrequenciaService;
import org.example.classroompb.service.DisciplinaService;
import org.example.classroompb.service.FrequenciaService;
import org.example.classroompb.service.PeriodoLetivoService;
import org.example.classroompb.service.TurmaService;
import org.example.classroompb.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RF30: O sistema deve alertar quando o aluno estiver abaixo do mínimo exigido.
 *
 * <p>Testa a geração de alertas quando:
 * - Nota lançada é inferior a 7.0 (mínimo para aprovação direta)
 * - Frequência cai abaixo de 75% (mínimo exigido)
 *
 * <p>Também testa a consulta e marcação de alertas como lidos.
 */
class RF30AlertaSituacaoTest {

    private AlertaService alertaService;
    private FrequenciaService frequenciaService;
    private TurmaService turmaService;
    private UsuarioService usuarioService;
    private DisciplinaService disciplinaService;
    private ConsultaFrequenciaService consultaFrequenciaService;
    private PeriodoLetivoService periodoLetivoService;
    
    private Turma turmaCC101;
    private Turma turmaMAT101;
    
    private final LocalDate dia1 = LocalDate.of(2026, 6, 1);
    private final LocalDate dia2 = LocalDate.of(2026, 6, 8);
    private final LocalDate dia3 = LocalDate.of(2026, 6, 15);
    private final LocalDate dia4 = LocalDate.of(2026, 6, 22);

    // ==================== Repositórios Fake ====================

    static class AlertaRepositorioFake extends AlertaRepository {
        private final List<Alerta> lista = new ArrayList<>();
        @Override public List<Alerta> carregarTodos() { return new ArrayList<>(lista); }
        @Override public void salvarTodos(List<Alerta> alertas) { lista.clear(); lista.addAll(alertas); }
    }

    static class FrequenciaRepositorioFake extends FrequenciaRepository {
        private final List<org.example.classroompb.model.RegistroFrequencia> lista = new ArrayList<>();
        @Override public List<org.example.classroompb.model.RegistroFrequencia> carregarTodos() { return new ArrayList<>(lista); }
        @Override public void salvarTodos(List<org.example.classroompb.model.RegistroFrequencia> registros) { lista.clear(); lista.addAll(registros); }
    }

    static class TurmaRepositorioFake extends TurmaRepository {
        private final List<Turma> lista = new ArrayList<>();
        @Override public List<Turma> carregarTodos() { return new ArrayList<>(lista); }
        @Override public void salvarTodos(List<Turma> turmas) { lista.clear(); lista.addAll(turmas); }
    }

    static class UsuarioRepositorioFake extends UsuarioRepository {
        private final List<Usuario> lista = new ArrayList<>();
        @Override public List<Usuario> carregarTodos() { return new ArrayList<>(lista); }
        @Override public void salvarTodos(List<Usuario> usuarios) { lista.clear(); lista.addAll(usuarios); }
    }

    static class DisciplinaRepositorioFake extends DisciplinaRepository {
        private final List<Disciplina> lista = new ArrayList<>();
        @Override public List<Disciplina> carregarTodos() { return new ArrayList<>(lista); }
        @Override public void salvarTodos(List<Disciplina> disciplinas) { lista.clear(); lista.addAll(disciplinas); }
    }

    static class PeriodoLetivoRepositorioFake extends PeriodoLetivoRepository {
        private final List<PeriodoLetivo> lista = new ArrayList<>();
        @Override public List<PeriodoLetivo> carregarTodos() { return new ArrayList<>(lista); }
        @Override public void salvarTodos(List<PeriodoLetivo> periodos) { lista.clear(); lista.addAll(periodos); }
    }

    // ==================== Setup ====================

    @BeforeEach
    void setUp() {
        usuarioService = new UsuarioService(new UsuarioRepositorioFake());
        disciplinaService = new DisciplinaService(new DisciplinaRepositorioFake());
        periodoLetivoService = new PeriodoLetivoService(new PeriodoLetivoRepositorioFake());
        turmaService = new TurmaService(new TurmaRepositorioFake(), usuarioService, disciplinaService);
        frequenciaService = new FrequenciaService(new FrequenciaRepositorioFake(), usuarioService, turmaService);
        consultaFrequenciaService = new ConsultaFrequenciaService(
                frequenciaService, turmaService, disciplinaService, usuarioService);
        alertaService = new AlertaService(new AlertaRepositorioFake(), usuarioService);

        usuarioService.cadastrar("PROFESSOR", "Prof. Ana", "P001", "ana@example.com", "1234");
        usuarioService.cadastrar("ALUNO", "Aluno 1", "A001", "aluno1@example.com", "1234");
        usuarioService.cadastrar("ALUNO", "Aluno 2", "A002", "aluno2@example.com", "1234");

        disciplinaService.cadastrar("CC101", "Programacao I", 60, 4);
        disciplinaService.cadastrar("MAT101", "Calculo I", 60, 4);
        
        periodoLetivoService.cadastrar("2026.1");
        PeriodoLetivo periodo = periodoLetivoService.buscarPorIdentificador("2026.1");

        turmaCC101 = turmaService.ofertarTurma("CC101", "P001", periodo, 30, "08:00-10:00", "Sala A1");
        turmaMAT101 = turmaService.ofertarTurma("MAT101", "P001", periodo, 30, "10:00-12:00", "Sala A2");
        
        turmaService.solicitarMatricula(turmaCC101.getCodigo(), "A001");
        turmaService.solicitarMatricula(turmaMAT101.getCodigo(), "A001");
        turmaService.solicitarMatricula(turmaCC101.getCodigo(), "A002");
    }

    // ==================== Testes de Alerta por Nota Baixa ====================

    @Test
    void deveGerarAlertaQuandoNotaAbaixo7() {
        // Alerta quando nota < 7.0
        alertaService.verificarEGerarAlertaDeNota("A001", turmaCC101.getCodigo(), "CC101", 6.5);
        
        List<Alerta> alertas = alertaService.obterAlertasNaoLidos("A001");
        assertEquals(1, alertas.size());
        assertEquals(Alerta.TipoAlerta.NOTA_BAIXA, alertas.get(0).getTipo());
        assertEquals(6.5, alertas.get(0).getValorAtual());
        assertEquals(7.0, alertas.get(0).getLimiteMinimo());
    }

    @Test
    void naoDeveGerarAlertaQuandoNotaEhIgualOuMaiorQue7() {
        alertaService.verificarEGerarAlertaDeNota("A001", turmaCC101.getCodigo(), "CC101", 7.0);
        alertaService.verificarEGerarAlertaDeNota("A001", turmaCC101.getCodigo(), "CC101", 8.5);
        
        List<Alerta> alertas = alertaService.obterAlertasNaoLidos("A001");
        assertEquals(0, alertas.size());
    }

    @Test
    void deveGerarAlertasMultiplosParaNotasBaixasEmDisciplinasdiferentes() {
        alertaService.verificarEGerarAlertaDeNota("A001", turmaCC101.getCodigo(), "CC101", 5.0);
        alertaService.verificarEGerarAlertaDeNota("A001", turmaMAT101.getCodigo(), "MAT101", 6.0);
        
        List<Alerta> alertas = alertaService.obterAlertasNaoLidos("A001");
        assertEquals(2, alertas.size());
    }

    @Test
    void deveGerarAlertaQuandoNotaIgualAZero() {
        alertaService.verificarEGerarAlertaDeNota("A001", turmaCC101.getCodigo(), "CC101", 0.0);
        
        List<Alerta> alertas = alertaService.obterAlertasNaoLidos("A001");
        assertEquals(1, alertas.size());
        assertEquals(0.0, alertas.get(0).getValorAtual());
    }

    // ==================== Testes de Alerta por Frequência Baixa ====================

    @Test
    void deveGerarAlertaQuandoFrequenciaAbaixo75Porcento() {
        frequenciaService.registrarPresenca(turmaCC101.getCodigo(), "A001", dia1);
        frequenciaService.registrarFalta(turmaCC101.getCodigo(), "A001", dia2);
        frequenciaService.registrarFalta(turmaCC101.getCodigo(), "A001", dia3);

        FrequenciaDisciplina frequencia =
                consultaFrequenciaService.consultarPorDisciplina("A001", "CC101");

        alertaService.verificarEGerarAlertaDeFrequencia("A001", frequencia);
        
        List<Alerta> alertas = alertaService.obterAlertasNaoLidos("A001");
        assertEquals(1, alertas.size());
        assertEquals(Alerta.TipoAlerta.FREQUENCIA_BAIXA, alertas.get(0).getTipo());
        assertEquals(33.333, alertas.get(0).getValorAtual(), 0.1);
        assertEquals(75.0, alertas.get(0).getLimiteMinimo());
    }

    @Test
    void naoDeveGerarAlertaQuandoFrequenciaEhIgualOuMaiorQue75Porcento() {
        frequenciaService.registrarPresenca(turmaCC101.getCodigo(), "A001", dia1);
        frequenciaService.registrarPresenca(turmaCC101.getCodigo(), "A001", dia2);
        frequenciaService.registrarPresenca(turmaCC101.getCodigo(), "A001", dia3);
        frequenciaService.registrarFalta(turmaCC101.getCodigo(), "A001", dia4);

        FrequenciaDisciplina frequencia =
                consultaFrequenciaService.consultarPorDisciplina("A001", "CC101");

        alertaService.verificarEGerarAlertaDeFrequencia("A001", frequencia);
        
        List<Alerta> alertas = alertaService.obterAlertasNaoLidos("A001");
        assertEquals(0, alertas.size());
    }

    @Test
    void deveGerarAlertaFrequenciaZero() {
        frequenciaService.registrarFalta(turmaCC101.getCodigo(), "A001", dia1);
        frequenciaService.registrarFalta(turmaCC101.getCodigo(), "A001", dia2);
        frequenciaService.registrarFalta(turmaCC101.getCodigo(), "A001", dia3);

        FrequenciaDisciplina frequencia =
                consultaFrequenciaService.consultarPorDisciplina("A001", "CC101");

        alertaService.verificarEGerarAlertaDeFrequencia("A001", frequencia);
        
        List<Alerta> alertas = alertaService.obterAlertasNaoLidos("A001");
        assertEquals(1, alertas.size());
        assertEquals(0.0, alertas.get(0).getValorAtual());
    }

    // ==================== Testes de Consulta e Marcação ====================

    @Test
    void deveConsultarApenasAlertasDoAlunoEspecifico() {
        alertaService.verificarEGerarAlertaDeNota("A001", turmaCC101.getCodigo(), "CC101", 5.0);
        alertaService.verificarEGerarAlertaDeNota("A002", turmaCC101.getCodigo(), "CC101", 6.0);
        
        List<Alerta> alertasA001 = alertaService.obterAlertasNaoLidos("A001");
        List<Alerta> alertasA002 = alertaService.obterAlertasNaoLidos("A002");
        
        assertEquals(1, alertasA001.size());
        assertEquals(1, alertasA002.size());
        assertEquals("A001", alertasA001.get(0).getMatriculaAluno());
        assertEquals("A002", alertasA002.get(0).getMatriculaAluno());
    }

    @Test
    void deveMarcarAlertaComoLido() {
        alertaService.verificarEGerarAlertaDeNota("A001", turmaCC101.getCodigo(), "CC101", 5.0);
        
        List<Alerta> alertasAntes = alertaService.obterAlertasNaoLidos("A001");
        assertEquals(1, alertasAntes.size());
        assertFalse(alertasAntes.get(0).isLido());
        
        String idAlerta = alertasAntes.get(0).getId();
        alertaService.marcarAlertoComoLido(idAlerta);
        
        List<Alerta> alertasDepois = alertaService.obterAlertasNaoLidos("A001");
        assertEquals(0, alertasDepois.size());
    }

    @Test
    void deveObterTodosOsAlertasIncluindoLidos() {
        alertaService.verificarEGerarAlertaDeNota("A001", turmaCC101.getCodigo(), "CC101", 5.0);
        alertaService.verificarEGerarAlertaDeNota("A001", turmaMAT101.getCodigo(), "MAT101", 6.0);
        
        String idAlerta1 = alertaService.obterAlertasNaoLidos("A001").get(0).getId();
        alertaService.marcarAlertoComoLido(idAlerta1);
        
        List<Alerta> todosAlertas = alertaService.obterTodosOsAlertas("A001");
        assertEquals(2, todosAlertas.size());
        
        int lidos = (int) todosAlertas.stream().filter(Alerta::isLido).count();
        assertEquals(1, lidos);
    }

    @Test
    void deveObterAlertasPorTipo() {
        alertaService.verificarEGerarAlertaDeNota("A001", turmaCC101.getCodigo(), "CC101", 5.0);
        
        frequenciaService.registrarFalta(turmaCC101.getCodigo(), "A001", dia1);
        frequenciaService.registrarFalta(turmaCC101.getCodigo(), "A001", dia2);
        FrequenciaDisciplina freq = consultaFrequenciaService.consultarPorDisciplina("A001", "CC101");
        alertaService.verificarEGerarAlertaDeFrequencia("A001", freq);
        
        List<Alerta> alertasNota = alertaService.obterAlertasPorTipo("A001", Alerta.TipoAlerta.NOTA_BAIXA);
        List<Alerta> alertasFreq = alertaService.obterAlertasPorTipo("A001", Alerta.TipoAlerta.FREQUENCIA_BAIXA);
        
        assertEquals(1, alertasNota.size());
        assertEquals(1, alertasFreq.size());
    }

    @Test
    void deveContarAlertasNaoLidos() {
        alertaService.verificarEGerarAlertaDeNota("A001", turmaCC101.getCodigo(), "CC101", 5.0);
        alertaService.verificarEGerarAlertaDeNota("A001", turmaMAT101.getCodigo(), "MAT101", 6.0);
        
        assertEquals(2, alertaService.contarAlertasNaoLidos("A001"));
        
        String idAlerta = alertaService.obterAlertasNaoLidos("A001").get(0).getId();
        alertaService.marcarAlertoComoLido(idAlerta);
        
        assertEquals(1, alertaService.contarAlertasNaoLidos("A001"));
    }

    @Test
    void deveGerarResumoDeSituacao() {
        String resumoVazio = alertaService.obterResumoDeSituacao("A001");
        assertEquals("Sem alertas de situação crítica.", resumoVazio);
        
        alertaService.verificarEGerarAlertaDeNota("A001", turmaCC101.getCodigo(), "CC101", 5.0);
        alertaService.verificarEGerarAlertaDeNota("A001", turmaMAT101.getCodigo(), "MAT101", 6.0);
        
        frequenciaService.registrarFalta(turmaCC101.getCodigo(), "A001", dia1);
        frequenciaService.registrarFalta(turmaCC101.getCodigo(), "A001", dia2);
        FrequenciaDisciplina freq = consultaFrequenciaService.consultarPorDisciplina("A001", "CC101");
        alertaService.verificarEGerarAlertaDeFrequencia("A001", freq);
        
        String resumoComAlertas = alertaService.obterResumoDeSituacao("A001");
        assertTrue(resumoComAlertas.contains("2 disciplina(s) com nota(s) baixa(s)"));
        assertTrue(resumoComAlertas.contains("1 disciplina(s) com frequência insuficiente"));
    }

    // ==================== Testes de Validação ====================

    @Test
    void deveLancarExcecaoQuandoAlunoNaoExiste() {
        assertThrows(AlunoNaoEncontradoException.class, () ->
            alertaService.verificarEGerarAlertaDeNota("A999", turmaCC101.getCodigo(), "CC101", 5.0)
        );
    }

    @Test
    void deveLancarExcecaoAoConsultarAlunoInexistente() {
        assertThrows(AlunoNaoEncontradoException.class, () ->
            alertaService.obterAlertasNaoLidos("A999")
        );
    }

    @Test
    void deveLancarExcecaoAoMarcarAlertaInexistente() {
        assertThrows(IllegalArgumentException.class, () ->
            alertaService.marcarAlertoComoLido("ALERTA_INEXISTENTE")
        );
    }

    @Test
    void deveDescricaoAlertaSerAmigavel() {
        alertaService.verificarEGerarAlertaDeNota("A001", turmaCC101.getCodigo(), "CC101", 5.0);
        
        List<Alerta> alertas = alertaService.obterAlertasNaoLidos("A001");
        assertEquals(1, alertas.size(), "Deve haver 1 alerta não lido");
        
        Alerta alerta = alertas.get(0);
        assertFalse(alerta.isLido(), "Alerta deve estar não lido");
        assertEquals(Alerta.TipoAlerta.NOTA_BAIXA, alerta.getTipo(), "Tipo deve ser NOTA_BAIXA");
        assertEquals(5.0, alerta.getValorAtual(), "Valor atual deve ser 5.0");
        
        String descricao = alerta.getDescricao();
        assertFalse(descricao == null || descricao.isEmpty(), "Descrição não pode ser vazia");
        assertTrue(descricao.contains("CC101"), "Descrição deve conter o código da disciplina CC101");
    }
}
