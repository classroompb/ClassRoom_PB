package classroompb.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import org.example.classroompb.dto.RelatorioUsuariosDTO;
import org.example.classroompb.exception.AcessoNegadoException;
import org.example.classroompb.model.Usuario;
import org.example.classroompb.repository.UsuarioRepository;
import org.example.classroompb.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** RF43 — o administrador gera o relatório geral de usuários cadastrados. */
class RF43RelatorioGeralUsuariosTest {

    private UsuarioService usuarioService;

    static class UsuarioRepositorioFake extends UsuarioRepository {
        private final List<Usuario> lista = new ArrayList<>();

        @Override
        public List<Usuario> carregarTodos() {
            return new ArrayList<>(lista);
        }

        @Override
        public void salvarTodos(List<Usuario> usuarios) {
            lista.clear();
            lista.addAll(usuarios);
        }
    }

    @BeforeEach
    void setUp() {
        usuarioService = new UsuarioService(new UsuarioRepositorioFake());
        usuarioService.cadastrar("ADMINISTRADOR", "Admin", "ADM1", "adm@example.com", "1234");
        usuarioService.cadastrar("COORDENADOR", "Coord", "C001", "coord@example.com", "1234");
        usuarioService.cadastrar("PROFESSOR", "Prof 1", "P001", "p1@example.com", "1234");
        usuarioService.cadastrar("PROFESSOR", "Prof 2", "P002", "p2@example.com", "1234");
        usuarioService.cadastrar("ALUNO", "Aluno 1", "A001", "a1@example.com", "1234");
        usuarioService.cadastrar("ALUNO", "Aluno 2", "A002", "a2@example.com", "1234");
        usuarioService.cadastrar("ALUNO", "Aluno 3", "A003", "a3@example.com", "1234");
    }

    @Test
    void contaUsuariosPorPerfilEOTotal() {
        Usuario admin = usuarioService.buscarPorMatricula("ADM1");

        RelatorioUsuariosDTO relatorio = usuarioService.relatorioGeralUsuarios(admin);

        assertEquals(3, relatorio.totalAlunos());
        assertEquals(2, relatorio.totalProfessores());
        assertEquals(1, relatorio.totalCoordenadores());
        assertEquals(1, relatorio.totalAdministradores());
        assertEquals(7, relatorio.total());
    }

    @Test
    void naoAdministradorRecebeAcessoNegado() {
        Usuario coordenador = usuarioService.buscarPorMatricula("C001");
        Usuario professor = usuarioService.buscarPorMatricula("P001");
        Usuario aluno = usuarioService.buscarPorMatricula("A001");

        assertThrows(
                AcessoNegadoException.class,
                () -> usuarioService.relatorioGeralUsuarios(coordenador));
        assertThrows(
                AcessoNegadoException.class,
                () -> usuarioService.relatorioGeralUsuarios(professor));
        assertThrows(
                AcessoNegadoException.class, () -> usuarioService.relatorioGeralUsuarios(aluno));
        assertThrows(
                AcessoNegadoException.class, () -> usuarioService.relatorioGeralUsuarios(null));
    }
}
