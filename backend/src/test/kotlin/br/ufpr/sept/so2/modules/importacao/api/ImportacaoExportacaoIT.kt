package br.ufpr.sept.so2.modules.importacao.api

import br.ufpr.sept.so2.modules.academico.application.ports.CursoRepository
import br.ufpr.sept.so2.modules.academico.application.ports.CursoSecretarioRepository
import br.ufpr.sept.so2.modules.academico.domain.Curso
import br.ufpr.sept.so2.modules.iam.application.ports.PasswordHasher
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.shared.ItJson
import br.ufpr.sept.so2.shared.ItUsuarioFixture
import br.ufpr.sept.so2.shared.infrastructure.Uuids
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.OffsetDateTime

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ImportacaoExportacaoIT {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var usuarioRepository: UsuarioRepository

    @Autowired
    private lateinit var passwordHasher: PasswordHasher

    @Autowired
    private lateinit var cursoRepository: CursoRepository

    @Autowired
    private lateinit var cursoSecretarioRepository: CursoSecretarioRepository

    private val usuariosIt
        get() = ItUsuarioFixture(usuarioRepository, passwordHasher, mockMvc)

    @BeforeEach
    fun seed() {
        val agora = OffsetDateTime.parse("2026-10-03T12:00:00Z")
        val cursoId = if (cursoRepository.findByCodigo("EXP-IT").isPresent) {
            cursoRepository.findByCodigo("EXP-IT").get().id
        } else {
            cursoRepository.save(Curso(Uuids.v7(), "Export IT", "EXIT", "EXP-IT", null, 120, true, agora, agora)).id
        }
        val sec = usuariosIt.criarUsuario(EMAIL_SEC, "GRR20249621", listOf("import.run", "export.run"))
        cursoSecretarioRepository.adicionarSeAusente(cursoId, sec.id)
        usuariosIt.criarUsuario(EMAIL_SEM_CAP, "GRR20249622", listOf("course.manage"))
    }

    @Test
    fun importacaoSemCap403EArquivoGrande422() {
        mockMvc.perform(multipart("/importacoes/usuarios").file(arquivoPequeno()))
            .andExpect(status().isUnauthorized)
        val semCap = usuariosIt.login(EMAIL_SEM_CAP)
        mockMvc.perform(
            multipart("/importacoes/usuarios").file(arquivoPequeno()).header("Authorization", "Bearer $semCap"),
        ).andExpect(status().isForbidden)

        val sec = usuariosIt.login(EMAIL_SEC)
        mockMvc.perform(
            multipart("/importacoes/usuarios")
                .file(MockMultipartFile("arquivo", "grande.csv", "text/csv", ByteArray(20 * 1024 * 1024 + 1)))
                .header("Authorization", "Bearer $sec"),
        )
            .andExpect(status().isUnprocessableEntity)
            .andExpect(jsonPath("$.detail").value("Arquivo excede 20 MB."))
    }

    @Test
    fun exportacaoDevolve202EDownloadPorLink() {
        mockMvc.perform(post("/exportacoes/alunos").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isUnauthorized)
        val semCap = usuariosIt.login(EMAIL_SEM_CAP)
        mockMvc.perform(
            post("/exportacoes/alunos")
                .header("Authorization", "Bearer $semCap")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"),
        ).andExpect(status().isForbidden)

        val sec = usuariosIt.login(EMAIL_SEC)
        val criado = mockMvc.perform(
            post("/exportacoes/alunos")
                .header("Authorization", "Bearer $sec")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"),
        )
            .andExpect(status().isAccepted)
            .andExpect(jsonPath("$.status").value("PROCESSANDO"))
            .andReturn()
        val corpo = criado.response.contentAsString
        assertTrue(!corpo.contains("nome,grr"), corpo)
        val id = ItJson.text(corpo, "id")

        var pronto = ""
        repeat(25) {
            val atual = mockMvc.perform(get("/exportacoes/$id").header("Authorization", "Bearer $sec"))
                .andExpect(status().isOk)
                .andReturn()
                .response.contentAsString
            if (atual.contains("\"download\"")) {
                pronto = atual
                return@repeat
            }
            Thread.sleep(400)
        }
        assertTrue(pronto.contains("\"status\":\"PRONTO\""), pronto.ifBlank { "exportação não ficou pronta" })

        mockMvc.perform(get("/exportacoes/$id/download").header("Authorization", "Bearer $sec"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.url").isNotEmpty)
            .andExpect(jsonPath("$.expiresInSeconds").value(900))
    }

    @Test
    fun auditLogSemCap403EDeleteNaoExiste() {
        mockMvc.perform(get("/audit-log")).andExpect(status().isUnauthorized)
        val semCap = usuariosIt.login(EMAIL_SEM_CAP)
        mockMvc.perform(get("/audit-log").header("Authorization", "Bearer $semCap"))
            .andExpect(status().isForbidden)
        val leitor = usuariosIt.criarUsuario(EMAIL_AUDIT, "GRR20249623", listOf("audit.read"))
        val token = usuariosIt.login(EMAIL_AUDIT)
        leitor.id
        mockMvc.perform(get("/audit-log").header("Authorization", "Bearer $token"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
        mockMvc.perform(delete("/audit-log").header("Authorization", "Bearer $token"))
            .andExpect(status().isMethodNotAllowed)
        mockMvc.perform(patch("/audit-log").header("Authorization", "Bearer $token").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isMethodNotAllowed)
    }

    private fun arquivoPequeno() = MockMultipartFile("arquivo", "usuarios.csv", "text/csv", "nome,email\n".toByteArray())

    companion object {
        private const val EMAIL_SEC = "it.import.sec@ufpr.br"
        private const val EMAIL_SEM_CAP = "it.import.semcap@ufpr.br"
        private const val EMAIL_AUDIT = "it.audit.admin@ufpr.br"
    }
}
