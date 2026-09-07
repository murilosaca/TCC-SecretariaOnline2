package br.ufpr.sept.so2.modules.solicitacoes.domain

import java.time.OffsetDateTime
import java.util.UUID

class Solicitacao(
    val id: UUID,
    val tipoId: UUID,
    val tipoCodigo: String,
    val tipoNome: String,
    val tipoVersao: Int,
    val solicitanteId: UUID,
    val protocolo: Protocolo,
    estado: String,
    val payloadJson: String?,
    val formSchemaSnapshot: String?,
    val workflowSnapshot: String?,
    val prazoEm: OffsetDateTime?,
    val hashSha256: String?,
    eventos: List<SolicitacaoEvento>?,
    val createdAt: OffsetDateTime?,
    updatedAt: OffsetDateTime?,
) {
    var estado: String = estado
        private set

    var updatedAt: OffsetDateTime? = updatedAt
        private set

    private val eventosInternos: MutableList<SolicitacaoEvento> =
        if (eventos == null) mutableListOf() else eventos.toMutableList()

    val eventos: List<SolicitacaoEvento>
        get() = eventosInternos.toList()

    fun transicionar(
        eventoId: UUID,
        workflow: WorkflowDefinicao,
        acao: String?,
        atorId: UUID?,
        parecer: String?,
        agora: OffsetDateTime,
    ) {
        val destino = workflow.transicionar(estado, acao)
        val origem = this.estado
        this.estado = destino
        this.updatedAt = agora
        eventosInternos += SolicitacaoEvento(
            eventoId,
            EVENTO_TRANSICAO,
            origem,
            destino,
            atorId,
            parecer,
            null,
            agora,
        )
    }

    fun pertenceA(usuarioId: UUID): Boolean = solicitanteId == usuarioId

    fun prazoVencido(agora: OffsetDateTime): Boolean =
        prazoEm != null && agora.isAfter(prazoEm)

    companion object {
        const val EVENTO_CRIADA = "CRIADA"
        const val EVENTO_TRANSICAO = "TRANSICAO"

        @JvmStatic
        fun abrir(
            id: UUID,
            eventoId: UUID,
            tipo: TipoSolicitacao,
            solicitanteId: UUID,
            protocolo: Protocolo,
            payloadJson: String?,
            workflow: WorkflowDefinicao,
            agora: OffsetDateTime,
        ): Solicitacao {
            val estadoInicial = workflow.inicial
            val solicitacao = Solicitacao(
                id,
                tipo.id,
                tipo.codigo,
                tipo.nome,
                tipo.versao,
                solicitanteId,
                protocolo,
                estadoInicial,
                payloadJson,
                tipo.formSchema,
                tipo.workflowJson,
                agora.plusDays(tipo.prazoDias.toLong()),
                null,
                mutableListOf(),
                agora,
                agora,
            )
            solicitacao.eventosInternos += SolicitacaoEvento(
                eventoId,
                EVENTO_CRIADA,
                null,
                estadoInicial,
                solicitanteId,
                null,
                payloadJson,
                agora,
            )
            return solicitacao
        }
    }
}
