package br.ufpr.sept.so2.modules.solicitacoes.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface TipoSolicitacaoVersaoJpaRepository : JpaRepository<TipoSolicitacaoVersaoJpaEntity, UUID> {
    fun countByTipoId(tipoId: UUID): Long

    @Query("select max(v.versao) from TipoSolicitacaoVersaoJpaEntity v where v.tipoId = :tipoId")
    fun maiorVersao(@Param("tipoId") tipoId: UUID): Int?
}
