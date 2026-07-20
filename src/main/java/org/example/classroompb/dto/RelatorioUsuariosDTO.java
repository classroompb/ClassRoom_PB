package org.example.classroompb.dto;

/**
 * Relatório geral de usuários cadastrados (RF43). Um snapshot com a contagem por perfil e o total,
 * para o administrador ter a visão de quantas pessoas de cada tipo existem no sistema.
 */
public record RelatorioUsuariosDTO(
        int totalAlunos,
        int totalProfessores,
        int totalCoordenadores,
        int totalAdministradores,
        int total) {

    @Override
    public String toString() {
        return "Usuários cadastrados: "
                + total
                + " (alunos="
                + totalAlunos
                + ", professores="
                + totalProfessores
                + ", coordenadores="
                + totalCoordenadores
                + ", administradores="
                + totalAdministradores
                + ")";
    }
}
