package br.ufpr.sept.so2.modules.suporte.application

import br.ufpr.sept.so2.modules.suporte.infrastructure.persistence.FaqItemJpaRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

data class FaqItem(val id: UUID, val pergunta: String, val resposta: String)

@Service
class ListarFaqUseCase(
    private val repositorio: FaqItemJpaRepository,
) {
    @Transactional(readOnly = true)
    fun execute(): List<FaqItem> =
        repositorio.findAllByOrderByOrdemAsc().map {
            FaqItem(it.id!!, it.pergunta!!, it.resposta!!)
        }
}
