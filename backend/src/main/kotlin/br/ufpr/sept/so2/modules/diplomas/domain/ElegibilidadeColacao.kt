package br.ufpr.sept.so2.modules.diplomas.domain

import java.util.UUID

/**
 * Resultado da avaliação RN-F5-005-07 para um aluno no wizard de colação.
 * Critérios implementáveis nesta fatia: TCC aprovado e horas formativas.
 * Currículo, finanças e solicitações bloqueantes ficam passantes enquanto
 * a integração correspondente não existir (mesma regra da spec para finanças).
 */
data class ElegibilidadeColacao(
    val alunoId: UUID,
    val nome: String,
    val grr: String,
    val elegivel: Boolean,
    val bloqueio: Bloqueio?,
) {
    data class Bloqueio(
        val razao: String,
        val detalhe: String?,
    )
}

object AvaliarElegibilidadeColacao {
    fun avaliar(
        alunoId: UUID,
        nome: String,
        grr: String,
        tccAprovado: Boolean,
        horasValidadas: Int,
        horasRequeridas: Int,
    ): ElegibilidadeColacao {
        if (!tccAprovado) {
            return inelegivel(alunoId, nome, grr, "TCC não aprovado", null)
        }
        if (horasValidadas < horasRequeridas) {
            return inelegivel(
                alunoId,
                nome,
                grr,
                "Horas formativas insuficientes: $horasValidadas/$horasRequeridas h",
                "Validadas $horasValidadas h; mínimo $horasRequeridas h",
            )
        }
        return ElegibilidadeColacao(alunoId, nome, grr, true, null)
    }

    private fun inelegivel(
        alunoId: UUID,
        nome: String,
        grr: String,
        razao: String,
        detalhe: String?,
    ): ElegibilidadeColacao =
        ElegibilidadeColacao(
            alunoId,
            nome,
            grr,
            false,
            ElegibilidadeColacao.Bloqueio(razao, detalhe),
        )
}
