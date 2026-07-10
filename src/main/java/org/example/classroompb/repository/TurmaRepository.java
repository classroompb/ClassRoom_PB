package org.example.classroompb.repository;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import org.example.classroompb.model.Turma;

public class TurmaRepository {

    private static final String ARQUIVO = "dados/turmas.dat";

    public TurmaRepository() {
        new File("dados").mkdirs();
    }

    @SuppressWarnings("unchecked")
    public List<Turma> carregarTodos() {
        File arquivo = new File(ARQUIVO);
        if (!arquivo.exists()) return new ArrayList<>();

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(arquivo))) {
            return (List<Turma>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Erro ao carregar turmas: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public void salvarTodos(List<Turma> turmas) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ARQUIVO))) {
            oos.writeObject(turmas);
        } catch (IOException e) {
            System.err.println("Erro ao salvar turmas: " + e.getMessage());
        }
    }
}
