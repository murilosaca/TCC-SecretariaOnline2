package br.ufpr.sept.so2.modules.iam.application.ports

interface OpaqueTokenHasher {
    fun hash(rawToken: String): String
}
