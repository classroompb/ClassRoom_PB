package org.example.classroompb.service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.example.classroompb.dto.AlunoMatriculadoDTO;
import org.example.classroompb.dto.OcupacaoVagasDTO;
import org.example.classroompb.exception.AcessoNegadoException;
import org.example.classroompb.model.*;
import org.example.classroompb.repository.TurmaRepository;

public class TurmaService {

    private final TurmaRepository repository;
    private List<Turma> turmas;
    private final UsuarioService usuarioService;
    private final DisciplinaService disciplinaService;

    public TurmaService(
            TurmaRepository repository,
            UsuarioService usuarioService,
            DisciplinaService disciplinaService) {
        this.repository = repository;
        this.usuarioService = usuarioService;
        this.disciplinaService = disciplinaService;
        this.turmas = repository.carregarTodos();
    }

    /**
     * RF10: O coordenador deve poder ofertar turmas para uma disciplina em um período letivo RF11:
     * Cada turma deve possuir professor responsável, limite de vagas, horário e sala
     */
    public Turma ofertarTurma(
            String codigoDisciplina,
            String matriculaProfessor,
            PeriodoLetivo periodoLetivo,
            int limiteVagas,
            String horario,
            String sala) {

        validarCampos(
                codigoDisciplina, matriculaProfessor, periodoLetivo, limiteVagas, horario, sala);

        // Busca disciplina
        Disciplina disciplina = disciplinaService.buscarPorCodigo(codigoDisciplina);
        if (disciplina == null) {
            throw new IllegalArgumentException("Disciplina não encontrada: " + codigoDisciplina);
        }

        // Busca professor
        Usuario usuarioTemp = usuarioService.buscarPorMatricula(matriculaProfessor);
        if (!(usuarioTemp instanceof Professor)) {
            throw new IllegalArgumentException("Professor não encontrado: " + matriculaProfessor);
        }
        Professor professor = (Professor) usuarioTemp;

        // RN06: Professor não pode ministrar duas turmas no mesmo horário
        validarHorarioProfessor(professor, horario);

        // Gera código único para a turma
        String codigoTurma = gerarCodigoTurma(codigoDisciplina, periodoLetivo);

        Turma turma =
                new Turma(
                        codigoTurma,
                        disciplina,
                        professor,
                        periodoLetivo,
                        limiteVagas,
                        horario,
                        sala);
        turmas.add(turma);
        repository.salvarTodos(turmas);

        return turma;
    }

    /**
     * RN03: Uma turma não pode ultrapassar o número máximo de vagas RN01: Um aluno não pode se
     * matricular duas vezes na mesma turma
     */
    public void matricularAluno(String codigoTurma, String matriculaAluno) {
        Turma turma = buscarPorCodigo(codigoTurma);
        if (turma == null) {
            throw new IllegalArgumentException("Turma não encontrada: " + codigoTurma);
        }

        // Valida aluno existe
        Usuario usuario = usuarioService.buscarPorMatricula(matriculaAluno);
        if (!(usuario instanceof Aluno)) {
            throw new IllegalArgumentException("Aluno não encontrado: " + matriculaAluno);
        }

        // RN01: Verifica se aluno já está matriculado
        if (turma.alunoJaMatriculado(matriculaAluno)) {
            throw new IllegalArgumentException("Aluno já está matriculado nesta turma. (RN01)");
        }

        // RN02: Verifica conflitos de horários
        validarConflitosHorario(matriculaAluno, turma.getHorario());

        // RN04 + RN05: Valida pré-requisitos
        validarPreRequisitos(matriculaAluno, turma.getDisciplina());

        // RN03: Valida limite de vagas
        if (!turma.temVagasDisponiveis()) {
            throw new IllegalArgumentException("Turma sem vagas disponíveis. (RN03)");
        }

        turma.matricularAluno(matriculaAluno);
        repository.salvarTodos(turmas);
    }

