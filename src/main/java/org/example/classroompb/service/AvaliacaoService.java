package org.example.classroompb.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.example.classroompb.model.Avaliacao;
import org.example.classroompb.model.SituacaoFinal;
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
    private List<Avaliacao> avaliacoes;

    public AvaliacaoService(AvaliacaoRepository repository, TurmaService turmaService) {
        this.repository = repository;
        this.turmaService = turmaService;
        this.avaliacoes = repository.carregarTodos();
    }

    // RF31 - Professor lança uma nota para um aluno de uma turma
    public void lancarNota(String codigoTurma, String matriculaAluno, double nota) {
        Avaliacao avaliacao = obterOuCriarAvaliacao(codigoTurma, matriculaAluno);
        avaliacao.adicionarNota(nota);
        repository.salvarTodos(avaliacoes);
    }

    // Registra a frequência (0..100) do aluno na turma; base do cálculo da situação (RF33)
    public void registrarFrequencia(String codigoTurma, String matriculaAluno, double percentual) {
        Avaliacao avaliacao = obterOuCriarAvaliacao(codigoTurma, matriculaAluno);
        avaliacao.setFrequencia(percentual);
        repository.salvarTodos(avaliacoes);
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
                    "Não há notas lançadas para definir a situação final do aluno " + matriculaAluno + ".");
        }
        SituacaoFinal situacao = calcularSituacao(avaliacao.calcularMedia(), avaliacao.getFrequencia());
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
                .filter(a -> a.getCodigoTurma().equalsIgnoreCase(codigoTurma)
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
                    "Não há avaliação lançada para o aluno " + matriculaAluno + " na turma " + codigoTurma + ".");
        }
        return avaliacao;
    }

    private Avaliacao obterOuCriarAvaliacao(String codigoTurma, String matriculaAluno) {
        Turma turma = turmaService.buscarPorCodigo(codigoTurma);
        if (turma == null) {
            throw new IllegalArgumentException("Turma não encontrada: " + codigoTurma);
        }
        if (!turma.alunoJaMatriculado(matriculaAluno)) {
            throw new IllegalArgumentException(
                    "Aluno " + matriculaAluno + " não está matriculado na turma " + codigoTurma + ".");
        }
        Avaliacao avaliacao = buscarAvaliacao(codigoTurma, matriculaAluno);
        if (avaliacao == null) {
            avaliacao = new Avaliacao(matriculaAluno, codigoTurma);
            avaliacoes.add(avaliacao);
        }
        return avaliacao;
    }
}
