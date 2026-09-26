package br.ufpr.sept.so2.modules.iam.application

import br.ufpr.sept.so2.modules.iam.application.ports.AuditLogPort
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.modules.iam.domain.IdentificadorLogin
import br.ufpr.sept.so2.modules.iam.domain.Usuario
import br.ufpr.sept.so2.shared.domain.exception.ConflitoEstadoException
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import br.ufpr.sept.so2.shared.domain.valueobject.Email
import br.ufpr.sept.so2.shared.domain.valueobject.Grr
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.UUID

@Service
class AtualizarUsuarioAdminUseCase(
    private val usuarioRepository: UsuarioRepository,
    private val auditLogPort: AuditLogPort,
) {
    @Transactional
    fun execute(
        operadorId: UUID,
        id: UUID,
        nome: String,
        emailInstitucional: String,
        emailPessoal: String?,
        grr: String?,
        ip: String?,
    ): Usuario {
        val usuario = usuarioRepository.findById(id)
            .orElseThrow { RecursoNaoEncontradoException("Usuário não encontrado.") }
        val emailInst = Email.of(emailInstitucional)
        val outroEmail = usuarioRepository.findByEmail(emailInst.value)
        if (outroEmail.isPresent && outroEmail.get().id != id) {
            throw ConflitoEstadoException("Já existe usuário com este e-mail institucional.")
        }
        val emailPes = emailPessoal?.trim()?.takeIf { it.isNotEmpty() }?.let { Email.of(it) }
        if (emailPes != null) {
            val outroPes = usuarioRepository.findByEmail(emailPes.value)
            if (outroPes.isPresent && outroPes.get().id != id) {
                throw ConflitoEstadoException("Já existe usuário com este e-mail pessoal.")
            }
        }
        val grrVo = grr?.trim()?.takeIf { it.isNotEmpty() }?.let { Grr.of(it) }
        if (grrVo != null) {
            val idGrr = IdentificadorLogin.tryParse(grrVo.value)
            if (idGrr != null) {
                val outroGrr = usuarioRepository.findByIdentificador(idGrr)
                if (outroGrr.isPresent && outroGrr.get().id != id) {
                    throw ConflitoEstadoException("Já existe usuário com este GRR.")
                }
            }
        }
        val agora = OffsetDateTime.now()
        usuario.atualizarPerfil(nome, emailInst, emailPes, grrVo, agora)
        val persistido = usuarioRepository.save(usuario)
        auditLogPort.append(
            "iam.user_updated",
            operadorId,
            """{"targetUserId":"$id"}""",
            ip,
        )
        return persistido
    }
}
