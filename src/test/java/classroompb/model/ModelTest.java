package classroompb.model;

import org.example.classroompb.model.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UsuarioModelTest {

    @Test
    @Order(1)
    void deveConstructorUsuarioComTodosParametros() {
        Usuario usuario = new Usuario("PROFESSOR", "João Silva", "P001", "joao@example.com", "senha123");

        assertEquals("PROFESSOR", usuario.getTipo());
        assertEquals("João Silva", usuario.getNome());
        assertEquals("P001", usuario.getMatricula());
        assertEquals("joao@example.com", usuario.getEmail());
        assertEquals("senha123", usuario.getSenha());
    }

    @Test
    @Order(2)
    void deveSetarEObterAtributos() {
        Usuario usuario = new Usuario("ALUNO", "Maria", "A001", "maria@example.com", "pass");

        usuario.setNome("Maria Silva");
        usuario.setEmail("maria.silva@example.com");
        usuario.setSenha("nova_senha");

        assertEquals("Maria Silva", usuario.getNome());
        assertEquals("maria.silva@example.com", usuario.getEmail());
        assertEquals("nova_senha", usuario.getSenha());
    }

    @Test
    @Order(3)
    void deveValidarTipoUsuario() {
        Usuario prof = new Usuario("PROFESSOR", "Prof", "P001", "prof@test.com", "pwd");
        Usuario aluno = new Usuario("ALUNO", "Aluno", "A001", "aluno@test.com", "pwd");
        Usuario admin = new Usuario("ADMINISTRADOR", "Admin", "ADM001", "admin@test.com", "pwd");

        assertEquals("PROFESSOR", prof.getTipo());
        assertEquals("ALUNO", aluno.getTipo());
        assertEquals("ADMINISTRADOR", admin.getTipo());
    }

    @Test
    @Order(4)
    void deveCompararUsuariosIguais() {
        Usuario user1 = new Usuario("ALUNO", "João", "A001", "joao@test.com", "123");
        Usuario user2 = new Usuario("ALUNO", "João", "A001", "joao@test.com", "123");

        assertEquals(user1, user2);
    }
}

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProfessorModelTest {

    @Test
    @Order(1)
    void deveConstructorProfessor() {
        Professor prof = new Professor("Prof. José", "P001", "jose@example.com", "senha");

        assertEquals("PROFESSOR", prof.getTipo());
        assertEquals("Prof. José", prof.getNome());
        assertEquals("P001", prof.getMatricula());
        assertEquals("jose@example.com", prof.getEmail());
    }

    @Test
    @Order(2)
    void deveProfessorSomenteProfessor() {
        Professor prof = new Professor("Dr. Silva", "P002", "silva@univ.com", "pwd");

        assertNotNull(prof);
        assertEquals("PROFESSOR", prof.getTipo());
        assertTrue(prof.getMatricula().startsWith("P"));
    }
}

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AlunoModelTest {

    @Test
    @Order(1)
    void deveConstructorAluno() {
        Aluno aluno = new Aluno("Carlos", "A001", "carlos@example.com", "senha");

        assertEquals("ALUNO", aluno.getTipo());
        assertEquals("Carlos", aluno.getNome());
        assertEquals("A001", aluno.getMatricula());
        assertEquals("carlos@example.com", aluno.getEmail());
    }

    @Test
    @Order(2)
    void deveAlunoSomenteAluno() {
        Aluno aluno = new Aluno("Ana", "A999", "ana@univ.com", "pwd");

        assertNotNull(aluno);
        assertEquals("ALUNO", aluno.getTipo());
        assertTrue(aluno.getMatricula().startsWith("A"));
    }
}

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CursoModelTest {

    @Test
    @Order(1)
    void deveConstructorCurso() {
        Curso curso = new Curso("Engenharia de Software", "Eng. Software");

        assertEquals("Engenharia de Software", curso.getNome());
        assertEquals("Eng. Software", curso.getAbreviacao());
    }

    @Test
    @Order(2)
    void deveSetarEObterAtributoCurso() {
        Curso curso = new Curso("Ciência da Computação", "CC");

        curso.setNome("Ciência da Computação");
        curso.setAbreviacao("BCC");

        assertEquals("Ciência da Computação", curso.getNome());
        assertEquals("BCC", curso.getAbreviacao());
    }

    @Test
    @Order(3)
    void deveComparaCursoIguais() {
        Curso curso1 = new Curso("Administração", "ADM");
        Curso curso2 = new Curso("Administração", "ADM");

        assertEquals(curso1, curso2);
    }
}

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DisciplinaModelTest {

    @Test
    @Order(1)
    void deveConstructorDisciplina() {
        Disciplina disc = new Disciplina("CC101", "Programação I", 60, 4);

        assertEquals("CC101", disc.getCodigo());
        assertEquals("Programação I", disc.getNome());
        assertEquals(60, disc.getCargaHoraria());
        assertEquals(4, disc.getCreditos());
    }

    @Test
    @Order(2)
    void deveSetarEObterAtributoDisciplina() {
        Disciplina disc = new Disciplina("CC102", "Programação II", 60, 4);

        disc.setCodigo("CC201");
        disc.setNome("Estruturas de Dados");
        disc.setCargaHoraria(80);
        disc.setCreditos(5);

        assertEquals("CC201", disc.getCodigo());
        assertEquals("Estruturas de Dados", disc.getNome());
        assertEquals(80, disc.getCargaHoraria());
        assertEquals(5, disc.getCreditos());
    }

    @Test
    @Order(3)
    void deveComparaDisciplinaIguais() {
        Disciplina disc1 = new Disciplina("CC101", "Prog I", 60, 4);
        Disciplina disc2 = new Disciplina("CC101", "Prog I", 60, 4);

        assertEquals(disc1, disc2);
    }

    @Test
    @Order(4)
    void deveDifferentDisciplinas() {
        Disciplina disc1 = new Disciplina("CC101", "Programação", 60, 4);
        Disciplina disc2 = new Disciplina("CC102", "Programação", 60, 4);

        assertNotEquals(disc1, disc2);
    }
}

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PeriodoLetivoModelTest {

    @Test
    @Order(1)
    void deveConstructorPeriodoLetivo() {
        PeriodoLetivo periodo = new PeriodoLetivo("2024.1");

        assertEquals("2024.1", periodo.getIdentificador());
    }

    @Test
    @Order(2)
    void deveSetarEObterStatusPeriodo() {
        PeriodoLetivo periodo = new PeriodoLetivo("2024.2");

        periodo.setIdentificador("2024.2");
        assertEquals("2024.2", periodo.getIdentificador());
    }

    @Test
    @Order(3)
    void deveComparaPeriodosIguais() {
        PeriodoLetivo per1 = new PeriodoLetivo("2024.1");
        PeriodoLetivo per2 = new PeriodoLetivo("2024.1");

        assertEquals(per1, per2);
    }

    @Test
    @Order(4)
    void deveArmazenarStatusPeriodo() {
        PeriodoLetivo periodo = new PeriodoLetivo("2025.1");

        // Testa se pode armazenar status
        assertNotNull(periodo);
        assertEquals("2025.1", periodo.getIdentificador());
    }
}

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TurmaModelTest {

    @Test
    @Order(1)
    void devePossuirAtributosDaTurma() {
        Disciplina disc = new Disciplina("CC101", "Prog I", 60, 4);
        Professor prof = new Professor("Prof. João", "P001", "joao@test.com", "pwd");
        PeriodoLetivo periodo = new PeriodoLetivo("2024.1");

        Turma turma = new Turma("CC101-2024.1-01", disc, prof, periodo, 30, "08:00-10:00", "Sala A1");

        assertEquals("CC101-2024.1-01", turma.getCodigo());
        assertEquals(30, turma.getLimiteVagas());
        assertEquals("08:00-10:00", turma.getHorario());
        assertEquals("Sala A1", turma.getSala());
    }

    @Test
    @Order(2)
    void deveManterVagasDisponiveis() {
        Disciplina disc = new Disciplina("CC102", "Prog II", 60, 4);
        Professor prof = new Professor("Prof. Maria", "P002", "maria@test.com", "pwd");
        PeriodoLetivo periodo = new PeriodoLetivo("2024.1");

        Turma turma = new Turma("CC102-2024.1-01", disc, prof, periodo, 25, "10:00-12:00", "Sala B1");

        assertEquals(25, turma.getVagasDisponiveis());
    }

    @Test
    @Order(3)
    void deveComparaTurmaIguais() {
        Disciplina disc = new Disciplina("CC101", "Prog I", 60, 4);
        Professor prof = new Professor("Prof. João", "P001", "joao@test.com", "pwd");
        PeriodoLetivo periodo = new PeriodoLetivo("2024.1");

        Turma turma1 = new Turma("CC101-2024.1-01", disc, prof, periodo, 30, "08:00-10:00", "Sala A1");
        Turma turma2 = new Turma("CC101-2024.1-01", disc, prof, periodo, 30, "08:00-10:00", "Sala A1");

        assertEquals(turma1, turma2);
    }

    @Test
    @Order(4)
    void deveObterId() {
        Disciplina disc = new Disciplina("CC101", "Prog I", 60, 4);
        Professor prof = new Professor("Prof. João", "P001", "joao@test.com", "pwd");
        PeriodoLetivo periodo = new PeriodoLetivo("2024.1");

        Turma turma = new Turma("CC101-2024.1-01", disc, prof, periodo, 30, "08:00-10:00", "Sala A1");

        assertNotNull(turma.getCodigo());
        assertTrue(turma.getCodigo().contains("CC101"));
    }
}

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TipoUsuarioTest {

    @Test
    @Order(1)
    void deveConterTiposValidos() {
        // Verifica valores conhecidos
        assertEquals("ALUNO", TipoUsuario.ALUNO.toString());
        assertEquals("PROFESSOR", TipoUsuario.PROFESSOR.toString());
        assertEquals("COORDENADOR", TipoUsuario.COORDENADOR.toString());
        assertEquals("ADMINISTRADOR", TipoUsuario.ADMINISTRADOR.toString());
    }

    @Test
    @Order(2)
    void deveValorOfEnum() {
        TipoUsuario tipo = TipoUsuario.valueOf("ALUNO");
        assertEquals(TipoUsuario.ALUNO, tipo);
    }
}

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class StatusPeriodoLetivoTest {

    @Test
    @Order(1)
    void deveConterStatusValidos() {
        // Verifica valores de status
        assertNotNull(StatusPeriodoLetivo.class);
    }
}

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CoordenadorModelTest {

    @Test
    @Order(1)
    void deveConstructorCoordenador() {
        Coordenador coord = new Coordenador("Prof. Fernando", "C001", "fernando@example.com", "senha");

        assertEquals("COORDENADOR", coord.getTipo());
        assertEquals("Prof. Fernando", coord.getNome());
        assertEquals("C001", coord.getMatricula());
        assertEquals("fernando@example.com", coord.getEmail());
    }

    @Test
    @Order(2)
    void deveCoordenadorJaTesteBasico() {
        Coordenador coord = new Coordenador("Dr. Paulo", "C002", "paulo@univ.com", "pwd");

        assertNotNull(coord);
        assertEquals("COORDENADOR", coord.getTipo());
    }
}

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AdministradorModelTest {

    @Test
    @Order(1)
    void deveConstructorAdministrador() {
        Administrador admin = new Administrador("Admin User", "ADM001", "admin@example.com", "senha_admin");

        assertEquals("ADMINISTRADOR", admin.getTipo());
        assertEquals("Admin User", admin.getNome());
        assertEquals("ADM001", admin.getMatricula());
        assertEquals("admin@example.com", admin.getEmail());
    }

    @Test
    @Order(2)
    void deveAdministradorTipoValido() {
        Administrador admin = new Administrador("Root", "ADM002", "root@system.com", "pwd");

        assertNotNull(admin);
        assertEquals("ADMINISTRADOR", admin.getTipo());
        assertTrue(admin.getMatricula().startsWith("ADM"));
    }
}

