package br.ufpr.sept.so2.modules.academico.application.ports;

import br.ufpr.sept.so2.modules.academico.domain.Disciplina;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface DisciplinaRepository {

    Disciplina save(Disciplina disciplina);

    Optional<Disciplina> findById(UUID id);

    Page<Disciplina> findAll(UUID idCurso, Pageable pageable);

    boolean existsByCursoAndCodigo(UUID idCurso, String codigo);

    void deleteById(UUID id);
}
