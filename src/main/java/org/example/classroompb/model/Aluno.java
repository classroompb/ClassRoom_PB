package org.example.classroompb.model;

public class Aluno extends Usuario {

    private static final long serialVersionUID = 1L;

    // RF39: curso ao qual o aluno está vinculado. Fica nulo até ser definido via
    // vincularCurso(...) — o cadastro (RF01) não exige curso, para não afetar os demais RFs.
    private String codigoCurso;

    public Aluno(String nome, String matricula, String email, String senha) {
        super(nome, matricula, email, senha, TipoUsuario.ALUNO);
    }

    public String getCodigoCurso() {
        return codigoCurso;
    }

    public void vincularCurso(String codigoCurso) {
        this.codigoCurso = codigoCurso;
    }
}