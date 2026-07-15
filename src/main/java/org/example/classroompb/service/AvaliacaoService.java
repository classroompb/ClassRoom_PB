package org.example.classroompb.service;

import java.util.List;
import java.util.stream.Collectors;
import org.example.classroompb.model.Avaliacao;
import org.example.classroompb.model.SituacaoFinal;
import org.example.classroompb.model.StatusPeriodoLetivo;
import org.example.classroompb.model.Turma;
import org.example.classroompb.repository.AvaliacaoRepository;

// Avaliação da Release 3: lançamento de notas (RF31), média (RF32) e situação final (RF33).
public class AvaliacaoService {

    // RN08 - frequência mínima para aprovação
    public static final double FREQUENCIA_MINIMA = 75.0;
    // RN09 - média mínima para aprovação direta
    public static final double MEDIA_APROVACAO = 7.0;
    // RN10 - média mínima para recuperação (abaixo disso reprova por nota, RN11)
    public static final double MEDIA_RECUPERACAO = 4.0;

    private final AvaliacaoRepository repository;
    private final TurmaService turmaService;
    private final FrequenciaService frequenciaService;
    private List<Avaliacao> avaliacoes;

    /**
     * Construtor sem frequência por registros. A frequência usada na situação final é a que for
     * informada via {@link #registrarFrequencia(String, String, double)}.
     */
    public AvaliacaoService(AvaliacaoRepository repository, TurmaService turmaService) {
        this(repository, turmaService, null);
    }

    /**
     * Construtor integrado ao RF27. Quando o aluno tem registros de presença/falta lançados, a
     * frequência da situação final passa a ser derivada desses registros em vez do percentual
     * digitado à mão. É assim que o CLI monta o serviço.
     */
    public AvaliacaoService(
            AvaliacaoRepository repository,
            TurmaService turmaService,
            FrequenciaService frequenciaService) {
        this.repository = repository;
        this.turmaService = turmaService;
        this.frequenciaService = frequenciaService;
        this.avaliacoes = repository.carregarTodos();
    }

    // RF31 - Professor lança uma nota para um aluno de uma turma
    public void lancarNota(String codigoTurma, String matriculaAluno, double nota) {
        Avaliacao avaliacao = obterOuCriarAvaliacao(codigoTurma, matriculaAluno);
        avaliacao.adicionarNota(nota);
        repository.salvarTodos(avaliacoes);
    }

    // RF34 - Professor edita uma nota de um aluno de uma turma
    public void alterarNota(
            String codigoTurma, String matriculaAluno, int indiceNota, double novaNota) {
        Turma turma = turmaService.buscarPorCodigo(codigoTurma);
        if (turma == null) {
            throw new IllegalArgumentException("Turma não encontrada: " + codigoTurma);
        }
        if (turma.isFechada()) {
            throw new IllegalStateException("Não é possível alterar notas de uma turma fechada.");
        }
        if (turma.getPeriodoLetivo().getStatus() == StatusPeriodoLetivo.ENCERRADO) {
            throw new IllegalStateException("Não é possível alterar notas de uma turma encerrada.");
        }

        Avaliacao avaliacao = exigirAvaliacao(codigoTurma, matriculaAluno);
        avaliacao.alterarNota(indiceNota, novaNota);
        repository.salvarTodos(avaliacoes);
    }

    /**
     * Registra a frequência (0..100) do aluno na turma informando o percentual direto.
     *
     * <p>Usar só quando não há registros de aula do RF27. Se houver, o {@link
     * #definirSituacaoFinal(String, String)} recalcula a frequência a partir deles e sobrescreve o
     * valor gravado aqui, porque os registros são a fonte de verdade.
     */
    public void registrarFrequencia(String codigoTurma, String matriculaAluno, double percentual) {
        Avaliacao avaliacao = obterOuCriarAvaliacao(codigoTurma, matriculaAluno);
        avaliacao.setFrequencia(percentual);
        repository.salvarTodos(avaliacoes);
    }

    /**
     * Frequência que vale para a situação final. Se o aluno tem registros de presença/falta (RF27),
     * o percentual vem deles. Se não tem, vale o que foi informado à mão.
     */
    private double frequenciaVigente(Avaliacao avaliacao) {
        String turma = avaliacao.getCodigoTurma();
        String aluno = avaliacao.getMatriculaAluno();
        if (frequenciaService != null && frequenciaService.temRegistros(turma, aluno)) {
            return frequenciaService.calcularPercentualFrequencia(turma, aluno);
        }
        return avaliacao.getFrequencia();
    }

    // RF32 - Média final do aluno (média aritmética das notas lançadas)
    public double calcularMedia(String codigoTurma, String matriculaAluno) {
        return exigirAvaliacao(codigoTurma, matriculaAluno).calcularMedia();
    }

