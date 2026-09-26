package br.ufpr.sept.so2.modules.formativas.infrastructure

import br.ufpr.sept.so2.modules.formativas.application.ports.ComissaoMembroPort
import br.ufpr.sept.so2.modules.formativas.domain.TipoComissao
import br.ufpr.sept.so2.modules.formativas.infrastructure.persistence.ComissaoMembroJpaEntity
import br.ufpr.sept.so2.modules.formativas.infrastructure.persistence.ComissaoMembroJpaRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Component
class ComissaoMembroAdapter(
    private val jpaRepository: ComissaoMembroJpaRepository,
) : ComissaoMembroPort {

    @Transactional(readOnly = true)
    override fun cursosDoMembro(usuarioId: UUID, tipo: TipoComissao): Set<UUID> =
        jpaRepository.findByIdUsuarioAndTipo(usuarioId, tipo.name).mapNotNull { it.idCurso }.toSet()

    @Transactional(readOnly = true)
    override fun membrosDosCursos(cursoIds: Collection<UUID>, tipo: TipoComissao): List<UUID> {
        if (cursoIds.isEmpty()) {
            return emptyList()
        }
        return jpaRepository.findByIdCursoInAndTipo(cursoIds.toSet(), tipo.name)
            .mapNotNull { it.idUsuario }
            .distinct()
    }

    @Transactional(readOnly = true)
    override fun ehMembro(usuarioId: UUID, cursoId: UUID, tipo: TipoComissao): Boolean =
        jpaRepository.existsByIdCursoAndIdUsuarioAndTipo(cursoId, usuarioId, tipo.name)

    @Transactional
    override fun adicionarSeAusente(cursoId: UUID, usuarioId: UUID, tipo: TipoComissao) {
        if (jpaRepository.existsByIdCursoAndIdUsuarioAndTipo(cursoId, usuarioId, tipo.name)) {
            return
        }
        jpaRepository.save(
            ComissaoMembroJpaEntity().apply {
                this.idCurso = cursoId
                this.idUsuario = usuarioId
                this.tipo = tipo.name
            },
        )
    }
}
