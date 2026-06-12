package org.example.classroompb.model;

/**
 * RF26: Representa uma entrada da lista de espera de uma turma, usada para que
 * o coordenador possa visualizar a fila com posição, matrícula e nome do aluno.
 *
 * A posição é 1-indexed e reflete a ordem de solicitação (RF25).
 */
public record ItemListaEspera(int posicao, String matriculaAluno, String nomeAluno) {

    @Override
    public String toString() {
        return posicao + "º - " + nomeAluno + " (" + matriculaAluno + ")";
    }
}