    // RF33 - Define a situação final a partir da média e da frequência (RN08-RN12)
    public SituacaoFinal definirSituacaoFinal(String codigoTurma, String matriculaAluno) {
        Avaliacao avaliacao = exigirAvaliacao(codigoTurma, matriculaAluno);
        if (avaliacao.getNotas().isEmpty()) {
            throw new IllegalArgumentException(
                    "Não há notas lançadas para definir a situação final do aluno "
                            + matriculaAluno
                            + ".");
        }
        // RF27: se o professor lançou presença/falta, a frequência real prevalece sobre o
        // percentual informado à mão. Grava de volta para o campo não ficar mentindo.
        double frequencia = frequenciaVigente(avaliacao);
        avaliacao.setFrequencia(frequencia);

        SituacaoFinal situacao = calcularSituacao(avaliacao.calcularMedia(), frequencia);
        avaliacao.setSituacao(situacao);
        repository.salvarTodos(avaliacoes);
        return situacao;
    }

    // Regra pura da situação (RN08-RN12); RN12: reprovação por falta prevalece sobre a nota
    public SituacaoFinal calcularSituacao(double media, double frequenciaPercentual) {
        if (frequenciaPercentual < FREQUENCIA_MINIMA) {
            return SituacaoFinal.REPROVADO_POR_FALTA;
        }
        if (media >= MEDIA_APROVACAO) {
            return SituacaoFinal.APROVADO;
        }
        if (media >= MEDIA_RECUPERACAO) {
            return SituacaoFinal.RECUPERACAO;
        }
        return SituacaoFinal.REPROVADO_POR_NOTA;
    }

    public Avaliacao buscarAvaliacao(String codigoTurma, String matriculaAluno) {
        return avaliacoes.stream()
                .filter(
                        a ->
                                a.getCodigoTurma().equalsIgnoreCase(codigoTurma)
                                        && a.getMatriculaAluno().equalsIgnoreCase(matriculaAluno))
                .findFirst()
                .orElse(null);
    }

    public List<Avaliacao> listarPorTurma(String codigoTurma) {
        return avaliacoes.stream()
                .filter(a -> a.getCodigoTurma().equalsIgnoreCase(codigoTurma))
                .collect(Collectors.toList());
    }

    private Avaliacao exigirAvaliacao(String codigoTurma, String matriculaAluno) {
        Avaliacao avaliacao = buscarAvaliacao(codigoTurma, matriculaAluno);
        if (avaliacao == null) {
            throw new IllegalArgumentException(
                    "Não há avaliação lançada para o aluno "
                            + matriculaAluno
                            + " na turma "
                            + codigoTurma
                            + ".");
        }
        return avaliacao;
    }

    private Avaliacao obterOuCriarAvaliacao(String codigoTurma, String matriculaAluno) {
        Turma turma = turmaService.buscarPorCodigo(codigoTurma);
        if (turma == null) {
            throw new IllegalArgumentException("Turma não encontrada: " + codigoTurma);
        }
        if (turma.isFechada()) {
            throw new IllegalStateException("Não é possível lançar notas em uma turma fechada.");
        }
        if (!turma.alunoJaMatriculado(matriculaAluno)) {
            throw new IllegalArgumentException(
                    "Aluno "
                            + matriculaAluno
                            + " não está matriculado na turma "
                            + codigoTurma
                            + ".");
        }
        Avaliacao avaliacao = buscarAvaliacao(codigoTurma, matriculaAluno);
        if (avaliacao == null) {
            avaliacao = new Avaliacao(matriculaAluno, codigoTurma);
            avaliacoes.add(avaliacao);
        }
        return avaliacao;
    }

    // RF36 - Professor fecha as notas de uma turma
    public void fecharTurma(String codigoTurma) {
        Turma turma = turmaService.buscarPorCodigo(codigoTurma);
        if (turma == null) {
            throw new IllegalArgumentException("Turma não encontrada: " + codigoTurma);
        }
        if (turma.isFechada()) {
            throw new IllegalStateException("A turma já está fechada.");
        }

        List<String> matriculas = turma.getAlunoMatriculados();
        for (String matricula : matriculas) {
            Avaliacao avaliacao = buscarAvaliacao(codigoTurma, matricula);
            if (avaliacao == null || avaliacao.getNotas().isEmpty()) {
                throw new IllegalStateException(
                        "Não é possível fechar a turma: o aluno "
                                + matricula
                                + " não possui notas lançadas.");
            }
        }

        turma.fechar();
        for (String matricula : matriculas) {
            Avaliacao avaliacao = buscarAvaliacao(codigoTurma, matricula);
            if (avaliacao != null) {
                avaliacao.fechar();
            }
        }

        repository.salvarTodos(avaliacoes);
        turmaService.salvarTodas();
    }
}
