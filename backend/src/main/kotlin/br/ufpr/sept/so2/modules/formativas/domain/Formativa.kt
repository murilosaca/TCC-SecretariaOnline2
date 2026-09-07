package br.ufpr.sept.so2.modules.formativas.domain

import br.ufpr.sept.so2.shared.domain.exception.ConflitoEstadoException
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import java.time.OffsetDateTime
import java.util.UUID

class Formativa(
    val id: UUID,
    val idAluno: UUID,
    val idEvento: UUID?,
    val origem: FormativaOrigem,
    val titulo: String,
    val cargaHoraria: Int,
    estado: FormativaEstado,
    val createdAt: OffsetDateTime,
    updatedAt: OffsetDateTime,
) {
    var estado: FormativaEstado = estado
        private set

    var updatedAt: OffsetDateTime = updatedAt
        private set

    init {
        validar(titulo, cargaHoraria, origem, idEvento)
    }

    fun chaveUnica(): Pair<UUID, UUID> {
        val eventoId = idEvento
            ?: throw DadoInvalidoException("Formativa via presença exige evento.")
        return eventoId to idAluno
    }

    fun mesmaInscricao(eventoId: UUID, alunoId: UUID): Boolean =
        idEvento == eventoId && idAluno == alunoId

    fun confirmar(agora: OffsetDateTime) {
        garantirPendente()
        estado = FormativaEstado.AGUARDANDO_CAAF
        updatedAt = agora
    }

    fun cancelar(agora: OffsetDateTime) {
        garantirPendente()
        estado = FormativaEstado.CANCELADA
        updatedAt = agora
    }

    private fun garantirPendente() {
        if (!estado.podeConfirmarOuCancelar()) {
            throw ConflitoEstadoException("A formativa não está pendente de confirmação.")
        }
    }

    companion object {
        fun viaPresenca(
            id: UUID,
            idAluno: UUID,
            idEvento: UUID,
            titulo: String,
            cargaHoraria: Int,
            agora: OffsetDateTime,
        ): Formativa = Formativa(
            id,
            idAluno,
            idEvento,
            FormativaOrigem.PRESENCA_VALIDADA,
            titulo,
            cargaHoraria,
            FormativaEstado.PENDENTE_CONFIRMACAO,
            agora,
            agora,
        )

        fun viaPresencaOuExistente(
            existente: Formativa?,
            id: UUID,
            idAluno: UUID,
            idEvento: UUID,
            titulo: String,
            cargaHoraria: Int,
            agora: OffsetDateTime,
        ): Formativa {
            if (existente != null) {
                if (!existente.mesmaInscricao(idEvento, idAluno)) {
                    throw ConflitoEstadoException("Já existe formativa para este evento e aluno.")
                }
                return existente
            }
            return viaPresenca(id, idAluno, idEvento, titulo, cargaHoraria, agora)
        }

        fun viaComprovante(): Formativa {
            throw ConflitoEstadoException("Submissão por comprovante não está disponível.")
        }

        private fun validar(
            titulo: String?,
            cargaHoraria: Int,
            origem: FormativaOrigem,
            idEvento: UUID?,
        ) {
            if (titulo.isNullOrBlank()) {
                throw DadoInvalidoException("Título da formativa é obrigatório.")
            }
            if (cargaHoraria <= 0) {
                throw DadoInvalidoException("Carga horária deve ser positiva.")
            }
            if (origem == FormativaOrigem.PRESENCA_VALIDADA && idEvento == null) {
                throw DadoInvalidoException("Formativa via presença exige evento.")
            }
            if (origem == FormativaOrigem.COMPROVANTE) {
                throw ConflitoEstadoException("Submissão por comprovante não está disponível.")
            }
        }
    }
}
