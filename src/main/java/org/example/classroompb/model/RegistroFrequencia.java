package org.example.classroompb.model;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * RF27: Registro de presença/falta de um aluno em uma turma numa data de aula.
 *
 * <p>Cada instância representa a chamada de um único aluno em um único dia. O
 * professor marca {@code presente = true} (presença) ou {@code presente = false}
 * (falta). A chave lógica é a tripla (turma, aluno, data) — não há dois registros
 * para o mesmo aluno na mesma turma e data.
 */
public class RegistroFrequencia implements Serializable {

    private static final long serialVersionUID = 1L;

    private String codigoTurma;
    private String matriculaAluno;
    private LocalDate data;
    private boolean presente;

    public RegistroFrequencia(String codigoTurma, String matriculaAluno, LocalDate data, boolean presente) {
        this.codigoTurma = codigoTurma;
        this.matriculaAluno = matriculaAluno;
        this.data = data;
        this.presente = presente;
    }

    public String getCodigoTurma() { return codigoTurma; }
    public String getMatriculaAluno() { return matriculaAluno; }
    public LocalDate getData() { return data; }
    public boolean isPresente() { return presente; }

    /** Indica se este registro é uma falta (oposto de presença). */
    public boolean isFalta() { return !presente; }

    @Override
    public String toString() {
        return "RegistroFrequencia{"
                + "turma='" + codigoTurma + '\''
                + ", aluno='" + matriculaAluno + '\''
                + ", data=" + data
                + ", status=" + (presente ? "PRESENTE" : "FALTA")
                + '}';
    }
}
