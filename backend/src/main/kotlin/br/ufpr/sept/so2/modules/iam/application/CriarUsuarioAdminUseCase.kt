package br.ufpr.sept.so2.modules.iam.application

import br.ufpr.sept.so2.modules.iam.application.ports.AuditLogPort
import br.ufpr.sept.so2.modules.iam.application.ports.OutboxPort
import br.ufpr.sept.so2.modules.iam.application.ports.PasswordHasher
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.modules.iam.domain.SenhaTemporaria
import br.ufpr.sept.so2.modules.iam.domain.Usuario
import br.ufpr.sept.so2.shared.domain.exception.ConflitoEstadoException
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import br.ufpr.sept.so2.shared.domain.valueobject.Email
import br.ufpr.sept.so2.shared.domain.valueobject.Grr
import br.ufpr.sept.so2.shared.infrastructure.Uuids
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.UUID

@Service
class CriarUsuarioAdminUseCase(
    private val usuarioRepository: UsuarioRepository,
    private val passwordHasher: PasswordHasher,
    private val outboxPort: OutboxPort,
    private val auditLogPort: AuditLogPort,
    private val objectMapper: ObjectMapper,
) {
    @Transactional
    fun execute(
        operadorId: UUID,
        nome: String,
        emailInstitucional: String,
        emailPessoal: String?,
        grr: String?,
        ip: String?,
    ): Usuario {
        val emailInst = Email.of(emailInstitucional)
        if (usuarioRepository.findByEmail(emailInst.value).isPresent) {
            throw ConflitoEstadoException("Já existe usuário com este e-mail institucional.")
        }
        val emailPes = emailPessoal?.trim()?.takeIf { it.isNotEmpty() }?.let { Email.of(it) }
        if (emailPes != null && usuarioRepository.findByEmail(emailPes.value).isPresent) {
            throw ConflitoEstadoException("Já existe usuário com este e-mail pessoal.")
        }
        val grrVo = grr?.trim()?.takeIf { it.isNotEmpty() }?.let { Grr.of(it) }
        if (grrVo != null) {
            val idGrr = br.ufpr.sept.so2.modules.iam.domain.IdentificadorLogin.tryParse(grrVo.value)
            if (idGrr != null && usuarioRepository.findByIdentificador(idGrr).isPresent) {
                throw ConflitoEstadoException("Já existe usuário com este GRR.")
            }
        }
        val nomeLimpo = nome.trim()
        if (nomeLimpo.isEmpty() || nomeLimpo.length > 200) {
            throw DadoInvalidoException("Nome é obrigatório (até 200 caracteres).")
        }

        val agora = OffsetDateTime.now()
        val senhaTemporaria = SenhaTemporaria.gerar()
        val usuario = Usuario(
            id = Uuids.v7(),
            nome = nomeLimpo,
            emailInstitucional = emailInst,
            emailPessoal = emailPes,
            grr = grrVo,
            senhaHash = passwordHasher.hash(senhaTemporaria),
            senhaAlterada = false,
            lgpdAceiteEm = null,
            lgpdAceiteIp = null,
            lgpdAceiteUserAgent = null,
            ativo = true,
            falhasConsecutivas = 0,
            bloqueadoAte = null,
            authorities = emptyList(),
            createdAt = agora,
            updatedAt = agora,
        )
        val persistido = usuarioRepository.save(usuario)
        outboxPort.enqueue("iam.user_created", toJson(persistido))
        auditLogPort.append(
            "iam.user_created",
            operadorId,
            """{"targetUserId":"${persistido.id}","email":"${mascarar(emailInst.value)}"}""",
            ip,
        )
        return persistido
    }

    private fun toJson(usuario: Usuario): String {
        try {
            return objectMapper.writeValueAsString(
                mapOf(
                    "usuarioId" to usuario.id.toString(),
                    "emailInstitucional" to usuario.emailInstitucional.value,
                ),
            )
        } catch (_: JsonProcessingException) {
            throw DadoInvalidoException("Não foi possível enfileirar o acesso inicial.")
        }
    }

    private fun mascarar(email: String): String {
        val at = email.indexOf('@')
        if (at <= 1) return "***"
        return email[0] + "***" + email.substring(at)
    }
}
