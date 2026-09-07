package br.ufpr.sept.so2.modules.academico.domain

import java.time.OffsetDateTime
import java.util.UUID

class Curso(
    val id: UUID,
    var nome: String,
    var sigla: String,
    var codigo: String,
    var idCoordenador: UUID?,
    var horasFormativasMinimas: Int,
    @get:JvmName("isAtivo")
    var ativo: Boolean,
    val createdAt: OffsetDateTime,
    var updatedAt: OffsetDateTime,
) {
    fun atualizar(
        nome: String?,
        sigla: String?,
        codigo: String?,
        idCoordenador: UUID?,
        horasFormativasMinimas: Int?,
        ativo: Boolean?,
    ) {
        if (nome != null) {
            this.nome = nome
        }
        if (sigla != null) {
            this.sigla = sigla
        }
        if (codigo != null) {
            this.codigo = codigo
        }
        if (idCoordenador != null) {
            this.idCoordenador = idCoordenador
        }
        if (horasFormativasMinimas != null) {
            this.horasFormativasMinimas = horasFormativasMinimas
        }
        if (ativo != null) {
            this.ativo = ativo
        }
        this.updatedAt = OffsetDateTime.now()
    }
}
