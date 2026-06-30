package org.example.classroompb.service;

import org.example.classroompb.exception.AlunoNaoEncontradoException;
import org.example.classroompb.model.Alerta;
import org.example.classroompb.model.FrequenciaDisciplina;
import org.example.classroompb.model.Usuario;
import org.example.classroompb.repository.AlertaRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * RF30: O sistema deve alertar quando o aluno estiver abaixo do mínimo exigido.
 *
 * <p>Gerencia alertas de situação crítica do aluno em relação a:
 * - Notas (média abaixo de 7.0 = reprovação direta ou recuperação)
 * - Frequência (abaixo de 75% = reprovação por falta)
 *
 * <p>Os alertas são gerados quando:
 * 1. Uma nota é lançada abaixo do mínimo (nota < 7.0)
 * 2. Frequência cai abaixo do mínimo (< 75%)
 *
 * <p>O aluno pode consultar seus alertas não lidos para tomar ações corretivas.
 */
public class AlertaService {

    private final AlertaRepository repository;
    private List<Alerta> alertas;
    private final UsuarioService usuarioService;

    public AlertaService(AlertaRepository repository, UsuarioService usuarioService) {
        this.repository = repository;
        this.usuarioService = usuarioService;
        this.alertas = repository.carregarTodos();
    }

    /**
     * RF30: Gera um alerta quando nota é lançada abaixo do mínimo (7.0).
     *
     * @param matriculaAluno matrícula do aluno
     * @param codigoTurma    código da turma
     * @param codigoDisciplina código da disciplina
     * @param nota           nota lançada
     */
    public void verificarEGerarAlertaDeNota(String matriculaAluno, String codigoTurma,
                                            String codigoDisciplina, double nota) {
        validarAluno(matriculaAluno);

        final double NOTA_MINIMA = 7.0;

        if (nota < NOTA_MINIMA) {
            Alerta alerta = new Alerta(matriculaAluno, codigoTurma, codigoDisciplina,
                    Alerta.TipoAlerta.NOTA_BAIXA, nota, NOTA_MINIMA);
            alertas.add(alerta);
            repository.salvarTodos(alertas);
        }
    }

    /**
     * RF30: Gera um alerta quando frequência fica abaixo do mínimo (75%).
     *
     * @param matriculaAluno  matrícula do aluno
     * @param frequencia      objeto com informações de frequência
     */
    public void verificarEGerarAlertaDeFrequencia(String matriculaAluno,
                                                  FrequenciaDisciplina frequencia) {
        validarAluno(matriculaAluno);

        final double FREQUENCIA_MINIMA = 75.0;

        if (!frequencia.atingiuFrequenciaMinima()) {
            Alerta alerta = new Alerta(matriculaAluno, 
                    frequencia.getCodigosTurmas().isEmpty() ? "" : frequencia.getCodigosTurmas().get(0),
                    frequencia.getCodigoDisciplina(),
                    Alerta.TipoAlerta.FREQUENCIA_BAIXA,
                    frequencia.getPercentualFrequencia(),
                    FREQUENCIA_MINIMA);
            alertas.add(alerta);
            repository.salvarTodos(alertas);
        }
    }

    /**
     * RF30: Retorna todos os alertas não lidos de um aluno.
     *
     * @param matriculaAluno matrícula do aluno
     * @return lista de alertas não lidos
     */
    public List<Alerta> obterAlertasNaoLidos(String matriculaAluno) {
        validarAluno(matriculaAluno);

        return alertas.stream()
                .filter(a -> a.getMatriculaAluno().equals(matriculaAluno))
                .filter(a -> !a.isLido())
                .collect(Collectors.toList());
    }

    /**
     * RF30: Retorna todos os alertas de um aluno (lidos e não lidos).
     *
     * @param matriculaAluno matrícula do aluno
     * @return lista de todos os alertas do aluno
     */
    public List<Alerta> obterTodosOsAlertas(String matriculaAluno) {
        validarAluno(matriculaAluno);

        return alertas.stream()
                .filter(a -> a.getMatriculaAluno().equals(matriculaAluno))
                .collect(Collectors.toList());
    }

    /**
     * RF30: Marca um alerta como lido.
     *
     * @param idAlerta ID do alerta
     */
    public void marcarAlertoComoLido(String idAlerta) {
        Alerta alerta = alertas.stream()
                .filter(a -> a.getId().equals(idAlerta))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Alerta não encontrado: " + idAlerta));

        alerta.marcarComoLido();
        repository.salvarTodos(alertas);
    }

    /**
     * RF30: Retorna alertas ativos (não lidos) de um aluno por tipo.
     *
     * @param matriculaAluno matrícula do aluno
     * @param tipo           tipo de alerta (NOTA_BAIXA ou FREQUENCIA_BAIXA)
     * @return lista de alertas do tipo especificado e não lidos
     */
    public List<Alerta> obterAlertasPorTipo(String matriculaAluno, Alerta.TipoAlerta tipo) {
        validarAluno(matriculaAluno);

        return alertas.stream()
                .filter(a -> a.getMatriculaAluno().equals(matriculaAluno))
                .filter(a -> !a.isLido())
                .filter(a -> a.getTipo() == tipo)
                .collect(Collectors.toList());
    }

    /**
     * RF30: Retorna quantidade de alertas não lidos.
     *
     * @param matriculaAluno matrícula do aluno
     * @return quantidade de alertas não lidos
     */
    public int contarAlertasNaoLidos(String matriculaAluno) {
        validarAluno(matriculaAluno);
        return (int) alertas.stream()
                .filter(a -> a.getMatriculaAluno().equals(matriculaAluno))
                .filter(a -> !a.isLido())
                .count();
    }

    /**
     * RF30: Retorna uma mensagem de resumo do status crítico do aluno.
     *
     * @param matriculaAluno matrícula do aluno
     * @return string com resumo de alertas (ex: "2 notas baixas, 1 frequência insuficiente")
     */
    public String obterResumoDeSituacao(String matriculaAluno) {
        validarAluno(matriculaAluno);

        List<Alerta> alertasAtivos = obterAlertasNaoLidos(matriculaAluno);
        if (alertasAtivos.isEmpty()) {
            return "Sem alertas de situação crítica.";
        }

        long notasBaixas = alertasAtivos.stream()
                .filter(a -> a.getTipo() == Alerta.TipoAlerta.NOTA_BAIXA)
                .count();

        long frequenciasBaixas = alertasAtivos.stream()
                .filter(a -> a.getTipo() == Alerta.TipoAlerta.FREQUENCIA_BAIXA)
                .count();

        StringBuilder resumo = new StringBuilder();
        if (notasBaixas > 0) {
            resumo.append(notasBaixas).append(" disciplina(s) com nota(s) baixa(s)");
        }
        if (frequenciasBaixas > 0) {
            if (resumo.length() > 0) {
                resumo.append(", ");
            }
            resumo.append(frequenciasBaixas).append(" disciplina(s) com frequência insuficiente");
        }
        resumo.append(".");

        return resumo.toString();
    }

    /**
     * Valida se o aluno existe no sistema.
     */
    private void validarAluno(String matriculaAluno) {
        Usuario usuario = usuarioService.buscarPorMatricula(matriculaAluno);
        if (usuario == null) {
            throw new AlunoNaoEncontradoException(matriculaAluno);
        }
    }

    /**
     * Método auxiliar para testes: limpa todos os alertas.
     */
    protected void limparTodos() {
        alertas.clear();
        repository.salvarTodos(alertas);
    }
}