    /**
     * RN01: Cancela matrícula de aluno em turma RF22: O aluno deve poder cancelar matrícula dentro
     * do período permitido
     */
    public void cancelarMatricula(String codigoTurma, String matriculaAluno) {
        Turma turma = buscarPorCodigo(codigoTurma);
        if (turma == null) {
            throw new IllegalArgumentException("Turma não encontrada: " + codigoTurma);
        }

        // RF22: Validar se cancelamento está dentro do período permitido
        PeriodoLetivo periodo = turma.getPeriodoLetivo();
        if (periodo != null && !periodo.permiteCancelamento()) {
            throw new IllegalArgumentException(
                    "Cancelamento de matrícula não é permitido fora do prazo. (RF22)");
        }

        turma.cancelarMatricula(matriculaAluno);

        // RF24: Ao liberar uma vaga, o sistema chama automaticamente o próximo
        // aluno da lista de espera (respeitando a ordem de solicitação - RF25).
        // Pré-requisitos e horários já foram validados na entrada da fila (RF21).
        promoverDaListaDeEspera(turma);

        repository.salvarTodos(turmas);
    }

    /**
     * RF24 + RF25: Promove os próximos alunos da lista de espera enquanto houver vagas, sempre na
     * ordem de solicitação (FIFO). Não persiste — quem chama é responsável por salvar. Retorna as
     * matrículas promovidas, em ordem.
     */
    private List<String> promoverDaListaDeEspera(Turma turma) {
        List<String> promovidos = new ArrayList<>();
        while (turma.temVagasDisponiveis() && turma.getTotalEmEspera() > 0) {
            String promovido = turma.promoverDaEspera();
            if (promovido == null) {
                break;
            }
            promovidos.add(promovido);
        }
        return promovidos;
    }

    /**
     * RF24: Quando uma vaga for liberada, o sistema deve chamar automaticamente o próximo aluno da
     * lista de espera. Este método torna a chamada explícita e pode ser acionado por qualquer
     * evento que libere vagas (cancelamento de matrícula, aumento do limite de vagas etc.).
     *
     * @return lista de matrículas promovidas (na ordem de solicitação - RF25), ou lista vazia se
     *     não houver vagas/ninguém na fila.
     */
    public List<String> chamarProximosDaListaDeEspera(String codigoTurma) {
        Turma turma = buscarPorCodigo(codigoTurma);
        if (turma == null) {
            throw new IllegalArgumentException("Turma não encontrada: " + codigoTurma);
        }
        List<String> promovidos = promoverDaListaDeEspera(turma);
        if (!promovidos.isEmpty()) {
            repository.salvarTodos(turmas);
        }
        return promovidos;
    }

    /**
     * RF25: A lista de espera deve respeitar a ordem de solicitação. Retorna as matrículas da fila
     * na ordem em que foram solicitadas (FIFO).
     */
    public List<String> listarListaDeEspera(String codigoTurma) {
        Turma turma = buscarPorCodigo(codigoTurma);
        if (turma == null) {
            throw new IllegalArgumentException("Turma não encontrada: " + codigoTurma);
        }
        return turma.getAlunosEmEspera();
    }

    /**
     * RF26: O coordenador deve poder visualizar a lista de espera de cada turma. Retorna a fila
     * ordenada (RF25) com posição, matrícula e nome de cada aluno.
     */
    public List<ItemListaEspera> visualizarListaDeEspera(String codigoTurma) {
        Turma turma = buscarPorCodigo(codigoTurma);
        if (turma == null) {
            throw new IllegalArgumentException("Turma não encontrada: " + codigoTurma);
        }

        List<ItemListaEspera> itens = new ArrayList<>();
        List<String> fila = turma.getAlunosEmEspera();
        for (int i = 0; i < fila.size(); i++) {
            String matricula = fila.get(i);
            Usuario usuario = usuarioService.buscarPorMatricula(matricula);
            String nome = (usuario != null) ? usuario.getNome() : "(aluno não encontrado)";
            itens.add(new ItemListaEspera(i + 1, matricula, nome));
        }
        return itens;
    }

    /**
     * RF26: Lista todas as turmas que possuem alunos em lista de espera, para que o coordenador
     * tenha uma visão geral de onde há fila.
     */
    public List<Turma> listarTurmasComListaDeEspera() {
        return turmas.stream().filter(t -> t.getTotalEmEspera() > 0).collect(Collectors.toList());
    }

