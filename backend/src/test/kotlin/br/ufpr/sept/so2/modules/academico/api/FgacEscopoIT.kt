package br.ufpr.sept.so2.modules.academico.api

import br.ufpr.sept.so2.modules.academico.application.ports.AlunoRepository
import br.ufpr.sept.so2.modules.academico.application.ports.CursoRepository
import br.ufpr.sept.so2.modules.academico.application.ports.CursoSecretarioRepository
import br.ufpr.sept.so2.modules.academico.application.ports.DisciplinaRepository
import br.ufpr.sept.so2.modules.academico.domain.Aluno
import br.ufpr.sept.so2.modules.academico.domain.AlunoSituacao
import br.ufpr.sept.so2.modules.academico.domain.Curso
import br.ufpr.sept.so2.modules.academico.domain.Disciplina
import br.ufpr.sept.so2.modules.iam.application.ports.PasswordHasher
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.modules.iam.domain.Usuario
import br.ufpr.sept.so2.shared.domain.valueobject.Email
import br.ufpr.sept.so2.shared.domain.valueobject.Grr
import br.ufpr.sept.so2.shared.ItJson
import br.ufpr.sept.so2.shared.infrastructure.Uuids
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.OffsetDateTime
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FgacEscopoIT {
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
    private lateinit var disciplinaRepository: DisciplinaRepository

    private lateinit var tokenSec: String
    private lateinit var alunoForaId: UUID
    private lateinit var disciplinaForaId: UUID

    @BeforeEach
    fun seed() {
        val agora = OffsetDateTime.now()
        val sec = criarUsuario(EMAIL_SEC, "GRR20247121", AUTHORITIES_SEC, agora)
        val outro = criarUsuario(EMAIL_OUTRO, "GRR20247122", AUTHORITIES_SEC, agora)
        val cursoA = curso("Curso A Escopo", "ESCA", "ESCA-FGAC", agora)
        val cursoB = curso("Curso B Escopo", "ESCB", "ESCB-FGAC", agora)
        cursoSecretarioRepository.replaceAll(cursoA.id, listOf(sec.id))
        cursoSecretarioRepository.replaceAll(cursoB.id, listOf(outro.id))
        alunoForaId = aluno("Aluno Fora", "GRR20247129", "it.fgac.aluno.fora@ufpr.br", cursoB.id, agora).id
        disciplinaForaId = disciplina(cursoB.id, "DISC-FORA", agora).id
        tokenSec = login(EMAIL_SEC)
    }

    @Test
    fun putAlunoForaDoEscopoRetorna404() {
        mockMvc.perform(
            put("/academico/alunos/$alunoForaId")
                .header("Authorization", "Bearer $tokenSec")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "nome": "Tentativa",
                      "grr": "GRR20247129",
                      "emailInstitucional": "it.fgac.aluno.fora@ufpr.br",
                      "idCurso": "${alunoRepository.findById(alunoForaId).get().idCurso}",
                      "situacao": "MATRICULADO"
                    }
                    """.trimIndent(),
                ),
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.type").value(org.hamcrest.Matchers.containsString("not-found")))
    }

    @Test
    fun adicionarSeAusentePreservaSecretarioManual() {
        val sec = usuarioRepository.findByEmail(EMAIL_SEC).get()
        val outro = usuarioRepository.findByEmail(EMAIL_OUTRO).get()
        val curso = cursoRepository.findByCodigo("ESCA-FGAC").get()
        cursoSecretarioRepository.replaceAll(curso.id, listOf(outro.id))
        cursoSecretarioRepository.adicionarSeAusente(curso.id, sec.id)
        val ids = cursoSecretarioRepository.findUsuarioIdsByCursoId(curso.id)
        org.junit.jupiter.api.Assertions.assertTrue(ids.contains(outro.id))
        org.junit.jupiter.api.Assertions.assertTrue(ids.contains(sec.id))
        cursoSecretarioRepository.adicionarSeAusente(curso.id, sec.id)
        org.junit.jupiter.api.Assertions.assertEquals(2, cursoSecretarioRepository.findUsuarioIdsByCursoId(curso.id).size)
        cursoSecretarioRepository.replaceAll(curso.id, listOf(sec.id))
    }

    @Test
    fun putDisciplinaForaDoEscopoRetorna404() {
        mockMvc.perform(
            put("/academico/disciplinas/$disciplinaForaId")
                .header("Authorization", "Bearer $tokenSec")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "idCurso": "${disciplinaRepository.findById(disciplinaForaId)!!.idCurso}",
                      "codigo": "DISC-FORA",
                      "nome": "Tentativa",
                      "periodo": 1,
                      "cargaHorariaTotal": 60,
                      "creditos": 4
                    }
                    """.trimIndent(),
                ),
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.type").value(org.hamcrest.Matchers.containsString("not-found")))
    }

    private fun curso(nome: String, sigla: String, codigo: String, agora: OffsetDateTime): Curso {
        val existente = cursoRepository.findByCodigo(codigo)
        if (existente.isPresent) {
            return existente.get()
        }
        return cursoRepository.save(Curso(Uuids.v7(), nome, sigla, codigo, null, 120, true, agora, agora))
    }

    private fun aluno(nome: String, grr: String, email: String, idCurso: UUID, agora: OffsetDateTime): Aluno {
        val existente = alunoRepository.findByGrr(grr)
        if (existente.isPresent) {
            return existente.get()
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
                AlunoSituacao.MATRICULADO,
                true,
                agora,
                agora,
            ),
        )
    }

    private fun disciplina(idCurso: UUID, codigo: String, agora: OffsetDateTime): Disciplina {
        val existente = disciplinaRepository.findAll(idCurso, setOf(idCurso), org.springframework.data.domain.PageRequest.of(0, 20))
            .content.firstOrNull { it.codigo == codigo }
        if (existente != null) {
            return existente
        }
        return disciplinaRepository.save(
            Disciplina(Uuids.v7(), idCurso, codigo, "Disciplina fora", 1, 60, 4, true, agora, agora),
        )
    }

    private fun criarUsuario(email: String, grr: String, authorities: List<String>, agora: OffsetDateTime): Usuario {
        val existente = usuarioRepository.findByEmail(email)
        if (existente.isPresent) {
            return existente.get()
        }
        return usuarioRepository.save(
            Usuario(
                Uuids.v7(),
                Email.of(email),
                null,
                Grr.of(grr),
                passwordHasher.hash(SENHA),
                true,
                agora,
                "127.0.0.1",
                "it",
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
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"identificador\":\"$email\",\"senha\":\"$SENHA\"}"),
        )
            .andExpect(status().isOk)
            .andReturn()
        return ItJson.text(result.response.contentAsString, "accessToken")
    }

    companion object {
        private const val SENHA = "TroqueEstaSenha1!"
        private const val EMAIL_SEC = "it.fgac.escopo.sec@ufpr.br"
        private const val EMAIL_OUTRO = "it.fgac.escopo.outro@ufpr.br"
        private val AUTHORITIES_SEC = listOf(
            "course.manage",
            "subject.manage",
            "user.manage_students",
            "calendar.manage",
            "request.view_curso",
            "request.triage",
            "request.deliberate",
        )
    }
}
