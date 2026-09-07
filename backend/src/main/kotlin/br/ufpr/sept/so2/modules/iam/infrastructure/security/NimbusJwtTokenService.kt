package br.ufpr.sept.so2.modules.iam.infrastructure.security

import br.ufpr.sept.so2.modules.iam.application.ports.JwtTokenService
import br.ufpr.sept.so2.modules.iam.domain.Usuario
import br.ufpr.sept.so2.modules.iam.infrastructure.IamProperties
import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.crypto.RSASSAVerifier
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.time.Instant
import java.util.Date
import java.util.UUID

@Component
class NimbusJwtTokenService(
    private val properties: IamProperties,
) : JwtTokenService {
    private val privateKey: RSAPrivateKey
    private val publicKey: RSAPublicKey

    init {
        val pair = resolveKeyPair(properties)
        privateKey = pair.private as RSAPrivateKey
        publicKey = pair.public as RSAPublicKey
    }

    override fun emitAccessToken(usuario: Usuario): String {
        val agora = Instant.now()
        val claims = JWTClaimsSet.Builder()
            .subject(usuario.id.toString())
            .issuer(properties.issuer)
            .audience(properties.audience)
            .jwtID(UUID.randomUUID().toString())
            .issueTime(Date.from(agora))
            .expirationTime(Date.from(agora.plusSeconds(properties.accessTtlSeconds)))
            .claim("authorities", usuario.authorities)
            .claim("mustChangePassword", usuario.precisaPrimeiroAcesso())
            .build()
        return sign(claims)
    }

    override fun emitResetToken(usuario: Usuario): String {
        val agora = Instant.now()
        val claims = JWTClaimsSet.Builder()
            .subject(usuario.id.toString())
            .issuer(properties.issuer)
            .audience(properties.resetAudience)
            .jwtID(UUID.randomUUID().toString())
            .issueTime(Date.from(agora))
            .expirationTime(Date.from(agora.plusSeconds(properties.resetTtlSeconds)))
            .build()
        return sign(claims)
    }

    override fun parseAccessToken(token: String): JwtTokenService.AccessTokenClaims {
        val claims = parse(token, properties.audience)
        val authorities = readAuthorities(claims)
        val mustChange = claims.getClaim("mustChangePassword") == true
        return JwtTokenService.AccessTokenClaims(
            UUID.fromString(claims.subject),
            authorities,
            mustChange,
            claims.jwtid,
        )
    }

    override fun parseResetToken(token: String): JwtTokenService.ResetTokenClaims {
        val claims = parse(token, properties.resetAudience)
        return JwtTokenService.ResetTokenClaims(UUID.fromString(claims.subject), claims.jwtid)
    }

    private fun sign(claims: JWTClaimsSet): String {
        return try {
            val header = JWSHeader.Builder(JWSAlgorithm.RS256)
                .type(JOSEObjectType.JWT)
                .keyID("so2-iam")
                .build()
            val jwt = SignedJWT(header, claims)
            jwt.sign(RSASSASigner(privateKey))
            jwt.serialize()
        } catch (ex: Exception) {
            throw IllegalStateException("Falha ao assinar JWT", ex)
        }
    }

    private fun parse(token: String, audienceEsperada: String): JWTClaimsSet {
        try {
            val jwt = SignedJWT.parse(token)
            if (!jwt.verify(RSASSAVerifier(publicKey))) {
                throw IllegalArgumentException("assinatura")
            }
            val claims = jwt.jwtClaimsSet
            val exp = claims.expirationTime
            if (exp == null || exp.before(Date())) {
                throw IllegalArgumentException("expirado")
            }
            if (claims.audience == null || !claims.audience.contains(audienceEsperada)) {
                throw IllegalArgumentException("audience")
            }
            if (claims.subject == null || claims.jwtid == null) {
                throw IllegalArgumentException("claims")
            }
            return claims
        } catch (ex: IllegalArgumentException) {
            throw ex
        } catch (ex: Exception) {
            throw IllegalArgumentException("JWT inválido", ex)
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(NimbusJwtTokenService::class.java)

        private fun readAuthorities(claims: JWTClaimsSet): List<String> {
            val raw = claims.getClaim("authorities")
            if (raw is List<*>) {
                return raw.map { it.toString() }
            }
            return emptyList()
        }

        private fun resolveKeyPair(properties: IamProperties): java.security.KeyPair {
            val privatePem = properties.jwtPrivateKeyPem
            val publicPem = properties.jwtPublicKeyPem
            if (privatePem.isNotBlank() && publicPem.isNotBlank()) {
                return java.security.KeyPair(PemKeys.parsePublic(publicPem), PemKeys.parsePrivate(privatePem))
            }
            LOG.warn("Par RSA JWT ausente nas variáveis de ambiente; gerando chave efêmera (dev/test).")
            return PemKeys.generateRsa2048()
        }
    }
}
