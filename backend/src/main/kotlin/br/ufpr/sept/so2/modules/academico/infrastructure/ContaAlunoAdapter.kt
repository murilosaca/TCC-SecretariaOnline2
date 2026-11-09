package br.ufpr.sept.so2.modules.academico.infrastructure

import br.ufpr.sept.so2.modules.academico.application.ports.ContaAlunoPort
import br.ufpr.sept.so2.modules.academico.application.ports.ContaProvisionada
import br.ufpr.sept.so2.modules.academico.application.ports.ProvisionarContaAluno
import br.ufpr.sept.so2.modules.academico.domain.ContaExistente
import br.ufpr.sept.so2.modules.academico.domain.VinculoContaAluno
import br.ufpr.sept.so2.modules.iam.application.CatalogoPapeis
import br.ufpr.sept.so2.modules.iam.application.ResetSenhaAdminUseCase
import br.ufpr.sept.so2.modules.iam.application.ports.OutboxPort
import br.ufpr.sept.so2.modules.iam.application.ports.PasswordHasher
import br.ufpr.sept.so2.modules.iam.application.ports.PerfilAcessoPort
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.modules.iam.domain.IdentificadorLogin
import br.ufpr.sept.so2.modules.iam.domain.SenhaTemporaria
import br.ufpr.sept.so2.modules.iam.domain.Usuario
import br.ufpr.sept.so2.shared.domain.exception.ConflitoEstadoException
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import br.ufpr.sept.so2.shared.domain.valueobject.Email
import br.ufpr.sept.so2.shared.domain.valueobject.Grr
import br.ufpr.sept.so2.shared.infrastructure.Uuids
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component
import java.time.OffsetDateTime

/**
 * F5.6-D02. A senha temporária só existe para o aluno fazer o primeiro acesso
 * (senhaAlterada=false e LGPD). Ela viaja no outbox `aluno.boas-vindas` porque
 * o e-mail é assíncrono e o critério 2 pede a senha nesse e-mail. Não volta no
 * HTTP do cadastro, não entra no audit_log e o handler não a loga. O resumo de
 * GET /admin/outbox redige a chave senhaTemporaria.
 *
 * Conta já existente com o mesmo GRR e o mesmo e-mail institucional é vinculada,
 * sem nova senha e sem outro e-mail. Par incompatível devolve 409.
 *
 * O reset (D04) não repete a senha em claro: ver ResetSenhaAdminUseCase.
 */
@Component
class ContaAlunoAdapter(
    private val usuarioRepository: UsuarioRepository,
    private val passwordHasher: PasswordHasher,
    private val outboxPort: OutboxPort,
    private val perfilAcessoPort: PerfilAcessoPort,
    private val resetSenhaAdminUseCase: ResetSenhaAdminUseCase,
    private val objectMapper: ObjectMapper,
) : ContaAlunoPort {
    override fun provisionar(comando: ProvisionarContaAluno): ContaProvisionada {
        val grr = Grr.of(comando.grr).value
        val email = Email.of(comando.emailInstitucional).value
        val existente = VinculoContaAluno.resolver(buscarPorGrr(grr), buscarPorEmail(email), grr, email)
        if (existente != null) {
            return ContaProvisionada(existente, vinculada = true)
        }
        return ContaProvisionada(criar(comando, grr, email), vinculada = false)
    }

    override fun localizar(grr: String, emailInstitucional: String): java.util.UUID? {
        val grrNormalizado = Grr.of(grr).value
        val email = Email.of(emailInstitucional).value
        return VinculoContaAluno.resolver(buscarPorGrr(grrNormalizado), buscarPorEmail(email), grrNormalizado, email)
    }

    override fun resetarSenha(operadorId: java.util.UUID, usuarioId: java.util.UUID, ip: String?) {
        resetSenhaAdminUseCase.executeParaSecretaria(operadorId, usuarioId, ip)
    }

    private fun criar(comando: ProvisionarContaAluno, grr: String, email: String): java.util.UUID {
        val agora = OffsetDateTime.now()
        val senhaTemporaria = SenhaTemporaria.gerar()
        val pessoal = comando.emailPessoal?.trim()?.takeIf { it.isNotEmpty() }?.let { Email.of(it) }
        if (pessoal != null && usuarioRepository.findByEmail(pessoal.value).isPresent) {
            throw ConflitoEstadoException("Já existe usuário com este e-mail pessoal.")
        }
        val usuario = Usuario(
            id = Uuids.v7(),
            nome = comando.nome.trim(),
            emailInstitucional = Email.of(email),
            emailPessoal = pessoal,
            grr = Grr.of(grr),
            senhaHash = passwordHasher.hash(senhaTemporaria),
            senhaAlterada = false,
            lgpdAceiteEm = null,
            lgpdAceiteIp = null,
            lgpdAceiteUserAgent = null,
            ativo = true,
            falhasConsecutivas = 0,
            bloqueadoAte = null,
            authorities = CatalogoPapeis.aluno,
            createdAt = agora,
            updatedAt = agora,
        )
        val persistido = usuarioRepository.save(usuario)
        perfilAcessoPort.garantirVinculo(persistido.id, listOf(CatalogoPapeis.ALUNO))
        outboxPort.enqueue("aluno.boas-vindas", boasVindas(persistido.id, comando.nome.trim(), senhaTemporaria))
        return persistido.id
    }

    private fun buscarPorGrr(grr: String): ContaExistente? {
        val id = IdentificadorLogin.tryParse(grr) ?: return null
        return usuarioRepository.findByIdentificador(id).map { conta(it) }.orElse(null)
    }

    private fun buscarPorEmail(email: String): ContaExistente? =
        usuarioRepository.findByEmail(email).map { conta(it) }.orElse(null)

    private fun conta(usuario: Usuario) = ContaExistente(
        usuario.id,
        usuario.grr?.value,
        usuario.emailInstitucional.value,
        usuario.ativo,
    )

    private fun boasVindas(usuarioId: java.util.UUID, nome: String, senhaTemporaria: String): String {
        try {
            return objectMapper.writeValueAsString(
                mapOf(
                    "usuarioId" to usuarioId.toString(),
                    "nome" to nome,
                    "senhaTemporaria" to senhaTemporaria,
                ),
            )
        } catch (_: JsonProcessingException) {
            throw DadoInvalidoException("Não foi possível enfileirar o e-mail de boas-vindas.")
        }
    }
}
