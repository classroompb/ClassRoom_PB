package org.example.classroompb.service;

import java.util.List;
import org.example.classroompb.model.Disciplina;
import org.example.classroompb.repository.DisciplinaRepository;

public class DisciplinaService {

    private final DisciplinaRepository repository;
    private List<Disciplina> disciplinas;

    public DisciplinaService(DisciplinaRepository repository) {
        this.repository = repository;
        this.disciplinas = repository.carregarTodos();
    }

    // RF06 - Task de cadastro
    public Disciplina cadastrar(String codigo, String nome, int cargaHora, int creditos) {

        if (codigo == null || codigo.isBlank())
            throw new IllegalArgumentException("Código de disciplina inválido");
        if (nome == null || nome.isBlank())
            throw new IllegalArgumentException("Nome da disciplina inválido");

        if (jaExiste(codigo))
            throw new IllegalArgumentException("Já existe uma disciplina com o código - " + codigo);
        if (cargaHora <= 0)
            throw new IllegalArgumentException("Carga horária deve ser maior que zero");
        if (creditos <= 0) throw new IllegalArgumentException("Crédito deve ser maior que zero");

        Disciplina disciplina = new Disciplina(codigo, nome, cargaHora, creditos);
        disciplinas.add(disciplina);
        repository.salvarTodos(disciplinas);
        return disciplina;
    }

    public Disciplina buscarPorCodigo(String codigo) {

        if (codigo == null) return null;

        return disciplinas.stream()
                .filter(c -> c.getCodigo().equalsIgnoreCase(codigo))
                .findFirst()
                .orElse(null);
    }

    public boolean jaExiste(String codigo) {
        return buscarPorCodigo(codigo) != null;
    }

    public void adicionarPreRequisito(String codigoDisciplina, String codigoPreReq) {
        Disciplina disciplina = buscarPorCodigo(codigoDisciplina);

        if (disciplina == null)
            throw new IllegalArgumentException("Disciplina não encontrada - " + codigoDisciplina);

        if (buscarPorCodigo(codigoPreReq) == null)
            throw new IllegalArgumentException("Pré-requisito não encontrado - " + codigoPreReq);

        disciplina.adicionarPreRequisito(codigoPreReq);
        repository.salvarTodos(disciplinas);
    }

    public List<Disciplina> listarTodos() {
        return disciplinas;
    }
}
