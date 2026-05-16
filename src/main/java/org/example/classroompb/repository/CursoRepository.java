package org.example.classroompb.repository;

import org.example.classroompb.model.Curso;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CursoRepository {

    private static final String ARQUIVO = "dados/cursos.dat";

    public CursoRepository() {
        new File("dados").mkdirs();
    }

    @SuppressWarnings("unchecked")
    public List<Curso> carregarTodos() {
        File arquivo = new File(ARQUIVO);
        if (!arquivo.exists()) return new ArrayList<>();

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(arquivo))) {
            return (List<Curso>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Erro ao carregar cursos: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public void salvarTodos(List<Curso> cursos) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ARQUIVO))) {
            oos.writeObject(cursos);
        } catch (IOException e) {
            System.err.println("Erro ao salvar cursos: " + e.getMessage());
        }
    }
}
