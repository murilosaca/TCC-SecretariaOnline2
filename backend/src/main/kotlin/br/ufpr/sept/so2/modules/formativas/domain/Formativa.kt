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
    parecer: String? = null,
    idRevisor: UUID? = null,
    reviewedAt: OffsetDateTime? = null,
    idResponsavel: UUID? = null,
) {
    var estado: FormativaEstado = estado
        private set

    var updatedAt: OffsetDateTime = updatedAt
        private set

    var parecer: String? = parecer
        private set

    var idRevisor: UUID? = idRevisor
        private set

    var reviewedAt: OffsetDateTime? = reviewedAt
        private set

    var idResponsavel: UUID? = idResponsavel
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

    fun aprovar(parecer: String, revisorId: UUID, agora: OffsetDateTime) {
        garantirAguardandoCaaf()
        this.parecer = validarParecer(parecer, exigirMinimoIndefer = false)
        this.idRevisor = revisorId
        this.reviewedAt = agora
        estado = FormativaEstado.APROVADA
        updatedAt = agora
    }

    fun indeferir(parecer: String, revisorId: UUID, agora: OffsetDateTime) {
        garantirAguardandoCaaf()
        this.parecer = validarParecer(parecer, exigirMinimoIndefer = true)
        this.idRevisor = revisorId
        this.reviewedAt = agora
        estado = FormativaEstado.INDEFERIDA
        updatedAt = agora
    }

    fun semResponsavel(): Boolean = idResponsavel == null

    fun atribuidaA(usuarioId: UUID): Boolean = idResponsavel == usuarioId

    fun podeAtribuir(atorId: UUID): Boolean =
        estado.podeRevisar() && (idResponsavel == null || idResponsavel == atorId)

    fun atribuirResponsavel(novoId: UUID, agora: OffsetDateTime) {
        garantirAguardandoCaaf()
        if (idResponsavel == novoId) {
            throw ConflitoEstadoException("Esta formativa já está atribuída a este responsável.")
        }
        idResponsavel = novoId
        updatedAt = agora
    }

    /** Lote CAAF (F4.1): só presença já validada pelo sistema. */
    fun elegivelAprovacaoEmLote(): Boolean =
        estado.podeRevisar() && origem == FormativaOrigem.PRESENCA_VALIDADA

    private fun garantirPendente() {
        if (!estado.podeConfirmarOuCancelar()) {
            throw ConflitoEstadoException("A formativa não está pendente de confirmação.")
        }
    }

    private fun garantirAguardandoCaaf() {
        if (!estado.podeRevisar()) {
            throw ConflitoEstadoException("A formativa não está aguardando revisão da CAAF.")
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

        const val PARECER_INDEFER_MIN = 20

        fun validarParecer(parecer: String?, exigirMinimoIndefer: Boolean): String {
            val limpo = parecer?.trim().orEmpty()
            if (exigirMinimoIndefer && limpo.length < PARECER_INDEFER_MIN) {
                throw DadoInvalidoException(
                    "Informe o parecer para indeferimento (mín. $PARECER_INDEFER_MIN caracteres).",
                )
            }
            if (limpo.isEmpty()) {
                throw DadoInvalidoException("Informe o parecer.")
            }
            return limpo
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
