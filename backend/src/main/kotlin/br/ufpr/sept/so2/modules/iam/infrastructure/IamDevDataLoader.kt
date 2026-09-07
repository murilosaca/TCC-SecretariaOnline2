package br.ufpr.sept.so2.modules.iam.infrastructure

import br.ufpr.sept.so2.modules.iam.application.ports.PasswordHasher
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.modules.iam.domain.Usuario
import br.ufpr.sept.so2.shared.domain.valueobject.Email
import br.ufpr.sept.so2.shared.domain.valueobject.Grr
import br.ufpr.sept.so2.shared.infrastructure.Uuids
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Profile
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.time.OffsetDateTime

@Component
@Profile("dev")
@Order(10)
class IamDevDataLoader(
    private val properties: IamProperties,
    private val usuarioRepository: UsuarioRepository,
    private val passwordHasher: PasswordHasher,
) : ApplicationRunner {
    override fun run(args: ApplicationArguments) {
        if (!properties.seed.enabled) {
            return
        }
        var senha = properties.seed.password
        if (senha.isBlank()) {
            senha = "TroqueEstaSenha1!"
            LOG.warn("IAM_DEV_SEED_PASSWORD ausente; usando senha local de bootstrap (apenas profile=dev).")
        }
        val hash = passwordHasher.hash(senha)
        val agora = OffsetDateTime.now()
        criarSeAusente(
            "aluno.dev@ufpr.br",
            "GRR20240001",
            hash,
            true,
            agora,
            agora,
            authoritiesAluno(),
        )
        criarSeAusente(
            "novo.dev@ufpr.br",
            "GRR20240002",
            hash,
            false,
            null,
            agora,
            authoritiesAluno(),
        )
        criarSeAusente(
            "professor.dev@ufpr.br",
            "GRR20240003",
            hash,
            true,
            agora,
            agora,
            authoritiesProfessor(),
        )
        LOG.info("Usuários de desenvolvimento IAM prontos (aluno.dev / novo.dev / professor.dev).")
    }

    private fun criarSeAusente(
        email: String,
        grr: String,
        hash: String,
        senhaAlterada: Boolean,
        lgpd: OffsetDateTime?,
        agora: OffsetDateTime,
        authorities: List<String>,
    ) {
        val existente = usuarioRepository.findByEmail(email)
        if (existente.isPresent) {
            val usuario = existente.get()
            if (usuario.concederAuthorities(authorities, agora)) {
                usuarioRepository.save(usuario)
            }
            return
        }
        val usuario = Usuario(
            Uuids.v7(),
            Email.of(email),
            null,
            Grr.of(grr),
            hash,
            senhaAlterada,
            lgpd,
            if (lgpd == null) null else "127.0.0.1",
            if (lgpd == null) null else "so2-dev-seed",
            true,
            0,
            null,
            authorities,
            agora,
            agora,
        )
        usuarioRepository.save(usuario)
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(IamDevDataLoader::class.java)

        private fun authoritiesAluno(): List<String> =
            listOf(
                "dashboard.view_own",
                "request.view_own",
                "request.open",
                "attendance.view_open",
                "attendance.check_in",
            )

        private fun authoritiesProfessor(): List<String> =
            listOf(
                "dashboard.view_own",
                "event.manage",
                "event.host",
            )
    }
}
