package br.ufpr.sept.so2.modules.bff.application

import br.ufpr.sept.so2.modules.bff.api.dto.ProfessorDashboardResponse
import br.ufpr.sept.so2.modules.bff.api.dto.ProfessorDashboardResponse.FilaSolicitacaoResponse
import br.ufpr.sept.so2.modules.bff.api.dto.ProfessorDashboardResponse.ItemFilaResponse
import br.ufpr.sept.so2.modules.bff.api.dto.ProfessorDashboardResponse.KpisResponse
import br.ufpr.sept.so2.modules.bff.api.dto.ProfessorDashboardResponse.MeuEventoResponse
import br.ufpr.sept.so2.modules.bff.api.dto.ProfessorDashboardResponse.SaudacaoResponse
import br.ufpr.sept.so2.modules.bff.application.ports.DeliberacaoDashboardQueryPort
import br.ufpr.sept.so2.modules.bff.application.ports.DeliberacaoDashboardQueryPort.FilaItemResumo
import br.ufpr.sept.so2.modules.bff.application.ports.EstagiosProfessorDashboardQueryPort
import br.ufpr.sept.so2.modules.bff.application.ports.EventosProfessorDashboardQueryPort
import br.ufpr.sept.so2.modules.bff.application.ports.EventosProfessorDashboardQueryPort.MeuEventoResumo
import br.ufpr.sept.so2.modules.bff.application.ports.FormativasCaafDashboardQueryPort
import br.ufpr.sept.so2.modules.bff.application.ports.ProfessorIdentidadeQueryPort
import br.ufpr.sept.so2.modules.bff.application.ports.TccsProfessorDashboardQueryPort
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionException
import java.util.concurrent.Executor
import java.util.concurrent.ForkJoinPool

