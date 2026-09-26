package br.ufpr.sept.so2.modules.estagio.application

import br.ufpr.sept.so2.modules.estagio.domain.Estagio
import br.ufpr.sept.so2.modules.estagio.domain.EstagioSituacao
import br.ufpr.sept.so2.modules.iam.application.ports.AuditLogPort
import br.ufpr.sept.so2.modules.iam.application.ports.OutboxPort
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import java.util.UUID

internal object EstagioTrilha {
    fun registrar(
        outboxPort: OutboxPort,
        auditLogPort: AuditLogPort,
        mapper: ObjectMapper,
        tipo: String,
        atorId: UUID,
        estagio: Estagio,
        ip: String?,
    ) {
        val evento = EstagioJson.de(
            mapper,
            mapOf(
                "estagioId" to estagio.id.toString(),
                "alunoId" to estagio.idAluno.toString(),
                "cursoId" to estagio.idCurso.toString(),
                "atorId" to atorId.toString(),
                "situacao" to estagio.situacao.name,
            ),
        )
        outboxPort.enqueue(tipo, evento)
        auditLogPort.append(tipo, atorId, evento, ip)
    }
}

internal object EstagioJson {
    fun de(mapper: ObjectMapper, valor: Map<String, Any?>): String {
        try {
            return mapper.writeValueAsString(valor)
        } catch (_: JsonProcessingException) {
            throw DadoInvalidoException("Não foi possível serializar o evento de estágio.")
        }
    }
}

internal object EstagioPagina {
    fun de(pageable: Pageable): Pageable {
        val size = pageable.pageSize.coerceIn(1, 100)
        val sort = if (pageable.sort.isSorted) pageable.sort else Sort.by(Sort.Direction.DESC, "createdAt")
        return PageRequest.of(pageable.pageNumber, size, sort)
    }

    fun situacao(raw: String?): EstagioSituacao? {
        if (raw.isNullOrBlank()) {
            return null
        }
        return EstagioSituacao.from(raw)
    }
}

internal object EstagioArquivo {
    fun nomeSeguro(bruto: String?): String {
        val base = bruto?.substringAfterLast('/')?.substringAfterLast('\\')?.trim().orEmpty()
        return if (base.isEmpty()) "documento.pdf" else base.take(200)
    }
}
