package br.ufpr.sept.so2.modules.tcc.application

import br.ufpr.sept.so2.modules.tcc.application.ports.TccRepository
import br.ufpr.sept.so2.modules.tcc.domain.Tcc
import br.ufpr.sept.so2.modules.tcc.domain.TccEstado
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ListarFilaRevisaoTccUseCase(
    private val tccRepository: TccRepository,
) {
    @Transactional(readOnly = true)
    fun execute(revisorId: UUID, estado: String?, pageable: Pageable): Page<Tcc> {
        val filtro = TccPagina.estado(estado) ?: TccEstado.SUBMETIDO
        return tccRepository.findParaRevisao(revisorId, filtro, TccPagina.de(pageable))
    }
}
