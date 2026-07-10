package org.example.classroompb.model;

public class Administrador extends Usuario {

    private static final long serialVersionUID = 1L;

    public Administrador(String nome, String matricula, String email, String senha) {
        super(nome, matricula, email, senha, TipoUsuario.ADMINISTRADOR);
    }
}
