package org.example.classroompb.model;

import java.io.Serializable;

public abstract class Usuario implements Serializable {

    private static final long serialVersionUID = 1L;

    private String nome;
    private String matricula;
    private String email;
    private String senha;
    private TipoUsuario tipo;

    public Usuario(String nome, String matricula, String email, String senha, TipoUsuario tipo) {
        this.nome = nome;
        this.matricula = matricula;
        this.email = email;
        this.senha = senha;
        this.tipo = tipo;
    }

    public String getNome() {
        return nome;
    }

    public String getMatricula() {
        return matricula;
    }

    public String getEmail() {
        return email;
    }

    public String getSenha() {
        return senha;
    }

    public TipoUsuario getTipo() {
        return tipo;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    @Override
    public String toString() {
        return "[" + tipo + "] " + nome + " | Matrícula: " + matricula + " | Email: " + email;
    }
}
