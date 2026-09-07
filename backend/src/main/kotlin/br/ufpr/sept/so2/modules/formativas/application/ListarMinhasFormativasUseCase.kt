package br.ufpr.sept.so2.modules.formativas.application

import br.ufpr.sept.so2.modules.formativas.application.ports.AlunoPorUsuarioPort
import br.ufpr.sept.so2.modules.formativas.application.ports.FormativaRepository
import br.ufpr.sept.so2.modules.formativas.domain.Formativa
import br.ufpr.sept.so2.shared.domain.exception.AcessoNegadoException
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ListarMinhasFormativasUseCase(
    private val alunoPorUsuarioPort: AlunoPorUsuarioPort,
    private val formativaRepository: FormativaRepository,
) {
    @Transactional(readOnly = true)
    fun execute(usuarioId: UUID, audience: String, pageable: Pageable): Page<Formativa> {
        if (!audience.equals("me", ignoreCase = true)) {
            throw AcessoNegadoException("Neste momento só é possível listar as próprias formativas.")
        }
        val aluno = FormativaAcesso.exigirAlunoAtivo(alunoPorUsuarioPort, usuarioId)
        val size = pageable.pageSize.coerceIn(1, 100)
        val sort = if (pageable.sort.isSorted) pageable.sort else Sort.by(Sort.Direction.DESC, "createdAt")
        return formativaRepository.findByAluno(aluno.id, PageRequest.of(pageable.pageNumber, size, sort))
    }
}
