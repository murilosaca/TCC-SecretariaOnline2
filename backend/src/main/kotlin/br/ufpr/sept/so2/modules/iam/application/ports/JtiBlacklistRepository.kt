package br.ufpr.sept.so2.modules.iam.application.ports

import java.time.OffsetDateTime

interface JtiBlacklistRepository {
    fun add(jti: String, expiresAt: OffsetDateTime)

    fun contains(jti: String): Boolean
}
