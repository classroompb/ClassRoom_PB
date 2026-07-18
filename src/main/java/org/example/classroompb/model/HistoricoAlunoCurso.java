package org.example.classroompb.model;

import java.util.List;

/**
 * RF39: Representa o histórico acadêmico de um aluno dentro da consulta que o coordenador faz
 * para todos os alunos de um curso — agrupa a identificação do aluno com os itens de histórico já
 * usados no RF37 ({@link ItemHistoricoAcademico}).
 */
public record HistoricoAlunoCurso(
        String matriculaAluno, String nomeAluno, List<ItemHistoricoAcademico> itens) {

    @Override
    public String toString() {
        return nomeAluno + " (" + matriculaAluno + ") | " + itens.size() + " item(ns)";
    }
}