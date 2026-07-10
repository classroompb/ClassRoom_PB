package org.example.classroompb.repository;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import org.example.classroompb.model.Avaliacao;

/**
 * Persistência das avaliações em disco (Release 3). Segue o mesmo padrão dos demais repositórios do
 * projeto: serialização de objetos em arquivo .dat na pasta dados/.
 */
public class AvaliacaoRepository {

    private static final String ARQUIVO = "dados/avaliacoes.dat";

    public AvaliacaoRepository() {
        new File("dados").mkdirs();
    }

    @SuppressWarnings("unchecked")
    public List<Avaliacao> carregarTodos() {
        File arquivo = new File(ARQUIVO);
        if (!arquivo.exists()) return new ArrayList<>();

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(arquivo))) {
            return (List<Avaliacao>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Erro ao carregar avaliações: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public void salvarTodos(List<Avaliacao> avaliacoes) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ARQUIVO))) {
            oos.writeObject(avaliacoes);
        } catch (IOException e) {
            System.err.println("Erro ao salvar avaliações: " + e.getMessage());
        }
    }
}
