package br.ufpr.sept.so2.modules.academico.application.ports;

import br.ufpr.sept.so2.modules.academico.domain.Curso;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface CursoRepository {

    Curso save(Curso curso);

    Optional<Curso> findById(UUID id);

    Page<Curso> findAll(Pageable pageable);

    boolean existsBySigla(String sigla);

    boolean existsByCodigo(String codigo);

    void deleteById(UUID id);
}
