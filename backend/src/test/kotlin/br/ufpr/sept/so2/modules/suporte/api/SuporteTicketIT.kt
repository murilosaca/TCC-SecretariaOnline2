package br.ufpr.sept.so2.modules.suporte.api

import br.ufpr.sept.so2.modules.iam.application.ports.PasswordHasher
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.modules.solicitacoes.application.ports.SolicitacaoRepository
import br.ufpr.sept.so2.modules.solicitacoes.application.ports.TipoSolicitacaoRepository
import br.ufpr.sept.so2.modules.solicitacoes.infrastructure.SuporteTecnicoSeed
import br.ufpr.sept.so2.shared.ItUsuarioFixture
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.startsWith
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.OffsetDateTime

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SuporteTicketIT {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var usuarioRepository: UsuarioRepository

    @Autowired
    private lateinit var passwordHasher: PasswordHasher

    @Autowired
    private lateinit var tipoSolicitacaoRepository: TipoSolicitacaoRepository

    @Autowired
    private lateinit var solicitacaoRepository: SolicitacaoRepository

    private val usuariosIt
        get() = ItUsuarioFixture(usuarioRepository, passwordHasher, mockMvc)

    @BeforeEach
    fun seed() {
        if (tipoSolicitacaoRepository.findByCodigo(SuporteTecnicoSeed.CODIGO).isEmpty) {
            tipoSolicitacaoRepository.save(SuporteTecnicoSeed.tipo(OffsetDateTime.now()))
        }
        usuariosIt.criarUsuario(EMAIL, "GRR20243601", listOf("request.open", "request.view_own"))
    }

    @Test
    fun faqExigeSessaoEQuartoTicketNaoCriaSolicitacao() {
        mockMvc.perform(get("/suporte/faq"))
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.type").value(containsString("authentication-required")))

        val usuario = usuarioRepository.findByEmail(EMAIL).orElseThrow()
        val token = usuariosIt.login(EMAIL)
        mockMvc.perform(get("/suporte/faq").header("Authorization", "Bearer $token"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].pergunta").value("Como redefinir minha senha?"))

        repeat(3) {
            mockMvc.perform(
                post("/requests")
                    .header("Authorization", "Bearer $token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(CORPO),
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.tipoCodigo").value("SUPORTE_TECNICO"))
                .andExpect(jsonPath("$.protocolo").value(startsWith("PROT-")))
        }
        val abertas = total(usuario.id)
        org.junit.jupiter.api.Assertions.assertEquals(3, abertas)

        mockMvc.perform(
            post("/requests")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(CORPO),
        )
            .andExpect(status().isTooManyRequests)
            .andExpect(jsonPath("$.type").value(containsString("rate-limit")))
            .andExpect(header().exists("Retry-After"))

        org.junit.jupiter.api.Assertions.assertEquals(abertas, total(usuario.id))
    }

    private fun total(usuarioId: java.util.UUID): Long =
        solicitacaoRepository.findMinhas(usuarioId, null, "SUPORTE_TECNICO", null, PageRequest.of(0, 20))
            .totalElements

    companion object {
        private const val EMAIL = "aluno.suporte36@ufpr.br"
        private const val CORPO =
            """{"tipoCodigo":"SUPORTE_TECNICO","payload":{"assunto":"Duvida de prazo","mensagem":"Qual e o prazo?"}}"""
    }
}
