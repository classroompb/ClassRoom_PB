package classroompb.service;

import org.example.classroompb.model.PeriodoLetivo;
import org.example.classroompb.model.StatusPeriodoLetivo;
import org.example.classroompb.repository.PeriodoLetivoRepository;
import org.example.classroompb.service.PeriodoLetivoService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RF09: Gerenciamento de Períodos Letivos
 *
 * O coordenador deve poder gerenciar os períodos letivos,
 * incluindo ativação e encerramento.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RF09GerenciamentoPeriodoTest {

    @TempDir
    Path tempDir;

    private PeriodoLetivoService service;

    @BeforeEach
    void setUp() {
        Path arquivoJson = tempDir.resolve("periodos-letivos.json");
        PeriodoLetivoRepository repository = new PeriodoLetivoRepository(arquivoJson.toString());
        service = new PeriodoLetivoService(repository);
    }

    @Test
    @Order(1)
    void deveAtivarPeriodoInativo() {
        service.cadastrar("2026.1");

        PeriodoLetivo periodo = service.ativar("2026.1");

        assertEquals(StatusPeriodoLetivo.ATIVO, periodo.getStatus());
    }

    @Test
    @Order(2)
    void deveManterSomenteUmPeriodoAtivoPorVez() {
        service.cadastrar("2026.1");
        service.cadastrar("2026.2");

        PeriodoLetivo primeiro = service.ativar("2026.1");
        PeriodoLetivo segundo = service.ativar("2026.2");

        assertEquals(StatusPeriodoLetivo.INATIVO, primeiro.getStatus());
        assertEquals(StatusPeriodoLetivo.ATIVO, segundo.getStatus());
    }

    @Test
    @Order(3)
    void deveEncerrarPeriodoAtivo() {
        service.cadastrar("2026.1");
        service.ativar("2026.1");

        PeriodoLetivo periodo = service.encerrar("2026.1");

        assertEquals(StatusPeriodoLetivo.ENCERRADO, periodo.getStatus());
    }

    @Test
    @Order(4)
    void deveRejeitarAtivarPeriodoInexistente() {
        assertThrows(IllegalArgumentException.class, () ->
                service.ativar("2026.1"));
    }

    @Test
    @Order(5)
    void deveRejeitarAtivarPeriodoJaAtivo() {
        service.cadastrar("2026.1");
        service.ativar("2026.1");

        assertThrows(IllegalArgumentException.class, () ->
                service.ativar("2026.1"));
    }

    @Test
    @Order(6)
    void deveRejeitarAtivarPeriodoEncerrado() {
        service.cadastrar("2026.1");
        service.ativar("2026.1");
        service.encerrar("2026.1");

        assertThrows(IllegalArgumentException.class, () ->
                service.ativar("2026.1"));
    }

    @Test
    @Order(7)
    void deveRejeitarEncerrarPeriodoInexistente() {
        assertThrows(IllegalArgumentException.class, () ->
                service.encerrar("2026.1"));
    }

    @Test
    @Order(8)
    void deveRejeitarEncerrarPeriodoInativo() {
        service.cadastrar("2026.1");

        assertThrows(IllegalArgumentException.class, () ->
                service.encerrar("2026.1"));
    }

    @Test
    @Order(9)
    void deveRejeitarEncerrarPeriodoJaEncerrado() {
        service.cadastrar("2026.1");
        service.ativar("2026.1");
        service.encerrar("2026.1");

        assertThrows(IllegalArgumentException.class, () ->
                service.encerrar("2026.1"));
    }

    @Test
    @Order(10)
    void devePermitirAtivarOutroPeriodoAposEncerrar() {
        service.cadastrar("2026.1");
        service.cadastrar("2026.2");

        // Ativa o primeiro
        service.ativar("2026.1");
        // Encerra
        service.encerrar("2026.1");
        // Ativa o segundo
        PeriodoLetivo segundo = service.ativar("2026.2");

        assertEquals(StatusPeriodoLetivo.ATIVO, segundo.getStatus());
    }

    @Test
    @Order(11)
    void deveRetornarStatusAtualAoAtivar() {
        service.cadastrar("2026.1");

        PeriodoLetivo resultado = service.ativar("2026.1");

        assertNotNull(resultado);
        assertEquals(StatusPeriodoLetivo.ATIVO, resultado.getStatus());
    }

    @Test
    @Order(12)
    void deveRetornarStatusEncerradoAoEncerrar() {
        service.cadastrar("2026.1");
        service.ativar("2026.1");

        PeriodoLetivo resultado = service.encerrar("2026.1");

        assertNotNull(resultado);
        assertEquals(StatusPeriodoLetivo.ENCERRADO, resultado.getStatus());
    }

    @Test
    @Order(13)
    void devePersistirStatusAoAtivar() {
        service.cadastrar("2026.1");
        service.ativar("2026.1");

        // Busca após ativar
        PeriodoLetivo periodo = service.buscarPorIdentificador("2026.1");

        assertEquals(StatusPeriodoLetivo.ATIVO, periodo.getStatus());
    }

    @Test
    @Order(14)
    void devePersistirStatusAoEncerrar() {
        service.cadastrar("2026.1");
        service.ativar("2026.1");
        service.encerrar("2026.1");

        // Busca após encerrar
        PeriodoLetivo periodo = service.buscarPorIdentificador("2026.1");

        assertEquals(StatusPeriodoLetivo.ENCERRADO, periodo.getStatus());
    }

    @Test
    @Order(15)
    void deveRejeitarAttivarComIdentificadorNulo() {
        assertThrows(IllegalArgumentException.class, () ->
                service.ativar(null));
    }

    @Test
    @Order(16)
    void deveRejeitarEncerrarComIdentificadorNulo() {
        assertThrows(IllegalArgumentException.class, () ->
                service.encerrar(null));
    }

    @Test
    @Order(17)
    void deveRejeitarAttivarComIdentificadorVazio() {
        assertThrows(IllegalArgumentException.class, () ->
                service.ativar(""));
    }

    @Test
    @Order(18)
    void deveRejeitarEncerrarComIdentificadorVazio() {
        assertThrows(IllegalArgumentException.class, () ->
                service.encerrar(""));
    }

    @Test
    @Order(19)
    void deveListarTodosPeriodosComStatusAtualizados() {
        service.cadastrar("2026.1");
        service.cadastrar("2026.2");
        service.ativar("2026.1");

        var periodos = service.listarTodos();

        assertEquals(2, periodos.size());
        var ativo = periodos.stream().filter(p -> p.getStatus() == StatusPeriodoLetivo.ATIVO).findFirst();
        assertTrue(ativo.isPresent());
        assertEquals("2026.1", ativo.get().getIdentificador());
    }
}

