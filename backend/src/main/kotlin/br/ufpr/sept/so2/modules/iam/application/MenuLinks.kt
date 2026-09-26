package br.ufpr.sept.so2.modules.iam.application

import java.util.UUID

/**
 * Único ponto que olha authorities para montar o menu (rotas de UI, não de API).
 * Rel em kebab. Omite o item se a capability não existir.
 * `configurar-curso` só entra com `course.config` e exatamente um curso coordenado.
 */
object MenuLinks {
    fun from(authorities: Collection<String>, cursoConfigurarId: UUID? = null): Map<String, String> {
        val caps = authorities.toSet()
        val links = linkedMapOf<String, String>()
        if (CapsSessao.egressoPuro(caps)) {
            links["egresso-inicio"] = "/egresso/inicio"
        } else {
            links["inicio"] = "/inicio"
        }
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
        if (caps.contains("internship.view_own")) {
            links["estagios"] = "/estagios"
        }
        if (caps.contains("internship.review")) {
            links["estagios-revisao"] = "/estagios?to=me"
            links["comissoes-coe"] = "/comissoes/coe"
        }
        if (caps.contains("tcc.view_own")) {
            links["tccs"] = "/tccs"
        }
        if (caps.contains("tcc.review")) {
            links["tccs-revisao"] = "/tccs?to=me"
        }
        if (caps.contains("formative.review")) {
            links["revisao-caaf"] = "/formativas?to=me"
            links["comissoes-caaf"] = "/comissoes/caaf"
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
        if (caps.contains("internship.manage")) {
            links["estagios-secretaria"] = "/secretaria/estagios"
        }
        if (caps.contains("tcc.manage")) {
            links["tccs-secretaria"] = "/secretaria/tccs"
        }
        if (caps.contains("diploma.register")) {
            links["diplomas"] = "/secretaria/diplomas"
        }
        if (caps.contains("user.manage_all")) {
            links["usuarios"] = "/admin/usuarios"
        }
        if (caps.contains("course.config") && cursoConfigurarId != null) {
            links["configurar-curso"] = "/coordenacao/cursos/$cursoConfigurarId/configurar"
        }
        links["contato"] = "/contato"
        return links
    }

    private fun Set<String>.containsAny(vararg values: String): Boolean = values.any { contains(it) }
}
