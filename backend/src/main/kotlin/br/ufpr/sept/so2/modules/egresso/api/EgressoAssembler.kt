package br.ufpr.sept.so2.modules.egresso.api

import br.ufpr.sept.so2.modules.egresso.api.dto.EgressoPainelResponse
import br.ufpr.sept.so2.modules.egresso.api.dto.EgressoPainelResponse.CertificadoItemResponse
import br.ufpr.sept.so2.modules.egresso.api.dto.EgressoPainelResponse.KpisResponse
import br.ufpr.sept.so2.modules.egresso.application.PainelEgresso
import org.springframework.stereotype.Component

@Component
class EgressoAssembler {
    /**
     * Diploma, colação e data de conclusão ficam nulos até o registro da secretaria (F5.11).
     * Esta fatia não cria tabela de diploma.
     */
    fun from(painel: PainelEgresso): EgressoPainelResponse =
        EgressoPainelResponse(
            painel.nome,
            painel.curso,
            null,
            KpisResponse(painel.horasFormativasValidadas, painel.totalCertificados, null),
            null,
            null,
            painel.certificados.map { item ->
                CertificadoItemResponse(
                    item.id,
                    item.titulo,
                    item.tipo,
                    item.emitidoEm,
                    item.hashSha256,
                    linkedMapOf("reemitir" to "/egressos/me/certificados/${item.id}/reemissao"),
                )
            },
            linkedMapOf("self" to "/egressos/me"),
        )
}
