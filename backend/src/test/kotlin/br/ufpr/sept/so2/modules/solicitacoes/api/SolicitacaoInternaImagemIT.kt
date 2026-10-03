package br.ufpr.sept.so2.modules.solicitacoes.api

import br.ufpr.sept.so2.modules.academico.application.ports.AlunoRepository
import br.ufpr.sept.so2.modules.academico.application.ports.CursoRepository
import br.ufpr.sept.so2.modules.academico.application.ports.CursoSecretarioRepository
import br.ufpr.sept.so2.modules.academico.domain.Aluno
import br.ufpr.sept.so2.modules.academico.domain.AlunoSituacao
import br.ufpr.sept.so2.modules.academico.domain.Curso
import br.ufpr.sept.so2.modules.iam.application.ports.PasswordHasher
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.modules.solicitacoes.application.ports.TipoSolicitacaoRepository
import br.ufpr.sept.so2.modules.solicitacoes.infrastructure.AutorizacaoImagemSeed
import br.ufpr.sept.so2.modules.solicitacoes.infrastructure.DeclaracaoSimplesSeed
import br.ufpr.sept.so2.shared.ItJson
import br.ufpr.sept.so2.shared.ItUsuarioFixture
import br.ufpr.sept.so2.shared.domain.valueobject.Email
import br.ufpr.sept.so2.shared.domain.valueobject.Grr
import br.ufpr.sept.so2.shared.infrastructure.Uuids
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.OffsetDateTime
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SolicitacaoInternaImagemIT {
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

    @Autowired
    private lateinit var alunoRepository: AlunoRepository

    @Autowired
    private lateinit var tipoSolicitacaoRepository: TipoSolicitacaoRepository

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    private val usuariosIt
        get() = ItUsuarioFixture(usuarioRepository, passwordHasher, mockMvc)

    private lateinit var cursoId: UUID
    private lateinit var cursoForaId: UUID
    private lateinit var alunoId: UUID
    private lateinit var alunoForaId: UUID
    private lateinit var titularId: UUID

    @BeforeEach
    fun seed() {
        val agora = OffsetDateTime.parse("2026-10-03T12:00:00Z")
        if (tipoSolicitacaoRepository.findByCodigo(DeclaracaoSimplesSeed.CODIGO).isEmpty) {
            tipoSolicitacaoRepository.save(DeclaracaoSimplesSeed.tipo(agora))
        }
        if (tipoSolicitacaoRepository.findByCodigo(AutorizacaoImagemSeed.CODIGO).isEmpty) {
            tipoSolicitacaoRepository.save(AutorizacaoImagemSeed.tipo(agora))
        }
        cursoId = garantirCurso("INT-IT", "Interna IT", "INIT", agora)
        cursoForaId = garantirCurso("INT-FORA", "Interna Fora", "INFT", agora)
        val secretaria = usuariosIt.criarUsuario(
            EMAIL_SEC,
            GRR_SEC,
            listOf("request.internal_open", "image_authorization.review", "request.view_curso"),
        )
        cursoSecretarioRepository.adicionarSeAusente(cursoId, secretaria.id)
        usuariosIt.criarUsuario(EMAIL_SEM_CAP, GRR_SEM_CAP, listOf("request.open"))
        titularId = usuariosIt.criarUsuario(EMAIL_ALUNO, GRR_ALUNO, listOf("request.open", "request.view_own")).id
        val fora = usuariosIt.criarUsuario(EMAIL_FORA, GRR_FORA, listOf("request.open", "request.view_own"))
        alunoId = garantirAluno("Aluno Interna", GRR_ALUNO, EMAIL_ALUNO, cursoId, agora)
        alunoForaId = garantirAluno("Aluno Fora", GRR_FORA, EMAIL_FORA, cursoForaId, agora)
        fora.id
    }

    @Test
    fun internaSemCapEAnonimoEAlunoForaDoEscopo() {
        mockMvc.perform(
            post("/requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo(alunoId)),
        ).andExpect(status().isUnauthorized)

        val semCap = usuariosIt.login(EMAIL_SEM_CAP)
        mockMvc.perform(
            post("/requests")
                .header("Authorization", "Bearer $semCap")
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo(alunoId)),
        )
            .andExpect(status().isForbidden)

        val sec = usuariosIt.login(EMAIL_SEC)
        mockMvc.perform(
            post("/requests")
                .header("Authorization", "Bearer $sec")
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo(alunoForaId)),
        )
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.detail").value("O aluno selecionado não pertence aos cursos vinculados à sua conta."))
    }

    @Test
    fun internaGravaOAlunoComoTitular() {
        val sec = usuariosIt.login(EMAIL_SEC)
        val criado = mockMvc.perform(
            post("/requests")
                .header("Authorization", "Bearer $sec")
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo(alunoId)),
        )
            .andExpect(status().isCreated)
            .andReturn()
        val id = ItJson.text(criado.response.contentAsString, "id")
        val titular = jdbcTemplate.queryForObject(
            "select solicitante_id from solicitacao where id = ?",
            UUID::class.java,
            UUID.fromString(id),
        )
        assertEquals(titularId, titular)
    }

    @Test
    fun loteComItemForaDeAbertaNaoGravaNada() {
        val aluno = usuariosIt.login(EMAIL_ALUNO)
        val aberta = criarImagem(aluno)
        val fechada = criarImagem(aluno)
        jdbcTemplate.update("update solicitacao set estado = 'DEFERIDA' where id = ?", fechada)
        val outbox = contar("solicitacao.imagem_deliberada")

        mockMvc.perform(patch("/requests/bulk-deliberate").contentType(MediaType.APPLICATION_JSON).content(lote(aberta, fechada)))
            .andExpect(status().isUnauthorized)

        val semCap = usuariosIt.login(EMAIL_SEM_CAP)
        mockMvc.perform(
            patch("/requests/bulk-deliberate")
                .header("Authorization", "Bearer $semCap")
                .contentType(MediaType.APPLICATION_JSON)
                .content(lote(aberta, fechada)),
        ).andExpect(status().isForbidden)

        val sec = usuariosIt.login(EMAIL_SEC)
        mockMvc.perform(
            patch("/requests/bulk-deliberate")
                .header("Authorization", "Bearer $sec")
                .contentType(MediaType.APPLICATION_JSON)
                .content(lote(aberta, fechada)),
        )
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.type").value("https://secretariaonline.ufpr.br/errors/bulk-conflict"))
            .andExpect(jsonPath("$.failedIds[0]").value(fechada.toString()))

        assertEquals("ABERTA", estado(aberta))
        assertEquals("DEFERIDA", estado(fechada))
        assertEquals(outbox, contar("solicitacao.imagem_deliberada"))
    }

    private fun criarImagem(token: String): UUID {
        val criado = mockMvc.perform(
            post("/requests")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"tipoCodigo":"AUTORIZACAO_IMAGEM","payload":{"declaracao":"Autorizo o uso da imagem."}}
                    """.trimIndent(),
                ),
        )
            .andExpect(status().isCreated)
            .andReturn()
        return UUID.fromString(ItJson.text(criado.response.contentAsString, "id"))
    }

    private fun corpo(aluno: UUID) =
        """
        {"tipoCodigo":"DECLARACAO_SIMPLES","payload":{"finalidade":"Comprovação de vínculo"},"onBehalfOf":"$aluno"}
        """.trimIndent()

    private fun lote(vararg ids: UUID) =
        """{"ids":[${ids.joinToString(",") { "\"$it\"" }}],"decisao":"DEFERIDA"}"""

    private fun estado(id: UUID): String =
        jdbcTemplate.queryForObject("select estado from solicitacao where id = ?", String::class.java, id)!!

    private fun contar(tipo: String): Int =
        jdbcTemplate.queryForObject("select count(*) from outbox_event where tipo = ?", Int::class.java, tipo) ?: 0

    private fun garantirCurso(codigo: String, nome: String, sigla: String, agora: OffsetDateTime): UUID {
        val existente = cursoRepository.findByCodigo(codigo)
        if (existente.isPresent) return existente.get().id
        return cursoRepository.save(Curso(Uuids.v7(), nome, sigla, codigo, null, 120, true, agora, agora)).id
    }

    private fun garantirAluno(nome: String, grr: String, email: String, idCurso: UUID, agora: OffsetDateTime): UUID {
        val existente = alunoRepository.findByGrr(grr)
        if (existente.isPresent) return existente.get().id
        return alunoRepository.save(
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
        ).id
    }

    companion object {
        private const val EMAIL_SEC = "it.interna.sec@ufpr.br"
        private const val EMAIL_SEM_CAP = "it.interna.semcap@ufpr.br"
        private const val EMAIL_ALUNO = "it.interna.aluno@ufpr.br"
        private const val EMAIL_FORA = "it.interna.fora@ufpr.br"
        private const val GRR_SEC = "GRR20249611"
        private const val GRR_SEM_CAP = "GRR20249612"
        private const val GRR_ALUNO = "GRR20249613"
        private const val GRR_FORA = "GRR20249614"
    }
}
