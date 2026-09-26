package br.ufpr.sept.so2.modules.reports.domain

/**
 * Regras de filtro e rótulos do relatório F6.2.
 */
object RelatorioCoordenadorRegras {
    private val PERIODO = Regex("^(\\d{4})-([12])$")

    fun parsePeriodo(codigo: String?): AnoSemestre? {
        if (codigo.isNullOrBlank()) {
            return null
        }
        val m = PERIODO.matchEntire(codigo.trim())
            ?: throw IllegalArgumentException("Período inválido. Use o formato AAAA-S (ex.: 2026-2).")
        return AnoSemestre(m.groupValues[1].toInt(), m.groupValues[2].toInt())
    }

    fun rotulo(ano: Int, semestre: Int): String = "$ano/$semestre"

    fun codigo(ano: Int, semestre: Int): String = "$ano-$semestre"

    data class AnoSemestre(val ano: Int, val semestre: Int)
}
