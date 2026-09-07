package br.ufpr.sept.so2.modules.iam.application.ports

import br.ufpr.sept.so2.modules.iam.domain.RefreshSessao
import java.util.Optional
import java.util.UUID

interface RefreshTokenRepository {
    fun save(sessao: RefreshSessao): RefreshSessao

    fun lockByTokenHash(tokenHash: String): Optional<RefreshSessao>

    fun revokeAllByUsuarioId(usuarioId: UUID)
}
