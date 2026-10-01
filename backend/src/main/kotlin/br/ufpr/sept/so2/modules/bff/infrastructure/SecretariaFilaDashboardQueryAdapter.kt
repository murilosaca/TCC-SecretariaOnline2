package br.ufpr.sept.so2.modules.bff.infrastructure

import br.ufpr.sept.so2.modules.bff.application.SecretariaDashboardRegras
import br.ufpr.sept.so2.modules.bff.application.ports.SecretariaFilaDashboardQueryPort
import br.ufpr.sept.so2.modules.bff.application.ports.SecretariaFilaDashboardQueryPort.FilaSecretaria
import br.ufpr.sept.so2.modules.bff.application.ports.SecretariaFilaDashboardQueryPort.Item
import br.ufpr.sept.so2.modules.solicitacoes.application.SolicitacaoCursoEscopo
import br.ufpr.sept.so2.modules.solicitacoes.application.ports.SolicitacaoRepository
import br.ufpr.sept.so2.modules.solicitacoes.domain.Solicitacao
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.UUID

@Component
class SecretariaFilaDashboardQueryAdapter(
    private val solicitacaoCursoEscopo: SolicitacaoCursoEscopo,
    private val solicitacaoRepository: SolicitacaoRepository,
) : SecretariaFilaDashboardQueryPort {

    @Transactional(readOnly = true)
    override fun consultar(atorId: UUID, agora: OffsetDateTime): FilaSecretaria {
        val solicitantes = solicitacaoCursoEscopo.solicitanteIdsNoEscopo(atorId)
        if (solicitantes.isEmpty()) {
            return FilaSecretaria(0, 0, 0, emptyList())
        }
        val inicioDia = SecretariaDashboardRegras.inicioDoDia(agora)
        val abertas = total(solicitantes, false, agora)
        val atrasadas = total(solicitantes, true, agora)
        val concluidas = solicitacaoRepository.contarComEventoPara(
            solicitantes,
            SecretariaDashboardRegras.ESTADO_CONCLUIDA,
            inicioDia,
            inicioDia.plusDays(1),
        )
        val fila = solicitacaoRepository.findFilaCurso(
            solicitantes,
            SecretariaDashboardRegras.ESTADOS_ABERTA,
            null,
            false,
            agora,
            PageRequest.of(0, SecretariaDashboardRegras.FILA_LIMITE, ORDEM),
        )
        return FilaSecretaria(abertas, atrasadas, concluidas, fila.content.map(::toItem))
    }

    private fun total(solicitantes: Set<UUID>, somenteAtraso: Boolean, agora: OffsetDateTime): Int =
        solicitacaoRepository.findFilaCurso(
            solicitantes,
            SecretariaDashboardRegras.ESTADOS_ABERTA,
            null,
            somenteAtraso,
            agora,
            PageRequest.of(0, 1),
        ).totalElements.toInt()

    companion object {
        private val ORDEM: Sort = Sort.by(Sort.Order.asc("prazoEm"), Sort.Order.asc("createdAt"))

        private fun toItem(solicitacao: Solicitacao): Item =
            Item(
                solicitacao.id,
                solicitacao.protocolo.valor,
                solicitacao.tipoNome,
                solicitacao.estado,
                solicitacao.prazoEm,
                "/solicitacoes/${solicitacao.id}",
            )
    }
}
