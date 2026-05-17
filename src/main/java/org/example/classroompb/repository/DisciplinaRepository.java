package org.example.classroompb.repository;

import org.example.classroompb.model.Disciplina;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DisciplinaRepository {
	
	private static final String ARQUIVO = "dados/disciplina.dat";
	
	public DisciplinaRepository() {
		new File("dados").mkdirs();
	}
	
	@SuppressWarnings("unchecked")
	public List<Disciplina> carregarTodos() {
		File arquivo = new File(ARQUIVO);
		
		if(!arquivo.exists()) return new ArrayList<>();
		
		try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(arquivo))) {
            return (List<Disciplina>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Erro ao carregar - " + e.getMessage());
            return new ArrayList<>();
        }
    }
	
	
	public void salvarTodos(List<Disciplina> disciplinas) {
		try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ARQUIVO))) {
			oos.writeObject(disciplinas);
		} catch (IOException e) {
			System.err.println("Erro ao salvar - " + e.getMessage());
		}
	}

}
