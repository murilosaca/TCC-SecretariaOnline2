package br.ufpr.sept.so2.modules.academico.domain

import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

class PeriodoLetivo(
    val id: UUID,
    var ano: Int,
    var semestre: Int,
    var inicio: LocalDate,
    var fim: LocalDate,
    @get:JvmName("isAtivo")
    var ativo: Boolean,
    val createdAt: OffsetDateTime,
    var updatedAt: OffsetDateTime,
) {
    init {
        validar(ano, semestre, inicio, fim)
    }

    fun atualizar(ano: Int?, semestre: Int?, inicio: LocalDate?, fim: LocalDate?, ativo: Boolean?) {
        val novoAno = ano ?: this.ano
        val novoSemestre = semestre ?: this.semestre
        val novoInicio = inicio ?: this.inicio
        val novoFim = fim ?: this.fim
        validar(novoAno, novoSemestre, novoInicio, novoFim)
        this.ano = novoAno
        this.semestre = novoSemestre
        this.inicio = novoInicio
        this.fim = novoFim
        if (ativo != null) {
            this.ativo = ativo
        }
        this.updatedAt = OffsetDateTime.now()
    }

    fun sobrepoe(outro: PeriodoLetivo): Boolean =
        inicio.isBefore(outro.fim) && outro.inicio.isBefore(fim)

    fun vigenteEm(data: LocalDate): Boolean =
        ativo && !data.isBefore(inicio) && !data.isAfter(fim)

    companion object {
        private fun validar(ano: Int, semestre: Int, inicio: LocalDate?, fim: LocalDate?) {
            if (ano < 2000 || ano > 2100) {
                throw DadoInvalidoException("Ano letivo deve estar entre 2000 e 2100.")
            }
            if (semestre != 1 && semestre != 2) {
                throw DadoInvalidoException("Semestre deve ser 1 ou 2.")
            }
            if (inicio == null || fim == null) {
                throw DadoInvalidoException("Início e fim do período são obrigatórios.")
            }
            if (!fim.isAfter(inicio)) {
                throw DadoInvalidoException("A data de fim deve ser posterior à de início.")
            }
        }
    }
}
