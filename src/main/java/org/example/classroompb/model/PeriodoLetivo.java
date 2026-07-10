package org.example.classroompb.model;

import java.io.Serializable;
import java.time.LocalDate;

public class PeriodoLetivo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String identificador;
    private StatusPeriodoLetivo status;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private LocalDate dataLimiteCancelamento; // RF22: Data limite para cancelamento

    public PeriodoLetivo(String identificador) {
        this.identificador = identificador;
        this.status = StatusPeriodoLetivo.INATIVO;
        this.dataInicio = null;
        this.dataFim = null;
        this.dataLimiteCancelamento = null;
    }

    public PeriodoLetivo(
            String identificador,
            LocalDate dataInicio,
            LocalDate dataFim,
            LocalDate dataLimiteCancelamento) {
        this.identificador = identificador;
        this.status = StatusPeriodoLetivo.INATIVO;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.dataLimiteCancelamento = dataLimiteCancelamento;
    }

    public String getIdentificador() {
        return identificador;
    }

    public StatusPeriodoLetivo getStatus() {
        return status;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public LocalDate getDataFim() {
        return dataFim;
    }

    public LocalDate getDataLimiteCancelamento() {
        return dataLimiteCancelamento;
    }

    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }

    public void setDataFim(LocalDate dataFim) {
        this.dataFim = dataFim;
    }

    public void setDataLimiteCancelamento(LocalDate dataLimiteCancelamento) {
        this.dataLimiteCancelamento = dataLimiteCancelamento;
    }

    public void ativar() {
        this.status = StatusPeriodoLetivo.ATIVO;
    }

    public void inativar() {
        this.status = StatusPeriodoLetivo.INATIVO;
    }

    public void encerrar() {
        this.status = StatusPeriodoLetivo.ENCERRADO;
    }

    /** RF22: Verifica se cancelamento está permitido (dentro do prazo) */
    public boolean permiteCancelamento() {
        if (dataLimiteCancelamento == null) {
            return true; // Se não há limite, permite
        }
        return LocalDate.now().isBefore(dataLimiteCancelamento)
                || LocalDate.now().equals(dataLimiteCancelamento);
    }

    @Override
    public String toString() {
        return identificador + " - " + status;
    }
}
