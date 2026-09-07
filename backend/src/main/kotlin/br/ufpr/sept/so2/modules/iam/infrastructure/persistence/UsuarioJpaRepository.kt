package br.ufpr.sept.so2.modules.iam.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.Optional
import java.util.UUID

interface UsuarioJpaRepository : JpaRepository<UsuarioJpaEntity, UUID> {
    fun findByGrrIgnoreCase(grr: String): Optional<UsuarioJpaEntity>

    @Query(
        """
            select u from UsuarioJpaEntity u
            where lower(u.emailInstitucional) = lower(:email)
               or lower(u.emailPessoal) = lower(:email)
            """,
    )
    fun findByAnyEmail(@Param("email") email: String): Optional<UsuarioJpaEntity>
}
