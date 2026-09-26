package br.ufpr.sept.so2.modules.reports.application.ports

import br.ufpr.sept.so2.modules.reports.domain.RelatorioCoordenador
import br.ufpr.sept.so2.modules.reports.domain.RelatorioCoordenadorRegras
import br.ufpr.sept.so2.modules.reports.domain.RelatorioCoordenadorRegras.AnoSemestre
import java.time.LocalDate
import java.util.UUID

interface RelatorioCursoEscopoPort {
    fun idsCoordenados(usuarioId: UUID): Set<UUID>

    fun idsSecretariados(usuarioId: UUID): Set<UUID>

    fun resolverCurso(cursoId: UUID): CursoEscopo?

    fun localizarPorSigla(sigla: String, entre: Set<UUID>): CursoEscopo?

    fun listarCursos(ids: Set<UUID>): List<CursoEscopo>

    data class CursoEscopo(
        val id: UUID,
        val sigla: String,
        val nome: String,
        val idCoordenador: UUID?,
    )
}

interface RelatorioCoordenadorQueryPort {
    fun agregar(
        cursoId: UUID,
        periodo: PeriodoFiltro?,
    ): RelatorioCoordenador

    fun listarPeriodos(): List<PeriodoOpcao>

    data class PeriodoFiltro(
        val id: UUID,
        val ano: Int,
        val semestre: Int,
        val inicio: LocalDate,
        val fim: LocalDate,
    ) {
        val codigo: String get() = RelatorioCoordenadorRegras.codigo(ano, semestre)
        val rotulo: String get() = RelatorioCoordenadorRegras.rotulo(ano, semestre)
    }

    data class PeriodoOpcao(
        val id: UUID,
        val ano: Int,
        val semestre: Int,
        val inicio: LocalDate,
        val fim: LocalDate,
    ) {
        fun paraFiltro() = PeriodoFiltro(id, ano, semestre, inicio, fim)

        fun bate(anoSemestre: AnoSemestre): Boolean =
            ano == anoSemestre.ano && semestre == anoSemestre.semestre
    }
}
