package org.example.classroompb.model;

import java.io.Serializable;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class Turma implements Serializable {

    private static final long serialVersionUID = 1L;

    private String codigo;
    private Disciplina disciplina;
    private Professor professor;
    private PeriodoLetivo periodoLetivo;
    private int limiteVagas;
    private int vagasDisponiveis;
    private String horario; // Formato: "HH:mm-HH:mm" (ex: "08:00-10:00")
    private String sala;
    private List<String> alunoMatriculados; // Lista de matrículas de alunos
    private List<String> alunosEmEspera; // RF21: Lista de espera para quando não há vagas

    public Turma(String codigo, Disciplina disciplina, Professor professor, 
                 PeriodoLetivo periodoLetivo, int limiteVagas, String horario, String sala) {
        this.codigo = codigo;
        this.disciplina = disciplina;
        this.professor = professor;
        this.periodoLetivo = periodoLetivo;
        this.limiteVagas = limiteVagas;
        this.vagasDisponiveis = limiteVagas;
        this.horario = horario;
        this.sala = sala;
        this.alunoMatriculados = new ArrayList<>();
        this.alunosEmEspera = new ArrayList<>();
    }

    // Getters
    public String getCodigo() { return codigo; }
    public Disciplina getDisciplina() { return disciplina; }
    public Professor getProfessor() { return professor; }
    public PeriodoLetivo getPeriodoLetivo() { return periodoLetivo; }
    public int getLimiteVagas() { return limiteVagas; }
    public int getVagasDisponiveis() { return vagasDisponiveis; }
    public String getHorario() { return horario; }
    public String getSala() { return sala; }
    public List<String> getAlunoMatriculados() { return new ArrayList<>(alunoMatriculados); }
    public int getTotalMatriculados() { return alunoMatriculados.size(); }
    public List<String> getAlunosEmEspera() { return new ArrayList<>(alunosEmEspera); } // RF21
    public int getTotalEmEspera() { return alunosEmEspera.size(); } // RF21

    public void setProfessor(Professor professor) {
		this.professor = professor;
	}

	public void setLimiteVagas(int limiteVagas) {
		this.limiteVagas = limiteVagas;
	}

	/**
	 * Ajusta o limite de vagas mantendo a coerência com as vagas disponíveis.
	 * Ao aumentar o limite, novas vagas são liberadas (base para o RF24).
	 * Não permite reduzir abaixo do número de alunos já matriculados.
	 */
	public void ajustarLimiteVagas(int novoLimite) {
		if (novoLimite <= 0) {
			throw new IllegalArgumentException("Limite de vagas deve ser maior que zero.");
		}
		int matriculados = alunoMatriculados.size();
		if (novoLimite < matriculados) {
			throw new IllegalArgumentException("Novo limite (" + novoLimite
					+ ") não pode ser menor que o número de alunos já matriculados (" + matriculados + ").");
		}
		this.limiteVagas = novoLimite;
		this.vagasDisponiveis = novoLimite - matriculados;
	}

	public void setHorario(String horario) {
		this.horario = horario;
	}

	public void setSala(String sala) {
		this.sala = sala;
	}

	// Métodos de negócio
    public boolean temVagasDisponiveis() {
        return vagasDisponiveis > 0;
    }

    public boolean alunoJaMatriculado(String matriculaAluno) {
        return alunoMatriculados.contains(matriculaAluno);
    }

    public void matricularAluno(String matriculaAluno) {
        if (!temVagasDisponiveis()) {
            throw new IllegalArgumentException("Turma sem vagas disponíveis.");
        }
        if (alunoJaMatriculado(matriculaAluno)) {
            throw new IllegalArgumentException("Aluno já matriculado nesta turma. (RN01)");
        }
        alunoMatriculados.add(matriculaAluno);
        vagasDisponiveis--;
    }

    public void cancelarMatricula(String matriculaAluno) {
        if (alunoMatriculados.remove(matriculaAluno)) {
            vagasDisponiveis++;
        }
    }

    // RF21: Métodos para gerenciar lista de espera
    public boolean alunoJaEmEspera(String matriculaAluno) {
        return alunosEmEspera.contains(matriculaAluno);
    }

    public void adicionarAlunoEmEspera(String matriculaAluno) {
        if (alunoJaMatriculado(matriculaAluno)) {
            throw new IllegalArgumentException("Aluno já está matriculado nesta turma. (RN01)");
        }
        if (alunoJaEmEspera(matriculaAluno)) {
            throw new IllegalArgumentException("Aluno já está em lista de espera. (RF21)");
        }
        alunosEmEspera.add(matriculaAluno);
    }

    public String promoverDaEspera() {
        if (alunosEmEspera.isEmpty()) {
            return null;
        }
        String proximoAluno = alunosEmEspera.remove(0);
        if (temVagasDisponiveis()) {
            alunoMatriculados.add(proximoAluno);
            vagasDisponiveis--;
            return proximoAluno;
        }
        alunosEmEspera.add(0, proximoAluno); // Re-adiciona no início se ainda não há vagas
        return null;
    }

    public int obterPosicaoEmEspera(String matriculaAluno) {
        int indice = alunosEmEspera.indexOf(matriculaAluno);
        return indice >= 0 ? indice + 1 : -1; // Retorna 1-indexed, ou -1 se não está
    }

    public void removerDaEspera(String matriculaAluno) {
        alunosEmEspera.remove(matriculaAluno);
    }

    @Override
    public String toString() {
        return "Turma{" +
                "codigo='" + codigo + '\'' +
                ", disciplina=" + disciplina.getNome() +
                ", professor=" + professor.getNome() +
                ", horario='" + horario + '\'' +
                ", sala='" + sala + '\'' +
                ", vagas=" + vagasDisponiveis + "/" + limiteVagas +
                ", emEspera=" + alunosEmEspera.size() +
                '}';
    }
}

