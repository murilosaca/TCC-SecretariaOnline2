package br.ufpr.sept.so2.modules.comunicacao.infrastructure

import br.ufpr.sept.so2.modules.academico.application.ports.AlunoRepository
import br.ufpr.sept.so2.modules.academico.application.ports.CursoRepository
import br.ufpr.sept.so2.modules.comunicacao.application.ports.ComunicacaoEntregaRepository
import br.ufpr.sept.so2.modules.comunicacao.application.ports.ComunicacaoRepository
import br.ufpr.sept.so2.modules.comunicacao.application.ports.TurmaAudienciaPort
import br.ufpr.sept.so2.modules.comunicacao.application.ports.TurmaGravada
import br.ufpr.sept.so2.modules.comunicacao.domain.Comunicacao
import br.ufpr.sept.so2.modules.comunicacao.domain.ComunicacaoEntrega
import br.ufpr.sept.so2.modules.comunicacao.domain.PrioridadeComunicacao
import br.ufpr.sept.so2.modules.comunicacao.domain.TipoAudiencia
import br.ufpr.sept.so2.modules.comunicacao.domain.TipoComunicacao
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.modules.iam.infrastructure.IamProperties
import br.ufpr.sept.so2.shared.infrastructure.Uuids
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Profile
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.util.UUID

@Component
@Profile("dev")
@Order(90)
class ComunicacaoDevDataLoader(
    private val properties: IamProperties,
    private val usuarioRepository: UsuarioRepository,
    private val cursoRepository: CursoRepository,
    private val alunoRepository: AlunoRepository,
    private val turmaPort: TurmaAudienciaPort,
    private val comunicacaoRepository: ComunicacaoRepository,
    private val entregaRepository: ComunicacaoEntregaRepository,
) : ApplicationRunner {
    override fun run(args: ApplicationArguments) {
        if (!properties.seed.enabled) {
            return
        }
        val professorId = usuarioRepository.findByEmail(EMAIL_PROFESSOR).orElse(null)?.id ?: return
        val curso = cursoRepository.findByCodigo(CODIGO_TADS).orElse(null) ?: return
        val agora = OffsetDateTime.now()
        val turma = turmaPort.findByProfessorECodigo(professorId, CODIGO_TURMA) ?: turmaPort.salvar(
            TurmaGravada(Uuids.v7(), curso.id, professorId, CODIGO_TURMA, "ADS 2026/1"),
        )
        val alunos = listOf(EMAIL_ALUNO, EMAIL_NOVO).mapNotNull { email ->
            alunoRepository.findByEmailInstitucional(email).orElse(null)
        }
        alunos.forEach { turmaPort.matricularSeAusente(turma.id, it.id) }
        semearTurma(professorId, turma.id, agora)
        semearInbox(professorId, agora)
        LOG.info("Audiência de comunicação de desenvolvimento pronta (turma ADS 2026/1, professor.dev).")
    }

    private fun semearTurma(professorId: UUID, turmaId: UUID, agora: OffsetDateTime) {
        if (comunicacaoRepository.findByTitulo(TITULO_TURMA) != null) {
            return
        }
        val comunicacao = comunicacaoRepository.save(
            Comunicacao(
                Uuids.v7(),
                TipoComunicacao.TURMA,
                TITULO_TURMA,
                "Aula de reposição na **quinta**.",
                PrioridadeComunicacao.MEDIUM,
                professorId,
                TipoAudiencia.TURMA,
                turmaId,
                null,
                agora,
            ),
        )
        entregar(comunicacao.id, EMAIL_ALUNO, null, agora)
        entregar(comunicacao.id, EMAIL_NOVO, null, agora)
    }

    private fun semearInbox(professorId: UUID, agora: OffsetDateTime) {
        if (comunicacaoRepository.findByTitulo(TITULO_INBOX) != null) {
            return
        }
        val comunicacao = comunicacaoRepository.save(
            Comunicacao(
                Uuids.v7(),
                TipoComunicacao.INBOX,
                TITULO_INBOX,
                "Confira telefone e preferências de notificação.",
                PrioridadeComunicacao.HIGH,
                professorId,
                TipoAudiencia.TURMA,
                Uuids.v7(),
                null,
                agora,
            ),
        )
        entregar(comunicacao.id, EMAIL_ALUNO, "/perfil", agora)
    }

    private fun entregar(comunicacaoId: UUID, email: String, acao: String?, agora: OffsetDateTime) {
        val usuarioId = usuarioRepository.findByEmail(email).orElse(null)?.id ?: return
        if (entregaRepository.findByComunicacaoEDestinatario(comunicacaoId, usuarioId) != null) {
            return
        }
        entregaRepository.save(
            ComunicacaoEntrega(
                Uuids.v7(),
                comunicacaoId,
                usuarioId,
                null,
                acao,
                true,
                true,
                agora,
            ),
        )
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(ComunicacaoDevDataLoader::class.java)
        private const val EMAIL_PROFESSOR = "professor.dev@ufpr.br"
        private const val EMAIL_ALUNO = "aluno.dev@ufpr.br"
        private const val EMAIL_NOVO = "novo.dev@ufpr.br"
        private const val CODIGO_TADS = "TADS-SEPT"
        private const val CODIGO_TURMA = "ADS20261"
        private const val TITULO_TURMA = "Aula de reposição — ADS 2026/1"
        private const val TITULO_INBOX = "Atualize seu perfil"
    }
}
