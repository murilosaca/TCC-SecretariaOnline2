package br.ufpr.sept.so2.modules.iam.domain

import br.ufpr.sept.so2.shared.domain.exception.ConflitoEstadoException
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import br.ufpr.sept.so2.shared.domain.valueobject.Email
import br.ufpr.sept.so2.shared.domain.valueobject.Grr
import java.time.OffsetDateTime
import java.util.UUID

class Usuario(
    val id: UUID,
    var emailInstitucional: Email,
    var emailPessoal: Email?,
    var grr: Grr?,
    var senhaHash: String,
    var senhaAlterada: Boolean,
    var lgpdAceiteEm: OffsetDateTime?,
    var lgpdAceiteIp: String?,
    var lgpdAceiteUserAgent: String?,
    var ativo: Boolean,
    var falhasConsecutivas: Int,
    var bloqueadoAte: OffsetDateTime?,
    authorities: List<String>?,
    val createdAt: OffsetDateTime,
    var updatedAt: OffsetDateTime,
) {
    private val _authorities: MutableList<String> =
        if (authorities == null) ArrayList() else ArrayList(authorities)

    val authorities: List<String>
        get() = _authorities.toList()

    fun liberarBloqueioSeExpirado(agora: OffsetDateTime) {
        if (bloqueadoAte != null && !bloqueadoAte!!.isAfter(agora)) {
            bloqueadoAte = null
            falhasConsecutivas = 0
            updatedAt = agora
        }
    }

    fun estaBloqueado(agora: OffsetDateTime): Boolean =
        bloqueadoAte != null && bloqueadoAte!!.isAfter(agora)

    fun registrarFalha(agora: OffsetDateTime, maxFalhas: Int, minutosBloqueio: Int): Boolean {
        falhasConsecutivas++
        val bloqueou = falhasConsecutivas >= maxFalhas
        if (bloqueou) {
            bloqueadoAte = agora.plusMinutes(minutosBloqueio.toLong())
        }
        updatedAt = agora
        return bloqueou
    }

    fun registrarLoginOk(agora: OffsetDateTime) {
        falhasConsecutivas = 0
        bloqueadoAte = null
        updatedAt = agora
    }

    fun completarPrimeiroAcesso(novoHash: String, agora: OffsetDateTime, ip: String?, userAgent: String?) {
        if (senhaAlterada) {
            throw ConflitoEstadoException("O primeiro acesso já foi concluído.")
        }
        senhaHash = novoHash
        senhaAlterada = true
        lgpdAceiteEm = agora
        lgpdAceiteIp = ip
        lgpdAceiteUserAgent = truncar(userAgent, 300)
        updatedAt = agora
    }

    fun redefinirSenha(novoHash: String?, agora: OffsetDateTime) {
        if (novoHash.isNullOrBlank()) {
            throw DadoInvalidoException("Hash de senha obrigatório.")
        }
        senhaHash = novoHash
        senhaAlterada = true
        falhasConsecutivas = 0
        bloqueadoAte = null
        updatedAt = agora
    }

    fun precisaPrimeiroAcesso(): Boolean = !senhaAlterada

    fun concederAuthorities(novas: List<String>, agora: OffsetDateTime): Boolean {
        var mudou = false
        for (authority in novas) {
            if (authority != null && !authority.isBlank() && !_authorities.contains(authority)) {
                _authorities.add(authority)
                mudou = true
            }
        }
        if (mudou) {
            updatedAt = agora
        }
        return mudou
    }

    companion object {
        private fun truncar(valor: String?, max: Int): String? {
            if (valor == null) {
                return null
            }
            return if (valor.length <= max) valor else valor.substring(0, max)
        }
    }
}
