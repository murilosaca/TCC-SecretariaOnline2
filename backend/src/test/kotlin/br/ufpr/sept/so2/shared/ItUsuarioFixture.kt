package br.ufpr.sept.so2.shared

import br.ufpr.sept.so2.modules.iam.application.ports.PasswordHasher
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.modules.iam.domain.Usuario
import br.ufpr.sept.so2.shared.domain.valueobject.Email
import br.ufpr.sept.so2.shared.domain.valueobject.Grr
import br.ufpr.sept.so2.shared.infrastructure.Uuids
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.OffsetDateTime

class ItUsuarioFixture(
    private val usuarioRepository: UsuarioRepository,
    private val passwordHasher: PasswordHasher,
    private val mockMvc: MockMvc,
) {
    fun criarUsuario(email: String, grr: String, authorities: List<String>): Usuario {
        val agora = OffsetDateTime.now()
        val existente = usuarioRepository.findByEmail(email)
        if (existente.isPresent) {
            val usuario = existente.get()
            if (usuario.substituirAuthorities(authorities, agora)) {
                return usuarioRepository.save(usuario)
            }
            return usuario
        }
        return usuarioRepository.save(
            Usuario(
                id = Uuids.v7(),
                nome = email.substringBefore('@'),
                emailInstitucional = Email.of(email),
                emailPessoal = null,
                grr = Grr.of(grr),
                senhaHash = passwordHasher.hash(SENHA),
                senhaAlterada = true,
                lgpdAceiteEm = agora,
                lgpdAceiteIp = "127.0.0.1",
                lgpdAceiteUserAgent = "it",
                ativo = true,
                falhasConsecutivas = 0,
                bloqueadoAte = null,
                authorities = authorities,
                createdAt = agora,
                updatedAt = agora,
            ),
        )
    }

    fun login(email: String): String {
        val result = mockMvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"identificador\":\"$email\",\"senha\":\"$SENHA\"}"),
        )
            .andExpect(status().isOk)
            .andReturn()
        return ItJson.text(result.response.contentAsString, "accessToken")
    }

    companion object {
        const val SENHA = "TroqueEstaSenha1!"
    }
}