@Service
class ProfessorDashboardApplicationService(
    private val identidadeQueryPort: ProfessorIdentidadeQueryPort,
    private val deliberacaoDashboardQueryPort: DeliberacaoDashboardQueryPort,
    private val eventosProfessorDashboardQueryPort: EventosProfessorDashboardQueryPort,
    private val formativasCaafDashboardQueryPort: FormativasCaafDashboardQueryPort,
    private val estagiosProfessorDashboardQueryPort: EstagiosProfessorDashboardQueryPort,
    private val tccsProfessorDashboardQueryPort: TccsProfessorDashboardQueryPort,
    private val executor: Executor,
) {

    @Autowired
    constructor(
        identidadeQueryPort: ProfessorIdentidadeQueryPort,
        deliberacaoDashboardQueryPort: DeliberacaoDashboardQueryPort,
        eventosProfessorDashboardQueryPort: EventosProfessorDashboardQueryPort,
        formativasCaafDashboardQueryPort: FormativasCaafDashboardQueryPort,
        estagiosProfessorDashboardQueryPort: EstagiosProfessorDashboardQueryPort,
        tccsProfessorDashboardQueryPort: TccsProfessorDashboardQueryPort,
    ) : this(
        identidadeQueryPort,
        deliberacaoDashboardQueryPort,
        eventosProfessorDashboardQueryPort,
        formativasCaafDashboardQueryPort,
        estagiosProfessorDashboardQueryPort,
        tccsProfessorDashboardQueryPort,
        ForkJoinPool.commonPool(),
    )

    fun execute(usuarioId: UUID, authorities: List<String>?): ProfessorDashboardResponse {
        val caps = authorities?.toSet().orEmpty()
        val temCaaf = caps.contains(AUTHORITY_FORMATIVE_REVIEW)
        val temEstagio = caps.contains(AUTHORITY_INTERNSHIP_REVIEW)
        val temTcc = caps.contains(AUTHORITY_TCC_REVIEW)

        val identidadeF = isoladoAsync("identidade") { identidadeQueryPort.consultar(usuarioId) }
        val deliberacaoF = isoladoAsync("deliberacao") { deliberacaoDashboardQueryPort.consultar() }
        val eventosF = isoladoAsync("eventos") {
            eventosProfessorDashboardQueryPort.consultar(usuarioId, OffsetDateTime.now())
        }
        val formativasF = if (temCaaf) {
            isoladoAsync("formativasCaaf") { formativasCaafDashboardQueryPort.consultar() }
        } else {
            CompletableFuture.completedFuture(null)
        }
        val estagiosF = if (temEstagio) {
            isoladoAsync("estagios") { estagiosProfessorDashboardQueryPort.consultar(usuarioId) }
        } else {
            CompletableFuture.completedFuture(null)
        }
        val tccsF = if (temTcc) {
            isoladoAsync("tccs") { tccsProfessorDashboardQueryPort.consultar(usuarioId) }
        } else {
            CompletableFuture.completedFuture(null)
        }

        val identidade = identidadeF.join()
        val deliberacao = deliberacaoF.join()
        val eventos = eventosF.join()
        val formativas = formativasF.join()
        val estagios = estagiosF.join()
        val tccs = tccsF.join()

        val saudacao = SaudacaoResponse(identidade?.nome ?: "Professor")
        val kpis = KpisResponse(
            deliberacao?.pendentes,
            if (temCaaf) formativas?.total else null,
            eventos?.hoje,
            deliberacao?.slaUrgentes,
        )
        return ProfessorDashboardResponse(
            saudacao,
            kpis,
            deliberacao?.fila?.map(::toFila),
            eventos?.meusEventos?.map(::toEvento),
            if (temCaaf) formativas?.itens?.map(::toItemCaaf) else null,
            if (temEstagio) estagios?.itens?.map(::toItemEstagio) else null,
            if (temTcc) tccs?.itens?.map(::toItemTcc) else null,
            links(caps),
        )
    }

    private fun <T> isoladoAsync(bloco: String, consulta: () -> T): CompletableFuture<T?> =
        CompletableFuture.supplyAsync({ isolado(bloco, consulta) }, executor)

    private fun <T> isolado(bloco: String, consulta: () -> T): T? =
        try {
            consulta()
        } catch (_: CompletionException) {
            LOG.warn("BFF dashboard professor: bloco {} indisponível", bloco)
            null
        } catch (_: RuntimeException) {
            LOG.warn("BFF dashboard professor: bloco {} indisponível", bloco)
            null
        }

    companion object {
        private val LOG = LoggerFactory.getLogger(ProfessorDashboardApplicationService::class.java)
        const val AUTHORITY_DASHBOARD: String = "dashboard.view_self_professor"
        const val AUTHORITY_FORMATIVE_REVIEW: String = "formative.review"
        const val AUTHORITY_INTERNSHIP_REVIEW: String = "internship.review"
        const val AUTHORITY_TCC_REVIEW: String = "tcc.review"
        const val AUTHORITY_REQUEST_DELIBERATE: String = "request.deliberate"
        const val AUTHORITY_EVENT_MANAGE: String = "event.manage"
        const val AUTHORITY_EVENT_HOST: String = "event.host"

        private fun toFila(item: FilaItemResumo): FilaSolicitacaoResponse =
            FilaSolicitacaoResponse(
                item.id,
                item.protocolo,
                item.tipoNome,
                item.estado,
                item.prazoEm,
                item.slaVencido,
                item.urgente,
                item.href,
            )

        private fun toEvento(item: MeuEventoResumo): MeuEventoResponse =
            MeuEventoResponse(
                item.id,
                item.titulo,
                item.inicioEm,
                item.fimEm,
                item.estado,
                item.links,
            )

        private fun toItemCaaf(item: FormativasCaafDashboardQueryPort.ItemResumo): ItemFilaResponse =
            ItemFilaResponse(item.id, item.titulo, item.estado, item.href)

        private fun toItemEstagio(item: EstagiosProfessorDashboardQueryPort.ItemResumo): ItemFilaResponse =
            ItemFilaResponse(item.id, item.titulo, item.estado, item.href)

        private fun toItemTcc(item: TccsProfessorDashboardQueryPort.ItemResumo): ItemFilaResponse =
            ItemFilaResponse(item.id, item.titulo, item.estado, item.href)

        private fun links(caps: Set<String>): Map<String, String> {
            val result = linkedMapOf("self" to "/bff/dashboard/professor")
            if (caps.contains(AUTHORITY_REQUEST_DELIBERATE)) {
                result["deliberar"] = "/solicitacoes?to=me"
            }
            if (caps.contains(AUTHORITY_EVENT_MANAGE) || caps.contains(AUTHORITY_EVENT_HOST)) {
                result["eventos"] = "/professor/eventos"
            }
            if (caps.contains(AUTHORITY_FORMATIVE_REVIEW)) {
                result["formativasCaaf"] = "/formativas?to=me"
            }
            if (caps.contains(AUTHORITY_INTERNSHIP_REVIEW)) {
                result["estagios"] = "/estagios?to=me"
            }
            if (caps.contains(AUTHORITY_TCC_REVIEW)) {
                result["tccs"] = "/tccs?to=me"
            }
            return result
        }
    }
}
