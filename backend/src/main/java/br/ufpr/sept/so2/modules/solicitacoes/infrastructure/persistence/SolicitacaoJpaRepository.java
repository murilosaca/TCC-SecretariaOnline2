package br.ufpr.sept.so2.modules.solicitacoes.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface SolicitacaoJpaRepository extends JpaRepository<SolicitacaoJpaEntity, UUID> {

    Optional<SolicitacaoJpaEntity> findByProtocolo(String protocolo);

    @Query("""
            SELECT s FROM SolicitacaoJpaEntity s
            WHERE s.solicitanteId = :solicitanteId
              AND (:estado IS NULL OR s.estado = :estado)
              AND (:tipoCodigo IS NULL OR s.tipoCodigo = :tipoCodigo)
              AND (:anoPrefixo IS NULL OR s.protocolo LIKE :anoPrefixo)
            """)
    Page<SolicitacaoJpaEntity> findMinhas(
            @Param("solicitanteId") UUID solicitanteId,
            @Param("estado") String estado,
            @Param("tipoCodigo") String tipoCodigo,
            @Param("anoPrefixo") String anoPrefixo,
            Pageable pageable
    );
}
