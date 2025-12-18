package storage;

import domain.Aluno;

import java.sql.*;
import java.util.List;
import java.util.Optional;

public class H2StudentRepository implements StudentRepository {

    @Override
    public long save(Aluno aluno) {
        String sql = """
        INSERT INTO aluno (matricula, nome, curso, email, ativo)
        VALUES (?, ?, ?, ?, TRUE)
    """;
        try (Connection c = DbBootstrap.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, aluno.getMatricula());
            ps.setString(2, aluno.getNome());
            ps.setString(3, aluno.getCurso());
            ps.setString(4, aluno.getEmail());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    aluno.setId(id);
                    return id;
                }
            }
            return -1L;

        } catch (java.sql.SQLIntegrityConstraintViolationException dup) {
            // matrícula já existe (UNIQUE)
            return -2L; // código especial para duplicado
        } catch (Exception e) {
            e.printStackTrace();
            return -1L;
        }
    }


    @Override
    public Optional<Aluno> findById(long id) {
        String sql = "SELECT id, matricula, nome, curso, email, ativo FROM aluno WHERE id = ?";
        try (Connection c = DbBootstrap.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();

                Aluno a = new Aluno();
                a.setId(rs.getLong("id"));
                a.setMatricula(rs.getLong("matricula"));
                a.setNome(rs.getString("nome"));
                a.setCurso(rs.getString("curso"));
                a.setEmail(rs.getString("email"));
                a.setAtivo(rs.getBoolean("ativo"));
                return Optional.of(a);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public List<long[]> listarChaves() {
        String sql = "SELECT matricula, id FROM aluno";
        var out = new java.util.ArrayList<long[]>();
        try (var c = DbBootstrap.getConnection();
             var ps = c.prepareStatement(sql);
             var rs = ps.executeQuery()) {
            while (rs.next()) {
                long m = rs.getLong("matricula");
                long id = rs.getLong("id");
                out.add(new long[]{m, id});
            }
        } catch (Exception e) { e.printStackTrace(); }
        return out;
    }
    @Override
    public boolean softDelete(long id) {
        String sql = "SELECT matricula, id FROM aluno";
        try (Connection c = DbBootstrap.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    // mantém como TODO por enquanto
    @Override public boolean update(Aluno aluno) { return false; }
    @Override public Optional<Aluno> findByMatricula(long m) { return Optional.empty(); }
}
