package br.ufpr.sept.so2.modules.solicitacoes.infrastructure.persistence

import br.ufpr.sept.so2.modules.solicitacoes.application.ports.TipoSolicitacaoVersaoPort
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.util.UUID

@Component
class TipoSolicitacaoVersaoJpaAdapter(
    private val jpaRepository: TipoSolicitacaoVersaoJpaRepository,
) : TipoSolicitacaoVersaoPort {
    override fun count(tipoId: UUID): Long = jpaRepository.countByTipoId(tipoId)

    override fun maior(tipoId: UUID): Int = jpaRepository.maiorVersao(tipoId) ?: 0

    override fun inserir(
        tipoId: UUID,
        versao: Int,
        formSchema: String,
        workflowJson: String,
        publicadoPor: UUID,
        publicadoEm: OffsetDateTime,
    ) {
        val entity = TipoSolicitacaoVersaoJpaEntity()
        entity.tipoId = tipoId
        entity.versao = versao
        entity.formSchema = formSchema
        entity.workflowJson = workflowJson
        entity.publicadoEm = publicadoEm
        entity.publicadoPor = publicadoPor
        jpaRepository.save(entity)
    }
}
