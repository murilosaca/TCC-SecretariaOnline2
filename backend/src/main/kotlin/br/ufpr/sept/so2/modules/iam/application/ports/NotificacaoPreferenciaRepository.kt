package br.ufpr.sept.so2.modules.iam.application.ports

import br.ufpr.sept.so2.modules.iam.domain.NotificacaoPreferencia
import java.util.Optional
import java.util.UUID

interface NotificacaoPreferenciaRepository {
    fun findByUsuarioId(usuarioId: UUID): Optional<NotificacaoPreferencia>

    fun save(preferencia: NotificacaoPreferencia): NotificacaoPreferencia
}
