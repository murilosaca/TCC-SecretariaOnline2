package br.ufpr.sept.so2.modules.atendimentos.application

import br.ufpr.sept.so2.modules.atendimentos.application.ports.CategoriaAtendimentoRepository
import br.ufpr.sept.so2.modules.atendimentos.domain.CategoriaAtendimento
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/** Lista pequena vinda do seed; não há CRUD de categoria nesta fatia. */
@Service
class ListarCategoriasAtendimentoUseCase(
    private val categoriaRepository: CategoriaAtendimentoRepository,
) {
    @Transactional(readOnly = true)
    fun execute(): List<CategoriaAtendimento> = categoriaRepository.findAtivas()
}
