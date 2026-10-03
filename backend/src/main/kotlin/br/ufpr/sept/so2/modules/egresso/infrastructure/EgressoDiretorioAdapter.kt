package br.ufpr.sept.so2.modules.egresso.infrastructure

import br.ufpr.sept.so2.modules.academico.application.ports.AlunoRepository
import br.ufpr.sept.so2.modules.academico.application.ports.CursoRepository
import br.ufpr.sept.so2.modules.egresso.application.ports.EgressoDiretorioPort
import br.ufpr.sept.so2.modules.egresso.application.ports.EgressoDiretorioPort.Aluno
import br.ufpr.sept.so2.modules.egresso.application.ports.EgressoDiretorioPort.Curso
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Component
class EgressoDiretorioAdapter(
    private val alunoRepository: AlunoRepository,
    private val cursoRepository: CursoRepository,
) : EgressoDiretorioPort {

    @Transactional(readOnly = true)
    override fun alunosPorId(ids: Collection<UUID>): Map<UUID, Aluno> =
        ids.distinct()
            .mapNotNull { id -> alunoRepository.findById(id).orElse(null) }
            .associate { aluno ->
                aluno.id to Aluno(aluno.id, aluno.nomeSocial ?: aluno.nome, aluno.grr.value)
            }

    @Transactional(readOnly = true)
    override fun cursosPorId(ids: Collection<UUID>): Map<UUID, Curso> =
        ids.distinct()
            .mapNotNull { id -> cursoRepository.findById(id).orElse(null) }
            .associate { curso -> curso.id to Curso(curso.id, curso.nome, curso.sigla) }
}
