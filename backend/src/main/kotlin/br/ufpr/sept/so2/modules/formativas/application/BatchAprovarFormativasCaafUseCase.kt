package br.ufpr.sept.so2.modules.formativas.application

import br.ufpr.sept.so2.modules.certificados.application.ports.CertificadoPorFormativaPort
import br.ufpr.sept.so2.modules.formativas.application.ports.AlunoResumoPort
import br.ufpr.sept.so2.modules.formativas.application.ports.ComissaoMembroPort
import br.ufpr.sept.so2.modules.formativas.application.ports.FormativaRepository
import br.ufpr.sept.so2.modules.formativas.domain.Formativa
import br.ufpr.sept.so2.modules.iam.application.ports.AuditLogPort
import br.ufpr.sept.so2.modules.iam.application.ports.OutboxPort
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.UUID

@Service
class BatchAprovarFormativasCaafUseCase(
    private val formativaRepository: FormativaRepository,
    private val comissaoMembroPort: ComissaoMembroPort,
    private val alunoResumoPort: AlunoResumoPort,
    private val obterPoolCaafUseCase: ObterPoolCaafUseCase,
    private val certificadoPorFormativaPort: CertificadoPorFormativaPort,
    private val outboxPort: OutboxPort,
    private val auditLogPort: AuditLogPort,
    private val objectMapper: ObjectMapper,
) {
    @Transactional
    fun execute(revisorId: UUID, ids: List<UUID>?, decisao: String?, ip: String?): CaafPoolVisao {
        val idsLimpos = ids?.distinct().orEmpty()
        if (idsLimpos.isEmpty()) {
            throw DadoInvalidoException("Selecione ao menos uma formativa para aprovação em lote.")
        }
        val decisaoNormalizada = decisao?.trim()?.uppercase().orEmpty()
        if (decisaoNormalizada != DECISAO_APROVADA) {
            throw DadoInvalidoException("Indeferimento em lote não está disponível. Use a revisão individual.")
        }
        val cursos = CaafAcesso.cursosDoMembro(comissaoMembroPort, revisorId)
        if (cursos.isEmpty()) {
            throw RecursoNaoEncontradoException("Formativa não encontrada.")
        }
        val formativas = idsLimpos.map { id ->
            formativaRepository.findById(id)
                ?: throw RecursoNaoEncontradoException("Formativa não encontrada.")
        }
        formativas.forEach { CaafAcesso.exigirNoEscopo(it, cursos, alunoResumoPort) }
        val invalidos = formativas.filterNot { it.elegivelAprovacaoEmLote() }.map { it.id }
        if (invalidos.isNotEmpty()) {
            throw DadoInvalidoException(
                "Aprovação em lote só para formativas com presença já validada pelo sistema. " +
                    "Itens incompatíveis: ${invalidos.joinToString(", ")}",
            )
        }
        val agora = OffsetDateTime.now()
        val aprovadas = mutableListOf<Formativa>()
        for (formativa in formativas) {
            formativa.aprovar(PARECER_LOTE, revisorId, agora)
            val salva = formativaRepository.save(formativa)
            certificadoPorFormativaPort.emitirSeAusente(
                salva.id,
                salva.idAluno,
                salva.idEvento,
                salva.titulo,
                salva.cargaHoraria,
                revisorId,
                ip,
            )
            registrarIndividual(revisorId, salva, ip)
            aprovadas.add(salva)
        }
        registrarLote(revisorId, aprovadas, ip)
        return obterPoolCaafUseCase.execute(revisorId)
    }

    private fun registrarIndividual(revisorId: UUID, formativa: Formativa, ip: String?) {
        val evento = toJson(
            mapOf(
                "formativaId" to formativa.id.toString(),
                "alunoId" to formativa.idAluno.toString(),
                "acao" to RevisarFormativaUseCase.ACAO_APROVAR,
                "estado" to formativa.estado.name,
                "cargaHoraria" to formativa.cargaHoraria,
                "revisorId" to revisorId.toString(),
                "lote" to true,
            ),
        )
        outboxPort.enqueue(TIPO_INDIVIDUAL, evento)
        auditLogPort.append(TIPO_INDIVIDUAL, revisorId, evento, ip)
    }

    private fun registrarLote(revisorId: UUID, formativas: List<Formativa>, ip: String?) {
        val evento = toJson(
            mapOf(
                "formativaIds" to formativas.map { it.id.toString() },
                "alunoIds" to formativas.map { it.idAluno.toString() }.distinct(),
                "quantidade" to formativas.size,
                "revisorId" to revisorId.toString(),
                "decisao" to DECISAO_APROVADA,
            ),
        )
        outboxPort.enqueue(TIPO_LOTE, evento)
        auditLogPort.append(TIPO_LOTE, revisorId, evento, ip)
    }

    private fun toJson(valor: Any): String {
        try {
            return objectMapper.writeValueAsString(valor)
        } catch (_: JsonProcessingException) {
            throw DadoInvalidoException("Não foi possível serializar o evento de lote.")
        }
    }

    companion object {
        const val DECISAO_APROVADA = "APROVADA"
        const val PARECER_LOTE = "Aprovada em lote — presença já validada pelo sistema."
        const val TIPO_INDIVIDUAL = "formativa.aprovada"
        const val TIPO_LOTE = "formativas.batch_approved"
    }
}
