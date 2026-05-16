package org.example.classroompb.model;

import java.io.Serializable;

public class Curso implements Serializable {

    private static final long serialVersionUID = 1L;

    private String codigo;
    private String nome;

    public Curso(String codigo, String nome) {
        this.codigo = codigo;
        this.nome = nome;
    }

    public String getCodigo() { return codigo; }
    public String getNome()   { return nome; }

    @Override
    public String toString() {
        return "[" + codigo + "] " + nome;
    }
}
