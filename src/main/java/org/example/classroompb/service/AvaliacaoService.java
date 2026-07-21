package org.example.classroompb.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.example.classroompb.dto.ReprovacaoDisciplinaDTO;
import org.example.classroompb.exception.AcessoNegadoException;
import org.example.classroompb.model.Aluno;
import org.example.classroompb.model.Avaliacao;
import org.example.classroompb.model.Disciplina;
import org.example.classroompb.model.HistoricoAlunoCurso;
import org.example.classroompb.model.ItemHistoricoAcademico;
import org.example.classroompb.model.ItemRelatorioTurma;
import org.example.classroompb.model.SituacaoFinal;
import org.example.classroompb.model.StatusPeriodoLetivo;
import org.example.classroompb.model.TipoUsuario;
import org.example.classroompb.model.Turma;
import org.example.classroompb.model.Usuario;
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
                // Consolida a situação final no fechamento (RF33). Usa a frequência vigente: os
                // registros de presença/falta do RF27 quando existirem, senão o percentual
                // informado à mão. Assim "notas consolidadas" reflete a situação de fato, e a
                // turma não fica fechada com avaliações ainda EM_ANDAMENTO.
                double frequencia = frequenciaVigente(avaliacao);
                avaliacao.setFrequencia(frequencia);
                avaliacao.setSituacao(calcularSituacao(avaliacao.calcularMedia(), frequencia));
                avaliacao.fechar();
            }
        }

        repository.salvarTodos(avaliacoes);
        turmaService.salvarTodas();
    }

    /**
     * RF37 - O aluno deve poder consultar seu histórico acadêmico: disciplinas cursadas, notas,
     * média e situação, organizado por período letivo.
     *
     * <p>Monta o histórico a partir das avaliações do aluno (uma por turma em que ele foi
     * matriculado e teve notas/frequência lançadas), usando o model de apoio {@link
     * ItemHistoricoAcademico} (task D1). O resultado vem ordenado por período letivo e, dentro do
     * mesmo período, pelo nome da disciplina — para facilitar a leitura.
     *
     * <p>Turmas em que o aluno está matriculado mas ainda não possuem avaliação lançada (nenhuma
     * nota/frequência registrada) não entram no histórico, pois ainda não há o que reportar.
     */
    public List<ItemHistoricoAcademico> consultarHistoricoAcademico(String matriculaAluno) {
        if (matriculaAluno == null || matriculaAluno.isBlank()) {
            throw new IllegalArgumentException("Matrícula do aluno não pode ser vazia.");
        }

        return avaliacoes.stream()
                .filter(a -> a.getMatriculaAluno().equalsIgnoreCase(matriculaAluno))
                .map(this::montarItemHistorico)
                .filter(item -> item != null)
                .sorted(
                        Comparator.comparing(ItemHistoricoAcademico::periodoLetivo)
                                .thenComparing(ItemHistoricoAcademico::nomeDisciplina))
                .collect(Collectors.toList());
    }

    private ItemHistoricoAcademico montarItemHistorico(Avaliacao avaliacao) {
        Turma turma = turmaService.buscarPorCodigo(avaliacao.getCodigoTurma());
        if (turma == null) {
            // Turma pode ter sido removida; não há como reportar disciplina/período. (RN de
            // consistência — não deve ocorrer em operação normal.)
            return null;
        }

        double frequencia = frequenciaVigente(avaliacao);

        return new ItemHistoricoAcademico(
                turma.getPeriodoLetivo().getIdentificador(),
                turma.getDisciplina().getCodigo(),
                turma.getDisciplina().getNome(),
                turma.getCodigo(),
                avaliacao.getNotas(),
                avaliacao.calcularMedia(),
                frequencia,
                avaliacao.getSituacao());
    }

    /**
     * RF39 - O coordenador deve poder consultar o histórico dos alunos do curso.
     *
     * <p>Não recebe o código do curso diretamente: quem resolve "quais alunos pertencem a este
     * curso" é a {@code UsuarioService} (via {@code listarAlunosPorCurso}), mantendo esta classe
     * sem uma dependência nova. Aqui só reaproveitamos o {@link
     * #consultarHistoricoAcademico(String)} do RF37 para cada aluno da lista, agrupando o resultado
     * em {@link HistoricoAlunoCurso}. Alunos sem nenhum item de histórico ainda aparecem no
     * resultado, com a lista de itens vazia.
     */
    public List<HistoricoAlunoCurso> consultarHistoricoPorCurso(List<Aluno> alunosDoCurso) {
        if (alunosDoCurso == null) {
            throw new IllegalArgumentException("Lista de alunos do curso não pode ser nula.");
        }

        return alunosDoCurso.stream()
                .map(
                        aluno ->
                                new HistoricoAlunoCurso(
                                        aluno.getMatricula(),
                                        aluno.getNome(),
                                        consultarHistoricoAcademico(aluno.getMatricula())))
                .collect(Collectors.toList());
    }

    public List<ItemRelatorioTurma> emitirRelatorioTurma(String codigoTurma) {
        if (codigoTurma == null || codigoTurma.isBlank()) {
            throw new IllegalArgumentException("Código da turma não pode ser vazio.");
        }

        return avaliacoes.stream()
                .filter(a -> a.getCodigoTurma().equalsIgnoreCase(codigoTurma))
                .map(this::montarItemRelatorio)
                .filter(item -> item != null)
                .collect(Collectors.toList());
    }

    private ItemRelatorioTurma montarItemRelatorio(Avaliacao avaliacao) {
        double frequencia = frequenciaVigente(avaliacao);
        return new ItemRelatorioTurma(
                avaliacao.getMatriculaAluno(),
                avaliacao.getNotas(),
                avaliacao.calcularMedia(),
                frequencia,
                avaliacao.getSituacao());
    }

    /**
     * RF42 - O coordenador deve gerar relatório de reprovação por disciplina.
     *
     * <p>Agrupa as avaliações já finalizadas por disciplina (a disciplina vem da turma) e separa
     * quem reprovou por nota (RN11) de quem reprovou por falta (RN12). Avaliações ainda
     * EM_ANDAMENTO não entram na conta, pois não têm veredito. A taxa de reprovação é o percentual
     * de reprovados sobre o total de avaliados da disciplina. O resultado vem ordenado por nome da
     * disciplina.
     */
    public List<ReprovacaoDisciplinaDTO> relatorioReprovacaoPorDisciplina(Usuario usuarioLogado) {
        if (usuarioLogado == null || usuarioLogado.getTipo() != TipoUsuario.COORDENADOR) {
            throw new AcessoNegadoException(
                    "Acesso negado: apenas coordenadores podem gerar este relatório.");
        }

        Map<String, Disciplina> disciplinaPorCodigo = new LinkedHashMap<>();
        Map<String, int[]> contadores = new HashMap<>();
        // int[]{avaliados, reprovadosPorNota, reprovadosPorFalta}

        for (Avaliacao avaliacao : avaliacoes) {
            SituacaoFinal situacao = avaliacao.getSituacao();
            if (situacao == null || situacao == SituacaoFinal.EM_ANDAMENTO) {
                continue; // sem veredito ainda, não conta
            }
            Turma turma = turmaService.buscarPorCodigo(avaliacao.getCodigoTurma());
            if (turma == null) {
                continue; // turma removida; não há disciplina para agrupar
            }

            Disciplina disciplina = turma.getDisciplina();
            String codigo = disciplina.getCodigo();
            disciplinaPorCodigo.putIfAbsent(codigo, disciplina);
            int[] c = contadores.computeIfAbsent(codigo, k -> new int[3]);
            c[0]++;
            if (situacao == SituacaoFinal.REPROVADO_POR_NOTA) {
                c[1]++;
            } else if (situacao == SituacaoFinal.REPROVADO_POR_FALTA) {
                c[2]++;
            }
        }

        List<ReprovacaoDisciplinaDTO> relatorio = new ArrayList<>();
        for (Map.Entry<String, Disciplina> entry : disciplinaPorCodigo.entrySet()) {
            int[] c = contadores.get(entry.getKey());
            int avaliados = c[0];
            int reprovadosPorNota = c[1];
            int reprovadosPorFalta = c[2];
            int totalReprovados = reprovadosPorNota + reprovadosPorFalta;
            double taxa = avaliados == 0 ? 0.0 : (totalReprovados * 100.0) / avaliados;
            relatorio.add(
                    new ReprovacaoDisciplinaDTO(
                            entry.getKey(),
                            entry.getValue().getNome(),
                            avaliados,
                            reprovadosPorNota,
                            reprovadosPorFalta,
                            totalReprovados,
                            taxa));
        }
        relatorio.sort(Comparator.comparing(ReprovacaoDisciplinaDTO::nomeDisciplina));
        return relatorio;
    }
}
