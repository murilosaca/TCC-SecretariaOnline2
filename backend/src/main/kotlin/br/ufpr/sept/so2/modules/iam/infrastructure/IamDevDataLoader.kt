package br.ufpr.sept.so2.modules.iam.infrastructure

import br.ufpr.sept.so2.modules.comunicacao.application.EmailMascarado
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
        criarSeAusente(
            "caaf.dev@ufpr.br",
            "GRR20240004",
            hash,
            true,
            agora,
            agora,
            authoritiesCaaf(),
        )
        criarSeAusente(
            "secretaria.dev@ufpr.br",
            "GRR20240005",
            hash,
            true,
            agora,
            agora,
            authoritiesSecretaria(),
        )
        criarSeAusente(
            "egresso.dev@ufpr.br",
            "GRR20240006",
            hash,
            true,
            agora,
            agora,
            authoritiesEgresso(),
        )
        criarSeAusente(
            "admin.dev@ufpr.br",
            "GRR20240007",
            hash,
            true,
            agora,
            agora,
            authoritiesAdmin(),
        )
        LOG.info(
            "Usuários de desenvolvimento IAM prontos (aluno.dev / novo.dev / professor.dev / caaf.dev / secretaria.dev / egresso.dev / admin.dev).",
        )
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
            val extras = usuario.authorities.filter { it !in authorities }
            if (usuario.substituirAuthorities(authorities, agora)) {
                usuarioRepository.save(usuario)
                if (extras.isNotEmpty()) {
                    LOG.info(
                        "Seed IAM (profile=dev) revogou authorities extras de {}: {}",
                        EmailMascarado.de(email),
                        extras.joinToString(", "),
                    )
                } else {
                    LOG.info("Seed IAM (profile=dev) alinhou authorities de {}.", EmailMascarado.de(email))
                }
            }
            return
        }
        val usuario = Usuario(
            Uuids.v7(),
            email.substringBefore('@'),
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
                "formative.view_own",
                "formative.confirm_own",
                "certificate.view_own",
                "internship.view_own",
                "tcc.view_own",
            )

        private fun authoritiesProfessor(): List<String> =
            listOf(
                "dashboard.view_own",
                "event.manage",
                "event.host",
                "request.deliberate",
                "internship.review",
                "tcc.review",
                "course.config",
            )

        private fun authoritiesCaaf(): List<String> =
            listOf(
                "dashboard.view_own",
                "formative.review",
            )

        private fun authoritiesSecretaria(): List<String> =
            listOf(
                "course.manage",
                "subject.manage",
                "user.manage_students",
                "calendar.manage",
                "internship.manage",
                "request.view_curso",
                "request.triage",
                "request.deliberate",
            )

        private fun authoritiesEgresso(): List<String> = listOf("alumni.view_own")

        private fun authoritiesAdmin(): List<String> =
            listOf(
                "dashboard.view_own",
                "user.manage_all",
                "user.reset_password",
            )
    }
}
