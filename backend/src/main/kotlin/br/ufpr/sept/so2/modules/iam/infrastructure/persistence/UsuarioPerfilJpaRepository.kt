package br.ufpr.sept.so2.modules.iam.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface UsuarioPerfilJpaRepository : JpaRepository<UsuarioPerfilJpaEntity, UsuarioPerfilId> {
    @Query("select up from UsuarioPerfilJpaEntity up where up.id.usuarioId = :usuarioId")
    fun findByUsuario(@Param("usuarioId") usuarioId: UUID): List<UsuarioPerfilJpaEntity>

    @Query("select up.id.usuarioId from UsuarioPerfilJpaEntity up where up.id.perfilId = :perfilId")
    fun idsUsuarios(@Param("perfilId") perfilId: UUID): List<UUID>

    @Query(
        """
        select count(up) from UsuarioPerfilJpaEntity up, UsuarioJpaEntity u
        where up.id.perfilId = :perfilId and u.id = up.id.usuarioId and u.ativo = true
        """,
    )
    fun contarUsuariosAtivos(@Param("perfilId") perfilId: UUID): Long

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from UsuarioPerfilJpaEntity up where up.id.usuarioId = :usuarioId")
    fun deleteByUsuario(@Param("usuarioId") usuarioId: UUID)

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from UsuarioPerfilJpaEntity up where up.id.perfilId = :perfilId")
    fun deleteByPerfil(@Param("perfilId") perfilId: UUID)
}
