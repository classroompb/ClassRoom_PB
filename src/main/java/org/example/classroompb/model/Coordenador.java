package org.example.classroompb.model;

public class Coordenador extends Usuario {

    private static final long serialVersionUID = 1L;

    public Coordenador(String nome, String matricula, String email, String senha) {
        super(nome, matricula, email, senha, TipoUsuario.COORDENADOR);
    }
}