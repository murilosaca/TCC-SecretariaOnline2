package br.ufpr.sept.so2.modules.solicitacoes.application.ports

import java.time.OffsetDateTime
import java.util.UUID

interface TipoSolicitacaoVersaoPort {
    fun count(tipoId: UUID): Long

    fun maior(tipoId: UUID): Int

    fun inserir(
        tipoId: UUID,
        versao: Int,
        formSchema: String,
        workflowJson: String,
        publicadoPor: UUID,
        publicadoEm: OffsetDateTime,
    )
}
