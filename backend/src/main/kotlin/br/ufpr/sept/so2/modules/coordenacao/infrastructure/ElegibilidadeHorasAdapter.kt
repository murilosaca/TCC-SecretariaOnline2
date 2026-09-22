package br.ufpr.sept.so2.modules.coordenacao.infrastructure

import br.ufpr.sept.so2.modules.academico.application.ports.AlunoRepository
import br.ufpr.sept.so2.modules.coordenacao.application.ports.ElegibilidadeHorasPort
import br.ufpr.sept.so2.modules.coordenacao.domain.ElegibilidadeHorasRegra
import br.ufpr.sept.so2.modules.coordenacao.infrastructure.persistence.ElegibilidadeHorasJpaEntity
import br.ufpr.sept.so2.modules.coordenacao.infrastructure.persistence.ElegibilidadeHorasJpaRepository
import br.ufpr.sept.so2.modules.formativas.application.ports.FormativaRepository
import br.ufpr.sept.so2.modules.formativas.domain.FormativaEstado
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.UUID

@Component
class ElegibilidadeHorasAdapter(
    private val alunoRepository: AlunoRepository,
    private val formativaRepository: FormativaRepository,
    private val jpaRepository: ElegibilidadeHorasJpaRepository,
) : ElegibilidadeHorasPort {

    @Transactional
    override fun congelarQuemJaAtingiu(idCurso: UUID, limiarAnterior: Int) {
        val agora = OffsetDateTime.now()
        alunoRepository.findByIdCursoIn(listOf(idCurso)).forEach { aluno ->
            congelarSeAtingiu(aluno.id, idCurso, limiarAnterior, agora)
        }
    }

    @Transactional(readOnly = true)
    override fun requeridas(alunoId: UUID, limiarAtual: Int, horasValidadas: Int): Int {
        val entity = jpaRepository.findById(alunoId).orElse(null)
        val limiar = if (entity?.elegivel == true) entity.limiarAplicado else null
        return ElegibilidadeHorasRegra.requeridas(horasValidadas, limiarAtual, limiar)
    }

    private fun congelarSeAtingiu(alunoId: UUID, idCurso: UUID, limiarAnterior: Int, agora: OffsetDateTime) {
        val existente = jpaRepository.findById(alunoId).orElse(null)
        val horas = formativaRepository.somarCargaHoraria(alunoId, FormativaEstado.APROVADA)
        val jaElegivel = existente?.elegivel == true
        if (!ElegibilidadeHorasRegra.deveCongelar(horas, limiarAnterior, jaElegivel)) {
            return
        }
        val entity = existente ?: ElegibilidadeHorasJpaEntity()
        entity.id = alunoId
        entity.idCurso = idCurso
        entity.elegivel = true
        entity.limiarAplicado = limiarAnterior
        entity.horasValidadas = horas
        entity.congeladaEm = agora
        jpaRepository.save(entity)
    }
}
