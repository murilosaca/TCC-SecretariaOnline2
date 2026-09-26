package br.ufpr.sept.so2.modules.diplomas.application

import br.ufpr.sept.so2.modules.diplomas.domain.Diploma
import br.ufpr.sept.so2.modules.iam.application.ports.AuditLogPort
import br.ufpr.sept.so2.modules.iam.application.ports.OutboxPort
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import java.util.UUID

internal object DiplomaJson {
    fun de(mapper: ObjectMapper, valor: Map<String, Any?>): String {
        try {
            return mapper.writeValueAsString(valor)
        } catch (_: JsonProcessingException) {
            throw DadoInvalidoException("Não foi possível serializar o evento de diploma.")
        }
    }
}

internal object DiplomaTrilha {
    fun registrar(
        outboxPort: OutboxPort,
        auditLogPort: AuditLogPort,
        mapper: ObjectMapper,
        tipo: String,
        atorId: UUID,
        diploma: Diploma,
        ip: String?,
        extras: Map<String, Any?> = emptyMap(),
    ) {
        val base = linkedMapOf<String, Any?>(
            "diplomaId" to diploma.id.toString(),
            "alunoId" to diploma.idAluno.toString(),
            "cursoId" to diploma.idCurso.toString(),
            "numero" to diploma.numero,
            "situacao" to diploma.situacao.name,
            "atorId" to atorId.toString(),
        )
        base.putAll(extras)
        val evento = DiplomaJson.de(mapper, base)
        outboxPort.enqueue(tipo, evento)
        auditLogPort.append(tipo, atorId, evento, ip)
    }
}

internal object DiplomaPagina {
    fun de(pageable: Pageable): Pageable {
        val size = pageable.pageSize.coerceIn(1, 100)
        val sort = if (pageable.sort.isSorted) pageable.sort else Sort.by(Sort.Direction.DESC, "dataColacao")
        return PageRequest.of(pageable.pageNumber, size, sort)
    }
}

internal object DiplomaArquivo {
    fun nomeSeguro(bruto: String?): String {
        val base = bruto?.substringAfterLast('/')?.substringAfterLast('\\')?.trim().orEmpty()
        return if (base.isEmpty()) "diploma.pdf" else base.take(200)
    }
}
