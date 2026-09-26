package br.ufpr.sept.so2.modules.diplomas.api

import br.ufpr.sept.so2.modules.academico.application.ports.AlunoRepository
import br.ufpr.sept.so2.modules.diplomas.api.dto.DiplomaResponse
import br.ufpr.sept.so2.modules.diplomas.api.dto.ElegiveisColacaoResponse
import br.ufpr.sept.so2.modules.diplomas.api.dto.ElegivelColacaoResponse
import br.ufpr.sept.so2.modules.diplomas.application.ListaElegiveisColacao
import br.ufpr.sept.so2.modules.diplomas.domain.Diploma
import br.ufpr.sept.so2.modules.diplomas.domain.DiplomaSituacao
import br.ufpr.sept.so2.modules.diplomas.domain.ElegibilidadeColacao
import org.springframework.stereotype.Component

@Component
class DiplomaAssembler(
    private val alunoRepository: AlunoRepository,
) {
    fun elegiveis(lista: ListaElegiveisColacao, podeRegistrar: Boolean): ElegiveisColacaoResponse {
        val links = linkedMapOf<String, String>()
        links["self"] =
            "/diplomas/elegiveis?cursoId=${lista.cursoId}&periodoId=${lista.periodoId}"
        if (podeRegistrar && lista.itens.any { it.elegivel }) {
            links["confirm"] = "/diplomas"
        }
        return ElegiveisColacaoResponse(
            lista.cursoId,
            lista.periodoId,
            lista.itens.map { item -> elegivel(item) },
            links,
        )
    }

    fun from(diploma: Diploma, podeRegistrar: Boolean): DiplomaResponse {
        val alunoNome = alunoRepository.findById(diploma.idAluno).orElse(null)?.let { aluno ->
            val social = aluno.nomeSocial?.trim().orEmpty()
            if (social.isNotEmpty()) social else aluno.nome
        }
        return DiplomaResponse(
            diploma.id,
            diploma.idAluno,
            alunoNome,
            diploma.idCurso,
            diploma.idPeriodoLetivo,
            diploma.numero,
            diploma.situacao.name,
            diploma.dataColacao,
            diploma.livro,
            diploma.folha,
            diploma.turma,
            diploma.metodoEntrega?.name,
            diploma.dataEntrega,
            diploma.temPdf(),
            linksDe(diploma, podeRegistrar),
        )
    }

    private fun elegivel(item: ElegibilidadeColacao): ElegivelColacaoResponse =
        ElegivelColacaoResponse(
            item.alunoId,
            item.nome,
            item.grr,
            item.elegivel,
            item.bloqueio?.let { ElegivelColacaoResponse.BloqueioResponse(it.razao, it.detalhe) },
        )

    private fun linksDe(diploma: Diploma, podeRegistrar: Boolean): Map<String, String> {
        val links = linkedMapOf<String, String>()
        links["self"] = "/diplomas/${diploma.id}"
        links["view"] = "/diplomas/${diploma.id}"
        if (podeRegistrar && diploma.situacao == DiplomaSituacao.PENDENTE) {
            links["confirm-delivery"] = "/diplomas/${diploma.id}/confirm-delivery"
        }
        if (podeRegistrar) {
            links["upload-pdf"] = "/diplomas/${diploma.id}/pdf"
        }
        return links
    }
}
