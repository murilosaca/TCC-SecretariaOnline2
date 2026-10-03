package br.ufpr.sept.so2.modules.iam.application

import java.util.UUID

data class PerfilResumo(
    val id: UUID,
    val nome: String,
    val descricao: String?,
    val tipo: String,
    val authorities: List<String>,
    val usuariosAtivos: Int,
)

data class AuthorityResumo(
    val nome: String,
    val descricao: String,
    val modulo: String,
    val sistema: Boolean,
)

data class MatrizFgac(
    val authorities: List<AuthorityResumo>,
    val perfis: List<PerfilResumo>,
)

data class AtribuicaoPerfis(
    val usuarioId: UUID,
    val selecionados: List<UUID>,
    val perfis: List<PerfilResumo>,
)
