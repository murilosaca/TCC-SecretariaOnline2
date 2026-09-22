package br.ufpr.sept.so2.modules.tcc.application

import br.ufpr.sept.so2.modules.tcc.application.ports.AlunoTccPort
import br.ufpr.sept.so2.modules.tcc.application.ports.TccRepository
import br.ufpr.sept.so2.modules.tcc.domain.Tcc
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ObterTccUseCase(
    private val alunoTccPort: AlunoTccPort,
    private val tccRepository: TccRepository,
) {
    @Transactional(readOnly = true)
    fun execute(id: UUID, usuarioId: UUID, authorities: List<String>): Tcc {
        val tcc = tccRepository.findById(id)
            ?: throw RecursoNaoEncontradoException("TCC não encontrado.")
        if (authorities.contains(TccAcesso.REVIEW) && tcc.membroDe(usuarioId) != null) {
            return tcc
        }
        if (!authorities.contains(TccAcesso.VIEW)) {
            throw RecursoNaoEncontradoException("TCC não encontrado.")
        }
        val aluno = TccAcesso.exigirAlunoAtivo(alunoTccPort, usuarioId)
        TccAcesso.exigirDono(tcc, aluno.id)
        return tcc
    }
}