    /** RN02: Valida se há conflitos de horário com outras turmas do aluno */
    private void validarConflitosHorario(String matriculaAluno, String horarioNova) {
        List<Turma> turmasDoAluno =
                turmas.stream()
                        .filter(t -> t.alunoJaMatriculado(matriculaAluno))
                        .collect(Collectors.toList());

        for (Turma turmaExistente : turmasDoAluno) {
            if (horarioConflita(turmaExistente.getHorario(), horarioNova)) {
                throw new IllegalArgumentException(
                        "Aluno possui conflito de horário com turma "
                                + turmaExistente.getCodigo()
                                + " ("
                                + turmaExistente.getHorario()
                                + "). (RN02)");
            }
        }
    }

    /** RN06: Valida se professor já tem turma no mesmo horário */
    private void validarHorarioProfessor(Professor professor, String horarioNova) {
        List<Turma> turmasProfessor =
                turmas.stream()
                        .filter(
                                t ->
                                        t.getProfessor()
                                                .getMatricula()
                                                .equals(professor.getMatricula()))
                        .collect(Collectors.toList());

        for (Turma turmaExistente : turmasProfessor) {
            if (horarioConflita(turmaExistente.getHorario(), horarioNova)) {
                throw new IllegalArgumentException(
                        "Professor não pode ministrar duas turmas no mesmo horário. "
                                + "Conflito com turma "
                                + turmaExistente.getCodigo()
                                + " ("
                                + turmaExistente.getHorario()
                                + "). (RN06)");
            }
        }
    }

    /** RN04 + RN05: Valida pré-requisitos */
    private void validarPreRequisitos(String matriculaAluno, Disciplina disciplina) {
        List<String> preRequisitos = disciplina.getPreRequisitos();

        if (preRequisitos == null || preRequisitos.isEmpty()) {
            return; // Sem pré-requisitos
        }

        throw new IllegalArgumentException(
                "Aluno não cumpriu os pré-requisitos da disciplina: "
                        + disciplina.getNome()
                        + ". Pré-requisitos: "
                        + preRequisitos
                        + ". (RN05)");
    }

    /** RN07: Valida notas entre 0 e 10 */
    public void validarNota(double nota) {
        if (nota < 0 || nota > 10) {
            throw new IllegalArgumentException("Nota deve estar entre 0 e 10. (RN07)");
        }
    }

    /** RN08: Frequência mínima para aprovação: 75% */
    public boolean verificarAprovacaoFrequencia(int frequenciaMinutos, int aulas_Totais) {
        double percentual = (frequenciaMinutos * 100.0) / aulas_Totais;
        return percentual >= 75.0;
    }

    /**
     * RN09, RN10, RN11: Calcula situação do aluno baseado em nota e frequência RN12: Reprovação por
     * falta prevalece
     */
    public String calcularSituacaoAluno(double media, double frequenciaPercentual) {
        // RN12: Reprovação por falta prevalece
        if (frequenciaPercentual < 75.0) {
            return "REPROVADO_POR_FALTA";
        }

        // RN09: Média mínima para aprovação direta: 7,0
        if (media >= 7.0) {
            return "APROVADO";
        }

        // RN10: Média entre 4,0 e 6,9 gera recuperação
        if (media >= 4.0 && media < 7.0) {
            return "RECUPERACAO";
        }

        // RN11: Média abaixo de 4,0 reprova por nota
        return "REPROVADO_POR_NOTA";
    }

    // =========== Métodos auxiliares ===========

    private void validarCampos(
            String codigoDisciplina,
            String matriculaProfessor,
            PeriodoLetivo periodoLetivo,
            int limiteVagas,
            String horario,
            String sala) {
        if (codigoDisciplina == null || codigoDisciplina.isBlank()) {
            throw new IllegalArgumentException("Código da disciplina não pode ser vazio.");
        }
        if (matriculaProfessor == null || matriculaProfessor.isBlank()) {
            throw new IllegalArgumentException("Matrícula do professor não pode ser vazia.");
        }
        if (periodoLetivo == null) {
            throw new IllegalArgumentException("Período letivo não pode ser nulo.");
        }
        if (limiteVagas <= 0) {
            throw new IllegalArgumentException("Limite de vagas deve ser maior que zero.");
        }
        if (horario == null || horario.isBlank()) {
            throw new IllegalArgumentException("Horário não pode ser vazio.");
        }
        if (sala == null || sala.isBlank()) {
            throw new IllegalArgumentException("Sala não pode ser vazia.");
        }
    }

