package org.example.classroompb.model;

public class Professor extends Usuario {

    private static final long serialVersionUID = 1L;

    public Professor(String nome, String matricula, String email, String senha) {
        super(nome, matricula, email, senha, TipoUsuario.PROFESSOR);
    }
}