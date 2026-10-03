package br.ufpr.sept.so2.modules.comunicacao.api

import br.ufpr.sept.so2.modules.iam.application.ports.PasswordHasher
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.modules.iam.infrastructure.persistence.OutboxEventJpaEntity
import br.ufpr.sept.so2.modules.iam.infrastructure.persistence.OutboxEventJpaRepository
import br.ufpr.sept.so2.shared.ItUsuarioFixture
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OutboxAdminIT {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var usuarioRepository: UsuarioRepository

    @Autowired
    private lateinit var passwordHasher: PasswordHasher

    @Autowired
    private lateinit var outboxEventJpaRepository: OutboxEventJpaRepository

    private val usuariosIt
        get() = ItUsuarioFixture(usuarioRepository, passwordHasher, mockMvc)

    @BeforeEach
    fun seed() {
        usuariosIt.criarUsuario(EMAIL_ADMIN, "GRR20243101", listOf("system.observe"))
        usuariosIt.criarUsuario(EMAIL_SEC, "GRR20243102", listOf("course.manage"))
    }

    @Test
    fun anonimoRecebe401() {
        mockMvc.perform(get("/admin/outbox"))
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.type").value(containsString("authentication-required")))
    }

    @Test
    fun semCapRecebe403() {
        val token = usuariosIt.login(EMAIL_SEC)
        mockMvc.perform(get("/admin/outbox").header("Authorization", "Bearer $token"))
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.type").value(containsString("access-denied")))
    }

    @Test
    fun reentregaDeFailedVoltaAPendingEZeraTentativas() {
        val token = usuariosIt.login(EMAIL_ADMIN)
        val evento = OutboxEventJpaEntity("solicitacao.criada", """{"solicitacaoId":"1"}""")
        evento.status = "FAILED"
        evento.tentativas = 4
        val salvo = outboxEventJpaRepository.save(evento)

        mockMvc.perform(
            post("/admin/outbox/${salvo.id}/reentrega").header("Authorization", "Bearer $token"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("PENDING"))
            .andExpect(jsonPath("$.tentativas").value(0))
            .andExpect(jsonPath("$._links.retry").doesNotExist())

        val recarregado = outboxEventJpaRepository.findById(salvo.id!!).orElseThrow()
        org.junit.jupiter.api.Assertions.assertEquals("PENDING", recarregado.status)
        org.junit.jupiter.api.Assertions.assertEquals(0, recarregado.tentativas)
    }

    @Test
    fun reentregaDeSentRecebe409() {
        val token = usuariosIt.login(EMAIL_ADMIN)
        val evento = OutboxEventJpaEntity("solicitacao.criada", """{"solicitacaoId":"2"}""")
        evento.status = "SENT"
        evento.tentativas = 1
        val salvo = outboxEventJpaRepository.save(evento)

        mockMvc.perform(
            post("/admin/outbox/${salvo.id}/reentrega").header("Authorization", "Bearer $token"),
        )
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.type").value(containsString("conflict")))

        val recarregado = outboxEventJpaRepository.findById(salvo.id!!).orElseThrow()
        org.junit.jupiter.api.Assertions.assertEquals("SENT", recarregado.status)
    }

    companion object {
        private const val EMAIL_ADMIN = "jobs.admin.it@ufpr.br"
        private const val EMAIL_SEC = "jobs.sec.it@ufpr.br"
    }
}
