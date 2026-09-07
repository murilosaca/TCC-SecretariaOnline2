package br.ufpr.sept.so2.modules.academico.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface AlunoJpaRepository extends JpaRepository<AlunoJpaEntity, UUID>, JpaSpecificationExecutor<AlunoJpaEntity> {

    Optional<AlunoJpaEntity> findByGrrIgnoreCase(String grr);

    Optional<AlunoJpaEntity> findByEmailInstitucionalIgnoreCase(String email);

    boolean existsByGrrIgnoreCase(String grr);

    boolean existsByEmailInstitucionalIgnoreCase(String email);

    Page<AlunoJpaEntity> findByIdCurso(UUID idCurso, Pageable pageable);
}
