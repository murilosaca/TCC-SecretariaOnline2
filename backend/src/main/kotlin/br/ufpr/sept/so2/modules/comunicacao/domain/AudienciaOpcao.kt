package br.ufpr.sept.so2.modules.comunicacao.domain

import java.util.UUID

data class AudienciaOpcao(
    val tipo: TipoAudiencia,
    val id: UUID,
    val rotulo: String,
)
