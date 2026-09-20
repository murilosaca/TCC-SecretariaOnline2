package br.ufpr.sept.so2.modules.certificados.api

import br.ufpr.sept.so2.modules.certificados.api.dto.CertificadoResponse
import br.ufpr.sept.so2.modules.certificados.domain.Certificado
import org.springframework.stereotype.Component

@Component
class CertificadoAssembler {
    fun from(certificado: Certificado): CertificadoResponse {
        val self = "/certificates/${certificado.id}"
        return CertificadoResponse(
            certificado.id,
            certificado.tipo.name,
            certificado.titulo,
            certificado.cargaHoraria,
            certificado.emitidoEm,
            certificado.hashSha256,
            linkedMapOf(
                "self" to self,
                "download" to "$self/download",
            ),
        )
    }
}
