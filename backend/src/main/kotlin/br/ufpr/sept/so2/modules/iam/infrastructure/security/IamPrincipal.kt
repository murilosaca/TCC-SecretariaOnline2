package br.ufpr.sept.so2.modules.iam.infrastructure.security

import java.util.UUID

@JvmRecord
data class IamPrincipal(
    val userId: UUID,
    val mustChangePassword: Boolean,
    val authorities: List<String>,
)
