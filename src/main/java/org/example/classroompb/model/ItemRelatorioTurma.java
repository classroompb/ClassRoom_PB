package org.example.classroompb.model;

import java.util.List;

public record ItemRelatorioTurma(
        String matriculaAluno,
        List<Double> notas,
        double media,
        double frequencia,
        SituacaoFinal situacao) {

    @Override
    public String toString() {
        return matriculaAluno
                + " | notas="
                + notas
                + " | média="
                + String.format("%.2f", media)
                + " | frequência="
                + String.format("%.1f", frequencia)
                + "%"
                + " | situação="
                + situacao;
    }
}
