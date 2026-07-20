package org.example.classroompb.dto;

/**
 * Linha do relatório de reprovação por disciplina (RF42). Agrupa as avaliações já finalizadas de
 * uma disciplina e separa quem reprovou por nota (RN11) de quem reprovou por falta (RN12).
 *
 * <p>{@code totalAvaliados} conta só as avaliações com situação definida (não entra quem ainda está
 * EM_ANDAMENTO). A taxa de reprovação é o percentual de reprovados sobre esse total.
 */
public record ReprovacaoDisciplinaDTO(
        String codigoDisciplina,
        String nomeDisciplina,
        int totalAvaliados,
        int reprovadosPorNota,
        int reprovadosPorFalta,
        int totalReprovados,
        double taxaReprovacao) {

    @Override
    public String toString() {
        return codigoDisciplina
                + " - "
                + nomeDisciplina
                + " | avaliados="
                + totalAvaliados
                + " | reprovados="
                + totalReprovados
                + " (nota="
                + reprovadosPorNota
                + ", falta="
                + reprovadosPorFalta
                + ")"
                + " | taxa="
                + String.format("%.1f", taxaReprovacao)
                + "%";
    }
}
