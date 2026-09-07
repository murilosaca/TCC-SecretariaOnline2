package br.ufpr.sept.so2.modules.solicitacoes.infrastructure.persistence

import br.ufpr.sept.so2.modules.solicitacoes.application.ports.SolicitacaoRepository
import br.ufpr.sept.so2.modules.solicitacoes.domain.Solicitacao
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import java.util.Optional
import java.util.UUID

@Component
class SolicitacaoJpaAdapter(
    private val jpaRepository: SolicitacaoJpaRepository,
    private val eventoRepository: SolicitacaoEventoJpaRepository,
) : SolicitacaoRepository {

    override fun save(solicitacao: Solicitacao): Solicitacao {
        val entity = jpaRepository.findById(solicitacao.id)
            .orElseGet { SolicitacaoJpaEntity.fromDomain(solicitacao) }
        entity.merge(solicitacao)
        jpaRepository.save(entity)
        for (evento in solicitacao.eventos) {
            if (!eventoRepository.existsById(evento.id)) {
                eventoRepository.save(SolicitacaoEventoJpaEntity.fromDomain(solicitacao.id, evento))
            }
        }
        return carregar(solicitacao.id).orElseThrow()
    }

    override fun findById(id: UUID): Optional<Solicitacao> = carregar(id)

    override fun findByProtocolo(protocolo: String): Optional<Solicitacao> =
        jpaRepository.findByProtocolo(protocolo)
            .map { entity -> entity.toDomain(eventosDe(entity.id!!)) }

    override fun findMinhas(
        solicitanteId: UUID,
        estado: String?,
        tipoCodigo: String?,
        ano: Int?,
        pageable: Pageable,
    ): Page<Solicitacao> {
        val anoPrefixo = if (ano == null) null else "PROT-$ano-%"
        return jpaRepository.findMinhas(solicitanteId, estado, tipoCodigo, anoPrefixo, pageable)
            .map(SolicitacaoJpaEntity::toDomainSemEventos)
    }

    private fun carregar(id: UUID): Optional<Solicitacao> =
        jpaRepository.findById(id).map { entity -> entity.toDomain(eventosDe(id)) }

    private fun eventosDe(solicitacaoId: UUID) =
        eventoRepository.findBySolicitacaoIdOrderByCreatedAtDesc(solicitacaoId)
            .map(SolicitacaoEventoJpaEntity::toDomain)
}
