package br.ufpr.sept.so2.modules.busca.api

import br.ufpr.sept.so2.modules.iam.application.ports.PasswordHasher
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.modules.solicitacoes.application.ports.TipoSolicitacaoRepository
import br.ufpr.sept.so2.modules.solicitacoes.infrastructure.DeclaracaoSimplesSeed
import br.ufpr.sept.so2.shared.ItUsuarioFixture
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.OffsetDateTime

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BuscaGlobalIT {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var usuarioRepository: UsuarioRepository

    @Autowired
    private lateinit var passwordHasher: PasswordHasher

    @Autowired
    private lateinit var tipoSolicitacaoRepository: TipoSolicitacaoRepository

    private val usuariosIt
        get() = ItUsuarioFixture(usuarioRepository, passwordHasher, mockMvc)

    @BeforeEach
    fun seed() {
        if (tipoSolicitacaoRepository.findByCodigo(DeclaracaoSimplesSeed.CODIGO).isEmpty) {
            tipoSolicitacaoRepository.save(DeclaracaoSimplesSeed.tipo(OffsetDateTime.now()))
        }
        usuariosIt.criarUsuario(EMAIL_A, "GRR20243501", listOf("request.open", "request.view_own"))
        usuariosIt.criarUsuario(EMAIL_B, "GRR20243502", listOf("request.open", "request.view_own"))
        usuariosIt.criarUsuario(EMAIL_ZEZINHO, "GRR20243503", listOf("user.update_own_profile"))
    }

    @Test
    fun alunoNaoVeSolicitacaoAlheiaNemUsuarios() {
        val tokenA = usuariosIt.login(EMAIL_A)
        val tokenB = usuariosIt.login(EMAIL_B)
        val protocoloA = abrir(tokenA, "Finalidade do aluno A busca")
        val protocoloB = abrir(tokenB, "Finalidade do aluno B busca")

        mockMvc.perform(get("/search").param("q", protocoloB).header("Authorization", "Bearer $tokenA"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.solicitacoes").isEmpty())
            .andExpect(jsonPath("$.usuarios").isEmpty())

        mockMvc.perform(get("/search").param("q", protocoloA).header("Authorization", "Bearer $tokenA"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.solicitacoes[0].titulo").value(protocoloA))
            .andExpect(jsonPath("$.usuarios").isEmpty())

        mockMvc.perform(get("/search").param("q", "zezinho").header("Authorization", "Bearer $tokenA"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.usuarios").isEmpty())
    }

    private fun abrir(token: String, finalidade: String): String {
        val corpo = mockMvc.perform(
            post("/requests")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"tipoCodigo":"DECLARACAO_SIMPLES","payload":{"finalidade":"$finalidade"}}"""),
        )
            .andExpect(status().isCreated)
            .andReturn()
            .response
            .contentAsString
        val protocolo = com.fasterxml.jackson.databind.ObjectMapper().readTree(corpo)["protocolo"].asText()
        org.junit.jupiter.api.Assertions.assertTrue(protocolo.startsWith("PROT-"))
        return protocolo
    }

    companion object {
        private const val EMAIL_A = "aluno.busca.a@ufpr.br"
        private const val EMAIL_B = "aluno.busca.b@ufpr.br"
        private const val EMAIL_ZEZINHO = "zezinho.busca@ufpr.br"
    }
}
