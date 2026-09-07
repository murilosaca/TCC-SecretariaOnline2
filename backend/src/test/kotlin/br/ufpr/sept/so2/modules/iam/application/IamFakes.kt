package br.ufpr.sept.so2.modules.iam.application

import br.ufpr.sept.so2.modules.iam.application.ports.AuditLogPort
import br.ufpr.sept.so2.modules.iam.application.ports.JtiBlacklistRepository
import br.ufpr.sept.so2.modules.iam.application.ports.JwtTokenService
import br.ufpr.sept.so2.modules.iam.application.ports.OpaqueTokenHasher
import br.ufpr.sept.so2.modules.iam.application.ports.OutboxPort
import br.ufpr.sept.so2.modules.iam.application.ports.PasswordHasher
import br.ufpr.sept.so2.modules.iam.application.ports.RefreshTokenRepository
import br.ufpr.sept.so2.modules.iam.application.ports.SenhaHistoricoRepository
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.modules.iam.domain.IdentificadorLogin
import br.ufpr.sept.so2.modules.iam.domain.RefreshSessao
import br.ufpr.sept.so2.modules.iam.domain.Usuario
import java.time.OffsetDateTime
import java.util.Optional
import java.util.UUID

object IamFakes {
    class Settings : IamSettings {
        override val accessTtlSeconds: Long = 900
        override val refreshTtlSeconds: Long = 604800
        override val resetTtlSeconds: Long = 86400
        override val maxFalhasConsecutivas: Int = 10
        override val minutosBloqueio: Int = 15
        override val frontendBaseUrl: String = "http://localhost:5173"
    }

    class Hasher : PasswordHasher {
        var dummyCalls: Int = 0

        override fun hash(raw: String): String = "h:$raw"

        override fun matches(raw: String?, hash: String?): Boolean = "h:$raw" == hash

        override fun matchesDummy() {
            dummyCalls++
        }
    }

    class TokenHasher : OpaqueTokenHasher {
        override fun hash(rawToken: String): String = "th:$rawToken"
    }

    class Jwt : JwtTokenService {
        var lastReset: String = "reset.jwt"

        override fun emitAccessToken(usuario: Usuario): String =
            "access:${usuario.id}:${usuario.precisaPrimeiroAcesso()}"

        override fun emitResetToken(usuario: Usuario): String {
            lastReset = "reset:${usuario.id}"
            return lastReset
        }

        override fun parseAccessToken(token: String): JwtTokenService.AccessTokenClaims =
            throw UnsupportedOperationException()

        override fun parseResetToken(token: String): JwtTokenService.ResetTokenClaims {
            if (!token.startsWith("reset:")) {
                throw IllegalArgumentException("jwt")
            }
            return JwtTokenService.ResetTokenClaims(
                UUID.fromString(token.substring("reset:".length)),
                "jti-1",
            )
        }
    }

    class Usuarios : UsuarioRepository {
        val byId: MutableMap<UUID, Usuario> = HashMap()

        override fun save(usuario: Usuario): Usuario {
            byId[usuario.id] = usuario
            return usuario
        }

        override fun findById(id: UUID): Optional<Usuario> = Optional.ofNullable(byId[id])

        override fun findByIdentificador(identificador: IdentificadorLogin): Optional<Usuario> =
            Optional.ofNullable(byId.values.firstOrNull { corresponde(it, identificador) })

        override fun findByEmail(email: String): Optional<Usuario> =
            Optional.ofNullable(
                byId.values.firstOrNull { usuario ->
                    email == usuario.emailInstitucional.value ||
                        (usuario.emailPessoal != null && email == usuario.emailPessoal!!.value)
                },
            )

        private fun corresponde(usuario: Usuario, identificador: IdentificadorLogin): Boolean {
            if (identificador.tipo == IdentificadorLogin.Tipo.GRR) {
                return usuario.grr != null && identificador.valor == usuario.grr!!.value
            }
            return identificador.valor == usuario.emailInstitucional.value ||
                (usuario.emailPessoal != null && identificador.valor == usuario.emailPessoal!!.value)
        }
    }

    class RefreshTokens : RefreshTokenRepository {
        val byHash: MutableMap<String, RefreshSessao> = HashMap()

        override fun save(sessao: RefreshSessao): RefreshSessao {
            byHash[sessao.tokenHash] = sessao
            return sessao
        }

        override fun lockByTokenHash(tokenHash: String): Optional<RefreshSessao> =
            Optional.ofNullable(byHash[tokenHash])

        override fun revokeAllByUsuarioId(usuarioId: UUID) {
            byHash.values
                .filter { it.usuarioId == usuarioId }
                .forEach { it.revogar(OffsetDateTime.now()) }
        }
    }

    class Historico : SenhaHistoricoRepository {
        val hashes: MutableMap<UUID, MutableList<String>> = HashMap()

        override fun append(usuarioId: UUID, senhaHash: String) {
            hashes.getOrPut(usuarioId) { ArrayList() }.add(senhaHash)
        }

        override fun findLastHashes(usuarioId: UUID, limite: Int): List<String> {
            val lista = hashes.getOrDefault(usuarioId, emptyList())
            val from = maxOf(0, lista.size - limite)
            return lista.subList(from, lista.size)
        }
    }

    class Jtis : JtiBlacklistRepository {
        val itens: MutableMap<String, OffsetDateTime> = HashMap()

        override fun add(jti: String, expiresAt: OffsetDateTime) {
            itens[jti] = expiresAt
        }

        override fun contains(jti: String): Boolean = itens.containsKey(jti)
    }

    class Outbox : OutboxPort {
        val tipos: MutableList<String> = ArrayList()
        val payloads: MutableList<String> = ArrayList()

        override fun enqueue(tipo: String, payload: String) {
            tipos.add(tipo)
            payloads.add(payload)
        }
    }

    class Audit : AuditLogPort {
        val tipos: MutableList<String> = ArrayList()

        override fun append(tipo: String, atorId: UUID?, payload: String?, ip: String?) {
            tipos.add(tipo)
        }
    }
}
