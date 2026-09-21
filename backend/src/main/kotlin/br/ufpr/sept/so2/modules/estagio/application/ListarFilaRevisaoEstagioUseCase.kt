package br.ufpr.sept.so2.modules.estagio.application

import br.ufpr.sept.so2.modules.estagio.application.ports.EstagioRepository
import br.ufpr.sept.so2.modules.estagio.domain.Estagio
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ListarFilaRevisaoEstagioUseCase(
    private val estagioRepository: EstagioRepository,
) {
    @Transactional(readOnly = true)
    fun execute(orientadorId: UUID, situacao: String?, pageable: Pageable): Page<Estagio> =
        estagioRepository.findParaRevisao(
            orientadorId,
            EstagioPagina.situacao(situacao),
            EstagioPagina.de(pageable),
        )
}
