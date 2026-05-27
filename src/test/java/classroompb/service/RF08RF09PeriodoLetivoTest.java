package classroompb.service;

import org.example.classroompb.model.PeriodoLetivo;
import org.example.classroompb.model.StatusPeriodoLetivo;
import org.example.classroompb.repository.PeriodoLetivoRepository;
import org.example.classroompb.service.PeriodoLetivoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RF08RF09PeriodoLetivoTest {

    @TempDir
    Path tempDir;

    private Path arquivoJson;
    private PeriodoLetivoRepository repository;
    private PeriodoLetivoService service;

    @BeforeEach
    void setUp() {
        arquivoJson = tempDir.resolve("periodos-letivos.json");
        repository = new PeriodoLetivoRepository(arquivoJson.toString());
        service = new PeriodoLetivoService(repository);
    }

    // RF08 - Cadastro de periodo letivo

    @Test
    void deveCadastrarPeriodoValidoComStatusInativo() {
        PeriodoLetivo periodo = service.cadastrar("2026.1");

        assertEquals("2026.1", periodo.getIdentificador());
        assertEquals(StatusPeriodoLetivo.INATIVO, periodo.getStatus());
        assertEquals(1, service.listarTodos().size());
    }

    @Test
    void deveRejeitarPeriodoDuplicado() {
        service.cadastrar("2026.1");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                service.cadastrar("2026.1"));

        assertTrue(ex.getMessage().toLowerCase().contains("ja existe"));
    }

    @Test
    void deveRejeitarPeriodoDuplicadoComCaseInsensitive() {
        service.cadastrar("2026.1");

        assertThrows(IllegalArgumentException.class, () ->
                service.cadastrar("2026.1"));
    }

    @Test
    void deveRejeitarIdentificadorNulo() {
        assertThrows(IllegalArgumentException.class, () ->
                service.cadastrar(null));
    }

    @Test
    void deveRejeitarIdentificadorVazio() {
        assertThrows(IllegalArgumentException.class, () ->
                service.cadastrar(""));
    }

    @Test
    void deveRejeitarIdentificadorApenasEspacos() {
        assertThrows(IllegalArgumentException.class, () ->
                service.cadastrar("   "));
    }

    @Test
    void deveRejeitarIdentificadorForaDoFormato() {
        assertThrows(IllegalArgumentException.class, () ->
                service.cadastrar("2026"));
        assertThrows(IllegalArgumentException.class, () ->
                service.cadastrar("26.1"));
        assertThrows(IllegalArgumentException.class, () ->
                service.cadastrar("2026.3"));
        assertThrows(IllegalArgumentException.class, () ->
                service.cadastrar("abcd.1"));
    }

    @Test
    void deveBuscarPeriodoPorIdentificador() {
        service.cadastrar("2026.2");

        PeriodoLetivo periodo = service.buscarPorIdentificador("2026.2");

        assertNotNull(periodo);
        assertEquals("2026.2", periodo.getIdentificador());
    }

    @Test
    void buscarPorIdentificadorRetornaNuloQuandoNaoExiste() {
        assertNull(service.buscarPorIdentificador("2026.1"));
    }

    @Test
    void buscarPorIdentificadorRetornaNuloQuandoIdentificadorENulo() {
        assertNull(service.buscarPorIdentificador(null));
    }

    @Test
    void jaExisteRetornaTrueParaPeriodoCadastrado() {
        service.cadastrar("2026.1");

        assertTrue(service.jaExiste("2026.1"));
    }

    @Test
    void jaExisteRetornaFalseParaPeriodoNaoCadastrado() {
        assertFalse(service.jaExiste("2026.1"));
    }

    // RF09 - Ativar e encerrar periodo letivo

    @Test
    void deveAtivarPeriodoInativo() {
        service.cadastrar("2026.1");

        PeriodoLetivo periodo = service.ativar("2026.1");

        assertEquals(StatusPeriodoLetivo.ATIVO, periodo.getStatus());
    }

    @Test
    void deveManterSomenteUmPeriodoAtivoPorVez() {
        service.cadastrar("2026.1");
        service.cadastrar("2026.2");

        PeriodoLetivo primeiro = service.ativar("2026.1");
        PeriodoLetivo segundo = service.ativar("2026.2");

        assertEquals(StatusPeriodoLetivo.INATIVO, primeiro.getStatus());
        assertEquals(StatusPeriodoLetivo.ATIVO, segundo.getStatus());
    }

    @Test
    void deveEncerrarPeriodoAtivo() {
        service.cadastrar("2026.1");
        service.ativar("2026.1");

        PeriodoLetivo periodo = service.encerrar("2026.1");

        assertEquals(StatusPeriodoLetivo.ENCERRADO, periodo.getStatus());
    }

    @Test
    void deveRejeitarAtivarPeriodoInexistente() {
        assertThrows(IllegalArgumentException.class, () ->
                service.ativar("2026.1"));
    }

    @Test
    void deveRejeitarAtivarPeriodoJaAtivo() {
        service.cadastrar("2026.1");
        service.ativar("2026.1");

        assertThrows(IllegalArgumentException.class, () ->
                service.ativar("2026.1"));
    }

    @Test
    void deveRejeitarAtivarPeriodoEncerrado() {
        service.cadastrar("2026.1");
        service.ativar("2026.1");
        service.encerrar("2026.1");

        assertThrows(IllegalArgumentException.class, () ->
                service.ativar("2026.1"));
    }

    @Test
    void deveRejeitarEncerrarPeriodoInexistente() {
        assertThrows(IllegalArgumentException.class, () ->
                service.encerrar("2026.1"));
    }

    @Test
    void deveRejeitarEncerrarPeriodoInativo() {
        service.cadastrar("2026.1");

        assertThrows(IllegalArgumentException.class, () ->
                service.encerrar("2026.1"));
    }

    @Test
    void deveRejeitarEncerrarPeriodoJaEncerrado() {
        service.cadastrar("2026.1");
        service.ativar("2026.1");
        service.encerrar("2026.1");

        assertThrows(IllegalArgumentException.class, () ->
                service.encerrar("2026.1"));
    }

    // Persistencia em JSON

    @Test
    void deveSalvarECarregarPeriodosDoJson() {
        service.cadastrar("2026.1");
        service.ativar("2026.1");

        PeriodoLetivoService novoService = new PeriodoLetivoService(new PeriodoLetivoRepository(arquivoJson.toString()));
        PeriodoLetivo periodoCarregado = novoService.buscarPorIdentificador("2026.1");

        assertNotNull(periodoCarregado);
        assertEquals(StatusPeriodoLetivo.ATIVO, periodoCarregado.getStatus());
    }

    @Test
    void carregarTodosRetornaListaVaziaQuandoArquivoNaoExiste() {
        List<PeriodoLetivo> periodos = repository.carregarTodos();

        assertTrue(periodos.isEmpty());
    }

    @Test
    void carregarTodosRetornaListaVaziaQuandoArquivoEstaVazio() throws IOException {
        Files.writeString(arquivoJson, "");

        List<PeriodoLetivo> periodos = repository.carregarTodos();

        assertTrue(periodos.isEmpty());
    }

    @Test
    void carregarTodosRetornaListaVaziaQuandoJsonEstaCorrompido() throws IOException {
        Files.writeString(arquivoJson, "{ json invalido");

        List<PeriodoLetivo> periodos = repository.carregarTodos();

        assertTrue(periodos.isEmpty());
    }
}
