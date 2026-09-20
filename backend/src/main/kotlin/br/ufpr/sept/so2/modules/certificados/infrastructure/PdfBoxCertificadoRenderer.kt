package br.ufpr.sept.so2.modules.certificados.infrastructure

import br.ufpr.sept.so2.modules.certificados.application.ports.PdfCertificadoRenderer
import br.ufpr.sept.so2.modules.certificados.application.ports.PdfCertificadoRenderer.Dados
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
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Component
class PdfBoxCertificadoRenderer : PdfCertificadoRenderer {

    override fun render(dados: Dados, qrUrl: String?): ByteArray {
        PDDocument().use { doc ->
            val page = PDPage(PDRectangle.A4)
            doc.addPage(page)
            PDPageContentStream(doc, page).use { cs ->
                val title = PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD)
                val body = PDType1Font(Standard14Fonts.FontName.HELVETICA)
                var y = 760f
                cs.beginText()
                cs.setFont(title, 14f)
                cs.newLineAtOffset(50f, y)
                cs.showText(winAnsi("UFPR SEPT · Secretaria Online 2"))
                cs.endText()
                y -= 36f
                cs.beginText()
                cs.setFont(title, 20f)
                cs.newLineAtOffset(50f, y)
                cs.showText(winAnsi("Certificado de participação"))
                cs.endText()
                y -= 40f
                linha(cs, body, 50f, y, "Beneficiário", dados.beneficiarioNome)
                y -= 22f
                linha(cs, body, 50f, y, "Atividade", dados.titulo)
                y -= 22f
                linha(cs, body, 50f, y, "Carga horária", "${dados.cargaHoraria} hora(s)")
                y -= 22f
                linha(cs, body, 50f, y, "Emitido em", DATA.format(dados.emitidoEm.atZoneSameInstant(ZoneOffset.UTC)))
                if (!dados.hashSha256.isNullOrBlank()) {
                    y -= 22f
                    linha(cs, body, 50f, y, "Hash SHA-256", dados.hashSha256)
                }
                y -= 36f
                cs.beginText()
                cs.setFont(body, 10f)
                cs.newLineAtOffset(50f, y)
                cs.showText(winAnsi("Documento gerado exclusivamente pelo sistema. Não é upload externo."))
                cs.endText()
                if (!qrUrl.isNullOrBlank()) {
                    val qr = LosslessFactory.createFromImage(doc, qrImage(qrUrl))
                    cs.drawImage(qr, 400f, 560f, 140f, 140f)
                    cs.beginText()
                    cs.setFont(body, 8f)
                    cs.newLineAtOffset(400f, 546f)
                    cs.showText(winAnsi("Verificação pública (F0.7)"))
                    cs.endText()
                }
            }
            val out = ByteArrayOutputStream()
            doc.save(out)
            return out.toByteArray()
        }
    }

    private fun linha(
        cs: PDPageContentStream,
        font: PDType1Font,
        x: Float,
        y: Float,
        rotulo: String,
        valor: String,
    ) {
        cs.beginText()
        cs.setFont(font, 11f)
        cs.newLineAtOffset(x, y)
        cs.showText(winAnsi("$rotulo: $valor"))
        cs.endText()
    }

    companion object {
        private val DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm 'UTC'")

        private fun qrImage(conteudo: String): BufferedImage {
            val matrix = QRCodeWriter().encode(
                conteudo,
                BarcodeFormat.QR_CODE,
                280,
                280,
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
                texto.forEach { ch ->
                    append(if (ch.code in 32..255) ch else '?')
                }
            }
    }
}