    private String gerarCodigoTurma(String codigoDisciplina, PeriodoLetivo periodoLetivo) {
        // Formato: DISC-PERIODO-NUMERO (ex: CC-2024.1-01)
        long contagem =
                turmas.stream()
                        .filter(
                                t ->
                                        t.getDisciplina().getCodigo().equals(codigoDisciplina)
                                                && t.getPeriodoLetivo()
                                                        .getIdentificador()
                                                        .equals(periodoLetivo.getIdentificador()))
                        .count();

        return codigoDisciplina
                + "-"
                + periodoLetivo.getIdentificador()
                + "-"
                + String.format("%02d", contagem + 1);
    }

    private boolean horarioConflita(String horario1, String horario2) {
        try {
            LocalTime inicio1 = LocalTime.parse(horario1.split("-")[0]);
            LocalTime fim1 = LocalTime.parse(horario1.split("-")[1]);
            LocalTime inicio2 = LocalTime.parse(horario2.split("-")[0]);
            LocalTime fim2 = LocalTime.parse(horario2.split("-")[1]);

            // Verifica sobreposição de horários (horários back-to-back não conflitam)
            // Não há conflito se: turma1 termina antes ou no mesmo tempo que turma2 começa
            // OU turma2 termina antes ou no mesmo tempo que turma1 começa
            return !(fim1.compareTo(inicio2) <= 0 || fim2.compareTo(inicio1) <= 0);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Formato de horário inválido: " + horario1 + " or " + horario2);
        }
    }

    public Turma buscarPorCodigo(String codigo) {
        return turmas.stream()
                .filter(t -> t.getCodigo().equalsIgnoreCase(codigo))
                .findFirst()
                .orElse(null);
    }

    public List<Turma> listarTurmasPorDisciplina(String codigoDisciplina) {
        return turmas.stream()
                .filter(t -> t.getDisciplina().getCodigo().equals(codigoDisciplina))
                .collect(Collectors.toList());
    }

    public List<Turma> listarTurmasPorPeriodo(PeriodoLetivo periodoLetivo) {
        return turmas.stream()
                .filter(
                        t ->
                                t.getPeriodoLetivo()
                                        .getIdentificador()
                                        .equals(periodoLetivo.getIdentificador()))
                .collect(Collectors.toList());
    }

    public List<Turma> listarTurmasPorProfessor(String matriculaProfessor) {
        return turmas.stream()
                .filter(t -> t.getProfessor().getMatricula().equals(matriculaProfessor))
                .collect(Collectors.toList());
    }

    public List<Turma> listarTodasAsTurmas() {
        return new ArrayList<>(turmas);
    }

    /** RF15: O aluno deve poder consultar disciplinas/turmas disponiveis. */
    public List<Turma> consultarTurmasDisponiveis() {
        return turmas.stream().filter(Turma::temVagasDisponiveis).collect(Collectors.toList());
    }

    public List<Turma> consultarTurmasDisponiveisPorPeriodo(String identificadorPeriodo) {
        if (identificadorPeriodo == null || identificadorPeriodo.isBlank()) {
            throw new IllegalArgumentException("Periodo letivo deve ser informado.");
        }

        return turmas.stream()
                .filter(Turma::temVagasDisponiveis)
                .filter(
                        t ->
                                t.getPeriodoLetivo()
                                        .getIdentificador()
                                        .equalsIgnoreCase(identificadorPeriodo))
                .collect(Collectors.toList());
    }

    /**
     * RF16: O aluno deve poder solicitar matricula em uma turma. RF21: Se não houver vaga, o aluno
     * entra em lista de espera.
     */
    public void solicitarMatricula(String codigoTurma, String matriculaAluno) {
        Turma turma = buscarPorCodigo(codigoTurma);
        if (turma == null) {
            throw new IllegalArgumentException("Turma não encontrada: " + codigoTurma);
        }

        // Valida aluno existe
        Usuario usuario = usuarioService.buscarPorMatricula(matriculaAluno);
        if (!(usuario instanceof Aluno)) {
            throw new IllegalArgumentException("Aluno não encontrado: " + matriculaAluno);
        }

        // RN01: Verifica se aluno já está matriculado
        if (turma.alunoJaMatriculado(matriculaAluno)) {
            throw new IllegalArgumentException("Aluno já está matriculado nesta turma. (RN01)");
        }

        // RN02: Verifica conflitos de horários
        validarConflitosHorario(matriculaAluno, turma.getHorario());

        // RN04 + RN05: Valida pré-requisitos
        validarPreRequisitos(matriculaAluno, turma.getDisciplina());

        // RF21: Se houver vagas, matricula normalmente
        if (turma.temVagasDisponiveis()) {
            turma.matricularAluno(matriculaAluno);
        } else {
            // RF21: Sem vagas, aluno entra em lista de espera
            turma.adicionarAlunoEmEspera(matriculaAluno);
        }

        repository.salvarTodos(turmas);
    }

