package br.ufpr.sept.so2.modules.bff.application

import br.ufpr.sept.so2.modules.bff.api.dto.AlunoDashboardResponse
import br.ufpr.sept.so2.modules.bff.api.dto.AlunoDashboardResponse.KpisResponse
import br.ufpr.sept.so2.modules.bff.api.dto.AlunoDashboardResponse.PendenciaResponse
import br.ufpr.sept.so2.modules.bff.api.dto.AlunoDashboardResponse.PeriodoVigenteResponse
import br.ufpr.sept.so2.modules.bff.api.dto.AlunoDashboardResponse.ProximoEventoResponse
import br.ufpr.sept.so2.modules.bff.api.dto.AlunoDashboardResponse.SaudacaoResponse
import br.ufpr.sept.so2.modules.bff.api.dto.AlunoDashboardResponse.UltimaSolicitacaoResponse
import br.ufpr.sept.so2.modules.bff.application.ports.AlunoIdentidadeQueryPort
import br.ufpr.sept.so2.modules.bff.application.ports.AlunoIdentidadeQueryPort.AlunoIdentidade
import br.ufpr.sept.so2.modules.bff.application.ports.EventosDashboardQueryPort
import br.ufpr.sept.so2.modules.bff.application.ports.EventosDashboardQueryPort.EventoResumo
import br.ufpr.sept.so2.modules.bff.application.ports.EventosDashboardQueryPort.EventosDashboard
import br.ufpr.sept.so2.modules.bff.application.ports.FormativasDashboardQueryPort
import br.ufpr.sept.so2.modules.bff.application.ports.PeriodoVigenteQueryPort
import br.ufpr.sept.so2.modules.bff.application.ports.PeriodoVigenteQueryPort.PeriodoVigenteResumo
import br.ufpr.sept.so2.modules.bff.application.ports.SolicitacoesDashboardQueryPort
import br.ufpr.sept.so2.modules.bff.application.ports.SolicitacoesDashboardQueryPort.SolicitacoesDashboard
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionException
import java.util.concurrent.Executor
import java.util.concurrent.ForkJoinPool

@Service
class AlunoDashboardApplicationService(
    private val identidadeQueryPort: AlunoIdentidadeQueryPort,
    private val periodoVigenteQueryPort: PeriodoVigenteQueryPort,
    private val solicitacoesDashboardQueryPort: SolicitacoesDashboardQueryPort,
    private val eventosDashboardQueryPort: EventosDashboardQueryPort,
    private val formativasDashboardQueryPort: FormativasDashboardQueryPort,
    private val executor: Executor,
) {

    @Autowired
    constructor(
        identidadeQueryPort: AlunoIdentidadeQueryPort,
        periodoVigenteQueryPort: PeriodoVigenteQueryPort,
        solicitacoesDashboardQueryPort: SolicitacoesDashboardQueryPort,
        eventosDashboardQueryPort: EventosDashboardQueryPort,
        formativasDashboardQueryPort: FormativasDashboardQueryPort,
    ) : this(
        identidadeQueryPort,
        periodoVigenteQueryPort,
        solicitacoesDashboardQueryPort,
        eventosDashboardQueryPort,
        formativasDashboardQueryPort,
        ForkJoinPool.commonPool(),
    )

    fun execute(usuarioId: UUID, authorities: List<String>?): AlunoDashboardResponse {
        val identidadeF = isoladoAsync("identidade") { identidadeQueryPort.consultar(usuarioId) }
        val periodoF = isoladoAsync("periodo") { carregarPeriodo() }
        val solicitacoesF = isoladoAsync("solicitacoes") { solicitacoesDashboardQueryPort.consultar(usuarioId) }
        val eventosF = isoladoAsync("eventos") { eventosDashboardQueryPort.consultar(OffsetDateTime.now()) }
        val formativasF = isoladoAsync("formativas") { formativasDashboardQueryPort.consultar(usuarioId) }
        val identidade = identidadeF.join()
        val periodo = periodoF.join()
        val solicitacoes = solicitacoesF.join()
        val eventos = eventosF.join()
        val formativas = formativasF.join()

        val saudacao = if (identidade == null) {
            SaudacaoResponse("Aluno", null)
        } else {
            SaudacaoResponse(identidade.nome, identidade.curso)
        }
        val periodoVigente = periodo?.vigente
        val alertaPeriodo = periodo?.alertaAusente
        val horas = formativas?.let {
            AlunoDashboardResponse.KpiHorasFormativas(it.horasValidadas, it.horasRequeridas)
        }
        val kpis = KpisResponse(
            horas,
            solicitacoes?.abertas,
            eventos?.hoje,
            null,
        )
        val pendencias = solicitacoes?.pendencias?.map { item ->
            PendenciaResponse(item.id, item.titulo, item.estado, item.href)
        }
        val pendenciasFormativas = formativas?.pendentes?.map { item ->
            PendenciaResponse(item.id, item.titulo, item.estado, item.href)
        }
        val ultimas = solicitacoes?.ultimas?.map { item ->
            UltimaSolicitacaoResponse(
                item.id,
                item.protocolo,
                item.tipoNome,
                item.estado,
                item.prazoEm,
                item.slaVencido,
            )
        }
        val proximos = eventos?.proximos?.map(::toEvento)
        return AlunoDashboardResponse(
            saudacao,
            periodoVigente,
            alertaPeriodo,
            kpis,
            pendencias,
            ultimas,
            proximos,
            pendenciasFormativas,
            links(authorities),
        )
    }

    private fun carregarPeriodo(): PeriodoBloco {
        val resumo = periodoVigenteQueryPort.consultar(LocalDate.now())
        return if (resumo == null) {
            PeriodoBloco(null, true)
        } else {
            PeriodoBloco(toResponse(resumo), false)
        }
    }

    private fun <T> isoladoAsync(bloco: String, consulta: () -> T): CompletableFuture<T?> =
        CompletableFuture.supplyAsync({ isolado(bloco, consulta) }, executor)

    private fun <T> isolado(bloco: String, consulta: () -> T): T? =
        try {
            consulta()
        } catch (_: CompletionException) {
            LOG.warn("BFF dashboard: bloco {} indisponível", bloco)
            null
        } catch (_: RuntimeException) {
            LOG.warn("BFF dashboard: bloco {} indisponível", bloco)
            null
        }

    private data class PeriodoBloco(
        val vigente: PeriodoVigenteResponse?,
        val alertaAusente: Boolean,
    )

    companion object {
        private val LOG = LoggerFactory.getLogger(AlunoDashboardApplicationService::class.java)
        const val AUTHORITY_DASHBOARD: String = "dashboard.view_own"
        const val AUTHORITY_REQUEST_OPEN: String = "request.open"

        private fun toResponse(resumo: PeriodoVigenteResumo): PeriodoVigenteResponse =
            PeriodoVigenteResponse(
                resumo.id,
                resumo.ano,
                resumo.semestre,
                resumo.rotulo(),
                resumo.inicio,
                resumo.fim,
            )

        private fun toEvento(item: EventoResumo): ProximoEventoResponse =
            ProximoEventoResponse(
                item.id,
                item.titulo,
                item.inicioEm,
                item.fimEm,
                item.janelaAtiva,
                "/eventos/${item.id}/presenca",
            )

        private fun links(authorities: List<String>?): Map<String, String> {
            val result = linkedMapOf("self" to "/bff/dashboard/aluno")
            if (authorities != null && authorities.contains(AUTHORITY_REQUEST_OPEN)) {
                result["novaSolicitacao"] = "/solicitacoes/nova"
            }
            return result
        }
    }
}
