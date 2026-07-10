package org.example.classroompb.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Avaliação de um aluno em uma turma (Release 3 / RF31+). Guarda as notas lançadas, a frequência e
 * a situação final.
 *
 * <p>Regras aplicáveis: RN07 (nota 0..10), RN08 (frequência mínima 75%), RN09/RN10/RN11 (faixas de
 * média) e RN12 (falta prevalece sobre a nota). O cálculo da média e da situação final é feito no
 * serviço (task D2).
 */
public class Avaliacao implements Serializable {

    private static final long serialVersionUID = 1L;

    private String matriculaAluno;
    private String codigoTurma;
    private List<Double> notas;
    private double frequencia; // percentual de presença, 0..100
    private SituacaoFinal situacao;
    private boolean fechada; // após fechada, não permite lançar/editar notas (task L2)

    public Avaliacao(String matriculaAluno, String codigoTurma) {
        this.matriculaAluno = matriculaAluno;
        this.codigoTurma = codigoTurma;
        this.notas = new ArrayList<>();
        this.frequencia = 0.0;
        this.situacao = SituacaoFinal.EM_ANDAMENTO;
        this.fechada = false;
    }

    // Getters
    public String getMatriculaAluno() {
        return matriculaAluno;
    }

    public String getCodigoTurma() {
        return codigoTurma;
    }

    public List<Double> getNotas() {
        return new ArrayList<>(notas);
    }

    public double getFrequencia() {
        return frequencia;
    }

    public SituacaoFinal getSituacao() {
        return situacao;
    }

    public boolean isFechada() {
        return fechada;
    }

    public void setFrequencia(double frequencia) {
        if (frequencia < 0 || frequencia > 100) {
            throw new IllegalArgumentException("Frequência deve estar entre 0 e 100.");
        }
        this.frequencia = frequencia;
    }

    public void setSituacao(SituacaoFinal situacao) {
        this.situacao = situacao;
    }

    /**
     * Fecha a avaliação: a partir daqui as notas não podem mais ser editadas (RF de edição — task
     * L2).
     */
    public void fechar() {
        this.fechada = true;
    }

    /**
     * Lança uma nota validando RN07 (0 a 10). Bloqueia se a avaliação já estiver fechada — base
     * para a task L2 (edição antes do fechamento).
     */
    public void adicionarNota(double nota) {
        if (fechada) {
            throw new IllegalArgumentException(
                    "Avaliação fechada — não é possível lançar/editar notas.");
        }
        if (nota < 0 || nota > 10) {
            throw new IllegalArgumentException("Nota deve estar entre 0 e 10. (RN07)");
        }
        notas.add(nota);
    }

    /** Altera uma nota específica no índice informado, validando RN07 e o fechamento. */
    public void alterarNota(int index, double novaNota) {
        if (fechada) {
            throw new IllegalArgumentException(
                    "Avaliação fechada — não é possível lançar/editar notas.");
        }
        if (novaNota < 0 || novaNota > 10) {
            throw new IllegalArgumentException("Nota deve estar entre 0 e 10. (RN07)");
        }
        if (index < 0 || index >= notas.size()) {
            throw new IllegalArgumentException("Índice de nota inválido.");
        }
        notas.set(index, novaNota);
    }

    /** Média aritmética simples das notas lançadas. Retorna 0 se não houver notas. */
    public double calcularMedia() {
        if (notas.isEmpty()) {
            return 0.0;
        }
        double soma = 0.0;
        for (double n : notas) {
            soma += n;
        }
        return soma / notas.size();
    }

    @Override
    public String toString() {
        return "Avaliacao{"
                + "aluno='"
                + matriculaAluno
                + '\''
                + ", turma='"
                + codigoTurma
                + '\''
                + ", notas="
                + notas
                + ", frequencia="
                + frequencia
                + "%"
                + ", situacao="
                + situacao
                + ", fechada="
                + fechada
                + '}';
    }
}
