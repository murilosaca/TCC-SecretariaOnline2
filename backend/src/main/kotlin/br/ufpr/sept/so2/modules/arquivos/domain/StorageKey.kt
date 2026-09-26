package br.ufpr.sept.so2.modules.arquivos.domain

/**
 * Chave de objeto no storage S3-compatível (sem path local).
 */
@JvmInline
value class StorageKey(val value: String) {
    init {
        require(value.isNotBlank()) { "storage_key é obrigatório." }
        require(!value.startsWith("/")) { "storage_key não deve começar com '/'." }
        require(!value.contains("..")) { "storage_key inválida." }
    }

    override fun toString(): String = value

    companion object {
        fun certificado(id: java.util.UUID): StorageKey =
            StorageKey("certificados/$id.pdf")

        fun estagioDocumento(estagioId: java.util.UUID, documentoId: java.util.UUID): StorageKey =
            StorageKey("estagios/$estagioId/$documentoId.pdf")

        fun tccVersaoFinal(tccId: java.util.UUID): StorageKey =
            StorageKey("tccs/$tccId/versao-final.pdf")
    }
}
