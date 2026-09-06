package br.ufpr.sept.so2.modules.academico.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CursoJpaRepository extends JpaRepository<CursoJpaEntity, UUID> {

    boolean existsBySiglaIgnoreCase(String sigla);

    boolean existsByCodigoIgnoreCase(String codigo);
}
