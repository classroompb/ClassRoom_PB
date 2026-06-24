package org.example.classroompb.exception;

/**
 * RF29: Exceção base para falhas na consulta de frequência por disciplina.
 *
 * <p>Estende {@link IllegalArgumentException} para manter compatibilidade com o
 * restante do sistema, que captura {@code IllegalArgumentException} para sinalizar
 * erros de negócio (ver {@code CLI} e demais services). As subclasses permitem o
 * tratamento específico de cada cenário de erro exigido pela RF29.
 */
public class ConsultaFrequenciaException extends IllegalArgumentException {

    private static final long serialVersionUID = 1L;

    public ConsultaFrequenciaException(String mensagem) {
        super(mensagem);
    }
}