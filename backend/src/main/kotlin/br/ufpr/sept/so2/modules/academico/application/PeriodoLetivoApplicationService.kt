package br.ufpr.sept.so2.modules.academico.application

import br.ufpr.sept.so2.modules.academico.application.ports.PeriodoLetivoRepository
import br.ufpr.sept.so2.modules.academico.domain.PeriodoLetivo
import br.ufpr.sept.so2.shared.domain.exception.ConflitoEstadoException
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import br.ufpr.sept.so2.shared.infrastructure.Uuids
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

@Service
@Transactional
class PeriodoLetivoApplicationService(
    private val periodoLetivoRepository: PeriodoLetivoRepository,
) {
    @Transactional(readOnly = true)
    fun listar(pageable: Pageable): Page<PeriodoLetivo> = periodoLetivoRepository.findAll(pageable)

    @Transactional(readOnly = true)
    fun buscarPorId(id: UUID): PeriodoLetivo =
        periodoLetivoRepository.findById(id)
            .orElseThrow { RecursoNaoEncontradoException("Período letivo não encontrado: $id") }

    @Transactional(readOnly = true)
    fun buscarVigente(data: LocalDate): PeriodoLetivo =
        periodoLetivoRepository.findVigente(data)
            .orElseThrow { RecursoNaoEncontradoException("Não há período letivo vigente em $data.") }

    fun criar(ano: Int, semestre: Int, inicio: LocalDate, fim: LocalDate): PeriodoLetivo {
        if (periodoLetivoRepository.existsByAnoAndSemestre(ano, semestre)) {
            throw ConflitoEstadoException("Já existe período letivo para $ano/$semestre.")
        }
        val agora = OffsetDateTime.now()
        val periodo = PeriodoLetivo(Uuids.v7(), ano, semestre, inicio, fim, true, agora, agora)
        garantirSemSobreposicao(periodo)
        return periodoLetivoRepository.save(periodo)
    }

    fun atualizar(
        id: UUID,
        ano: Int?,
        semestre: Int?,
        inicio: LocalDate?,
        fim: LocalDate?,
        ativo: Boolean?,
    ): PeriodoLetivo {
        val periodo = buscarPorId(id)
        val anoAlvo = ano ?: periodo.ano
        val semestreAlvo = semestre ?: periodo.semestre
        if ((anoAlvo != periodo.ano || semestreAlvo != periodo.semestre) &&
            periodoLetivoRepository.existsByAnoAndSemestre(anoAlvo, semestreAlvo)
        ) {
            throw ConflitoEstadoException("Já existe período letivo para $anoAlvo/$semestreAlvo.")
        }
        periodo.atualizar(ano, semestre, inicio, fim, ativo)
        garantirSemSobreposicao(periodo)
        return periodoLetivoRepository.save(periodo)
    }

    fun excluir(id: UUID) {
        buscarPorId(id)
        periodoLetivoRepository.deleteById(id)
    }

    private fun garantirSemSobreposicao(candidato: PeriodoLetivo) {
        val sobrepoe = periodoLetivoRepository.findAll()
            .filter { it.id != candidato.id }
            .any { candidato.sobrepoe(it) }
        if (sobrepoe) {
            throw ConflitoEstadoException("O intervalo informado sobrepõe outro período letivo.")
        }
    }
}
