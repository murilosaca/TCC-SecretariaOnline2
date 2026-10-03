package br.ufpr.sept.so2.modules.atendimentos.application

import br.ufpr.sept.so2.modules.atendimentos.application.ports.AlunoAtendimentoPort
import br.ufpr.sept.so2.modules.atendimentos.application.ports.AtendimentoRepository
import br.ufpr.sept.so2.modules.atendimentos.application.ports.CategoriaAtendimentoRepository
import br.ufpr.sept.so2.modules.atendimentos.domain.Atendimento
import br.ufpr.sept.so2.modules.atendimentos.domain.AtendimentoEstado
import br.ufpr.sept.so2.shared.domain.exception.AcessoNegadoException
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ListarAtendimentosDoAlunoUseCase(
    private val atendimentoRepository: AtendimentoRepository,
    private val categoriaRepository: CategoriaAtendimentoRepository,
    private val alunoAtendimentoPort: AlunoAtendimentoPort,
) {
    @Transactional(readOnly = true)
    fun execute(usuarioId: UUID, aluno: String, status: String?, pageable: Pageable): Page<AtendimentoItem> {
        if (!aluno.equals("me", ignoreCase = true)) {
            throw AcessoNegadoException(SO_PROPRIOS)
        }
        val cadastro = AtendimentoAcesso.exigirAlunoAutenticado(alunoAtendimentoPort, usuarioId)
        val pagina = atendimentoRepository.findByAluno(
            cadastro.id,
            AtendimentoEstado.optional(status),
            paginar(pageable),
        )
        val categorias = categoriaRepository.findAtivas().associate { it.id to it.nome }
        return pagina.map { AtendimentoItem(it, categorias[it.idCategoria]) }
    }

    private fun paginar(pageable: Pageable): Pageable = PageRequest.of(
        pageable.pageNumber,
        pageable.pageSize.coerceAtMost(MAX_SIZE),
        Sort.by(Sort.Direction.DESC, "createdAt"),
    )

    companion object {
        const val SO_PROPRIOS = "Neste momento só é possível listar os próprios atendimentos."
        private const val MAX_SIZE = 100
    }
}

data class AtendimentoItem(
    val atendimento: Atendimento,
    val categoriaNome: String?,
)
