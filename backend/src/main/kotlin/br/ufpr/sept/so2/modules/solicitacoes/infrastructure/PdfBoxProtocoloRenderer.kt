package br.ufpr.sept.so2.modules.solicitacoes.infrastructure

import br.ufpr.sept.so2.modules.solicitacoes.application.ports.PdfProtocoloRenderer
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.font.Standard14Fonts
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory
import org.springframework.stereotype.Component
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream

@Component
class PdfBoxProtocoloRenderer : PdfProtocoloRenderer {
    override fun render(dados: PdfProtocoloRenderer.Dados): ByteArray {
        PDDocument().use { doc ->
            val page = PDPage(PDRectangle.A4)
            doc.addPage(page)
            PDPageContentStream(doc, page).use { cs ->
                val title = PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD)
                val body = PDType1Font(Standard14Fonts.FontName.HELVETICA)
                var y = 780f
                texto(cs, title, 16f, 50f, y, "UFPR SEPT · Protocolo de solicitação")
                y -= 28f
                texto(cs, body, 12f, 50f, y, "Protocolo: ${dados.protocolo}")
                y -= 18f
                texto(cs, body, 12f, 50f, y, "Tipo: ${dados.tipoNome}")
                y -= 18f
                texto(cs, body, 12f, 50f, y, "Estado: ${dados.estado}")
                y -= 28f
                texto(cs, title, 12f, 50f, y, "Formulário")
                for (campo in dados.campos.take(12)) {
                    y -= 16f
                    if (y < 80f) break
                    texto(cs, body, 10f, 50f, y, "${campo.rotulo}: ${campo.valor}")
                }
                y -= 24f
                texto(cs, title, 12f, 50f, y, "Linha do tempo")
                for (evento in dados.eventos.take(8)) {
                    y -= 16f
                    if (y < 80f) break
                    texto(cs, body, 10f, 50f, y, evento)
                }
                val qr = LosslessFactory.createFromImage(doc, qrImage(dados.verificacaoUrl))
                cs.drawImage(qr, 400f, 620f, 120f, 120f)
                texto(cs, body, 8f, 400f, 608f, "Verificação pública")
            }
            val out = ByteArrayOutputStream()
            doc.save(out)
            return out.toByteArray()
        }
    }

    private fun texto(cs: PDPageContentStream, font: PDType1Font, size: Float, x: Float, y: Float, valor: String) {
        cs.beginText()
        cs.setFont(font, size)
        cs.newLineAtOffset(x, y)
        cs.showText(winAnsi(valor).take(110))
        cs.endText()
    }

    private fun qrImage(conteudo: String): BufferedImage {
        val matrix = QRCodeWriter().encode(
            conteudo,
            BarcodeFormat.QR_CODE,
            240,
            240,
            mapOf(
                EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
                EncodeHintType.MARGIN to 1,
            ),
        )
        val image = BufferedImage(matrix.width, matrix.height, BufferedImage.TYPE_INT_RGB)
        for (x in 0 until matrix.width) {
            for (y in 0 until matrix.height) {
                image.setRGB(x, y, if (matrix.get(x, y)) Color.BLACK.rgb else Color.WHITE.rgb)
            }
        }
        return image
    }

    private fun winAnsi(texto: String): String =
        buildString(texto.length) {
            texto.forEach { ch -> append(if (ch.code in 32..255) ch else '?') }
        }
}
