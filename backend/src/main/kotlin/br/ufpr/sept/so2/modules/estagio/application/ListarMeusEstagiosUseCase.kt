package br.ufpr.sept.so2.modules.estagio.application

import br.ufpr.sept.so2.modules.estagio.application.ports.AlunoEstagioPort
import br.ufpr.sept.so2.modules.estagio.application.ports.EstagioRepository
import br.ufpr.sept.so2.modules.estagio.domain.Estagio
import br.ufpr.sept.so2.shared.domain.exception.AcessoNegadoException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ListarMeusEstagiosUseCase(
    private val alunoEstagioPort: AlunoEstagioPort,
    private val estagioRepository: EstagioRepository,
) {
    @Transactional(readOnly = true)
    fun execute(usuarioId: UUID, aluno: String, situacao: String?, pageable: Pageable): Page<Estagio> {
        if (!aluno.equals("me", ignoreCase = true)) {
            throw AcessoNegadoException("Neste momento só é possível listar os próprios estágios.")
        }
        val cadastro = EstagioAcesso.exigirAlunoAtivo(alunoEstagioPort, usuarioId)
        return estagioRepository.findByAluno(
            cadastro.id,
            EstagioPagina.situacao(situacao),
            EstagioPagina.de(pageable),
        )
    }
}
