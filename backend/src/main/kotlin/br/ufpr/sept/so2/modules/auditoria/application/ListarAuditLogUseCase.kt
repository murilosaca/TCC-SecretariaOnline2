package br.ufpr.sept.so2.modules.auditoria.application

import br.ufpr.sept.so2.modules.auditoria.application.ports.AuditLogConsultaPort
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Service
class ListarAuditLogUseCase(
    private val consulta: AuditLogConsultaPort,
) {
    @Transactional(readOnly = true)
    fun execute(ator: String?, acao: String?, de: LocalDate?, ate: LocalDate?, pageable: Pageable): AuditLogPagina {
        val hoje = LocalDate.now(ZoneOffset.UTC)
        var fim = ate ?: hoje
        var inicio = de ?: fim.minusYears(1)
        if (inicio.isAfter(fim)) {
            throw DadoInvalidoException("A data inicial não pode ser posterior à data final.")
        }
        val piso = fim.minusYears(JANELA_MAXIMA_ANOS)
        if (inicio.isBefore(piso)) {
            inicio = piso
        }
        val desde = inicio.atStartOfDay().atOffset(ZoneOffset.UTC)
        val ateExclusivo = fim.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC)
        val tamanho = pageable.pageSize.coerceIn(1, TAMANHO_PAGINA)
        val numero = pageable.pageNumber.coerceAtLeast(0)
        val total = consulta.contar(ator, acao, desde, ateExclusivo)
        val itens = consulta.listar(ator, acao, desde, ateExclusivo, numero * tamanho, tamanho)
        return AuditLogPagina(itens, numero, tamanho, total)
    }

    companion object {
        const val TAMANHO_PAGINA: Int = 50
        private const val JANELA_MAXIMA_ANOS: Long = 5
    }
}
