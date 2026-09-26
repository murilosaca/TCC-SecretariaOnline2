package br.ufpr.sept.so2.modules.formativas.application

import br.ufpr.sept.so2.modules.formativas.application.ports.AlunoResumoPort
import br.ufpr.sept.so2.modules.formativas.application.ports.ComissaoMembroPort
import br.ufpr.sept.so2.modules.formativas.domain.Formativa
import br.ufpr.sept.so2.modules.formativas.domain.TipoComissao
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import java.util.UUID

internal object CaafAcesso {
    const val REVIEW = "formative.review"
    const val ATRIBUIR = "assign-member"
    const val BATCH_APPROVE = "batch-approve"
    val TIPO = TipoComissao.CAAF

    fun cursosDoMembro(port: ComissaoMembroPort, usuarioId: UUID): Set<UUID> =
        port.cursosDoMembro(usuarioId, TIPO)

    fun exigirNoEscopo(formativa: Formativa, cursos: Set<UUID>, alunoResumoPort: AlunoResumoPort) {
        val cursoId = alunoResumoPort.cursoDe(formativa.idAluno)
            ?: throw RecursoNaoEncontradoException("Formativa não encontrada.")
        if (cursoId !in cursos) {
            throw RecursoNaoEncontradoException("Formativa não encontrada.")
        }
    }

    fun cursoDaFormativa(formativa: Formativa, alunoResumoPort: AlunoResumoPort): UUID =
        alunoResumoPort.cursoDe(formativa.idAluno)
            ?: throw RecursoNaoEncontradoException("Formativa não encontrada.")
}
