package org.example.classroompb.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * RF30: Modelo de alerta para situação crítica do aluno.
 *
 * <p>Representa um alerta gerado quando um aluno está abaixo do mínimo exigido em:
 * - Nota/Média (abaixo de 7.0)
 * - Frequência (abaixo de 75%)
 *
 * <p>O alerta é imutável após criação e contém informações sobre:
 * - Qual aluno está em situação crítica
 * - Qual disciplina/turma está afetada
 * - Tipo de alerta (NOTA_BAIXA ou FREQUENCIA_BAIXA)
 * - Valor atual (nota ou percentual)
 * - Quando foi gerado
 */
public final class Alerta implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum TipoAlerta {
        NOTA_BAIXA,
        FREQUENCIA_BAIXA
    }

    private final String id;
    private final String matriculaAluno;
    private final String codigoTurma;
    private final String codigoDisciplina;
    private final TipoAlerta tipo;
    private final double valorAtual;
    private final double limiteMinimo;
    private final LocalDateTime dataCriacao;
    private boolean lido;

    public Alerta(String matriculaAluno, String codigoTurma, String codigoDisciplina,
                  TipoAlerta tipo, double valorAtual, double limiteMinimo) {
        this.id = gerarId();
        this.matriculaAluno = matriculaAluno;
        this.codigoTurma = codigoTurma;
        this.codigoDisciplina = codigoDisciplina;
        this.tipo = tipo;
        this.valorAtual = valorAtual;
        this.limiteMinimo = limiteMinimo;
        this.dataCriacao = LocalDateTime.now();
        this.lido = false;
    }

    private static String gerarId() {
        return "ALERTA_" + System.currentTimeMillis();
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getMatriculaAluno() {
        return matriculaAluno;
    }

    public String getCodigoTurma() {
        return codigoTurma;
    }

    public String getCodigoDisciplina() {
        return codigoDisciplina;
    }

    public TipoAlerta getTipo() {
        return tipo;
    }

    public double getValorAtual() {
        return valorAtual;
    }

    public double getLimiteMinimo() {
        return limiteMinimo;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public boolean isLido() {
        return lido;
    }

    public void marcarComoLido() {
        this.lido = true;
    }

    /**
     * Retorna uma descrição amigável do alerta para exibição ao aluno.
     */
    public String getDescricao() {
        if (tipo != null && tipo == TipoAlerta.NOTA_BAIXA) {
            return String.format("Nota baixa: %.1f (mínimo: %.1f) em %s (Turma: %s)",
                    valorAtual, limiteMinimo, codigoDisciplina, codigoTurma);
        } else if (tipo != null && tipo == TipoAlerta.FREQUENCIA_BAIXA) {
            return String.format("Frequência insuficiente: %.1f%% (mínimo: %.1f%%) em %s (Turma: %s)",
                    valorAtual, limiteMinimo, codigoDisciplina, codigoTurma);
        } else {
            return "Alerta de situação crítica";
        }
    }

    @Override
    public String toString() {
        return "Alerta{" +
                "id='" + id + '\'' +
                ", matriculaAluno='" + matriculaAluno + '\'' +
                ", codigoTurma='" + codigoTurma + '\'' +
                ", codigoDisciplina='" + codigoDisciplina + '\'' +
                ", tipo=" + tipo +
                ", valorAtual=" + valorAtual +
                ", limiteMinimo=" + limiteMinimo +
                ", dataCriacao=" + dataCriacao +
                ", lido=" + lido +
                '}';
    }
}
