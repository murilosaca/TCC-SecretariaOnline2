package br.ufpr.sept.so2.modules.diplomas.domain

import br.ufpr.sept.so2.shared.domain.exception.ConflitoEstadoException
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import java.time.OffsetDateTime
import java.util.UUID

class Diploma(
    val id: UUID,
    val idAluno: UUID,
    val idCurso: UUID,
    val idPeriodoLetivo: UUID?,
    val numero: String,
    situacao: DiplomaSituacao,
    val dataColacao: OffsetDateTime,
    val livro: String,
    val folha: String,
    val turma: String?,
    storageKey: String?,
    metodoEntrega: MetodoEntregaDiploma?,
    dataEntrega: OffsetDateTime?,
    val createdAt: OffsetDateTime,
    updatedAt: OffsetDateTime,
) {
    var situacao: DiplomaSituacao = situacao
        private set
    var storageKey: String? = storageKey
        private set
    var metodoEntrega: MetodoEntregaDiploma? = metodoEntrega
        private set
    var dataEntrega: OffsetDateTime? = dataEntrega
        private set
    var updatedAt: OffsetDateTime = updatedAt
        private set

    init {
        validar(numero, livro, folha, turma)
    }

    fun confirmarEntrega(metodo: MetodoEntregaDiploma, quando: OffsetDateTime, agora: OffsetDateTime) {
        if (situacao != DiplomaSituacao.PENDENTE) {
            throw ConflitoEstadoException("Só é possível confirmar entrega de diploma PENDENTE.")
        }
        metodoEntrega = metodo
        dataEntrega = quando
        situacao = DiplomaSituacao.ENTREGUE
        updatedAt = agora
    }

    fun anexarPdf(storageKey: String, agora: OffsetDateTime) {
        if (storageKey.isBlank()) {
            throw DadoInvalidoException("Chave de armazenamento do diploma é obrigatória.")
        }
        this.storageKey = storageKey.trim()
        updatedAt = agora
    }

    fun temPdf(): Boolean = !storageKey.isNullOrBlank()

    companion object {
        fun registrar(
            id: UUID,
            idAluno: UUID,
            idCurso: UUID,
            idPeriodoLetivo: UUID?,
            numero: String,
            dataColacao: OffsetDateTime,
            livro: String,
            folha: String,
            turma: String?,
            agora: OffsetDateTime,
        ): Diploma = Diploma(
            id,
            idAluno,
            idCurso,
            idPeriodoLetivo,
            numero.trim(),
            DiplomaSituacao.PENDENTE,
            dataColacao,
            livro.trim(),
            folha.trim(),
            turma?.trim()?.takeIf { it.isNotEmpty() },
            null,
            null,
            null,
            agora,
            agora,
        )

        private fun validar(numero: String?, livro: String?, folha: String?, turma: String?) {
            if (numero.isNullOrBlank()) {
                throw DadoInvalidoException("Número do diploma é obrigatório.")
            }
            if (livro.isNullOrBlank()) {
                throw DadoInvalidoException("Livro da colação é obrigatório.")
            }
            if (folha.isNullOrBlank()) {
                throw DadoInvalidoException("Folha da colação é obrigatória.")
            }
            if (turma != null && turma.length > 80) {
                throw DadoInvalidoException("Turma da colação deve ter no máximo 80 caracteres.")
            }
        }
    }
}
