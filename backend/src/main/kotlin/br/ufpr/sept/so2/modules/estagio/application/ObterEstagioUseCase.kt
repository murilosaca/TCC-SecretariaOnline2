package br.ufpr.sept.so2.modules.estagio.application

import br.ufpr.sept.so2.modules.estagio.application.ports.AlunoEstagioPort
import br.ufpr.sept.so2.modules.estagio.application.ports.EstagioRepository
import br.ufpr.sept.so2.modules.estagio.domain.Estagio
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ObterEstagioUseCase(
    private val alunoEstagioPort: AlunoEstagioPort,
    private val estagioRepository: EstagioRepository,
) {
    @Transactional(readOnly = true)
    fun execute(id: UUID, usuarioId: UUID, authorities: List<String>): Estagio {
        val estagio = estagioRepository.findById(id)
            ?: throw RecursoNaoEncontradoException("Estágio não encontrado.")
        if (authorities.contains(EstagioAcesso.REVIEW) && estagio.orientadoPor(usuarioId)) {
            return estagio
        }
        if (!authorities.contains(EstagioAcesso.VIEW)) {
            throw RecursoNaoEncontradoException("Estágio não encontrado.")
        }
        val aluno = EstagioAcesso.exigirAlunoAtivo(alunoEstagioPort, usuarioId)
        EstagioAcesso.exigirDono(estagio, aluno.id)
        return estagio
    }
}
