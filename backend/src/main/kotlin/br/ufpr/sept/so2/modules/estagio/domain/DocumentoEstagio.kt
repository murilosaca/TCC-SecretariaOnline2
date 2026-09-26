package br.ufpr.sept.so2.modules.estagio.domain

import br.ufpr.sept.so2.shared.domain.exception.ConflitoEstadoException
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import java.time.OffsetDateTime
import java.util.UUID

class DocumentoEstagio(
    val id: UUID,
    val tipo: TipoDocumentoEstagio,
    val obrigatorio: Boolean,
    estado: EstadoDocumentoEstagio,
    nomeArquivo: String? = null,
    contentType: String? = null,
    storageKey: String? = null,
    tamanho: Int? = null,
    enviadoEm: OffsetDateTime? = null,
    pareceres: List<ParecerEstagio> = emptyList(),
) {
    var estado: EstadoDocumentoEstagio = estado
        private set

    var nomeArquivo: String? = nomeArquivo
        private set

    var contentType: String? = contentType
        private set

    var storageKey: String? = storageKey
        private set

    var tamanho: Int? = tamanho
        private set

    var enviadoEm: OffsetDateTime? = enviadoEm
        private set

    private val _pareceres: MutableList<ParecerEstagio> = pareceres.toMutableList()

    val pareceres: List<ParecerEstagio>
        get() = _pareceres.toList()

    fun temArquivo(): Boolean = !storageKey.isNullOrBlank()

    fun podeEnviar(): Boolean = estado.podeEnviar()

    fun podeRevisar(): Boolean = estado.podeRevisar()

    fun registrarEnvio(
        nome: String,
        contentType: String?,
        bytes: ByteArray,
        storageKey: String,
        agora: OffsetDateTime,
    ) {
        if (!podeEnviar()) {
            throw ConflitoEstadoException("Este documento não aceita envio no estado atual.")
        }
        if (storageKey.isBlank()) {
            throw DadoInvalidoException("Chave de armazenamento do documento é obrigatória.")
        }
        val pdf = validarPdf(nome, contentType, bytes)
        this.nomeArquivo = pdf
        this.contentType = "application/pdf"
        this.storageKey = storageKey
        this.tamanho = bytes.size
        this.enviadoEm = agora
        this.estado = EstadoDocumentoEstagio.AGUARDANDO_PARECER
    }

    fun receberParecer(
        acao: AcaoParecerEstagio,
        parecer: String?,
        autorId: UUID,
        parecerId: UUID,
        agora: OffsetDateTime,
    ): ParecerEstagio {
        if (!podeRevisar()) {
            throw ConflitoEstadoException("O documento não está aguardando parecer.")
        }
        val texto = ParecerEstagio.validarTexto(acao, parecer)
        val registrado = ParecerEstagio(parecerId, id, autorId, acao.resultado(), texto, agora)
        _pareceres.add(registrado)
        estado = if (acao == AcaoParecerEstagio.APROVAR) {
            EstadoDocumentoEstagio.APROVADO
        } else {
            EstadoDocumentoEstagio.REPROVADO
        }
        return registrado
    }

    companion object {
        const val PDF_MAX_BYTES = 5 * 1024 * 1024
        private val PDF_MAGIC = byteArrayOf(0x25, 0x50, 0x44, 0x46)

        fun pendente(id: UUID, tipo: TipoDocumentoEstagio, obrigatorio: Boolean = true): DocumentoEstagio =
            DocumentoEstagio(id, tipo, obrigatorio, EstadoDocumentoEstagio.PENDENTE)

        fun validarPdf(nome: String, contentType: String?, bytes: ByteArray): String {
            if (bytes.isEmpty() || bytes.size > PDF_MAX_BYTES) {
                throw DadoInvalidoException("Envie um PDF de até 5 MB.")
            }
            val cabecalhoPdf = bytes.size >= PDF_MAGIC.size &&
                PDF_MAGIC.indices.all { bytes[it] == PDF_MAGIC[it] }
            val tipo = contentType?.substringBefore(';')?.trim()?.lowercase()
            val tipoAceito = tipo == null ||
                tipo == "application/pdf" ||
                tipo == "application/x-pdf" ||
                tipo == "application/octet-stream"
            if (!cabecalhoPdf || !tipoAceito) {
                throw DadoInvalidoException("O arquivo precisa ser um PDF.")
            }
            val limpo = nome.trim().ifEmpty { "documento.pdf" }
            return limpo.take(200)
        }
    }
}
