package br.ufpr.sept.so2.modules.comunicacao.application

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

data class MensagemTemplate(val assunto: String, val corpo: String)

fun interface RenderizadorTemplatePort {
    fun renderizar(tipo: String, payload: String): MensagemTemplate?
}

/**
 * Visível ao handler durante o despacho. Sem template, permanece nulo
 * e o handler segue o texto que já tinha.
 */
object TemplateMensagemAtual {
    private val local = ThreadLocal<MensagemTemplate?>()

    fun definir(mensagem: MensagemTemplate?) {
        if (mensagem == null) local.remove() else local.set(mensagem)
    }

    fun atual(): MensagemTemplate? = local.get()

    fun limpar() {
        local.remove()
    }
}

fun MailComTemplate(assunto: String, corpo: String): Pair<String, String> {
    val atual = TemplateMensagemAtual.atual() ?: return assunto to corpo
    return atual.assunto.ifBlank { assunto } to atual.corpo.ifBlank { corpo }
}

@Component
class RenderizadorTemplate(
    private val repositorio: TemplateComunicacaoRepository,
    private val objectMapper: ObjectMapper,
) : RenderizadorTemplatePort {
    override fun renderizar(tipo: String, payload: String): MensagemTemplate? {
        val atual = repositorio.revisaoAtualPorNome(tipo) ?: return null
        val valores = valoresDoPayload(payload)
        LOG.debug("Template {} aplicado na revisão corrente.", tipo)
        return MensagemTemplate(
            PlaceholdersTemplate.aplicar(atual.assunto, valores),
            PlaceholdersTemplate.aplicar(atual.corpo, valores),
        )
    }

    private fun valoresDoPayload(payload: String): Map<String, String> {
        return try {
            val json = objectMapper.readTree(payload)
            PlaceholdersTemplate.NOMES.mapNotNull { chave ->
                val node = json.get(chave) ?: return@mapNotNull null
                if (node.isNull) null else chave to node.asText()
            }.toMap()
        } catch (_: Exception) {
            emptyMap()
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(RenderizadorTemplate::class.java)
    }
}
