package org.example.classroompb.service;

import java.util.List;
import org.example.classroompb.model.PeriodoLetivo;
import org.example.classroompb.model.StatusPeriodoLetivo;
import org.example.classroompb.repository.PeriodoLetivoRepository;

public class PeriodoLetivoService {

    private final PeriodoLetivoRepository repository;
    private List<PeriodoLetivo> periodos;

    public PeriodoLetivoService(PeriodoLetivoRepository repository) {
        this.repository = repository;
        this.periodos = repository.carregarTodos();
    }

    // RF08 - Coordenador cadastra periodos letivos
    public PeriodoLetivo cadastrar(String identificador) {
        validarIdentificador(identificador);

        if (jaExiste(identificador)) {
            throw new IllegalArgumentException(
                    "Ja existe um periodo letivo com o identificador: " + identificador);
        }

        PeriodoLetivo periodo = new PeriodoLetivo(identificador);
        periodos.add(periodo);
        repository.salvarTodos(periodos);
        return periodo;
    }

    // RF09 - Ativar periodo letivo
    public PeriodoLetivo ativar(String identificador) {
        PeriodoLetivo periodo = buscarPorIdentificador(identificador);

        if (periodo == null) {
            throw new IllegalArgumentException("Periodo letivo nao encontrado: " + identificador);
        }

        if (periodo.getStatus() == StatusPeriodoLetivo.ENCERRADO) {
            throw new IllegalArgumentException("Periodo letivo encerrado nao pode ser ativado.");
        }

        if (periodo.getStatus() == StatusPeriodoLetivo.ATIVO) {
            throw new IllegalArgumentException("Periodo letivo ja esta ativo.");
        }

        for (PeriodoLetivo outro : periodos) {
            if (outro.getStatus() == StatusPeriodoLetivo.ATIVO) {
                outro.inativar();
            }
        }

        periodo.ativar();
        repository.salvarTodos(periodos);
        return periodo;
    }

    // RF09 - Encerrar periodo letivo
    public PeriodoLetivo encerrar(String identificador) {
        PeriodoLetivo periodo = buscarPorIdentificador(identificador);

        if (periodo == null) {
            throw new IllegalArgumentException("Periodo letivo nao encontrado: " + identificador);
        }

        if (periodo.getStatus() != StatusPeriodoLetivo.ATIVO) {
            throw new IllegalArgumentException("Apenas periodo letivo ativo pode ser encerrado.");
        }

        periodo.encerrar();
        repository.salvarTodos(periodos);
        return periodo;
    }

    public PeriodoLetivo buscarPorIdentificador(String identificador) {
        if (identificador == null) {
            return null;
        }

        return periodos.stream()
                .filter(p -> p.getIdentificador().equalsIgnoreCase(identificador))
                .findFirst()
                .orElse(null);
    }

    public boolean jaExiste(String identificador) {
        return buscarPorIdentificador(identificador) != null;
    }

    public List<PeriodoLetivo> listarTodos() {
        return periodos;
    }

    private void validarIdentificador(String identificador) {
        if (identificador == null || identificador.isBlank()) {
            throw new IllegalArgumentException(
                    "Identificador do periodo letivo nao pode ser vazio.");
        }

        if (!identificador.matches("\\d{4}\\.[12]")) {
            throw new IllegalArgumentException(
                    "Identificador deve seguir o formato YYYY.N, como 2026.1 ou 2026.2.");
        }
    }
}
