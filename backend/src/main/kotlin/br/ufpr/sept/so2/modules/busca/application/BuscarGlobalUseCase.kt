package br.ufpr.sept.so2.modules.busca.application

import br.ufpr.sept.so2.modules.academico.application.ports.CursoEscopoPort
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class BuscarGlobalUseCase(
    private val consulta: BuscaConsultaPort,
    private val cursoEscopo: CursoEscopoPort,
    private val identidade: IdentidadeBuscaPort,
    private val solicitantesNoEscopo: SolicitantesNoEscopoPort,
) {
    fun execute(atorId: UUID, authorities: Collection<String>, q: String): ResultadoBusca {
        val termo = termoDe(q)
        if (termo == null) {
            return vazio(q.trim())
        }
        val caps = authorities.toSet()
        return ResultadoBusca(
            q.trim(),
            alunos(atorId, caps, termo),
            solicitacoes(atorId, caps, termo),
            eventos(atorId, caps, termo),
            usuarios(caps, termo),
        )
    }

    private fun alunos(atorId: UUID, caps: Set<String>, termo: String): List<BuscaHit> {
        if ("user.manage_students" in caps) {
            val cursos = cursoEscopo.cursoIdsDoUsuario(atorId)
            if (cursos.isEmpty()) {
                return emptyList()
            }
            return consulta.alunosPorCursos(termo, cursos).map { hit(it, "/secretaria/alunos") }
        }
        if ("request.view_own" !in caps) {
            return emptyList()
        }
        val quem = identidade.de(atorId) ?: return emptyList()
        return consulta.alunoProprio(termo, quem.grr, quem.email).map { hit(it, "/perfil") }
    }

    private fun solicitacoes(atorId: UUID, caps: Set<String>, termo: String): List<BuscaHit> {
        val ids = when {
            "request.view_curso" in caps -> solicitantesNoEscopo.ids(atorId)
            "request.view_own" in caps -> setOf(atorId)
            else -> return emptyList()
        }
        if (ids.isEmpty()) {
            return emptyList()
        }
        return consulta.solicitacoes(termo, ids).map { hit(it, "/solicitacoes/${it.id}") }
    }

    private fun eventos(atorId: UUID, caps: Set<String>, termo: String): List<BuscaHit> {
        if ("event.view_curso" in caps) {
            val cursos = cursoEscopo.cursoIdsDoUsuario(atorId)
            if (cursos.isEmpty()) {
                return emptyList()
            }
            return consulta.eventosPorCursos(termo, cursos).map { hit(it, "/secretaria/eventos/${it.id}") }
        }
        if ("event.manage" in caps || "event.host" in caps) {
            return consulta.eventosDoAnfitriao(termo, atorId).map { hit(it, "/professor/eventos/${it.id}") }
        }
        if ("attendance.view_open" !in caps) {
            return emptyList()
        }
        return consulta.eventosAbertos(termo).map { hit(it, "/eventos/${it.id}/presenca") }
    }

    private fun usuarios(caps: Set<String>, termo: String): List<BuscaHit> {
        if ("user.manage_all" !in caps) {
            return emptyList()
        }
        return consulta.usuarios(termo).map { hit(it, "/admin/usuarios") }
    }

    private fun hit(registro: BuscaRegistro, href: String) =
        BuscaHit(registro.id, registro.titulo, registro.subtitulo, href)

    private fun termoDe(q: String): String? {
        val termo = q.trim().lowercase().replace("%", "").replace("_", "")
        if (termo.length < 2) {
            return null
        }
        return termo.take(80)
    }

    private fun vazio(q: String) = ResultadoBusca(q, emptyList(), emptyList(), emptyList(), emptyList())
}
