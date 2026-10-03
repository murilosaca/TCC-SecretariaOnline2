package br.ufpr.sept.so2.modules.comunicacao.infrastructure.persistence

import br.ufpr.sept.so2.modules.comunicacao.domain.Comunicacao
import br.ufpr.sept.so2.modules.comunicacao.domain.PrioridadeComunicacao
import br.ufpr.sept.so2.modules.comunicacao.domain.TipoAudiencia
import br.ufpr.sept.so2.modules.comunicacao.domain.TipoComunicacao
import br.ufpr.sept.so2.shared.infrastructure.persistence.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "comunicacao")
class ComunicacaoJpaEntity : BaseEntity() {
    @Column(nullable = false, length = 20)
    var tipo: String = ""

    @Column(nullable = false, length = 200)
    var titulo: String = ""

    @Column(nullable = false, columnDefinition = "TEXT")
    var corpo: String = ""

    @Column(nullable = false, length = 20)
    var prioridade: String = ""

    @Column(name = "id_autor", nullable = false)
    var autorId: UUID? = null

    @Column(name = "audiencia_tipo", nullable = false, length = 20)
    var audienciaTipo: String = ""

    @Column(name = "audiencia_id", nullable = false)
    var audienciaId: UUID? = null

    @Column(name = "expires_at")
    var expiresAt: OffsetDateTime? = null

    fun toDomain(): Comunicacao = Comunicacao(
        requireNotNull(id),
        TipoComunicacao.valueOf(tipo),
        titulo,
        corpo,
        PrioridadeComunicacao.valueOf(prioridade),
        requireNotNull(autorId),
        TipoAudiencia.valueOf(audienciaTipo),
        requireNotNull(audienciaId),
        expiresAt,
        requireNotNull(createdAt),
    )

    companion object {
        fun fromDomain(comunicacao: Comunicacao) = ComunicacaoJpaEntity().apply {
            id = comunicacao.id
            tipo = comunicacao.tipo.name
            titulo = comunicacao.titulo
            corpo = comunicacao.corpo
            prioridade = comunicacao.prioridade.name
            autorId = comunicacao.autorId
            audienciaTipo = comunicacao.audienciaTipo.name
            audienciaId = comunicacao.audienciaId
            expiresAt = comunicacao.expiresAt
        }
    }
}
