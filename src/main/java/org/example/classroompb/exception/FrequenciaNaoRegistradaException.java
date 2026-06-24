package org.example.classroompb.exception;

/**
 * RF29: Lançada quando o aluno está matriculado na disciplina, mas ainda não há
 * nenhum registro de frequência lançado pelo professor para ele.
 */
public class FrequenciaNaoRegistradaException extends ConsultaFrequenciaException {

    private static final long serialVersionUID = 1L;

    public FrequenciaNaoRegistradaException(String matriculaAluno, String codigoDisciplina) {
        super("Nenhum registro de frequência encontrado para o aluno " + matriculaAluno
                + " na disciplina " + codigoDisciplina + ".");
    }
}