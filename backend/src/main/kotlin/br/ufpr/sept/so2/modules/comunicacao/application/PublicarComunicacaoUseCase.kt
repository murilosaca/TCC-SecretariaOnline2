package br.ufpr.sept.so2.modules.comunicacao.application

import br.ufpr.sept.so2.modules.comunicacao.application.ports.AudienciaPort
import br.ufpr.sept.so2.modules.comunicacao.application.ports.ComunicacaoRepository
import br.ufpr.sept.so2.modules.comunicacao.domain.AudienciaOpcao
import br.ufpr.sept.so2.modules.comunicacao.domain.Comunicacao
import br.ufpr.sept.so2.modules.comunicacao.domain.ComunicadoRegras
import br.ufpr.sept.so2.modules.comunicacao.domain.PrioridadeComunicacao
import br.ufpr.sept.so2.modules.comunicacao.domain.TipoAudiencia
import br.ufpr.sept.so2.modules.iam.application.ports.OutboxPort
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import br.ufpr.sept.so2.shared.infrastructure.Uuids
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.UUID

data class PublicarComunicacaoComando(
    val titulo: String?,
    val corpo: String?,
    val audienciaTipo: String?,
    val audienciaId: UUID?,
    val prioridade: String?,
    val expiraEm: OffsetDateTime?,
)

@Service
class PublicarComunicacaoUseCase(
    private val comunicacaoRepository: ComunicacaoRepository,
    private val audienciaPort: AudienciaPort,
    private val outboxPort: OutboxPort,
    private val objectMapper: ObjectMapper,
) {
    @Transactional
    fun execute(autorId: UUID, comando: PublicarComunicacaoComando, agora: OffsetDateTime = OffsetDateTime.now()): Comunicacao {
        val titulo = ComunicadoRegras.titulo(comando.titulo)
        val corpo = ComunicadoRegras.corpo(comando.corpo)
        val prioridade = PrioridadeComunicacao.parse(comando.prioridade)
        val expiraEm = ComunicadoRegras.expiracao(comando.expiraEm, agora)
        val audienciaId = comando.audienciaId ?: throw DadoInvalidoException(TipoAudiencia.FORA_DE_ESCOPO)
        val audienciaTipo = TipoAudiencia.parse(comando.audienciaTipo)
        if (!audienciaPort.professorPode(autorId, audienciaTipo, audienciaId)) {
            throw DadoInvalidoException(TipoAudiencia.FORA_DE_ESCOPO)
        }
        val salva = comunicacaoRepository.save(
            Comunicacao(
                Uuids.v7(),
                audienciaTipo.tipoComunicacao(),
                titulo,
                corpo,
                prioridade,
                autorId,
                audienciaTipo,
                audienciaId,
                expiraEm,
                agora,
            ),
        )
        outboxPort.enqueue(TIPO_OUTBOX, objectMapper.writeValueAsString(mapOf("comunicacaoId" to salva.id.toString())))
        return salva
    }

    companion object {
        const val TIPO_OUTBOX = "comunicacao.published"
    }
}

@Service
class ListarAudienciasUseCase(
    private val audienciaPort: AudienciaPort,
) {
    fun execute(professorId: UUID): List<AudienciaOpcao> = audienciaPort.opcoes(professorId)
}
