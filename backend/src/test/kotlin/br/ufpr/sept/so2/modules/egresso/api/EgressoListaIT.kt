package br.ufpr.sept.so2.modules.egresso.api

import br.ufpr.sept.so2.modules.academico.application.ports.AlunoRepository
import br.ufpr.sept.so2.modules.academico.application.ports.CursoRepository
import br.ufpr.sept.so2.modules.academico.application.ports.CursoSecretarioRepository
import br.ufpr.sept.so2.modules.academico.domain.Aluno
import br.ufpr.sept.so2.modules.academico.domain.AlunoSituacao
import br.ufpr.sept.so2.modules.academico.domain.Curso
import br.ufpr.sept.so2.modules.diplomas.application.ports.DiplomaRepository
import br.ufpr.sept.so2.modules.diplomas.domain.Diploma
import br.ufpr.sept.so2.modules.iam.application.ports.PasswordHasher
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.shared.ItUsuarioFixture
import br.ufpr.sept.so2.shared.domain.valueobject.Email
import br.ufpr.sept.so2.shared.domain.valueobject.Grr
import br.ufpr.sept.so2.shared.infrastructure.Uuids
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.OffsetDateTime
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EgressoListaIT {
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
    private lateinit var diplomaRepository: DiplomaRepository

    private val usuariosIt
        get() = ItUsuarioFixture(usuarioRepository, passwordHasher, mockMvc)

    private lateinit var cursoId: UUID
    private lateinit var cursoForaId: UUID

    @BeforeEach
    fun seed() {
        val agora = OffsetDateTime.parse("2026-07-15T12:00:00Z")
        cursoId = garantirCurso(CODIGO, "TADS Lista Egresso", "LSEG", agora)
        cursoForaId = garantirCurso(CODIGO_FORA, "Outro Lista Egresso", "OLEG", agora)
        val secretaria = usuariosIt.criarUsuario(EMAIL_SEC, GRR_SEC, listOf("alumni.list"))
        cursoSecretarioRepository.adicionarSeAusente(cursoId, secretaria.id)
        val outra = usuariosIt.criarUsuario(EMAIL_OUTRA, GRR_OUTRA, listOf("alumni.list"))
        cursoSecretarioRepository.adicionarSeAusente(cursoForaId, outra.id)
        usuariosIt.criarUsuario(EMAIL_SEM_CURSO, GRR_SEM_CURSO, listOf("alumni.list"))
        usuariosIt.criarUsuario(EMAIL_SEM_CAP, GRR_SEM_CAP, listOf("course.manage"))
        val alunoId = garantirAluno("Ana Colada", GRR_EGRESSO, EMAIL_EGRESSO, cursoId, agora)
        if (diplomaRepository.findByAluno(alunoId) == null) {
            diplomaRepository.save(
                Diploma.registrar(
                    Uuids.v7(),
                    alunoId,
                    cursoId,
                    null,
                    "UFPR-LSEG-2026-0001",
                    agora,
                    "1",
                    "10",
                    "TADS 2026/1",
                    agora,
                ),
            )
        }
    }

    @Test
    fun anonimoRecebe401() {
        mockMvc.perform(get("/egressos")).andExpect(status().isUnauthorized)
    }

    @Test
    fun semCapabilityRecebe403() {
        val token = usuariosIt.login(EMAIL_SEM_CAP)
        mockMvc.perform(get("/egressos").header("Authorization", "Bearer $token"))
            .andExpect(status().isForbidden)
    }

    @Test
    fun cursoForaDoEscopoOuSemCursoRecebem403() {
        val token = usuariosIt.login(EMAIL_SEC)
        mockMvc.perform(
            get("/egressos")
                .param("cursoId", cursoForaId.toString())
                .header("Authorization", "Bearer $token"),
        )
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.detail").value("Curso fora do escopo da sua secretaria."))

        val semCurso = usuariosIt.login(EMAIL_SEM_CURSO)
        mockMvc.perform(get("/egressos").header("Authorization", "Bearer $semCurso"))
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.detail").value("Nenhum curso vinculado à sua secretaria foi encontrado."))
    }

    @Test
    fun listaReadOnlySemNovoECsvSincrono() {
        val token = usuariosIt.login(EMAIL_SEC)
        mockMvc.perform(get("/egressos").header("Authorization", "Bearer $token"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[0].nome").value("Ana Colada"))
            .andExpect(jsonPath("$.content[0].anoColacao").value(2026))
            .andExpect(jsonPath("$.content[0].situacaoDiploma").value("PENDENTE"))
            .andExpect(jsonPath("$._links.novo").doesNotExist())
            .andExpect(jsonPath("$.content[0]._links['confirm-delivery']").doesNotExist())

        mockMvc.perform(
            get("/egressos")
                .param("format", "csv")
                .param("ano", "2026")
                .header("Authorization", "Bearer $token"),
        )
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", containsString("text/csv")))
            .andExpect(header().string("Content-Disposition", containsString("egressos-")))
            .andExpect(content().string(containsString("Ana Colada")))
            .andExpect(content().string(containsString("PENDENTE")))
    }

    private fun garantirCurso(codigo: String, nome: String, sigla: String, agora: OffsetDateTime): UUID {
        val existente = cursoRepository.findByCodigo(codigo)
        if (existente.isPresent) {
            return existente.get().id
        }
        return cursoRepository.save(Curso(Uuids.v7(), nome, sigla, codigo, null, 120, true, agora, agora)).id
    }

    private fun garantirAluno(
        nome: String,
        grr: String,
        email: String,
        idCurso: UUID,
        agora: OffsetDateTime,
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
                AlunoSituacao.EGRESSO,
                true,
                agora,
                agora,
            ),
        ).id
    }

    companion object {
        private const val EMAIL_SEC = "it.eglista.sec@ufpr.br"
        private const val EMAIL_OUTRA = "it.eglista.outra@ufpr.br"
        private const val EMAIL_SEM_CURSO = "it.eglista.semcurso@ufpr.br"
        private const val EMAIL_SEM_CAP = "it.eglista.semcap@ufpr.br"
        private const val EMAIL_EGRESSO = "it.eglista.ana@ufpr.br"
        private const val GRR_SEC = "GRR20249601"
        private const val GRR_OUTRA = "GRR20249602"
        private const val GRR_SEM_CURSO = "GRR20249603"
        private const val GRR_SEM_CAP = "GRR20249604"
        private const val GRR_EGRESSO = "GRR20249605"
        private const val CODIGO = "LSEG-IT"
        private const val CODIGO_FORA = "OLEG-IT"
    }
}
