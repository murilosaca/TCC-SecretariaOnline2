package br.ufpr.sept.so2.modules.solicitacoes.application

import br.ufpr.sept.so2.modules.academico.application.ports.AlunoRepository
import br.ufpr.sept.so2.modules.academico.application.ports.CursoEscopoPort
import br.ufpr.sept.so2.modules.iam.application.ports.AuditLogPort
import br.ufpr.sept.so2.modules.iam.application.ports.OutboxPort
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.modules.iam.domain.IdentificadorLogin
import br.ufpr.sept.so2.modules.solicitacoes.application.ports.ProtocoloSequenciaPort
import br.ufpr.sept.so2.modules.solicitacoes.application.ports.SolicitacaoRepository
import br.ufpr.sept.so2.modules.solicitacoes.application.ports.TipoSolicitacaoRepository
import br.ufpr.sept.so2.modules.solicitacoes.domain.Solicitacao
import br.ufpr.sept.so2.shared.domain.exception.AcessoNegadoException
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import br.ufpr.sept.so2.shared.infrastructure.Uuids
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.UUID

@Service
class CriarSolicitacaoUseCase(
    private val tipoRepository: TipoSolicitacaoRepository,
    private val solicitacaoRepository: SolicitacaoRepository,
    private val protocoloSequenciaPort: ProtocoloSequenciaPort,
    private val formSchemaValidator: FormSchemaValidator,
    private val workflowJsonParser: WorkflowJsonParser,
    private val outboxPort: OutboxPort,
    private val auditLogPort: AuditLogPort,
    private val objectMapper: ObjectMapper,
    private val alunoRepository: AlunoRepository,
    private val cursoEscopoPort: CursoEscopoPort,
    private val usuarioRepository: UsuarioRepository,
) {

    @Transactional
    fun execute(
        atorId: UUID,
        tipoCodigo: String,
        payload: Map<String, Any?>?,
        ip: String?,
        onBehalfOf: UUID? = null,
    ): Solicitacao {
        val titularId = titularDe(atorId, onBehalfOf)
        val tipo = tipoRepository.findByCodigo(tipoCodigo)
            .orElseThrow { RecursoNaoEncontradoException("Tipo de solicitação não encontrado.") }
        if (!tipo.isPublished()) {
            throw RecursoNaoEncontradoException("Tipo de solicitação não encontrado.")
        }
        formSchemaValidator.validar(tipo.formSchema, payload)
        val workflow = workflowJsonParser.parse(tipo.workflowJson)
        val agora = OffsetDateTime.now()
        val ano = agora.atZoneSameInstant(FUSO_SEPT).year
        val protocolo = protocoloSequenciaPort.proximo(ano)
        val solicitacao = Solicitacao.abrir(
            Uuids.v7(),
            Uuids.v7(),
            tipo,
            titularId,
            protocolo,
            toJson(payload),
            workflow,
            agora,
        )
        val persistida = solicitacaoRepository.save(solicitacao)
        val evento = toJson(
            mapOf(
                "solicitacaoId" to persistida.id.toString(),
                "protocolo" to persistida.protocolo.valor,
                "tipoCodigo" to persistida.tipoCodigo,
                "solicitanteId" to titularId.toString(),
                "abertoPor" to atorId.toString(),
                "onBehalfOf" to onBehalfOf?.toString(),
            ),
        )
        val tipoEvento = if (onBehalfOf == null) "solicitacao.criada" else "solicitacao.aberta_interna"
        outboxPort.enqueue(tipoEvento, evento)
        auditLogPort.append(tipoEvento, atorId, evento, ip)
        return persistida
    }

    private fun titularDe(atorId: UUID, onBehalfOf: UUID?): UUID {
        if (onBehalfOf == null) {
            return atorId
        }
        val aluno = alunoRepository.findById(onBehalfOf).orElse(null)
            ?: throw AcessoNegadoException(FORA_DO_ESCOPO)
        val cursos = cursoEscopoPort.cursoIdsDoUsuario(atorId)
        if (aluno.idCurso !in cursos) {
            throw AcessoNegadoException(FORA_DO_ESCOPO)
        }
        val porGrr = IdentificadorLogin.tryParse(aluno.grr.value)
            ?.let { usuarioRepository.findByIdentificador(it) }
            ?.orElse(null)
        val usuario = porGrr ?: usuarioRepository.findByEmail(aluno.emailInstitucional.value).orElse(null)
            ?: throw DadoInvalidoException("O aluno não possui conta para ser titular da solicitação.")
        return usuario.id
    }

    private fun toJson(valor: Any?): String {
        try {
            return objectMapper.writeValueAsString(valor)
        } catch (_: JsonProcessingException) {
            throw DadoInvalidoException("Não foi possível serializar o formulário.")
        }
    }

    companion object {
        private val FUSO_SEPT: ZoneId = ZoneId.of("America/Sao_Paulo")
        private const val FORA_DO_ESCOPO =
            "O aluno selecionado não pertence aos cursos vinculados à sua conta."
    }
}
