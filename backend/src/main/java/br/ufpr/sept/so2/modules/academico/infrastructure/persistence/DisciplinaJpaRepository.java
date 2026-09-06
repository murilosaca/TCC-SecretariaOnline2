package br.ufpr.sept.so2.modules.academico.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DisciplinaJpaRepository extends JpaRepository<DisciplinaJpaEntity, UUID> {

    Page<DisciplinaJpaEntity> findByIdCurso(UUID idCurso, Pageable pageable);

    boolean existsByIdCursoAndCodigoIgnoreCase(UUID idCurso, String codigo);
}
