package br.ufpr.sept.so2.modules.academico.application

import br.ufpr.sept.so2.modules.academico.application.ports.PeriodoLetivoRepository
import br.ufpr.sept.so2.modules.academico.domain.PeriodoLetivo
import br.ufpr.sept.so2.modules.iam.application.ports.AuditLogPort
import br.ufpr.sept.so2.shared.domain.exception.ConflitoEstadoException
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import br.ufpr.sept.so2.shared.infrastructure.Uuids
import com.fasterxml.jackson.databind.ObjectMapper
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
    private val auditLogPort: AuditLogPort,
    private val objectMapper: ObjectMapper,
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

    fun criar(ano: Int, semestre: Int, inicio: LocalDate, fim: LocalDate, atorId: UUID, ip: String?): PeriodoLetivo {
        if (periodoLetivoRepository.existsByAnoAndSemestre(ano, semestre)) {
            throw ConflitoEstadoException("Já existe período letivo para $ano/$semestre.")
        }
        val agora = OffsetDateTime.now()
        val periodo = PeriodoLetivo(Uuids.v7(), ano, semestre, inicio, fim, true, agora, agora)
        garantirSemSobreposicao(periodo)
        val salvo = periodoLetivoRepository.save(periodo)
        auditLogPort.appendDiff(
            "periodo.criado",
            atorId,
            null,
            AuditMutacao.json(objectMapper, "periodoId", salvo.id, AuditMutacao.entre(emptyMap(), foto(salvo))),
            ip,
        )
        return salvo
    }

    fun atualizar(
        id: UUID,
        ano: Int?,
        semestre: Int?,
        inicio: LocalDate?,
        fim: LocalDate?,
        ativo: Boolean?,
        atorId: UUID,
        ip: String?,
    ): PeriodoLetivo {
        val periodo = buscarPorId(id)
        val antes = foto(periodo)
        val anoAlvo = ano ?: periodo.ano
        val semestreAlvo = semestre ?: periodo.semestre
        if ((anoAlvo != periodo.ano || semestreAlvo != periodo.semestre) &&
            periodoLetivoRepository.existsByAnoAndSemestre(anoAlvo, semestreAlvo)
        ) {
            throw ConflitoEstadoException("Já existe período letivo para $anoAlvo/$semestreAlvo.")
        }
        periodo.atualizar(ano, semestre, inicio, fim, ativo)
        garantirSemSobreposicao(periodo)
        val salvo = periodoLetivoRepository.save(periodo)
        val alteracoes = AuditMutacao.entre(antes, foto(salvo))
        if (alteracoes.isNotEmpty()) {
            auditLogPort.appendDiff(
                "periodo.atualizado",
                atorId,
                objectMapper.writeValueAsString(antes),
                AuditMutacao.json(objectMapper, "periodoId", salvo.id, alteracoes),
                ip,
            )
        }
        return salvo
    }

    fun excluir(id: UUID, atorId: UUID, ip: String?) {
        buscarPorId(id)
        periodoLetivoRepository.deleteById(id)
        auditLogPort.append(
            "periodo.excluido",
            atorId,
            objectMapper.writeValueAsString(mapOf("periodoId" to id.toString())),
            ip,
        )
    }

    private fun foto(periodo: PeriodoLetivo): Map<String, String?> = linkedMapOf(
        "ano" to periodo.ano.toString(),
        "semestre" to periodo.semestre.toString(),
        "inicio" to periodo.inicio.toString(),
        "fim" to periodo.fim.toString(),
        "ativo" to periodo.ativo.toString(),
    )

    private fun garantirSemSobreposicao(candidato: PeriodoLetivo) {
        val conflito = periodoLetivoRepository.findAll()
            .filter { it.id != candidato.id }
            .firstOrNull { candidato.sobrepoe(it) }
        if (conflito != null) {
            throw DadoInvalidoException("Período sobrepõe ${conflito.ano}/${conflito.semestre}")
        }
    }
}
