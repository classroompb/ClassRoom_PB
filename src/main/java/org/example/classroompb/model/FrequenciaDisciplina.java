package org.example.classroompb.model;

import java.util.ArrayList;
import java.util.List;

/**
 * RF29: Resultado da consulta de frequência de um aluno em uma disciplina.
 *
 * <p>Agrega os registros de frequência do aluno em todas as turmas da disciplina
 * em que ele está matriculado e expõe os totais já calculados (presenças, faltas e
 * percentual). É um objeto imutável de leitura, montado pelo
 * {@code ConsultaFrequenciaService} — não há regras de escrita aqui.
 *
 * <p>O método {@link #atingiuFrequenciaMinima()} apenas reflete a RN08 (frequência
 * mínima de 75%) para informação ao aluno; não altera nenhuma regra de aprovação.
 */
public final class FrequenciaDisciplina {

    /** RN08: frequência mínima para aprovação é de 75%. */
    public static final double FREQUENCIA_MINIMA = 75.0;

    private final String codigoDisciplina;
    private final String nomeDisciplina;
    private final List<String> codigosTurmas;
    private final int totalAulas;
    private final int totalPresencas;
    private final int totalFaltas;
    private final double percentualFrequencia;

    public FrequenciaDisciplina(String codigoDisciplina, String nomeDisciplina,
            List<String> codigosTurmas, int totalPresencas, int totalAulas) {
        if (totalAulas <= 0) {
            throw new IllegalArgumentException("Total de aulas deve ser maior que zero.");
        }
        if (totalPresencas < 0 || totalPresencas > totalAulas) {
            throw new IllegalArgumentException("Total de presenças inconsistente com o total de aulas.");
        }
        this.codigoDisciplina = codigoDisciplina;
        this.nomeDisciplina = nomeDisciplina;
        this.codigosTurmas = List.copyOf(codigosTurmas);
        this.totalAulas = totalAulas;
        this.totalPresencas = totalPresencas;
        this.totalFaltas = totalAulas - totalPresencas;
        this.percentualFrequencia = (totalPresencas * 100.0) / totalAulas;
    }

    public String getCodigoDisciplina() {
        return codigoDisciplina;
    }

    public String getNomeDisciplina() {
        return nomeDisciplina;
    }

    public List<String> getCodigosTurmas() {
        return new ArrayList<>(codigosTurmas);
    }

    public int getTotalAulas() {
        return totalAulas;
    }

    public int getTotalPresencas() {
        return totalPresencas;
    }

    public int getTotalFaltas() {
        return totalFaltas;
    }

    public double getPercentualFrequencia() {
        return percentualFrequencia;
    }

    /** RN08: indica se o aluno atinge a frequência mínima de 75% na disciplina. */
    public boolean atingiuFrequenciaMinima() {
        return percentualFrequencia >= FREQUENCIA_MINIMA;
    }

    @Override
    public String toString() {
        return "Disciplina " + codigoDisciplina + " (" + nomeDisciplina + "): "
                + totalPresencas + "/" + totalAulas + " presenças"
                + " (" + String.format("%.1f", percentualFrequencia) + "%)"
                + " — " + (atingiuFrequenciaMinima() ? "frequência suficiente" : "frequência insuficiente");
    }
}