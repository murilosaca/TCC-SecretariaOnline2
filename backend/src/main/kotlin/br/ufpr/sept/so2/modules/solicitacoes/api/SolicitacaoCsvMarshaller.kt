package br.ufpr.sept.so2.modules.solicitacoes.api

import br.ufpr.sept.so2.modules.solicitacoes.api.dto.SolicitacaoResponse
import java.nio.charset.StandardCharsets
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

object SolicitacaoCsvMarshaller {
    private val DATA: DateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

    fun atrasados(itens: List<SolicitacaoResponse>): ByteArray {
        val sb = StringBuilder()
        sb.append('\uFEFF')
        sb.append("Número,Tipo,Aluno,GRR,Curso,Estado,Deliberador,Data Abertura,Prazo,Dias de Atraso\r\n")
        for (item in itens) {
            sb.append(csv(item.protocolo)).append(',')
            sb.append(csv(item.tipoNome)).append(',')
            sb.append(csv(item.solicitanteNome)).append(',')
            sb.append(csv(item.solicitanteGrr)).append(',')
            sb.append(csv(item.cursoNome ?: item.cursoSigla)).append(',')
            sb.append(csv(item.estado)).append(',')
            sb.append(csv(item.deliberadorNome)).append(',')
            sb.append(csv(formatar(item.createdAt))).append(',')
            sb.append(csv(formatar(item.prazoEm))).append(',')
            sb.append(item.diasAtraso ?: 0).append("\r\n")
        }
        return sb.toString().toByteArray(StandardCharsets.UTF_8)
    }

    private fun formatar(valor: OffsetDateTime?): String =
        valor?.format(DATA) ?: ""

    private fun csv(valor: String?): String {
        val raw = valor.orEmpty()
        val escaped = raw.replace("\"", "\"\"")
        return if (escaped.contains(',') || escaped.contains('"') || escaped.contains('\n') || escaped.contains('\r')) {
            "\"$escaped\""
        } else {
            escaped
        }
    }
}
