package br.ufpr.sept.so2.modules.comunicacao.application

import br.ufpr.sept.so2.modules.comunicacao.application.ports.AudienciaPort
import br.ufpr.sept.so2.modules.comunicacao.application.ports.ComunicacaoRepository
import br.ufpr.sept.so2.modules.comunicacao.application.ports.DestinatarioComunicacao
import br.ufpr.sept.so2.modules.comunicacao.domain.AudienciaOpcao
import br.ufpr.sept.so2.modules.comunicacao.domain.Comunicacao
import br.ufpr.sept.so2.modules.comunicacao.domain.TipoAudiencia
import br.ufpr.sept.so2.modules.iam.application.ports.OutboxClaim
import br.ufpr.sept.so2.modules.iam.application.ports.OutboxPort
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import java.time.OffsetDateTime
import java.util.UUID

class PublicarComunicacaoUseCaseTest : StringSpec({
    "audiência fora do escopo não grava nem enfileira" {
        val repo = MemoriaComunicacao()
        val outbox = MemoriaOutbox()
        val useCase = useCase(repo, outbox, escopo = false)
        shouldThrow<DadoInvalidoException> {
            useCase.execute(UUID.randomUUID(), comando(UUID.randomUUID()))
        }
        repo.salvas.isEmpty() shouldBe true
        outbox.tipos.isEmpty() shouldBe true
    }

    "publicar grava a comunicação e enfileira o outbox" {
        val repo = MemoriaComunicacao()
        val outbox = MemoriaOutbox()
        val audiencia = UUID.randomUUID()
        val salva = useCase(repo, outbox, escopo = true).execute(UUID.randomUUID(), comando(audiencia))
        repo.salvas.map { it.id } shouldBe listOf(salva.id)
        outbox.tipos shouldBe listOf("comunicacao.published")
        outbox.payloads.single() shouldContain salva.id.toString()
    }

    "tipo que não é turma nem curso é fora de escopo" {
        val repo = MemoriaComunicacao()
        val outbox = MemoriaOutbox()
        shouldThrow<DadoInvalidoException> {
            useCase(repo, outbox, escopo = true).execute(
                UUID.randomUUID(),
                comando(UUID.randomUUID()).copy(audienciaTipo = "UNIVERSIDADE"),
            )
        }
        outbox.tipos.isEmpty() shouldBe true
    }
})

private fun comando(audienciaId: UUID) = PublicarComunicacaoComando(
    "Aviso",
    "Corpo do aviso",
    "TURMA",
    audienciaId,
    "MEDIUM",
    null,
)

private fun useCase(repo: ComunicacaoRepository, outbox: OutboxPort, escopo: Boolean) =
    PublicarComunicacaoUseCase(repo, AudienciaFixa(escopo), outbox, ObjectMapper())

private class AudienciaFixa(private val permite: Boolean) : AudienciaPort {
    override fun opcoes(professorId: UUID): List<AudienciaOpcao> = emptyList()

    override fun professorPode(professorId: UUID, tipo: TipoAudiencia, audienciaId: UUID): Boolean = permite

    override fun destinatarios(tipo: TipoAudiencia, audienciaId: UUID, autorId: UUID): List<DestinatarioComunicacao> =
        emptyList()
}

private class MemoriaComunicacao : ComunicacaoRepository {
    val salvas = mutableListOf<Comunicacao>()

    override fun save(comunicacao: Comunicacao): Comunicacao {
        salvas += comunicacao
        return comunicacao
    }

    override fun findById(id: UUID): Comunicacao? = salvas.find { it.id == id }

    override fun findByAutor(autorId: UUID): List<Comunicacao> = salvas.filter { it.autorId == autorId }

    override fun findByIds(ids: Collection<UUID>): List<Comunicacao> = salvas.filter { it.id in ids }

    override fun findByTitulo(titulo: String): Comunicacao? = salvas.find { it.titulo == titulo }
}

private class MemoriaOutbox : OutboxPort {
    val tipos = mutableListOf<String>()
    val payloads = mutableListOf<String>()

    override fun enqueue(tipo: String, payload: String) {
        tipos += tipo
        payloads += payload
    }

    override fun claimPending(limit: Int, staleBefore: OffsetDateTime): List<OutboxClaim> = emptyList()

    override fun markSent(id: UUID) = Unit

    override fun markFailed(id: UUID, tentativas: Int, lastError: String?) = Unit

    override fun markDead(id: UUID, tentativas: Int, lastError: String?) = Unit

    override fun markPendingRetry(id: UUID, tentativas: Int, lastError: String?) = Unit
}
