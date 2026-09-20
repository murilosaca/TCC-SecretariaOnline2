package br.ufpr.sept.so2.modules.academico.infrastructure.persistence

import java.io.Serializable
import java.util.UUID

class CursoSecretarioId() : Serializable {
    var idCurso: UUID? = null
    var idUsuario: UUID? = null

    constructor(idCurso: UUID, idUsuario: UUID) : this() {
        this.idCurso = idCurso
        this.idUsuario = idUsuario
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is CursoSecretarioId) {
            return false
        }
        return idCurso == other.idCurso && idUsuario == other.idUsuario
    }

    override fun hashCode(): Int {
        var result = idCurso?.hashCode() ?: 0
        result = 31 * result + (idUsuario?.hashCode() ?: 0)
        return result
    }
}
