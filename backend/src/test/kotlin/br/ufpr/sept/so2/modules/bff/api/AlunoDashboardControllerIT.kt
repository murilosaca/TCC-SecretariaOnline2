package br.ufpr.sept.so2.modules.bff.api

import br.ufpr.sept.so2.modules.academico.application.ports.AlunoRepository
import br.ufpr.sept.so2.modules.academico.application.ports.CursoRepository
import br.ufpr.sept.so2.modules.academico.domain.Aluno
import br.ufpr.sept.so2.modules.academico.domain.AlunoSituacao
import br.ufpr.sept.so2.modules.academico.domain.Curso
import br.ufpr.sept.so2.modules.iam.application.ports.PasswordHasher
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.modules.iam.domain.Usuario
import br.ufpr.sept.so2.modules.solicitacoes.application.ports.TipoSolicitacaoRepository
import br.ufpr.sept.so2.modules.solicitacoes.infrastructure.DeclaracaoSimplesSeed
import br.ufpr.sept.so2.shared.domain.valueobject.Email
import br.ufpr.sept.so2.shared.domain.valueobject.Grr
import br.ufpr.sept.so2.shared.infrastructure.Uuids
import org.hamcrest.Matchers.nullValue
import org.hamcrest.Matchers.startsWith
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
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AlunoDashboardControllerIT {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var usuarioRepository: UsuarioRepository

    @Autowired
    private lateinit var passwordHasher: PasswordHasher

    @Autowired
    private lateinit var tipoSolicitacaoRepository: TipoSolicitacaoRepository

    @Autowired
    private lateinit var cursoRepository: CursoRepository

    @Autowired
    private lateinit var alunoRepository: AlunoRepository

    @BeforeEach
    fun seed() {
        val agora = OffsetDateTime.now()
        if (tipoSolicitacaoRepository.findByCodigo(DeclaracaoSimplesSeed.CODIGO).isEmpty) {
            tipoSolicitacaoRepository.save(DeclaracaoSimplesSeed.tipo(agora))
        }
        val idCurso = garantirCurso(agora)
        criarAlunoSeAusente("Aluno Dev", "GRR20240001", "aluno.dev@ufpr.br", idCurso, agora)
        criarUsuarioSeAusente(
            "aluno.dev@ufpr.br",
            "GRR20240001",
            true,
            agora,
            agora,
            listOf("dashboard.view_own", "request.view_own", "request.open"),
        )
        criarUsuarioSeAusente(
            "novo.dev@ufpr.br",
            "GRR20240002",
            false,
            null,
            agora,
            listOf("dashboard.view_own", "request.view_own", "request.open"),
        )
        criarUsuarioSeAusente(
            "professor.dashboard@ufpr.br",
            "GRR20240099",
            true,
            agora,
            agora,
            listOf("dashboard.view_own", "event.manage", "event.host"),
        )
    }

    @Test
    fun anonimoRecebe401() {
        mockMvc.perform(get("/bff/dashboard/aluno"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun primeiroAcessoRecebe403() {
        val token = login("novo.dev@ufpr.br")
        mockMvc.perform(
            get("/bff/dashboard/aluno")
                .header("Authorization", "Bearer $token"),
        ).andExpect(status().isForbidden)
    }

    @Test
    fun alunoAutenticadoVeProtocoloQuandoExiste() {
        val token = login("aluno.dev@ufpr.br")

        mockMvc.perform(
            post("/requests")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "tipoCodigo": "DECLARACAO_SIMPLES",
                      "payload": {
                        "finalidade": "Comprovação de vínculo",
                        "observacao": "Dashboard"
                      }
                    }
                    """.trimIndent(),
                ),
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.protocolo", startsWith("PROT-")))

        mockMvc.perform(
            get("/bff/dashboard/aluno")
                .header("Authorization", "Bearer $token"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.saudacao.nome").value("Aluno Dev"))
            .andExpect(jsonPath("$.kpis.horasFormativas.validadas").value(0))
            .andExpect(jsonPath("$.kpis.horasFormativas.requeridas").value(120))
            .andExpect(jsonPath("$.kpis.eventosHoje").isNumber)
            .andExpect(jsonPath("$.kpis.certificados").value(nullValue()))
            .andExpect(jsonPath("$.kpis.solicitacoesAbertas").isNumber)
            .andExpect(jsonPath("$.proximosEventos").isArray)
            .andExpect(jsonPath("$.pendenciasFormativas").isArray)
            .andExpect(jsonPath("$.ultimasSolicitacoes[0].protocolo", startsWith("PROT-")))
            .andExpect(jsonPath("$._links.self").value("/bff/dashboard/aluno"))
            .andExpect(jsonPath("$._links.novaSolicitacao").value("/solicitacoes/nova"))
            .andExpect(jsonPath("$._links.novaFormativa").doesNotExist())
    }

    @Test
    fun sessaoSemCapabilityDeAlunoRecebe403() {
        val token = login("professor.dashboard@ufpr.br")
        mockMvc.perform(
            get("/bff/dashboard/aluno")
                .header("Authorization", "Bearer $token"),
        ).andExpect(status().isForbidden)
    }

    private fun garantirCurso(agora: OffsetDateTime): UUID {
        val existente = cursoRepository.findByCodigo(CODIGO_CURSO)
        if (existente.isPresent) {
            return existente.get().id
        }
        return cursoRepository.save(
            Curso(Uuids.v7(), "TADS IT Dashboard", "TADSD", CODIGO_CURSO, null, 120, true, agora, agora),
        ).id
    }

    private fun criarAlunoSeAusente(
        nome: String,
        grr: String,
        email: String,
        idCurso: UUID,
        agora: OffsetDateTime,
    ) {
        if (alunoRepository.findByGrr(grr).isPresent) {
            return
        }
        alunoRepository.save(
            Aluno(
                Uuids.v7(),
                nome,
                null,
                Grr.of(grr),
                Email.of(email),
                null,
                null,
                idCurso,
                AlunoSituacao.MATRICULADO,
                true,
                agora,
                agora,
            ),
        )
    }

    private fun criarUsuarioSeAusente(
        email: String,
        grr: String,
        senhaAlterada: Boolean,
        lgpd: OffsetDateTime?,
        agora: OffsetDateTime,
        authorities: List<String>,
    ) {
        if (usuarioRepository.findByEmail(email).isPresent) {
            return
        }
        usuarioRepository.save(
            Usuario(
                Uuids.v7(),
                Email.of(email),
                null,
                Grr.of(grr),
                passwordHasher.hash("TroqueEstaSenha1!"),
                senhaAlterada,
                lgpd,
                if (lgpd == null) null else "127.0.0.1",
                if (lgpd == null) null else "it",
                true,
                0,
                null,
                authorities,
                agora,
                agora,
            ),
        )
    }

    private fun login(email: String): String {
        val result = mockMvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"identificador\":\"$email\",\"senha\":\"TroqueEstaSenha1!\"}"),
        )
            .andExpect(status().isOk)
            .andReturn()
        return extract(result.response.contentAsString, "\"accessToken\":\"", "\"")
    }

    companion object {
        private const val CODIGO_CURSO = "TADS-BFF-IT"

        private fun extract(json: String, startToken: String, endToken: String): String {
            val start = json.indexOf(startToken)
            val from = start + startToken.length
            val end = json.indexOf(endToken, from)
            return json.substring(from, end)
        }
    }
}