    /** RF17: O sistema deve verificar se ha vagas disponiveis. */
    public boolean verificarVagasDisponiveis(String codigoTurma) {
        Turma turma = buscarPorCodigo(codigoTurma);
        if (turma == null) {
            throw new IllegalArgumentException("Turma nao encontrada: " + codigoTurma);
        }
        return turma.temVagasDisponiveis();
    }

    public int consultarQuantidadeVagasDisponiveis(String codigoTurma) {
        Turma turma = buscarPorCodigo(codigoTurma);
        if (turma == null) {
            throw new IllegalArgumentException("Turma nao encontrada: " + codigoTurma);
        }
        return turma.getVagasDisponiveis();
    }

    /**
     * RF21: Consultar posição do aluno na lista de espera. Retorna a posição (1-indexed) ou -1 se
     * não estiver na fila.
     */
    public int obterPosicaoEmEspera(String codigoTurma, String matriculaAluno) {
        Turma turma = buscarPorCodigo(codigoTurma);
        if (turma == null) {
            throw new IllegalArgumentException("Turma não encontrada: " + codigoTurma);
        }
        return turma.obterPosicaoEmEspera(matriculaAluno);
    }

    /** RF21: Consultar quantidade de alunos na lista de espera. */
    public int consultarQuantidadeEmEspera(String codigoTurma) {
        Turma turma = buscarPorCodigo(codigoTurma);
        if (turma == null) {
            throw new IllegalArgumentException("Turma não encontrada: " + codigoTurma);
        }
        return turma.getTotalEmEspera();
    }

    /** RF21: Remover aluno da lista de espera (ex: se desistiu da disciplina). */
    public void removerDaEspera(String codigoTurma, String matriculaAluno) {
        Turma turma = buscarPorCodigo(codigoTurma);
        if (turma == null) {
            throw new IllegalArgumentException("Turma não encontrada: " + codigoTurma);
        }
        turma.removerDaEspera(matriculaAluno);
        repository.salvarTodos(turmas);
    }

    /** RF23: Obter turma atualizada com todas as informações de lista de espera */
    public Turma obterTurma(String codigoTurma) {
        Turma turma = buscarPorCodigo(codigoTurma);
        if (turma == null) {
            throw new IllegalArgumentException("Turma não encontrada: " + codigoTurma);
        }
        return turma;
    }

    // RF14: O coordenador deve poder alterar uma turma antes do início das aulas

    public Turma alterarTurma(
            String codigoTurma,
            String novaMatriculaProfessor,
            Integer novoLimiteVagas,
            String novoHorario,
            String novaSala) {

        Turma turma = buscarPorCodigo(codigoTurma);
        if (turma == null) {
            throw new IllegalArgumentException("Turma não encontrada: " + codigoTurma);
        }

        if (turma.getPeriodoLetivo().getStatus() != StatusPeriodoLetivo.INATIVO) {
            throw new IllegalStateException(
                    "Não é possível alterar turma após o início das aulas.");
        }

        if (novaMatriculaProfessor != null && !novaMatriculaProfessor.isBlank()) {
            Usuario u = usuarioService.buscarPorMatricula(novaMatriculaProfessor);
            if (!(u instanceof Professor)) {
                throw new IllegalArgumentException(
                        "Professor não encontrado: " + novaMatriculaProfessor);
            }
            Professor novoProfessor = (Professor) u;

            // Valida horário do novo professor (ignora a própria turma)
            for (Turma t : turmas) {
                if (!t.getCodigo().equals(codigoTurma)
                        && t.getProfessor().getMatricula().equals(novaMatriculaProfessor)) {
                    if (horarioConflita(t.getHorario(), turma.getHorario())) {
                        throw new IllegalArgumentException(
                                "Professor possui conflito de horário com turma "
                                        + t.getCodigo()
                                        + ". (RN06)");
                    }
                }
            }
            turma.setProfessor(novoProfessor);
        }

        if (novoLimiteVagas != null && novoLimiteVagas > 0) {
            // Ajusta o limite mantendo coerência com as vagas disponíveis.
            turma.ajustarLimiteVagas(novoLimiteVagas);
            // RF24: vagas liberadas pelo aumento chamam automaticamente a fila.
            promoverDaListaDeEspera(turma);
        }

        if (novoHorario != null && !novoHorario.isBlank()) {
            turma.setHorario(novoHorario);
        }

        if (novaSala != null && !novaSala.isBlank()) {
            turma.setSala(novaSala);
        }

        repository.salvarTodos(turmas);
        return turma;
    }

