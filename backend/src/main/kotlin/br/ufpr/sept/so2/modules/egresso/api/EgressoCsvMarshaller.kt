package br.ufpr.sept.so2.modules.egresso.api

import br.ufpr.sept.so2.modules.egresso.api.dto.EgressoItemResponse
import java.nio.charset.StandardCharsets
import java.time.format.DateTimeFormatter

/** Mesmo critério do CSV de atrasados (fatia 20): síncrono, sem job. */
object EgressoCsvMarshaller {
    private val DATA: DateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

    fun egressos(itens: List<EgressoItemResponse>): ByteArray {
        val sb = StringBuilder()
        sb.append('\uFEFF')
        sb.append("Nome,GRR,Curso,Ano de Colação,Situação do Diploma,Número,Data de Colação\r\n")
        for (item in itens) {
            sb.append(csv(item.nome)).append(',')
            sb.append(csv(item.grr)).append(',')
            sb.append(csv(item.cursoSigla)).append(',')
            sb.append(item.anoColacao).append(',')
            sb.append(csv(item.situacaoDiploma)).append(',')
            sb.append(csv(item.numeroDiploma)).append(',')
            sb.append(csv(DATA.format(item.dataColacao))).append("\r\n")
        }
        return sb.toString().toByteArray(StandardCharsets.UTF_8)
    }

    private fun csv(valor: String?): String {
        val escaped = valor.orEmpty().replace("\"", "\"\"")
        return if (escaped.contains(',') || escaped.contains('"') || escaped.contains('\n') || escaped.contains('\r')) {
            "\"$escaped\""
        } else {
            escaped
        }
    }
}
