package org.example.classroompb.model;

/**
 * Situação final do aluno em uma turma (Release 3).
 * Definida a partir da média (RN09/RN10/RN11) e da frequência (RN08/RN12).
 * REPROVADO_POR_FALTA prevalece sobre a nota (RN12).
 */
public enum SituacaoFinal {
    EM_ANDAMENTO,
    APROVADO,
    RECUPERACAO,
    REPROVADO_POR_NOTA,
    REPROVADO_POR_FALTA
}
