package org.example.classroompb.dto;

/**
 * Linha do relatório de ocupação de vagas (RF41). Uma linha por turma, com quantas vagas existem,
 * quantas estão preenchidas, quantas sobraram e quantos alunos esperam na fila.
 */
public record OcupacaoVagasDTO(
        String codigoTurma,
        String nomeDisciplina,
        String nomeProfessor,
        String periodoLetivo,
        int limiteVagas,
        int matriculados,
        int vagasDisponiveis,
        int emEspera) {

    @Override
    public String toString() {
        return codigoTurma
                + " | "
                + nomeDisciplina
                + " | prof. "
                + nomeProfessor
                + " | "
                + periodoLetivo
                + " | vagas="
                + matriculados
                + "/"
                + limiteVagas
                + " (livres="
                + vagasDisponiveis
                + ")"
                + " | espera="
                + emEspera;
    }
}
