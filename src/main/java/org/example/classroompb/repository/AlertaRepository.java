package org.example.classroompb.repository;

import org.example.classroompb.model.Alerta;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * RF30: Repositório de alertas (persistência em disco).
 *
 * <p>Segue o mesmo padrão de persistência dos outros repositórios do projeto,
 * salvando e carregando objetos Alerta em arquivo .dat serializado.
 */
public abstract class AlertaRepository {

    protected static final String ARQUIVO_ALERTAS = "dados/alertas.dat";

    public AlertaRepository() {
        garantirDiretorio();
    }

    protected void garantirDiretorio() {
        new File("dados").mkdirs();
    }

    /**
     * Carrega todos os alertas do arquivo de persistência.
     */
    public abstract List<Alerta> carregarTodos();

    /**
     * Salva todos os alertas no arquivo de persistência.
     */
    public abstract void salvarTodos(List<Alerta> alertas);

    /**
     * Implementação padrão com persistência em disco.
     */
    public static class AlertaRepositoryImpl extends AlertaRepository {

        @Override
        public List<Alerta> carregarTodos() {
            File arquivo = new File(ARQUIVO_ALERTAS);
            if (!arquivo.exists()) {
                return new ArrayList<>();
            }

            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(arquivo))) {
                @SuppressWarnings("unchecked")
                List<Alerta> alertas = (List<Alerta>) ois.readObject();
                return alertas;
            } catch (Exception e) {
                System.err.println("Erro ao carregar alertas: " + e.getMessage());
                return new ArrayList<>();
            }
        }

        @Override
        public void salvarTodos(List<Alerta> alertas) {
            try (ObjectOutputStream oos = new ObjectOutputStream(
                    new FileOutputStream(ARQUIVO_ALERTAS))) {
                oos.writeObject(alertas);
            } catch (Exception e) {
                System.err.println("Erro ao salvar alertas: " + e.getMessage());
            }
        }
    }
}
