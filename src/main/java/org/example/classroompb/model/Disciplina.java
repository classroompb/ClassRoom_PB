package org.example.classroompb.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Disciplina implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String codigo, nome;
	private int cargaHoraria, creditos;
	
	private List<String> preRequisitos;		// lista utiliza o código das disciplinas
	
	
	
	public Disciplina(String codigo, String nome, int cargaHora, int creditos) {
		this.codigo = codigo;
		this.nome = nome;
		this.cargaHoraria = cargaHora;
		this.creditos = creditos;
		
		this.preRequisitos = new ArrayList<String>();
	}

	
	public String getCodigo() {
		return codigo;
	}

	public String getNome() {
		return nome;
	}

	public int getCargaHoraria() {
		return cargaHoraria;
	}

	public int getCreditos() {
		return creditos;
	}

	public List<String> getPreRequisitos() {
		return preRequisitos;
	}
	
	
	public void adicionarPreRequisito(String codigo) {
		this.preRequisitos.add(codigo);
	}
	
}
