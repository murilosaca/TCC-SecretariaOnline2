package br.ufpr.sept.so2.modules.tcc.api.dto

import java.math.BigDecimal

data class RegistrarAvaliacaoTccRequest(
    val acao: String? = null,
    val nota: BigDecimal? = null,
    val parecer: String? = null,
)
