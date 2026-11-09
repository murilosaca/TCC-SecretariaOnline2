package br.ufpr.sept.so2.modules.solicitacoes.application.ports

interface PdfProtocoloRenderer {
    fun render(dados: Dados): ByteArray

    data class Dados(
        val protocolo: String,
        val tipoNome: String,
        val estado: String,
        val campos: List<Par>,
        val eventos: List<String>,
        val verificacaoUrl: String,
    )

    data class Par(val rotulo: String, val valor: String)
}
