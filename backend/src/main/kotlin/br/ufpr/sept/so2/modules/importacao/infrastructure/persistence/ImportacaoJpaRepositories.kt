package br.ufpr.sept.so2.modules.importacao.infrastructure.persistence

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ImportJobJpaRepository : JpaRepository<ImportJobJpaEntity, UUID> {
    fun findByOperadorIdOrderByCreatedAtDesc(operadorId: UUID, pageable: Pageable): Page<ImportJobJpaEntity>
}

interface ImportLinhaJpaRepository : JpaRepository<ImportLinhaJpaEntity, UUID> {
    fun findByImportJobIdOrderByNumeroAsc(importJobId: UUID): List<ImportLinhaJpaEntity>
}

interface ExportJobJpaRepository : JpaRepository<ExportJobJpaEntity, UUID> {
    fun findByOperadorIdOrderByCreatedAtDesc(operadorId: UUID, pageable: Pageable): Page<ExportJobJpaEntity>
}

interface AlocacaoProfessorJpaRepository : JpaRepository<AlocacaoProfessorJpaEntity, UUID> {
    fun existsByIdUsuarioAndIdDisciplina(idUsuario: UUID, idDisciplina: UUID): Boolean
}
