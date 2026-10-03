package br.ufpr.sept.so2.modules.atendimentos.domain

import br.ufpr.sept.so2.shared.domain.exception.ConflitoEstadoException
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import java.time.OffsetDateTime
import java.util.UUID

/**
 * Registro imutável de atendimento presencial (RF-F5-007).
 * Depois de gravado só muda de `PENDENTE_CIENCIA` para `CIENCIA_DADA` (RF-F1-011);
 * assunto, resposta e anexo não são editáveis e o registro não é excluído.
 */
class Atendimento(
    val id: UUID,
    val idAluno: UUID,
    val idCategoria: UUID,
    val idRegistrador: UUID,
    val assunto: String,
    val resposta: String,
    val storageKey: String?,
    var estado: AtendimentoEstado,
    var cienciaEm: OffsetDateTime?,
    var cienciaIp: String?,
    val createdAt: OffsetDateTime,
    var updatedAt: OffsetDateTime,
) {
    init {
        if (assunto.isBlank()) {
            throw DadoInvalidoException(ASSUNTO_OBRIGATORIO)
        }
        if (resposta.isBlank()) {
            throw DadoInvalidoException(RESPOSTA_OBRIGATORIA)
        }
        if (assunto.length > ASSUNTO_MAX) {
            throw DadoInvalidoException("Assunto deve ter no máximo $ASSUNTO_MAX caracteres.")
        }
    }

    fun pertenceAoAluno(alunoId: UUID): Boolean = idAluno == alunoId

    fun pendenteDeCiencia(): Boolean = estado == AtendimentoEstado.PENDENTE_CIENCIA

    fun temAnexo(): Boolean = !storageKey.isNullOrBlank()

    fun darCiencia(ip: String?, agora: OffsetDateTime) {
        if (!pendenteDeCiencia()) {
            throw ConflitoEstadoException(CIENCIA_JA_DADA)
        }
        estado = AtendimentoEstado.CIENCIA_DADA
        cienciaEm = agora
        cienciaIp = ip
        updatedAt = agora
    }

    companion object {
        const val ASSUNTO_OBRIGATORIO = "Assunto do atendimento é obrigatório."
        const val RESPOSTA_OBRIGATORIA = "Resposta do atendimento é obrigatória."
        const val CIENCIA_JA_DADA = "Ciência deste atendimento já foi registrada."
        const val ASSUNTO_MAX = 200

        fun registrar(
            id: UUID,
            idAluno: UUID,
            idCategoria: UUID,
            idRegistrador: UUID,
            assunto: String?,
            resposta: String?,
            storageKey: String?,
            agora: OffsetDateTime,
        ): Atendimento = Atendimento(
            id,
            idAluno,
            idCategoria,
            idRegistrador,
            assunto?.trim() ?: throw DadoInvalidoException(ASSUNTO_OBRIGATORIO),
            resposta?.trim() ?: throw DadoInvalidoException(RESPOSTA_OBRIGATORIA),
            storageKey,
            AtendimentoEstado.PENDENTE_CIENCIA,
            null,
            null,
            agora,
            agora,
        )
    }
}
