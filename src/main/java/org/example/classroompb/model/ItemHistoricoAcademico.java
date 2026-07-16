package org.example.classroompb.model;

import java.util.List;

/**
 * RF37 (task D1): Representa uma entrada do histórico acadêmico de um aluno — uma disciplina
 * cursada em um determinado período letivo, com as notas lançadas, a média (RF32), a frequência e a
 * situação final (RF33).
 *
 * <p>É a unidade usada para montar o histórico completo do aluno (RF37), agrupando as disciplinas
 * cursadas por período letivo.
 */
public record ItemHistoricoAcademico(
        String periodoLetivo,
        String codigoDisciplina,
        String nomeDisciplina,
        String codigoTurma,
        List<Double> notas,
        double media,
        double frequencia,
        SituacaoFinal situacao) {

    @Override
    public String toString() {
        return periodoLetivo
                + " | "
                + codigoDisciplina
                + " - "
                + nomeDisciplina
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
