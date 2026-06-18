package org.example.classroompb.service;

import org.example.classroompb.model.Aluno;
import org.example.classroompb.model.RegistroFrequencia;
import org.example.classroompb.model.Turma;
import org.example.classroompb.model.Usuario;
import org.example.classroompb.repository.FrequenciaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * RF27: O professor deve poder registrar presença/falta do aluno.
 *
 * <p>Valida que a turma existe, que o aluno existe e está matriculado na turma, e
 * que ainda não há registro para aquele aluno naquela data, antes de gravar. Todos
 * os erros de negócio são sinalizados via {@link IllegalArgumentException}, no mesmo
 * padrão do restante do projeto.
 */
public class FrequenciaService {

    private final FrequenciaRepository repository;
    private final UsuarioService usuarioService;
    private final TurmaService turmaService;
    private List<RegistroFrequencia> registros;

    public FrequenciaService(FrequenciaRepository repository, UsuarioService usuarioService,
            TurmaService turmaService) {
        this.repository = repository;
        this.usuarioService = usuarioService;
        this.turmaService = turmaService;
        this.registros = repository.carregarTodos();
    }

    /**
     * RF27: Registra presença ou falta de um aluno em uma turma numa data.
     *
     * @param presente {@code true} para presença, {@code false} para falta.
     * @return o registro gravado.
     */
    public RegistroFrequencia registrarFrequencia(String codigoTurma, String matriculaAluno, LocalDate data,
            boolean presente) {
        if (codigoTurma == null || codigoTurma.isBlank()) {
            throw new IllegalArgumentException("Código da turma não pode ser vazio.");
        }
        if (matriculaAluno == null || matriculaAluno.isBlank()) {
            throw new IllegalArgumentException("Matrícula do aluno não pode ser vazia.");
        }
        if (data == null) {
            throw new IllegalArgumentException("Data da aula não pode ser nula.");
        }

        // Turma deve existir.
        Turma turma = turmaService.buscarPorCodigo(codigoTurma);
        if (turma == null) {
            throw new IllegalArgumentException("Turma não encontrada: " + codigoTurma);
        }

        // Aluno deve existir e ser do tipo ALUNO.
        Usuario usuario = usuarioService.buscarPorMatricula(matriculaAluno);
        if (!(usuario instanceof Aluno)) {
            throw new IllegalArgumentException("Aluno não encontrado: " + matriculaAluno);
        }

        // RF27: só se registra frequência de aluno matriculado na turma.
        if (!turma.alunoJaMatriculado(matriculaAluno)) {
            throw new IllegalArgumentException(
                    "Aluno não está matriculado na turma " + turma.getCodigo() + ": " + matriculaAluno);
        }

        // Não pode haver dois registros para o mesmo aluno/turma na mesma data.
        if (jaRegistrado(codigoTurma, matriculaAluno, data)) {
            throw new IllegalArgumentException(
                    "Já existe registro de frequência para o aluno " + matriculaAluno + " na data " + data + ".");
        }

        RegistroFrequencia registro = new RegistroFrequencia(turma.getCodigo(), matriculaAluno, data, presente);
        registros.add(registro);
        repository.salvarTodos(registros);
        return registro;
    }

    /** RF27: atalho para registrar presença. */
    public RegistroFrequencia registrarPresenca(String codigoTurma, String matriculaAluno, LocalDate data) {
        return registrarFrequencia(codigoTurma, matriculaAluno, data, true);
    }

    /** RF27: atalho para registrar falta. */
    public RegistroFrequencia registrarFalta(String codigoTurma, String matriculaAluno, LocalDate data) {
        return registrarFrequencia(codigoTurma, matriculaAluno, data, false);
    }

    /** Indica se já há registro para a tripla (turma, aluno, data). */
    public boolean jaRegistrado(String codigoTurma, String matriculaAluno, LocalDate data) {
        return registros.stream().anyMatch(r -> r.getCodigoTurma().equalsIgnoreCase(codigoTurma)
                && r.getMatriculaAluno().equalsIgnoreCase(matriculaAluno)
                && r.getData().equals(data));
    }

    /** Lista os registros de frequência de uma turma, na ordem de lançamento. */
    public List<RegistroFrequencia> listarPorTurma(String codigoTurma) {
        return registros.stream()
                .filter(r -> r.getCodigoTurma().equalsIgnoreCase(codigoTurma))
                .collect(Collectors.toList());
    }

    /** Lista os registros de frequência de um aluno em uma turma. */
    public List<RegistroFrequencia> listarPorAluno(String codigoTurma, String matriculaAluno) {
        return registros.stream()
                .filter(r -> r.getCodigoTurma().equalsIgnoreCase(codigoTurma))
                .filter(r -> r.getMatriculaAluno().equalsIgnoreCase(matriculaAluno))
                .collect(Collectors.toList());
    }

    /** Conta as faltas de um aluno em uma turma. */
    public long contarFaltas(String codigoTurma, String matriculaAluno) {
        return listarPorAluno(codigoTurma, matriculaAluno).stream().filter(RegistroFrequencia::isFalta).count();
    }

    /** Conta as presenças de um aluno em uma turma. */
    public long contarPresencas(String codigoTurma, String matriculaAluno) {
        return listarPorAluno(codigoTurma, matriculaAluno).stream().filter(RegistroFrequencia::isPresente).count();
    }
}
