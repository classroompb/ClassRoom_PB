package org.example.classroompb.exception;

/**
 * RF29: Lançada quando o aluno não está matriculado em nenhuma turma da disciplina
 * consultada e, portanto, não possui frequência a ser exibida nela.
 */
public class AlunoNaoMatriculadoException extends ConsultaFrequenciaException {

    private static final long serialVersionUID = 1L;

    public AlunoNaoMatriculadoException(String matriculaAluno, String codigoDisciplina) {
        super("Aluno " + matriculaAluno + " não está matriculado na disciplina " + codigoDisciplina + ".");
    }
}