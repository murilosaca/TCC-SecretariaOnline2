package br.ufpr.sept.so2.modules.iam.application

/**
 * Única definição de “aluno ativo” para o portal do egresso (RF-F2-001).
 * Egresso puro tem `alumni.view_own` e nenhuma destas capabilities.
 */
object CapsSessao {
    const val ALUMNI_VIEW_OWN: String = "alumni.view_own"

    fun alunoAtivo(authorities: Collection<String>): Boolean =
        authorities.any { cap ->
            cap == "request.open" ||
                cap == "tcc.view_own" ||
                cap.startsWith("attendance.") ||
                cap.startsWith("formative.") ||
                cap.startsWith("internship.")
        }

    fun egressoPuro(authorities: Collection<String>): Boolean =
        authorities.contains(ALUMNI_VIEW_OWN) && !alunoAtivo(authorities)
}
