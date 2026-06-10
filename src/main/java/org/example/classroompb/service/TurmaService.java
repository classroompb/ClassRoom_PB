package org.example.classroompb.service;

import org.example.classroompb.model.*;
import org.example.classroompb.repository.TurmaRepository;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TurmaService {

	private final TurmaRepository repository;
	private List<Turma> turmas;
	private final UsuarioService usuarioService;
	private final DisciplinaService disciplinaService;

	public TurmaService(TurmaRepository repository, UsuarioService usuarioService,
			DisciplinaService disciplinaService) {
		this.repository = repository;
		this.usuarioService = usuarioService;
		this.disciplinaService = disciplinaService;
		this.turmas = repository.carregarTodos();
	}

	/**
	 * RF10: O coordenador deve poder ofertar turmas para uma disciplina em um
	 * período letivo RF11: Cada turma deve possuir professor responsável, limite de
	 * vagas, horário e sala
	 */
	public Turma ofertarTurma(String codigoDisciplina, String matriculaProfessor, PeriodoLetivo periodoLetivo,
			int limiteVagas, String horario, String sala) {

		validarCampos(codigoDisciplina, matriculaProfessor, periodoLetivo, limiteVagas, horario, sala);

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

		Turma turma = new Turma(codigoTurma, disciplina, professor, periodoLetivo, limiteVagas, horario, sala);
		turmas.add(turma);
		repository.salvarTodos(turmas);

		return turma;
	}

	/**
	 * RN03: Uma turma não pode ultrapassar o número máximo de vagas RN01: Um aluno
	 * não pode se matricular duas vezes na mesma turma
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
	 * RN01: Cancela matrícula de aluno em turma
	 */
	public void cancelarMatricula(String codigoTurma, String matriculaAluno) {
		Turma turma = buscarPorCodigo(codigoTurma);
		if (turma == null) {
			throw new IllegalArgumentException("Turma não encontrada: " + codigoTurma);
		}

		turma.cancelarMatricula(matriculaAluno);
		
		// RF21: Promover aluno da lista de espera se houver vagas agora
		if (turma.temVagasDisponiveis() && !turma.getAlunosEmEspera().isEmpty()) {
			String proximoAluno = turma.promoverDaEspera();
			if (proximoAluno != null) {
				// Não precisamos validar pré-requisitos e horários novamente
				// pois foram validados quando entrou na fila
			}
		}
		
		repository.salvarTodos(turmas);
	}

	/**
	 * RN02: Valida se há conflitos de horário com outras turmas do aluno
	 */
	private void validarConflitosHorario(String matriculaAluno, String horarioNova) {
		List<Turma> turmasDoAluno = turmas.stream().filter(t -> t.alunoJaMatriculado(matriculaAluno))
				.collect(Collectors.toList());

		for (Turma turmaExistente : turmasDoAluno) {
			if (horarioConflita(turmaExistente.getHorario(), horarioNova)) {
				throw new IllegalArgumentException("Aluno possui conflito de horário com turma "
						+ turmaExistente.getCodigo() + " (" + turmaExistente.getHorario() + "). (RN02)");
			}
		}
	}

	/**
	 * RN06: Valida se professor já tem turma no mesmo horário
	 */
	private void validarHorarioProfessor(Professor professor, String horarioNova) {
		List<Turma> turmasProfessor = turmas.stream()
				.filter(t -> t.getProfessor().getMatricula().equals(professor.getMatricula()))
				.collect(Collectors.toList());

		for (Turma turmaExistente : turmasProfessor) {
			if (horarioConflita(turmaExistente.getHorario(), horarioNova)) {
				throw new IllegalArgumentException(
						"Professor não pode ministrar duas turmas no mesmo horário. " + "Conflito com turma "
								+ turmaExistente.getCodigo() + " (" + turmaExistente.getHorario() + "). (RN06)");
			}
		}
	}

	/**
	 * RN04 + RN05: Valida pré-requisitos
	 */
	private void validarPreRequisitos(String matriculaAluno, Disciplina disciplina) {
	    List<String> preRequisitos = disciplina.getPreRequisitos();

	    if (preRequisitos == null || preRequisitos.isEmpty()) {
	        return; // Sem pré-requisitos
	    }

	    throw new IllegalArgumentException(
	        "Aluno não cumpriu os pré-requisitos da disciplina: " + 
	        disciplina.getNome() + ". Pré-requisitos: " + preRequisitos + ". (RN05)"
	    );
	}

	/**
	 * RN07: Valida notas entre 0 e 10
	 */
	public void validarNota(double nota) {
		if (nota < 0 || nota > 10) {
			throw new IllegalArgumentException("Nota deve estar entre 0 e 10. (RN07)");
		}
	}

	/**
	 * RN08: Frequência mínima para aprovação: 75%
	 */
	public boolean verificarAprovacaoFrequencia(int frequenciaMinutos, int aulas_Totais) {
		double percentual = (frequenciaMinutos * 100.0) / aulas_Totais;
		return percentual >= 75.0;
	}

	/**
	 * RN09, RN10, RN11: Calcula situação do aluno baseado em nota e frequência
	 * RN12: Reprovação por falta prevalece
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

	private void validarCampos(String codigoDisciplina, String matriculaProfessor, PeriodoLetivo periodoLetivo,
			int limiteVagas, String horario, String sala) {
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
		long contagem = turmas.stream().filter(t -> t.getDisciplina().getCodigo().equals(codigoDisciplina)
				&& t.getPeriodoLetivo().getIdentificador().equals(periodoLetivo.getIdentificador())).count();

		return codigoDisciplina + "-" + periodoLetivo.getIdentificador() + "-" + String.format("%02d", contagem + 1);
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
			throw new IllegalArgumentException("Formato de horário inválido: " + horario1 + " or " + horario2);
		}
	}

	public Turma buscarPorCodigo(String codigo) {
		return turmas.stream().filter(t -> t.getCodigo().equalsIgnoreCase(codigo)).findFirst().orElse(null);
	}

	public List<Turma> listarTurmasPorDisciplina(String codigoDisciplina) {
		return turmas.stream().filter(t -> t.getDisciplina().getCodigo().equals(codigoDisciplina))
				.collect(Collectors.toList());
	}

	public List<Turma> listarTurmasPorPeriodo(PeriodoLetivo periodoLetivo) {
		return turmas.stream()
				.filter(t -> t.getPeriodoLetivo().getIdentificador().equals(periodoLetivo.getIdentificador()))
				.collect(Collectors.toList());
	}

	public List<Turma> listarTurmasPorProfessor(String matriculaProfessor) {
		return turmas.stream().filter(t -> t.getProfessor().getMatricula().equals(matriculaProfessor))
				.collect(Collectors.toList());
	}

	public List<Turma> listarTodasAsTurmas() {
		return new ArrayList<>(turmas);
	}

	/**
	 * RF15: O aluno deve poder consultar disciplinas/turmas disponiveis.
	 */
	public List<Turma> consultarTurmasDisponiveis() {
		return turmas.stream()
				.filter(Turma::temVagasDisponiveis)
				.collect(Collectors.toList());
	}

	public List<Turma> consultarTurmasDisponiveisPorPeriodo(String identificadorPeriodo) {
		if (identificadorPeriodo == null || identificadorPeriodo.isBlank()) {
			throw new IllegalArgumentException("Periodo letivo deve ser informado.");
		}

		return turmas.stream()
				.filter(Turma::temVagasDisponiveis)
				.filter(t -> t.getPeriodoLetivo().getIdentificador().equalsIgnoreCase(identificadorPeriodo))
				.collect(Collectors.toList());
	}

	/**
	 * RF16: O aluno deve poder solicitar matricula em uma turma.
	 * RF21: Se não houver vaga, o aluno entra em lista de espera.
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

	/**
	 * RF17: O sistema deve verificar se ha vagas disponiveis.
	 */
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
	 * RF21: Consultar posição do aluno na lista de espera.
	 * Retorna a posição (1-indexed) ou -1 se não estiver na fila.
	 */
	public int obterPosicaoEmEspera(String codigoTurma, String matriculaAluno) {
		Turma turma = buscarPorCodigo(codigoTurma);
		if (turma == null) {
			throw new IllegalArgumentException("Turma não encontrada: " + codigoTurma);
		}
		return turma.obterPosicaoEmEspera(matriculaAluno);
	}

	/**
	 * RF21: Consultar quantidade de alunos na lista de espera.
	 */
	public int consultarQuantidadeEmEspera(String codigoTurma) {
		Turma turma = buscarPorCodigo(codigoTurma);
		if (turma == null) {
			throw new IllegalArgumentException("Turma não encontrada: " + codigoTurma);
		}
		return turma.getTotalEmEspera();
	}

	/**
	 * RF21: Remover aluno da lista de espera (ex: se desistiu da disciplina).
	 */
	public void removerDaEspera(String codigoTurma, String matriculaAluno) {
		Turma turma = buscarPorCodigo(codigoTurma);
		if (turma == null) {
			throw new IllegalArgumentException("Turma não encontrada: " + codigoTurma);
		}
		turma.removerDaEspera(matriculaAluno);
		repository.salvarTodos(turmas);
	}

// RF14: O coordenador deve poder alterar uma turma antes do início das aulas

	public Turma alterarTurma(String codigoTurma, String novaMatriculaProfessor, Integer novoLimiteVagas,
			String novoHorario, String novaSala) {

		Turma turma = buscarPorCodigo(codigoTurma);
		if (turma == null) {
			throw new IllegalArgumentException("Turma não encontrada: " + codigoTurma);
		}

		if (turma.getPeriodoLetivo().getStatus() != StatusPeriodoLetivo.INATIVO) {
			throw new IllegalStateException("Não é possível alterar turma após o início das aulas.");
		}

		if (novaMatriculaProfessor != null && !novaMatriculaProfessor.isBlank()) {
			Usuario u = usuarioService.buscarPorMatricula(novaMatriculaProfessor);
			if (!(u instanceof Professor)) {
				throw new IllegalArgumentException("Professor não encontrado: " + novaMatriculaProfessor);
			}
			Professor novoProfessor = (Professor) u;

			// Valida horário do novo professor (ignora a própria turma)
			for (Turma t : turmas) {
			    if (!t.getCodigo().equals(codigoTurma) &&
			        t.getProfessor().getMatricula().equals(novaMatriculaProfessor)) {
			        if (horarioConflita(t.getHorario(), turma.getHorario())) {
			            throw new IllegalArgumentException(
			                "Professor possui conflito de horário com turma " + t.getCodigo() + ". (RN06)");
			        }
			    }
			}
			turma.setProfessor(novoProfessor);
		}

		if (novoLimiteVagas != null && novoLimiteVagas > 0) {
			turma.setLimiteVagas(novoLimiteVagas);
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

	/**
	 * RF14: O coordenador deve poder cancelar uma turma antes do início das aulas
	 */
	public void cancelarTurma(String codigoTurma) {

		Turma turma = buscarPorCodigo(codigoTurma);
		if (turma == null) {
			throw new IllegalArgumentException("Turma não encontrada: " + codigoTurma);
		}

		if (turma.getPeriodoLetivo().getStatus() != StatusPeriodoLetivo.INATIVO) {
			throw new IllegalStateException("Não é possível cancelar turma após o início das aulas.");
		}

		turmas.remove(turma);
		repository.salvarTodos(turmas);
	}
}
