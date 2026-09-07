package br.ufpr.sept.so2.modules.academico.domain

import java.time.OffsetDateTime
import java.util.UUID

class Disciplina(
    val id: UUID,
    val idCurso: UUID,
    var codigo: String,
    var nome: String,
    var periodo: Int,
    var cargaHorariaTotal: Int,
    var creditos: Int,
    @get:JvmName("isAtiva")
    var ativa: Boolean,
    val createdAt: OffsetDateTime,
    var updatedAt: OffsetDateTime,
) {
    fun atualizar(
        codigo: String?,
        nome: String?,
        periodo: Int?,
        cargaHorariaTotal: Int?,
        creditos: Int?,
        ativa: Boolean?,
    ) {
        if (codigo != null) {
            this.codigo = codigo
        }
        if (nome != null) {
            this.nome = nome
        }
        if (periodo != null) {
            this.periodo = periodo
        }
        if (cargaHorariaTotal != null) {
            this.cargaHorariaTotal = cargaHorariaTotal
        }
        if (creditos != null) {
            this.creditos = creditos
        }
        if (ativa != null) {
            this.ativa = ativa
        }
        this.updatedAt = OffsetDateTime.now()
    }
}
