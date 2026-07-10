package org.example.classroompb.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import org.example.classroompb.model.Disciplina;

public class DisciplinaRepository {

    private static final String ARQUIVO = "dados/disciplina.json";
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public DisciplinaRepository() {
        new File("dados").mkdirs();
    }

    public List<Disciplina> carregarTodos() {
        File arquivo = new File(ARQUIVO);

        if (!arquivo.exists()) return new ArrayList<>();

        try (Reader reader = new FileReader(arquivo)) {

            Type tipo = new TypeToken<List<Disciplina>>() {}.getType();
            List<Disciplina> lista = gson.fromJson(reader, tipo);
            return lista != null ? lista : new ArrayList<>();

        } catch (IOException e) {
            System.err.println("Erro ao carregar disciplinas: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public void salvarTodos(List<Disciplina> disciplinas) {
        try (Writer writer = new FileWriter(ARQUIVO)) {
            gson.toJson(disciplinas, writer);
        } catch (IOException e) {
            System.err.println("Erro ao salvar disciplinas: " + e.getMessage());
        }
    }
}
