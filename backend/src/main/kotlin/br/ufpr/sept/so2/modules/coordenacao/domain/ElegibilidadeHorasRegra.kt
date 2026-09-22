package br.ufpr.sept.so2.modules.coordenacao.domain

/**
 * RN-F6-001-07: subir o limiar não rebaixa quem já era elegível.
 * Linha congelada permanece. Quem não tem linha é medido pelo limiar atual.
 */
object ElegibilidadeHorasRegra {
    fun deveCongelar(horasValidadas: Int, limiarAnterior: Int, jaElegivel: Boolean): Boolean =
        !jaElegivel && horasValidadas >= limiarAnterior

    fun requeridas(horasValidadas: Int, limiarAtual: Int, limiarCongelado: Int?): Int =
        if (limiarCongelado != null && horasValidadas < limiarAtual) limiarCongelado else limiarAtual

    fun elegivel(horasValidadas: Int, limiarAtual: Int, limiarCongelado: Int?): Boolean =
        limiarCongelado != null || horasValidadas >= limiarAtual
}
