package br.ufpr.sept.so2.modules.solicitacoes.infrastructure.persistence

import br.ufpr.sept.so2.modules.solicitacoes.application.ports.ProtocoloSequenciaPort
import br.ufpr.sept.so2.modules.solicitacoes.domain.Protocolo
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class ProtocoloSequenciaAdapter(
    private val jpaRepository: ProtocoloSeqJpaRepository,
) : ProtocoloSequenciaPort {

    @Transactional
    override fun proximo(ano: Int): Protocolo {
        val row = jpaRepository.lockByAno(ano)
            .orElseGet { jpaRepository.saveAndFlush(ProtocoloSeqJpaEntity(ano, 0)) }
        row.ultimo = row.ultimo + 1
        jpaRepository.saveAndFlush(row)
        return Protocolo.formatar(ano, row.ultimo)
    }
}
