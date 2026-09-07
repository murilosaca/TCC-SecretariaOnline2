package br.ufpr.sept.so2.modules.solicitacoes.infrastructure.persistence

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.Optional
import java.util.UUID

interface SolicitacaoJpaRepository : JpaRepository<SolicitacaoJpaEntity, UUID> {

    fun findByProtocolo(protocolo: String): Optional<SolicitacaoJpaEntity>

    @Query(
        """
            SELECT s FROM SolicitacaoJpaEntity s
            WHERE s.solicitanteId = :solicitanteId
              AND (:estado IS NULL OR s.estado = :estado)
              AND (:tipoCodigo IS NULL OR s.tipoCodigo = :tipoCodigo)
              AND (:anoPrefixo IS NULL OR s.protocolo LIKE :anoPrefixo)
            """,
    )
    fun findMinhas(
        @Param("solicitanteId") solicitanteId: UUID,
        @Param("estado") estado: String?,
        @Param("tipoCodigo") tipoCodigo: String?,
        @Param("anoPrefixo") anoPrefixo: String?,
        pageable: Pageable,
    ): Page<SolicitacaoJpaEntity>
}
