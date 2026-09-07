package br.ufpr.sept.so2.modules.iam.application

import br.ufpr.sept.so2.modules.iam.application.ports.AuditLogPort
import br.ufpr.sept.so2.modules.iam.application.ports.OutboxPort
import br.ufpr.sept.so2.modules.iam.application.ports.PasswordHasher
import br.ufpr.sept.so2.modules.iam.application.ports.SenhaHistoricoRepository
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.modules.iam.domain.PoliticaSenha
import br.ufpr.sept.so2.modules.iam.domain.SenhaReutilizadaException
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.UUID

@Service
class PrimeiroAcessoUseCase(
    private val usuarioRepository: UsuarioRepository,
    private val senhaHistoricoRepository: SenhaHistoricoRepository,
    private val passwordHasher: PasswordHasher,
    private val auditLogPort: AuditLogPort,
    private val outboxPort: OutboxPort,
) {
    @Transactional
    fun execute(usuarioId: UUID, novaSenha: String, aceiteTermos: Boolean, ip: String?, userAgent: String?) {
        if (!aceiteTermos) {
            throw DadoInvalidoException("É obrigatório aceitar a política de privacidade (LGPD).")
        }
        PoliticaSenha.validar(novaSenha)
        val usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow { RecursoNaoEncontradoException("Usuário não encontrado.") }
        if (passwordHasher.matches(novaSenha, usuario.senhaHash)) {
            throw SenhaReutilizadaException.temporaria()
        }
        val agora = OffsetDateTime.now()
        val novoHash = passwordHasher.hash(novaSenha)
        senhaHistoricoRepository.append(usuario.id, usuario.senhaHash)
        usuario.completarPrimeiroAcesso(novoHash, agora, ip, userAgent)
        usuarioRepository.save(usuario)
        auditLogPort.append("iam.first_access_completed", usuario.id, "lgpd=true", ip)
        outboxPort.enqueue("iam.first_access_completed", "{\"usuarioId\":\"${usuario.id}\"}")
    }
}
