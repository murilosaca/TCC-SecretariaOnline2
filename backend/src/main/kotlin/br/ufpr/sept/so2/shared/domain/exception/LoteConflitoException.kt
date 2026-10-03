package br.ufpr.sept.so2.shared.domain.exception

import java.util.UUID

class LoteConflitoException(
    message: String,
    val failedIds: List<UUID>,
) : ConflitoEstadoException(message)
