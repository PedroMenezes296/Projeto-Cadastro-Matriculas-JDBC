package storage;

import domain.Aluno;
import java.util.Optional;
// storage/StudentRepository.java
import java.util.List;
public interface StudentRepository {
    long save(Aluno aluno);
    List<long[]> listarChaves(); // cada item = {matricula, id}
    Optional<Aluno> findById(long id);
    boolean update(Aluno aluno);
    boolean softDelete(long id);
    Optional<Aluno> findByMatricula(long matricula); // <- long
}
