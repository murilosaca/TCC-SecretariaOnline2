package br.ufpr.sept.so2.modules.coordenacao.infrastructure

import br.ufpr.sept.so2.modules.academico.application.ports.CursoRepository
import br.ufpr.sept.so2.modules.academico.domain.Curso
import br.ufpr.sept.so2.modules.coordenacao.application.ports.CursoConfigPort
import br.ufpr.sept.so2.modules.coordenacao.application.ports.CursoConfigPort.CursoResumo
import br.ufpr.sept.so2.modules.coordenacao.application.ports.CursoConfigPort.ParametrosBanca
import br.ufpr.sept.so2.modules.coordenacao.infrastructure.persistence.CursoConfiguracaoJpaEntity
import br.ufpr.sept.so2.modules.coordenacao.infrastructure.persistence.CursoConfiguracaoJpaRepository
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class CursoConfigAdapter(
    private val cursoRepository: CursoRepository,
    private val jpaRepository: CursoConfiguracaoJpaRepository,
) : CursoConfigPort {
    override fun obterCurso(id: UUID, bloquear: Boolean): CursoResumo? {
        val curso = if (bloquear) {
            cursoRepository.findByIdForUpdate(id).orElse(null)
        } else {
            cursoRepository.findById(id).orElse(null)
        }
        return curso?.toResumo()
    }

    override fun obterParametros(id: UUID): ParametrosBanca? =
        jpaRepository.findById(id).orElse(null)?.toParametros()

    override fun salvarHoras(id: UUID, horas: Int) {
        val curso = cursoRepository.findById(id)
            .orElseThrow { RecursoNaoEncontradoException("Curso não encontrado.") }
        curso.atualizar(null, null, null, null, horas, null)
        cursoRepository.save(curso)
    }

    override fun salvarParametros(parametros: ParametrosBanca) {
        val entity = jpaRepository.findById(parametros.idCurso).orElseGet {
            CursoConfiguracaoJpaEntity().apply { id = parametros.idCurso }
        }
        entity.duracaoCalendario = parametros.duracaoCalendario
        entity.bancaMembrosExternos = parametros.bancaMembrosExternos
        entity.bancaModalidade = parametros.bancaModalidade
        entity.regimento = parametros.regimento
        jpaRepository.save(entity)
    }

    private fun Curso.toResumo() = CursoResumo(
        id,
        nome,
        sigla,
        idCoordenador,
        horasFormativasMinimas,
        ativo,
    )

    private fun CursoConfiguracaoJpaEntity.toParametros() = ParametrosBanca(
        requireNotNull(id),
        duracaoCalendario,
        bancaMembrosExternos,
        bancaModalidade,
        regimento,
    )
}
