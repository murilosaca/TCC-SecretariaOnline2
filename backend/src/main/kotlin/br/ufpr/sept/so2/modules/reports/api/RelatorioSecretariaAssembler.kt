package br.ufpr.sept.so2.modules.reports.api

import br.ufpr.sept.so2.modules.reports.application.RelatorioSecretariaAcesso

object RelatorioSecretariaAssembler {
    fun links(
        authorities: Collection<String>,
        selfQuery: String,
    ): Map<String, String> {
        val caps = authorities.toSet()
        val links = linkedMapOf<String, String>()
        links["self"] = "/reports/secretary$selfQuery"
        if (RelatorioSecretariaAcesso.CAP in caps) {
            links["estatisticas"] = "/secretaria/estatisticas"
        }
        if ("request.view_curso" in caps || "request.deliberate" in caps) {
            links["deliberar"] = "/solicitacoes?to=me"
        }
        if ("course.manage" in caps) {
            links["cursos"] = "/secretaria/cursos"
        }
        if ("user.manage_students" in caps) {
            links["alunos"] = "/secretaria/alunos"
        }
        if ("calendar.manage" in caps) {
            links["calendarios"] = "/secretaria/calendarios"
        }
        if ("diploma.register" in caps) {
            links["diplomas"] = "/secretaria/diplomas"
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
