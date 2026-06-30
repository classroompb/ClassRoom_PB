package org.example.classroompb.exception;

/**
 * RF29: Lançada quando o código informado não corresponde a uma disciplina existente.
 */
public class DisciplinaNaoEncontradaException extends ConsultaFrequenciaException {

    private static final long serialVersionUID = 1L;

    public DisciplinaNaoEncontradaException(String codigoDisciplina) {
        super("Disciplina não encontrada: " + codigoDisciplina);
    }
}