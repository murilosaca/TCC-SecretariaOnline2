package br.ufpr.sept.so2.modules.iam.application

import br.ufpr.sept.so2.modules.arquivos.application.PresignDownloadUseCase
import br.ufpr.sept.so2.modules.arquivos.application.ports.ObjectStoragePort
import br.ufpr.sept.so2.modules.arquivos.domain.StorageKey
import br.ufpr.sept.so2.modules.iam.application.ports.AuditLogPort
import br.ufpr.sept.so2.modules.iam.application.ports.PasswordHasher
import br.ufpr.sept.so2.modules.iam.application.ports.RefreshTokenRepository
import br.ufpr.sept.so2.modules.iam.application.ports.SenhaHistoricoRepository
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.modules.iam.domain.FotoPerfilRegras
import br.ufpr.sept.so2.modules.iam.domain.PoliticaSenha
import br.ufpr.sept.so2.modules.iam.domain.SenhaAtualIncorretaException
import br.ufpr.sept.so2.modules.iam.domain.SenhaReutilizadaException
import br.ufpr.sept.so2.modules.iam.domain.Usuario
import br.ufpr.sept.so2.shared.domain.exception.ConflitoEstadoException
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import br.ufpr.sept.so2.shared.domain.valueobject.Email
import br.ufpr.sept.so2.shared.infrastructure.Uuids
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.UUID

data class PerfilVisao(
    val usuario: Usuario,
    val fotoUrl: String?,
)

@Service
class ConsultarPerfilUseCase(
    private val usuarioRepository: UsuarioRepository,
    private val objectStoragePort: ObjectStoragePort,
    private val presignDownloadUseCase: PresignDownloadUseCase,
) {
    @Transactional(readOnly = true)
    fun execute(usuarioId: UUID): PerfilVisao {
        val usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow { RecursoNaoEncontradoException("Usuário não encontrado.") }
        return PerfilVisao(usuario, urlFoto(usuario.fotoStorageKey))
    }

    private fun urlFoto(storageKey: String?): String? {
        if (storageKey.isNullOrBlank() || !objectStoragePort.exists(storageKey)) {
            return null
        }
        return presignDownloadUseCase.execute(storageKey, "foto")
    }
}

@Service
class AtualizarPerfilUseCase(
    private val usuarioRepository: UsuarioRepository,
    private val auditLogPort: AuditLogPort,
) {
    @Transactional
    fun execute(usuarioId: UUID, patch: PerfilPatch, ip: String?) {
        val usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow { RecursoNaoEncontradoException("Usuário não encontrado.") }
        if (!temCampo(patch)) {
            return
        }
        val agora = OffsetDateTime.now()
        if (patch.nomeSocial.presente) {
            usuario.definirNomeSocial(patch.nomeSocial.valor, agora)
        }
        if (patch.telefone.presente) {
            usuario.definirTelefone(patch.telefone.valor, agora)
        }
        if (patch.emailPessoal.presente) {
            usuario.definirEmailPessoal(emailPessoal(usuario, patch.emailPessoal.valor), agora)
        }
        if (patch.identidadeGenero.presente) {
            usuario.definirIdentidadeGenero(patch.identidadeGenero.valor, agora)
        }
        usuarioRepository.save(usuario)
        auditLogPort.append("iam.profile_updated", usuario.id, "ok", ip)
    }

    private fun temCampo(patch: PerfilPatch): Boolean =
        patch.nomeSocial.presente ||
            patch.telefone.presente ||
            patch.emailPessoal.presente ||
            patch.identidadeGenero.presente

    private fun emailPessoal(usuario: Usuario, bruto: String?): Email? {
        val texto = bruto?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        val email = try {
            Email.of(texto)
        } catch (_: IllegalArgumentException) {
            throw DadoInvalidoException("E-mail pessoal inválido.")
        }
        val outro = usuarioRepository.findByEmail(email.value).orElse(null)
        if (outro != null && outro.id != usuario.id) {
            throw ConflitoEstadoException("Já existe usuário com este e-mail pessoal.")
        }
        return email
    }
}

@Service
class EnviarFotoPerfilUseCase(
    private val usuarioRepository: UsuarioRepository,
    private val objectStoragePort: ObjectStoragePort,
    private val auditLogPort: AuditLogPort,
) {
    @Transactional
    fun execute(usuarioId: UUID, bytes: ByteArray, ip: String?) {
        val imagem = FotoPerfilRegras.validar(bytes)
        val usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow { RecursoNaoEncontradoException("Usuário não encontrado.") }
        val agora = OffsetDateTime.now()
        val key = StorageKey("perfis/$usuarioId/foto-${Uuids.v7()}.${imagem.extensao}").value
        objectStoragePort.putObject(key, imagem.contentType, bytes)
        usuario.definirFoto(key, agora)
        usuarioRepository.save(usuario)
        auditLogPort.append("iam.profile_photo_updated", usuario.id, "ok", ip)
    }
}

@Service
class TrocarSenhaUseCase(
    private val usuarioRepository: UsuarioRepository,
    private val passwordHasher: PasswordHasher,
    private val senhaHistoricoRepository: SenhaHistoricoRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val auditLogPort: AuditLogPort,
) {
    @Transactional
    fun execute(usuarioId: UUID, senhaAtual: String, novaSenha: String, sessaoAtualId: UUID?, ip: String?) {
        val usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow { RecursoNaoEncontradoException("Usuário não encontrado.") }
        if (!passwordHasher.matches(senhaAtual, usuario.senhaHash)) {
            throw SenhaAtualIncorretaException()
        }
        PoliticaSenha.validar(novaSenha)
        if (passwordHasher.matches(novaSenha, usuario.senhaHash)) {
            throw SenhaReutilizadaException("A nova senha deve ser diferente da senha atual.")
        }
        val atualId = sessaoAtualId
            ?: throw DadoInvalidoException("Não foi possível identificar a sessão atual.")
        val agora = OffsetDateTime.now()
        senhaHistoricoRepository.append(usuario.id, usuario.senhaHash)
        usuario.redefinirSenha(passwordHasher.hash(novaSenha), agora)
        usuarioRepository.save(usuario)
        refreshTokenRepository.revokeOthers(usuario.id, atualId)
        auditLogPort.append("iam.password_changed", usuario.id, "ok", ip)
    }
}
