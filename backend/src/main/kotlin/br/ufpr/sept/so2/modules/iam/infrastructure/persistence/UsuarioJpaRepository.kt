package br.ufpr.sept.so2.modules.iam.infrastructure.persistence

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
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

    @Query(
        """
            select distinct u from UsuarioJpaEntity u
            join u.authorities a
            where a = :authority and u.ativo = true
            """,
    )
    fun findAtivosByAuthority(@Param("authority") authority: String): List<UsuarioJpaEntity>

    @Query(
        value = """
            select u from UsuarioJpaEntity u
            where :termo is null or :termo = ''
               or lower(u.nome) like lower(concat('%', :termo, '%'))
               or lower(u.emailInstitucional) like lower(concat('%', :termo, '%'))
               or lower(coalesce(u.emailPessoal, '')) like lower(concat('%', :termo, '%'))
               or lower(coalesce(u.grr, '')) like lower(concat('%', :termo, '%'))
            """,
        countQuery = """
            select count(u) from UsuarioJpaEntity u
            where :termo is null or :termo = ''
               or lower(u.nome) like lower(concat('%', :termo, '%'))
               or lower(u.emailInstitucional) like lower(concat('%', :termo, '%'))
               or lower(coalesce(u.emailPessoal, '')) like lower(concat('%', :termo, '%'))
               or lower(coalesce(u.grr, '')) like lower(concat('%', :termo, '%'))
            """,
    )
    fun search(@Param("termo") termo: String?, pageable: Pageable): Page<UsuarioJpaEntity>
}
