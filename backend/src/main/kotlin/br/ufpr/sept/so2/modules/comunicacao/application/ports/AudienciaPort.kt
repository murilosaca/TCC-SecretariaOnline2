package br.ufpr.sept.so2.modules.comunicacao.application.ports

import br.ufpr.sept.so2.modules.comunicacao.domain.AudienciaOpcao
import br.ufpr.sept.so2.modules.comunicacao.domain.TipoAudiencia
import java.util.UUID

data class DestinatarioComunicacao(
    val usuarioId: UUID,
    val email: String,
)

interface AudienciaPort {
    fun opcoes(professorId: UUID): List<AudienciaOpcao>

    fun professorPode(professorId: UUID, tipo: TipoAudiencia, audienciaId: UUID): Boolean

    fun destinatarios(tipo: TipoAudiencia, audienciaId: UUID, autorId: UUID): List<DestinatarioComunicacao>
}
