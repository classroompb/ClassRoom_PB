package org.example.classroompb.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.example.classroompb.model.PeriodoLetivo;

public class PeriodoLetivoRepository {

    private static final String ARQUIVO_PADRAO = "dados/periodos-letivos.json";
    private final Gson gson;
    private final String arquivoDados;

    public PeriodoLetivoRepository() {
        this(ARQUIVO_PADRAO);
    }

    public PeriodoLetivoRepository(String arquivoDados) {
        this.arquivoDados = arquivoDados;
        File arquivo = new File(arquivoDados);
        File pasta = arquivo.getParentFile();
        if (pasta != null) {
            pasta.mkdirs();
        }
        // Configurar Gson com TypeAdapter para LocalDate
        this.gson =
                new GsonBuilder()
                        .setPrettyPrinting()
                        .registerTypeAdapter(
                                LocalDate.class,
                                new JsonSerializer<LocalDate>() {
                                    @Override
                                    public com.google.gson.JsonElement serialize(
                                            LocalDate src,
                                            Type typeOfSrc,
                                            JsonSerializationContext context) {
                                        return new com.google.gson.JsonPrimitive(src.toString());
                                    }
                                })
                        .registerTypeAdapter(
                                LocalDate.class,
                                new JsonDeserializer<LocalDate>() {
                                    @Override
                                    public LocalDate deserialize(
                                            JsonElement json,
                                            Type typeOfDest,
                                            JsonDeserializationContext context)
                                            throws com.google.gson.JsonParseException {
                                        return LocalDate.parse(json.getAsString());
                                    }
                                })
                        .create();
    }

    public List<PeriodoLetivo> carregarTodos() {
        File arquivo = new File(arquivoDados);

        if (!arquivo.exists()) {
            return new ArrayList<>();
        }

        try (Reader reader = new FileReader(arquivo)) {
            Type tipo = new TypeToken<List<PeriodoLetivo>>() {}.getType();
            List<PeriodoLetivo> lista = gson.fromJson(reader, tipo);
            return lista != null ? lista : new ArrayList<>();
        } catch (IOException | JsonSyntaxException e) {
            System.err.println("Erro ao carregar periodos letivos: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public void salvarTodos(List<PeriodoLetivo> periodos) {
        try (Writer writer = new FileWriter(arquivoDados)) {
            gson.toJson(periodos, writer);
        } catch (IOException e) {
            System.err.println("Erro ao salvar periodos letivos: " + e.getMessage());
        }
    }
}
