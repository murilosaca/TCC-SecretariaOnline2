package br.ufpr.sept.so2.modules.bff.application

import br.ufpr.sept.so2.modules.academico.application.ports.CursoEscopoPort
import br.ufpr.sept.so2.modules.bff.api.dto.SecretaryDashboardResponse
import br.ufpr.sept.so2.modules.bff.api.dto.SecretaryDashboardResponse.AgendaItemResponse
import br.ufpr.sept.so2.modules.bff.api.dto.SecretaryDashboardResponse.FilaItemResponse
import br.ufpr.sept.so2.modules.bff.api.dto.SecretaryDashboardResponse.KpisResponse
import br.ufpr.sept.so2.modules.bff.api.dto.SecretaryDashboardResponse.PeriodoVigenteResponse
import br.ufpr.sept.so2.modules.bff.api.dto.SecretaryDashboardResponse.SaudacaoResponse
import br.ufpr.sept.so2.modules.bff.application.ports.AgendaDiaQueryPort
import br.ufpr.sept.so2.modules.bff.application.ports.AgendaDiaQueryPort.AgendaDia
import br.ufpr.sept.so2.modules.bff.application.ports.PeriodoVigenteQueryPort
import br.ufpr.sept.so2.modules.bff.application.ports.PeriodoVigenteQueryPort.PeriodoVigenteResumo
import br.ufpr.sept.so2.modules.bff.application.ports.ProfessorIdentidadeQueryPort
import br.ufpr.sept.so2.modules.bff.application.ports.SecretariaFilaDashboardQueryPort
import br.ufpr.sept.so2.modules.bff.application.ports.SecretariaFilaDashboardQueryPort.FilaSecretaria
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
class SecretaryDashboardApplicationService(
    private val cursoEscopoPort: CursoEscopoPort,
    private val identidadeQueryPort: ProfessorIdentidadeQueryPort,
    private val periodoVigenteQueryPort: PeriodoVigenteQueryPort,
    private val filaQueryPort: SecretariaFilaDashboardQueryPort,
    private val agendaDiaQueryPort: AgendaDiaQueryPort,
    private val executor: Executor,
) {

    @Autowired
    constructor(
        cursoEscopoPort: CursoEscopoPort,
        identidadeQueryPort: ProfessorIdentidadeQueryPort,
        periodoVigenteQueryPort: PeriodoVigenteQueryPort,
        filaQueryPort: SecretariaFilaDashboardQueryPort,
        agendaDiaQueryPort: AgendaDiaQueryPort,
    ) : this(
        cursoEscopoPort,
        identidadeQueryPort,
        periodoVigenteQueryPort,
        filaQueryPort,
        agendaDiaQueryPort,
        ForkJoinPool.commonPool(),
    )

    fun execute(usuarioId: UUID, authorities: Collection<String>?): SecretaryDashboardResponse {
        SecretariaDashboardRegras.exigirCursos(cursoEscopoPort.cursoIdsDoUsuario(usuarioId))
        val agora = OffsetDateTime.now()
        val identidadeF = isoladoAsync("identidade") { identidadeQueryPort.consultar(usuarioId) }
        val periodoF = isoladoAsync("periodo") { carregarPeriodo() }
        val filaF = isoladoAsync("fila") { filaQueryPort.consultar(usuarioId, agora) }
        val agendaF = isoladoAsync("agenda") { agendaDiaQueryPort.consultar(agora) }
        return montar(
            authorities,
            agora,
            identidadeF.join(),
            periodoF.join(),
            filaF.join(),
            agendaF.join(),
        )
    }

    private fun montar(
        authorities: Collection<String>?,
        agora: OffsetDateTime,
        identidade: ProfessorIdentidadeQueryPort.ProfessorIdentidade?,
        periodo: PeriodoBloco?,
        fila: FilaSecretaria?,
        agenda: AgendaDia?,
    ): SecretaryDashboardResponse {
        val saudacao = SaudacaoResponse(identidade?.nome ?: "Secretaria")
        val kpis = KpisResponse(
            fila?.abertas,
            fila?.atrasadas,
            fila?.concluidasHoje,
            agenda?.total,
        )
        return SecretaryDashboardResponse(
            saudacao,
            periodo?.vigente,
            periodo?.alertaAusente,
            kpis,
            fila?.atrasadas,
            fila?.itens?.map { item ->
                FilaItemResponse(
                    item.id,
                    item.protocolo,
                    item.tipoNome,
                    item.estado,
                    item.prazoEm,
                    SecretariaDashboardRegras.slaStatus(item.prazoEm, agora),
                    item.href,
                )
            },
            agenda?.itens?.map { item ->
                AgendaItemResponse(item.id, item.titulo, item.inicioEm, item.fimEm, item.estado)
            },
            links(authorities),
        )
    }

    private fun carregarPeriodo(): PeriodoBloco {
        val resumo = periodoVigenteQueryPort.consultar(LocalDate.now())
        return if (resumo == null) {
            PeriodoBloco(null, true)
        } else {
            PeriodoBloco(toPeriodo(resumo), false)
        }
    }

    private fun <T> isoladoAsync(bloco: String, consulta: () -> T): CompletableFuture<T?> =
        CompletableFuture.supplyAsync({ isolado(bloco, consulta) }, executor)

    private fun <T> isolado(bloco: String, consulta: () -> T): T? =
        try {
            consulta()
        } catch (_: CompletionException) {
            LOG.warn("BFF dashboard secretaria: bloco {} indisponível", bloco)
            null
        } catch (_: RuntimeException) {
            LOG.warn("BFF dashboard secretaria: bloco {} indisponível", bloco)
            null
        }

    private data class PeriodoBloco(
        val vigente: PeriodoVigenteResponse?,
        val alertaAusente: Boolean,
    )

    companion object {
        private val LOG = LoggerFactory.getLogger(SecretaryDashboardApplicationService::class.java)

        private fun toPeriodo(resumo: PeriodoVigenteResumo): PeriodoVigenteResponse =
            PeriodoVigenteResponse(
                resumo.id,
                resumo.ano,
                resumo.semestre,
                resumo.rotulo(),
                resumo.inicio,
                resumo.fim,
            )

        private fun links(authorities: Collection<String>?): Map<String, String> {
            val caps = authorities?.toSet().orEmpty()
            val result = linkedMapOf("self" to "/bff/dashboard/secretary")
            if ("course.manage" in caps) {
                result["cursos"] = "/secretaria/cursos"
            }
            if ("subject.manage" in caps) {
                result["disciplinas"] = "/secretaria/disciplinas"
            }
            if ("user.manage_students" in caps) {
                result["alunos"] = "/secretaria/alunos"
            }
            if ("calendar.manage" in caps) {
                result["calendarios"] = "/secretaria/calendarios"
            }
            if ("request.view_curso" in caps) {
                result["fila-solicitacoes"] = "/solicitacoes"
                result["atrasados"] = "/secretaria/atrasados"
            }
            if ("diploma.register" in caps) {
                result["diplomas"] = "/secretaria/diplomas"
            }
            if ("report.view_secretary" in caps) {
                result["estatisticas"] = "/secretaria/estatisticas"
            }
            return result
        }
    }
}
