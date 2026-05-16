package org.example.classroompb.service;

import org.example.classroompb.model.Curso;
import org.example.classroompb.repository.CursoRepository;

import java.util.List;

public class CursoService {

    private final CursoRepository repository;
    private List<Curso> cursos;

    public CursoService(CursoRepository repository) {
        this.repository = repository;
        this.cursos = repository.carregarTodos();
    }

    // RF05 - Cadastro de cursos pelo administrador
    public Curso cadastrar(String codigo, String nome) {
        if (codigo == null || codigo.isBlank())
            throw new IllegalArgumentException("Código do curso não pode ser vazio.");
        if (nome == null || nome.isBlank())
            throw new IllegalArgumentException("Nome do curso não pode ser vazio.");

        if (codigoJaExiste(codigo))
            throw new IllegalArgumentException("Já existe um curso com o código: " + codigo);

        Curso curso = new Curso(codigo, nome);
        cursos.add(curso);
        repository.salvarTodos(cursos);
        return curso;
    }

    public Curso buscarPorCodigo(String codigo) {
        if (codigo == null) return null;
        return cursos.stream()
                .filter(c -> c.getCodigo().equalsIgnoreCase(codigo))
                .findFirst().orElse(null);
    }

    public boolean codigoJaExiste(String codigo) {
        return buscarPorCodigo(codigo) != null;
    }

    public List<Curso> listarTodos() {
        return cursos;
    }
}
