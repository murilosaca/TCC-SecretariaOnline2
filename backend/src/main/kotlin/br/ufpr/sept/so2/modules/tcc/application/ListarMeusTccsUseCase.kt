package br.ufpr.sept.so2.modules.tcc.application

import br.ufpr.sept.so2.modules.tcc.application.ports.AlunoTccPort
import br.ufpr.sept.so2.modules.tcc.application.ports.TccRepository
import br.ufpr.sept.so2.modules.tcc.domain.Tcc
import br.ufpr.sept.so2.shared.domain.exception.AcessoNegadoException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ListarMeusTccsUseCase(
    private val alunoTccPort: AlunoTccPort,
    private val tccRepository: TccRepository,
) {
    @Transactional(readOnly = true)
    fun execute(usuarioId: UUID, aluno: String, estado: String?, pageable: Pageable): Page<Tcc> {
        if (!aluno.equals("me", ignoreCase = true)) {
            throw AcessoNegadoException("Neste momento só é possível listar os próprios TCCs.")
        }
        val cadastro = TccAcesso.exigirAlunoAtivo(alunoTccPort, usuarioId)
        return tccRepository.findByAluno(cadastro.id, TccPagina.estado(estado), TccPagina.de(pageable))
    }
}
