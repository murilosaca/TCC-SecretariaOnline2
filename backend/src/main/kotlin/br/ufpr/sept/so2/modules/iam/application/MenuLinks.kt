package br.ufpr.sept.so2.modules.iam.application

/**
 * Único ponto que olha authorities para montar o menu (rotas de UI, não de API).
 * Rel em kebab. Omite o item se a capability não existir.
 */
object MenuLinks {
    fun from(authorities: Collection<String>): Map<String, String> {
        val caps = authorities.toSet()
        val links = linkedMapOf<String, String>()
        links["inicio"] = "/inicio"
        if (caps.contains("request.view_own")) {
            links["solicitacoes"] = "/solicitacoes"
        }
        if (caps.containsAny("request.deliberate", "request.view_curso")) {
            links["deliberar"] = "/solicitacoes?to=me"
        }
        if (caps.contains("attendance.view_open")) {
            links["eventos"] = "/eventos"
        }
        if (caps.contains("formative.view_own")) {
            links["formativas"] = "/formativas"
        }
        if (caps.contains("certificate.view_own")) {
            links["certificados"] = "/certificados"
        }
        if (caps.contains("formative.review")) {
            links["revisao-caaf"] = "/formativas?to=me"
        }
        if (caps.containsAny("event.manage", "event.host")) {
            links["eventos-professor"] = "/professor/eventos"
        }
        if (caps.contains("course.manage")) {
            links["cursos"] = "/secretaria/cursos"
        }
        if (caps.contains("subject.manage")) {
            links["disciplinas"] = "/secretaria/disciplinas"
        }
        if (caps.contains("user.manage_students")) {
            links["alunos"] = "/secretaria/alunos"
        }
        if (caps.contains("calendar.manage")) {
            links["calendarios"] = "/secretaria/calendarios"
        }
        links["contato"] = "/contato"
        return links
    }

    private fun Set<String>.containsAny(vararg values: String): Boolean = values.any { contains(it) }
}
