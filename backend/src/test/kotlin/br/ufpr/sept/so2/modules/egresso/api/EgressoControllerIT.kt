package br.ufpr.sept.so2.modules.egresso.api

import br.ufpr.sept.so2.modules.academico.application.ports.AlunoRepository
import br.ufpr.sept.so2.modules.academico.application.ports.CursoRepository
import br.ufpr.sept.so2.modules.academico.domain.Aluno
import br.ufpr.sept.so2.modules.academico.domain.AlunoSituacao
import br.ufpr.sept.so2.modules.academico.domain.Curso
import br.ufpr.sept.so2.modules.certificados.application.ports.CertificadoRepository
import br.ufpr.sept.so2.modules.certificados.domain.Certificado
import br.ufpr.sept.so2.modules.certificados.domain.CertificadoTipo
import br.ufpr.sept.so2.modules.iam.application.ports.PasswordHasher
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.modules.iam.domain.Usuario
import br.ufpr.sept.so2.shared.ItUsuarioFixture
import br.ufpr.sept.so2.shared.domain.valueobject.Email
import br.ufpr.sept.so2.shared.domain.valueobject.Grr
import br.ufpr.sept.so2.shared.infrastructure.Uuids
import org.hamcrest.Matchers.nullValue
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.security.MessageDigest
import java.time.OffsetDateTime
import java.util.HexFormat
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EgressoControllerIT {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var usuarioRepository: UsuarioRepository

    @Autowired
    private lateinit var passwordHasher: PasswordHasher

    @Autowired
    private lateinit var cursoRepository: CursoRepository

    @Autowired
    private lateinit var alunoRepository: AlunoRepository

    @Autowired
    private lateinit var certificadoRepository: CertificadoRepository

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    private val usuariosIt
        get() = ItUsuarioFixture(usuarioRepository, passwordHasher, mockMvc)

    private lateinit var certificadoId: UUID

    @BeforeEach
    fun seed() {
        val agora = OffsetDateTime.parse("2026-06-01T12:00:00Z")
        val cursoId = garantirCurso(agora)
        usuariosIt.criarUsuario(EMAIL_EGRESSO, GRR_EGRESSO, listOf("alumni.view_own"))
        val egressoId = criarAluno("Egresso IT", GRR_EGRESSO, EMAIL_EGRESSO, cursoId, agora, AlunoSituacao.EGRESSO)
        usuariosIt.criarUsuario(EMAIL_OUTRO, GRR_OUTRO, listOf("alumni.view_own"))
        criarAluno("Outro Egresso", GRR_OUTRO, EMAIL_OUTRO, cursoId, agora, AlunoSituacao.EGRESSO)
        usuariosIt.criarUsuario(EMAIL_ALUNO, GRR_ALUNO, listOf("dashboard.view_own", "request.open", "tcc.view_own"))
        criarAluno("Aluno Ativo", GRR_ALUNO, EMAIL_ALUNO, cursoId, agora, AlunoSituacao.MATRICULADO)
        usuariosIt.criarUsuario(EMAIL_COMBO, GRR_COMBO, listOf("alumni.view_own", "request.open"))
        criarAluno("Combo", GRR_COMBO, EMAIL_COMBO, cursoId, agora, AlunoSituacao.MATRICULADO)
        usuariosIt.criarUsuario(EMAIL_MATRICULADO, GRR_MATRICULADO, listOf("alumni.view_own"))
        criarAluno(
            "Matriculado Cap",
            GRR_MATRICULADO,
            EMAIL_MATRICULADO,
            cursoId,
            agora,
            AlunoSituacao.MATRICULADO,
        )
        usuariosIt.criarUsuario(EMAIL_SEMCAP, GRR_SEMCAP, listOf("dashboard.view_own"))
        certificadoId = garantirCertificado(egressoId, agora).id
    }

    @Test
    fun anonimoRecebe401() {
        mockMvc.perform(get("/egressos/me"))
            .andExpect(status().isUnauthorized)
        mockMvc.perform(get("/egressos/me/certificados/$certificadoId/reemissao"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun semCapabilityEAlunoAtivoRecebem403() {
        val semCap = usuariosIt.login(EMAIL_SEMCAP)
        mockMvc.perform(get("/egressos/me").header("Authorization", "Bearer $semCap"))
            .andExpect(status().isForbidden)

        val aluno = usuariosIt.login(EMAIL_ALUNO)
        mockMvc.perform(get("/egressos/me").header("Authorization", "Bearer $aluno"))
            .andExpect(status().isForbidden)

        val combo = usuariosIt.login(EMAIL_COMBO)
        mockMvc.perform(get("/egressos/me").header("Authorization", "Bearer $combo"))
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.detail").value("Aluno ativo não acessa o portal do egresso."))
    }

    @Test
    fun matriculadoSoComACapRecebe403() {
        val token = usuariosIt.login(EMAIL_MATRICULADO)
        mockMvc.perform(get("/egressos/me").header("Authorization", "Bearer $token"))
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.detail").value("Portal do egresso indisponível para esta sessão."))
    }

    @Test
    fun painelEReadOnlyEReemissaoMantemHashEAssinatura() {
        val token = usuariosIt.login(EMAIL_EGRESSO)
        val antes = contarCertificados()
        val hashAntes = coluna("hash_sha256")
        val assinaturaAntes = coluna("assinatura")

        mockMvc.perform(get("/auth/me").header("Authorization", "Bearer $token"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._links['egresso-inicio']").value("/egresso/inicio"))
            .andExpect(jsonPath("$._links.inicio").doesNotExist())
            .andExpect(jsonPath("$._links.solicitacoes").doesNotExist())
            .andExpect(jsonPath("$._links.formativas").doesNotExist())
            .andExpect(jsonPath("$._links.estagios").doesNotExist())
            .andExpect(jsonPath("$._links.tccs").doesNotExist())
            .andExpect(jsonPath("$._links.eventos").doesNotExist())

        mockMvc.perform(get("/egressos/me").header("Authorization", "Bearer $token"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.nome").value("Egresso IT"))
            .andExpect(jsonPath("$.curso").value("TADS Egresso IT"))
            .andExpect(jsonPath("$.concluidoEm").value(nullValue()))
            .andExpect(jsonPath("$.kpis.horasFormativasValidadas").value(0))
            .andExpect(jsonPath("$.kpis.certificadosEmitidos").value(1))
            .andExpect(jsonPath("$.kpis.situacaoDiploma").value(nullValue()))
            .andExpect(jsonPath("$.diploma").value(nullValue()))
            .andExpect(jsonPath("$.colacao").value(nullValue()))
            .andExpect(jsonPath("$.certificados[0].hashSha256").value(hashAntes))
            .andExpect(jsonPath("$.certificados[0]._links.reemitir").value(reemissao()))
            .andExpect(jsonPath("$._links.self").value("/egressos/me"))
            .andExpect(jsonPath("$._links.novaSolicitacao").doesNotExist())

        mockMvc.perform(get(reemissao()).header("Authorization", "Bearer $token"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_PDF))
            .andExpect(content().bytes(PDF))
        mockMvc.perform(get(reemissao()).header("Authorization", "Bearer $token"))
            .andExpect(status().isOk)
            .andExpect(content().bytes(PDF))

        org.junit.jupiter.api.Assertions.assertEquals(antes, contarCertificados())
        org.junit.jupiter.api.Assertions.assertEquals(hashAntes, coluna("hash_sha256"))
        org.junit.jupiter.api.Assertions.assertEquals(assinaturaAntes, coluna("assinatura"))
    }

    @Test
    fun outroEgressoEIdInexistenteRecebem404() {
        val token = usuariosIt.login(EMAIL_OUTRO)
        mockMvc.perform(get(reemissao()).header("Authorization", "Bearer $token"))
            .andExpect(status().isNotFound)
        val dono = usuariosIt.login(EMAIL_EGRESSO)
        mockMvc.perform(
            get("/egressos/me/certificados/${UUID.randomUUID()}/reemissao")
                .header("Authorization", "Bearer $dono"),
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun egressoRecebe403NasApisDeAlunoAtivo() {
        val token = usuariosIt.login(EMAIL_EGRESSO)
        val auth = "Bearer $token"
        listOf(
            get("/bff/dashboard/aluno"),
            get("/request-types"),
            get("/formativas"),
            get("/estagios").param("aluno", "me"),
            get("/tccs").param("aluno", "me"),
            get("/events"),
        ).forEach { builder ->
            mockMvc.perform(builder.header("Authorization", auth))
                .andExpect(status().isForbidden)
        }
    }

    @Test
    fun primeiroAcessoContinuaBloqueandoOPainel() {
        val agora = OffsetDateTime.parse("2026-06-01T12:00:00Z")
        if (usuarioRepository.findByEmail(EMAIL_PRIMEIRO).isEmpty) {
            usuarioRepository.save(
                Usuario(
                    Uuids.v7(),
                    Email.of(EMAIL_PRIMEIRO),
                    null,
                    Grr.of(GRR_PRIMEIRO),
                    passwordHasher.hash(ItUsuarioFixture.SENHA),
                    false,
                    null,
                    null,
                    null,
                    true,
                    0,
                    null,
                    listOf("alumni.view_own"),
                    agora,
                    agora,
                ),
            )
        }
        val login = mockMvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"identificador\":\"$EMAIL_PRIMEIRO\",\"senha\":\"${ItUsuarioFixture.SENHA}\"}"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.mustChangePassword").value(true))
            .andReturn()
        val token = br.ufpr.sept.so2.shared.ItJson.text(login.response.contentAsString, "accessToken")
        mockMvc.perform(get("/egressos/me").header("Authorization", "Bearer $token"))
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.detail").value("Defina uma senha forte e aceite a LGPD antes de continuar."))
    }

    private fun reemissao(): String = "/egressos/me/certificados/$certificadoId/reemissao"

    private fun contarCertificados(): Int =
        jdbcTemplate.queryForObject("select count(*) from certificado", Int::class.java) ?: 0

    private fun coluna(nome: String): String =
        jdbcTemplate.queryForObject(
            "select $nome from certificado where id = ?",
            String::class.java,
            certificadoId,
        )!!

    private fun garantirCurso(agora: OffsetDateTime): UUID {
        val existente = cursoRepository.findByCodigo(CODIGO)
        if (existente.isPresent) {
            return existente.get().id
        }
        return cursoRepository.save(
            Curso(Uuids.v7(), "TADS Egresso IT", "EGIT", CODIGO, null, 120, true, agora, agora),
        ).id
    }

    private fun criarAluno(
        nome: String,
        grr: String,
        email: String,
        idCurso: UUID,
        agora: OffsetDateTime,
        situacao: AlunoSituacao,
    ): UUID {
        val existente = alunoRepository.findByGrr(grr)
        if (existente.isPresent) {
            return existente.get().id
        }
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
                situacao,
                true,
                agora,
                agora,
            ),
        ).id
    }

    private fun garantirCertificado(alunoId: UUID, agora: OffsetDateTime): Certificado {
        val existente = certificadoRepository.findByHashSha256(HASH)
        if (existente != null && existente.pertenceAoAluno(alunoId)) {
            return existente
        }
        return certificadoRepository.save(
            Certificado(
                Uuids.v7(),
                alunoId,
                null,
                null,
                CertificadoTipo.EVENTO,
                "Seminário de extensão SEPT",
                8,
                "Egresso IT",
                HASH,
                ASSINATURA,
                PDF,
                agora,
                agora,
                agora,
            ),
        )
    }

    companion object {
        private const val EMAIL_EGRESSO = "it.egresso.dono@ufpr.br"
        private const val EMAIL_OUTRO = "it.egresso.outro@ufpr.br"
        private const val EMAIL_ALUNO = "it.egresso.aluno@ufpr.br"
        private const val EMAIL_COMBO = "it.egresso.combo@ufpr.br"
        private const val EMAIL_MATRICULADO = "it.egresso.matriculado@ufpr.br"
        private const val EMAIL_SEMCAP = "it.egresso.semcap@ufpr.br"
        private const val EMAIL_PRIMEIRO = "it.egresso.primeiro@ufpr.br"
        private const val GRR_EGRESSO = "GRR20249301"
        private const val GRR_OUTRO = "GRR20249302"
        private const val GRR_ALUNO = "GRR20249303"
        private const val GRR_COMBO = "GRR20249304"
        private const val GRR_MATRICULADO = "GRR20249305"
        private const val GRR_SEMCAP = "GRR20249306"
        private const val GRR_PRIMEIRO = "GRR20249307"
        private const val CODIGO = "TADS-EGRESSO-IT"
        private const val ASSINATURA = "assinatura-original-egresso"
        private val PDF = "%PDF-1.4 egresso-it-9.3\n".toByteArray(Charsets.US_ASCII)
        private val HASH = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(PDF))
    }
}
