package br.ufpr.sept.so2.modules.formativas.infrastructure

import br.ufpr.sept.so2.modules.academico.application.ports.AlunoRepository
import br.ufpr.sept.so2.modules.formativas.application.ports.AlunoResumoPort
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Component
class AlunoResumoAdapter(
    private val alunoRepository: AlunoRepository,
) : AlunoResumoPort {

    @Transactional(readOnly = true)
    override fun nomeDe(alunoId: UUID): String? {
        val aluno = alunoRepository.findById(alunoId).orElse(null) ?: return null
        val nomeSocial = aluno.nomeSocial?.trim().orEmpty()
        if (nomeSocial.isNotEmpty()) {
            return nomeSocial
        }
        val nome = aluno.nome.trim()
        return nome.ifEmpty { null }
    }
}