    /** RF14: O coordenador deve poder cancelar uma turma antes do início das aulas */
    public void cancelarTurma(String codigoTurma) {

        Turma turma = buscarPorCodigo(codigoTurma);
        if (turma == null) {
            throw new IllegalArgumentException("Turma não encontrada: " + codigoTurma);
        }

        if (turma.getPeriodoLetivo().getStatus() != StatusPeriodoLetivo.INATIVO) {
            throw new IllegalStateException(
                    "Não é possível cancelar turma após o início das aulas.");
        }

        turmas.remove(turma);
        repository.salvarTodos(turmas);
    }

    public void salvarTodas() {
        repository.salvarTodos(turmas);
    }

    /**
     * RF40: O coordenador deve gerar relatório de alunos matriculados por turma. Retorna a lista de
     * alunos matriculados contendo a matrícula e o nome.
     */
    public List<AlunoMatriculadoDTO> obterAlunosMatriculadosPorTurma(
            String codigoTurma, Usuario usuarioLogado) {
        if (usuarioLogado == null || usuarioLogado.getTipo() != TipoUsuario.COORDENADOR) {
            throw new AcessoNegadoException(
                    "Acesso negado: apenas coordenadores podem gerar este relatório.");
        }

        if (codigoTurma == null || codigoTurma.trim().isEmpty()) {
            throw new IllegalArgumentException("Código da turma não pode ser vazio.");
        }

        Turma turma = buscarPorCodigo(codigoTurma);
        if (turma == null) {
            throw new IllegalArgumentException("Turma não encontrada: " + codigoTurma);
        }

        List<String> matriculas = turma.getAlunoMatriculados();
        List<AlunoMatriculadoDTO> alunosDTO = new ArrayList<>();

        for (String matricula : matriculas) {
            Usuario aluno = usuarioService.buscarPorMatricula(matricula);
            String nome = (aluno != null) ? aluno.getNome() : "Desconhecido";
            alunosDTO.add(new AlunoMatriculadoDTO(matricula, nome));
        }

        return alunosDTO;
    }

    /**
     * RF41: O coordenador deve gerar relatório de ocupação de vagas. Uma linha por turma com o
     * limite de vagas, quantas estão preenchidas, quantas sobraram e quantos alunos estão na lista
     * de espera. Ordenado por período letivo e, dentro do período, por nome da disciplina.
     */
    public List<OcupacaoVagasDTO> relatorioOcupacaoVagas(Usuario usuarioLogado) {
        if (usuarioLogado == null || usuarioLogado.getTipo() != TipoUsuario.COORDENADOR) {
            throw new AcessoNegadoException(
                    "Acesso negado: apenas coordenadores podem gerar este relatório.");
        }

        return turmas.stream()
                .map(
                        t ->
                                new OcupacaoVagasDTO(
                                        t.getCodigo(),
                                        t.getDisciplina().getNome(),
                                        t.getProfessor().getNome(),
                                        t.getPeriodoLetivo().getIdentificador(),
                                        t.getLimiteVagas(),
                                        t.getTotalMatriculados(),
                                        t.getVagasDisponiveis(),
                                        t.getTotalEmEspera()))
                .sorted(
                        Comparator.comparing(OcupacaoVagasDTO::periodoLetivo)
                                .thenComparing(OcupacaoVagasDTO::nomeDisciplina))
                .collect(Collectors.toList());
    }
}
