package br.ufpr.sept.so2.modules.importacao.infrastructure

import br.ufpr.sept.so2.modules.importacao.application.ports.AlocacaoProfessorPort
import br.ufpr.sept.so2.modules.importacao.infrastructure.persistence.AlocacaoProfessorJpaEntity
import br.ufpr.sept.so2.modules.importacao.infrastructure.persistence.AlocacaoProfessorJpaRepository
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.util.UUID

@Component
class AlocacaoProfessorAdapter(
    private val repository: AlocacaoProfessorJpaRepository,
) : AlocacaoProfessorPort {
    override fun existe(idUsuario: UUID, idDisciplina: UUID): Boolean =
        repository.existsByIdUsuarioAndIdDisciplina(idUsuario, idDisciplina)

    override fun salvar(id: UUID, idUsuario: UUID, idDisciplina: UUID, idCurso: UUID, createdAt: OffsetDateTime) {
        repository.save(
            AlocacaoProfessorJpaEntity(
                id = id,
                idUsuario = idUsuario,
                idDisciplina = idDisciplina,
                idCurso = idCurso,
                createdAt = createdAt,
            ),
        )
    }
}
