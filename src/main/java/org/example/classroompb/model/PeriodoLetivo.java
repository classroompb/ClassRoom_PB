package org.example.classroompb.model;

import java.io.Serializable;

public class PeriodoLetivo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String identificador;
    private StatusPeriodoLetivo status;

    public PeriodoLetivo(String identificador) {
        this.identificador = identificador;
        this.status = StatusPeriodoLetivo.INATIVO;
    }

    public String getIdentificador() {
        return identificador;
    }

    public StatusPeriodoLetivo getStatus() {
        return status;
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

    @Override
    public String toString() {
        return identificador + " - " + status;
    }
}
