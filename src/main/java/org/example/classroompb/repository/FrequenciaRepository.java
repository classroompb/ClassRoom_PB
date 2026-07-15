package org.example.classroompb.repository;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import org.example.classroompb.model.RegistroFrequencia;

/**
 * RF27: Persistência dos registros de frequência em disco.
 *
 * <p>Segue o mesmo padrão de {@link TurmaRepository}: serialização de objetos para um arquivo
 * {@code .dat} na pasta {@code dados/}. Métodos sobrescrevíveis para permitir repositórios fake em
 * testes.
 */
public class FrequenciaRepository {

    private static final String ARQUIVO = "dados/frequencias.dat";

    public FrequenciaRepository() {
        new File("dados").mkdirs();
    }

    @SuppressWarnings("unchecked")
    public List<RegistroFrequencia> carregarTodos() {
        File arquivo = new File(ARQUIVO);
        if (!arquivo.exists()) return new ArrayList<>();

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(arquivo))) {
            return (List<RegistroFrequencia>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Erro ao carregar frequências: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public void salvarTodos(List<RegistroFrequencia> registros) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ARQUIVO))) {
            oos.writeObject(registros);
        } catch (IOException e) {
            System.err.println("Erro ao salvar frequências: " + e.getMessage());
        }
    }
}
