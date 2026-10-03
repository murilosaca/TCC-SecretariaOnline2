package br.ufpr.sept.so2.modules.busca.application

import java.util.UUID

data class BuscaRegistro(val id: UUID, val titulo: String, val subtitulo: String)

data class BuscaHit(val id: UUID, val titulo: String, val subtitulo: String, val href: String)

data class IdentidadeBusca(val grr: String?, val email: String)

data class ResultadoBusca(
    val q: String,
    val alunos: List<BuscaHit>,
    val solicitacoes: List<BuscaHit>,
    val eventos: List<BuscaHit>,
    val usuarios: List<BuscaHit>,
)

interface BuscaConsultaPort {
    fun alunosPorCursos(termo: String, cursos: Set<UUID>): List<BuscaRegistro>

    fun alunoProprio(termo: String, grr: String?, email: String): List<BuscaRegistro>

    fun solicitacoes(termo: String, solicitantes: Set<UUID>): List<BuscaRegistro>

    fun eventosPorCursos(termo: String, cursos: Set<UUID>): List<BuscaRegistro>

    fun eventosDoAnfitriao(termo: String, anfitriaoId: UUID): List<BuscaRegistro>

    fun eventosAbertos(termo: String): List<BuscaRegistro>

    fun usuarios(termo: String): List<BuscaRegistro>
}

fun interface IdentidadeBuscaPort {
    fun de(atorId: UUID): IdentidadeBusca?
}

fun interface SolicitantesNoEscopoPort {
    fun ids(atorId: UUID): Set<UUID>
}
