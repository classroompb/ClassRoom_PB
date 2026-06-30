package org.example.classroompb.service;

import org.example.classroompb.exception.AlunoNaoEncontradoException;
import org.example.classroompb.exception.AlunoNaoMatriculadoException;
import org.example.classroompb.exception.DisciplinaNaoEncontradaException;
import org.example.classroompb.exception.FrequenciaNaoRegistradaException;
import org.example.classroompb.model.Aluno;
import org.example.classroompb.model.Disciplina;
import org.example.classroompb.model.FrequenciaDisciplina;
import org.example.classroompb.model.RegistroFrequencia;
import org.example.classroompb.model.Turma;
import org.example.classroompb.model.Usuario;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * RF29: O aluno deve poder consultar sua frequência por disciplina.
 *
 * <p>Reúne, a partir dos registros já lançados pelo professor (RF27) e do cálculo
 * de percentual existente (RF28), a frequência do aluno em uma disciplina. Como uma
 * disciplina pode ser ofertada em mais de uma turma (e o aluno pode estar em mais de
 * uma delas em períodos distintos), os registros de todas as turmas da disciplina em
 * que o aluno está matriculado são agregados em um único {@link FrequenciaDisciplina}.
 *
 * <p>Este service é apenas de leitura: não cria, altera ou remove registros, e por
 * isso não recebe repositório próprio. Ele reaproveita {@link FrequenciaService},
 * {@link TurmaService} e {@link DisciplinaService}, mantendo a arquitetura em camadas
 * e sem alterar as funcionalidades já existentes (RF27/RF28).
 */
public class ConsultaFrequenciaService {

    private final FrequenciaService frequenciaService;
    private final TurmaService turmaService;
    private final DisciplinaService disciplinaService;
    private final UsuarioService usuarioService;

    public ConsultaFrequenciaService(FrequenciaService frequenciaService, TurmaService turmaService,
            DisciplinaService disciplinaService, UsuarioService usuarioService) {
        this.frequenciaService = frequenciaService;
        this.turmaService = turmaService;
        this.disciplinaService = disciplinaService;
        this.usuarioService = usuarioService;
    }

    /**
     * RF29: Consulta a frequência de um aluno em uma disciplina específica.
     *
     * @param matriculaAluno  matrícula do aluno.
     * @param codigoDisciplina código da disciplina.
     * @return resumo da frequência do aluno na disciplina.
     * @throws AlunoNaoEncontradoException     se a matrícula não for de um aluno existente.
     * @throws DisciplinaNaoEncontradaException se a disciplina não existir.
     * @throws AlunoNaoMatriculadoException     se o aluno não estiver em nenhuma turma da disciplina.
     * @throws FrequenciaNaoRegistradaException se ainda não houver registros de frequência.
     */
    public FrequenciaDisciplina consultarPorDisciplina(String matriculaAluno, String codigoDisciplina) {
        validarMatricula(matriculaAluno);
        if (codigoDisciplina == null || codigoDisciplina.isBlank()) {
            throw new IllegalArgumentException("Código da disciplina não pode ser vazio.");
        }

        Aluno aluno = obterAluno(matriculaAluno);

        Disciplina disciplina = disciplinaService.buscarPorCodigo(codigoDisciplina);
        if (disciplina == null) {
            throw new DisciplinaNaoEncontradaException(codigoDisciplina);
        }

        List<Turma> turmasDoAluno = turmaService.listarTurmasPorDisciplina(disciplina.getCodigo()).stream()
                .filter(t -> t.alunoJaMatriculado(aluno.getMatricula()))
                .collect(Collectors.toList());

        if (turmasDoAluno.isEmpty()) {
            throw new AlunoNaoMatriculadoException(aluno.getMatricula(), disciplina.getCodigo());
        }

        List<String> codigosTurmas = new ArrayList<>();
        int totalAulas = 0;
        int totalPresencas = 0;
        for (Turma turma : turmasDoAluno) {
            List<RegistroFrequencia> registros =
                    frequenciaService.listarPorAluno(turma.getCodigo(), aluno.getMatricula());
            if (registros.isEmpty()) {
                continue;
            }
            codigosTurmas.add(turma.getCodigo());
            totalAulas += registros.size();
            totalPresencas += (int) registros.stream().filter(RegistroFrequencia::isPresente).count();
        }

        if (totalAulas == 0) {
            throw new FrequenciaNaoRegistradaException(aluno.getMatricula(), disciplina.getCodigo());
        }

        return new FrequenciaDisciplina(disciplina.getCodigo(), disciplina.getNome(),
                codigosTurmas, totalPresencas, totalAulas);
    }

    /**
     * RF29: Consulta a frequência do aluno em todas as disciplinas em que está
     * matriculado e que já possuem registros de frequência.
     *
     * @param matriculaAluno matrícula do aluno.
     * @return lista de frequências por disciplina (vazia se nada houver a exibir).
     * @throws AlunoNaoEncontradoException se a matrícula não for de um aluno existente.
     */
    public List<FrequenciaDisciplina> consultarTodasAsDisciplinas(String matriculaAluno) {
        validarMatricula(matriculaAluno);
        Aluno aluno = obterAluno(matriculaAluno);

        List<String> codigosDisciplinas = turmaService.listarTodasAsTurmas().stream()
                .filter(t -> t.alunoJaMatriculado(aluno.getMatricula()))
                .map(t -> t.getDisciplina().getCodigo())
                .distinct()
                .collect(Collectors.toList());

        List<FrequenciaDisciplina> resultado = new ArrayList<>();
        for (String codigoDisciplina : codigosDisciplinas) {
            try {
                resultado.add(consultarPorDisciplina(aluno.getMatricula(), codigoDisciplina));
            } catch (FrequenciaNaoRegistradaException ignorada) {
                // Disciplina sem registros ainda não compõe o relatório consolidado.
            }
        }
        return resultado;
    }

    private void validarMatricula(String matriculaAluno) {
        if (matriculaAluno == null || matriculaAluno.isBlank()) {
            throw new IllegalArgumentException("Matrícula do aluno não pode ser vazia.");
        }
    }

    private Aluno obterAluno(String matriculaAluno) {
        Usuario usuario = usuarioService.buscarPorMatricula(matriculaAluno);
        if (!(usuario instanceof Aluno)) {
            throw new AlunoNaoEncontradoException(matriculaAluno);
        }
        return (Aluno) usuario;
    }
}