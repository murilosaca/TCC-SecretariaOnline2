package br.ufpr.sept.so2.modules.reports.api

import br.ufpr.sept.so2.modules.reports.application.RelatorioCoordenadorAcesso
import br.ufpr.sept.so2.modules.reports.domain.RelatorioCoordenador

object RelatorioCoordenadorAssembler {
    fun links(
        relatorio: RelatorioCoordenador,
        authorities: Collection<String>,
        selfQuery: String,
    ): Map<String, String> {
        val caps = authorities.toSet()
        val links = linkedMapOf<String, String>()
        links["self"] = "/reports/coordinator$selfQuery"
        if ("course.config" in caps) {
            links["configurar-curso"] = "/coordenacao/cursos/${relatorio.cursoId}/configurar"
        }
        if ("request.deliberate" in caps || "request.view_curso" in caps) {
            links["deliberar"] = "/solicitacoes?to=me"
        }
        if ("formative.review" in caps) {
            links["comissoes-caaf"] = "/comissoes/caaf"
        }
        if ("tcc.review" in caps) {
            links["tccs-revisao"] = "/tccs?to=me"
        }
        if ("tcc.manage" in caps) {
            links["tccs-secretaria"] = "/secretaria/tccs"
        }
        if ("request_type.manage" in caps) {
            links["tipos-solicitacao"] = "/admin/tipos-solicitacao"
        }
        if (RelatorioCoordenadorAcesso.CAP in caps) {
            links["relatorios"] = "/coordenacao/relatorios"
        }
        return links
    }

    fun selfQuery(periodo: String?, curso: String?): String {
        val parts = mutableListOf<String>()
        if (!periodo.isNullOrBlank()) {
            parts += "periodo=${periodo.trim()}"
        }
        if (!curso.isNullOrBlank()) {
            parts += "curso=${curso.trim()}"
        }
        return if (parts.isEmpty()) "" else "?${parts.joinToString("&")}"
    }
}
