package br.ufpr.sept.so2.modules.coordenacao.api.dto

import br.ufpr.sept.so2.modules.coordenacao.domain.ConfigCurso
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.UUID

data class ConfigCursoResponse(
    val id: UUID,
    val nome: String,
    val sigla: String,
    val horasFormativasMinimas: Int,
    val duracaoCalendario: Int,
    val bancaMembrosExternos: Int,
    val bancaModalidade: String,
    val regimento: String,
    @get:JsonProperty("_links")
    val links: Map<String, String>,
) {
    companion object {
        fun from(config: ConfigCurso, podeAtualizar: Boolean): ConfigCursoResponse {
            val path = "/coordenacao/cursos/${config.id}/config"
            val links = linkedMapOf("self" to path)
            if (podeAtualizar) {
                links["update"] = path
            }
            return ConfigCursoResponse(
                config.id,
                config.nome,
                config.sigla,
                config.horasFormativasMinimas,
                config.duracaoCalendario,
                config.bancaMembrosExternos,
                config.bancaModalidade.name,
                config.regimento,
                links,
            )
        }
    }
}
