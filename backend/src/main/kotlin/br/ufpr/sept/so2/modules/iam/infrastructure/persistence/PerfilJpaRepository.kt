package br.ufpr.sept.so2.modules.iam.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface AuthorityJpaRepository : JpaRepository<AuthorityJpaEntity, String>

interface PerfilJpaRepository : JpaRepository<PerfilJpaEntity, java.util.UUID> {
    fun findByNomeIgnoreCase(nome: String): java.util.Optional<PerfilJpaEntity>
}
