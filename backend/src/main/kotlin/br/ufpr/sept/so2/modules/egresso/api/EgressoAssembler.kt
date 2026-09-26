package br.ufpr.sept.so2.modules.egresso.api

import br.ufpr.sept.so2.modules.egresso.api.dto.EgressoPainelResponse
import br.ufpr.sept.so2.modules.egresso.api.dto.EgressoPainelResponse.CertificadoItemResponse
import br.ufpr.sept.so2.modules.egresso.api.dto.EgressoPainelResponse.ColacaoResponse
import br.ufpr.sept.so2.modules.egresso.api.dto.EgressoPainelResponse.DiplomaResponse
import br.ufpr.sept.so2.modules.egresso.api.dto.EgressoPainelResponse.KpisResponse
import br.ufpr.sept.so2.modules.egresso.application.PainelEgresso
import org.springframework.stereotype.Component

@Component
class EgressoAssembler {
    fun from(painel: PainelEgresso): EgressoPainelResponse {
        val diploma = painel.diploma?.let { item ->
            val links = linkedMapOf<String, String>()
            if (!item.storageKey.isNullOrBlank()) {
                links["download"] = "/egressos/me/diploma"
            }
            DiplomaResponse(item.numero, item.emitidoEm, links)
        }
        val colacao = painel.colacao?.let { ColacaoResponse(it.data, it.turma) }
        return EgressoPainelResponse(
            painel.nome,
            painel.curso,
            painel.concluidoEm,
            KpisResponse(
                painel.horasFormativasValidadas,
                painel.totalCertificados,
                painel.situacaoDiploma,
            ),
            diploma,
            colacao,
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
}
