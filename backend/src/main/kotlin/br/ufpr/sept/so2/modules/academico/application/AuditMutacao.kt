package br.ufpr.sept.so2.modules.academico.application

import com.fasterxml.jackson.databind.ObjectMapper
import java.util.UUID

/** Diff {campo, de, para} só do que mudou (RF-F5-003-D03 / RF-TR-004). */
object AuditMutacao {
    data class Alteracao(val campo: String, val de: String?, val para: String?)

    fun entre(antes: Map<String, String?>, depois: Map<String, String?>): List<Alteracao> {
        val campos = LinkedHashSet<String>()
        campos.addAll(antes.keys)
        campos.addAll(depois.keys)
        return campos.mapNotNull { campo ->
            val de = antes[campo]
            val para = depois[campo]
            if (de == para) null else Alteracao(campo, de, para)
        }
    }

    fun json(objectMapper: ObjectMapper, entidade: String, id: UUID, alteracoes: List<Alteracao>): String {
        val corpo = linkedMapOf<String, Any?>(
            entidade to id.toString(),
            "alteracoes" to alteracoes.map { alteracao ->
                linkedMapOf("campo" to alteracao.campo, "de" to alteracao.de, "para" to alteracao.para)
            },
        )
        return objectMapper.writeValueAsString(corpo)
    }
}
