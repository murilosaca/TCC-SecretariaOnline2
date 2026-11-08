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
    var vagas: Int = 0,
) {
    fun atualizar(
        codigo: String?,
        nome: String?,
        periodo: Int?,
        cargaHorariaTotal: Int?,
        creditos: Int?,
        ativa: Boolean?,
        vagas: Int? = null,
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
        if (vagas != null) {
            this.vagas = vagas
        }
        this.updatedAt = OffsetDateTime.now()
    }

    fun consumirVaga() {
        vagas -= 1
        updatedAt = OffsetDateTime.now()
    }
}
