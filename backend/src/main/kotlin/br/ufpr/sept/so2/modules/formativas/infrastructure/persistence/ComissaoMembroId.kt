package br.ufpr.sept.so2.modules.formativas.infrastructure.persistence

import java.io.Serializable
import java.util.UUID

class ComissaoMembroId() : Serializable {
    var idCurso: UUID? = null
    var idUsuario: UUID? = null
    var tipo: String? = null

    constructor(idCurso: UUID, idUsuario: UUID, tipo: String) : this() {
        this.idCurso = idCurso
        this.idUsuario = idUsuario
        this.tipo = tipo
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is ComissaoMembroId) {
            return false
        }
        return idCurso == other.idCurso && idUsuario == other.idUsuario && tipo == other.tipo
    }

    override fun hashCode(): Int {
        var result = idCurso?.hashCode() ?: 0
        result = 31 * result + (idUsuario?.hashCode() ?: 0)
        result = 31 * result + (tipo?.hashCode() ?: 0)
        return result
    }
}
