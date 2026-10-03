package br.ufpr.sept.so2.modules.comunicacao.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
import java.util.UUID

interface TemplateComunicacaoJpaRepository : JpaRepository<TemplateComunicacaoJpaEntity, UUID> {
    fun existsByNome(nome: String): Boolean

    fun findByNome(nome: String): Optional<TemplateComunicacaoJpaEntity>
}

interface TemplateRevisaoJpaRepository : JpaRepository<TemplateRevisaoJpaEntity, UUID> {
    fun findByTemplateIdOrderByVersaoDesc(templateId: UUID): List<TemplateRevisaoJpaEntity>

    fun findByTemplateIdAndStatus(templateId: UUID, status: String): Optional<TemplateRevisaoJpaEntity>
}
