package br.ufpr.sept.so2.modules.tcc.application

import br.ufpr.sept.so2.modules.tcc.domain.TccEstado
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort

internal object TccJson {
    fun de(mapper: ObjectMapper, valor: Map<String, Any?>): String {
        try {
            return mapper.writeValueAsString(valor)
        } catch (_: JsonProcessingException) {
            throw DadoInvalidoException("Não foi possível serializar o evento de TCC.")
        }
    }
}

internal object TccPagina {
    fun de(pageable: Pageable): Pageable {
        val size = pageable.pageSize.coerceIn(1, 100)
        val sort = if (pageable.sort.isSorted) pageable.sort else Sort.by(Sort.Direction.DESC, "createdAt")
        return PageRequest.of(pageable.pageNumber, size, sort)
    }

    fun estado(raw: String?): TccEstado? {
        if (raw.isNullOrBlank()) {
            return null
        }
        return TccEstado.from(raw)
    }
}

internal object TccArquivo {
    fun nomeSeguro(bruto: String?): String {
        val base = bruto?.substringAfterLast('/')?.substringAfterLast('\\')?.trim().orEmpty()
        return if (base.isEmpty()) "tcc.pdf" else base.take(200)
    }
}
