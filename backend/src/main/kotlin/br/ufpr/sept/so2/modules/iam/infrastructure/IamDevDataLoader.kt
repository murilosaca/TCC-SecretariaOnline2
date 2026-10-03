package br.ufpr.sept.so2.modules.iam.infrastructure

import br.ufpr.sept.so2.modules.comunicacao.application.EmailMascarado
import br.ufpr.sept.so2.modules.iam.application.CatalogoPapeis
import br.ufpr.sept.so2.modules.iam.application.ports.PerfilAcessoPort
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
    private val perfilAcessoPort: PerfilAcessoPort,
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
        semear("aluno.dev@ufpr.br", "GRR20240001", hash, true, agora, agora, CatalogoPapeis.aluno, listOf(CatalogoPapeis.ALUNO))
        semear("novo.dev@ufpr.br", "GRR20240002", hash, false, null, agora, CatalogoPapeis.aluno, listOf(CatalogoPapeis.ALUNO))
        semear(
            "professor.dev@ufpr.br",
            "GRR20240003",
            hash,
            true,
            agora,
            agora,
            CatalogoPapeis.uniao(CatalogoPapeis.professor, CatalogoPapeis.coordenador),
            listOf(CatalogoPapeis.PROFESSOR, CatalogoPapeis.COORDENADOR),
        )
        semear("caaf.dev@ufpr.br", "GRR20240004", hash, true, agora, agora, CatalogoPapeis.caaf, emptyList())
        semear("caaf.colegadev@ufpr.br", "GRR20240008", hash, true, agora, agora, CatalogoPapeis.caaf, emptyList())
        semear("secretaria.dev@ufpr.br", "GRR20240005", hash, true, agora, agora, CatalogoPapeis.secretaria, listOf(CatalogoPapeis.SECRETARIA))
        semear("egresso.dev@ufpr.br", "GRR20240006", hash, true, agora, agora, CatalogoPapeis.egresso, listOf(CatalogoPapeis.EGRESSO))
        semear("admin.dev@ufpr.br", "GRR20240007", hash, true, agora, agora, CatalogoPapeis.admin, listOf(CatalogoPapeis.ADMIN))
        LOG.info(
            "Usuários de desenvolvimento IAM prontos (aluno.dev / novo.dev / professor.dev / caaf.dev / caaf.colegadev / secretaria.dev / egresso.dev / admin.dev).",
        )
    }

    private fun semear(
        email: String,
        grr: String,
        hash: String,
        senhaAlterada: Boolean,
        lgpd: OffsetDateTime?,
        agora: OffsetDateTime,
        authorities: List<String>,
        perfis: List<String>,
    ) {
        val usuario = criarSeAusente(email, grr, hash, senhaAlterada, lgpd, agora, authorities)
        if (perfis.isNotEmpty()) {
            perfilAcessoPort.garantirVinculo(usuario.id, perfis)
        }
    }

    private fun criarSeAusente(
        email: String,
        grr: String,
        hash: String,
        senhaAlterada: Boolean,
        lgpd: OffsetDateTime?,
        agora: OffsetDateTime,
        authorities: List<String>,
    ): Usuario {
        val existente = usuarioRepository.findByEmail(email)
        if (existente.isPresent) {
            val usuario = existente.get()
            val extras = usuario.authorities.filter { it !in authorities }
            if (usuario.substituirAuthorities(authorities, agora)) {
                val salvo = usuarioRepository.save(usuario)
                if (extras.isNotEmpty()) {
                    LOG.info(
                        "Seed IAM (profile=dev) revogou authorities extras de {}: {}",
                        EmailMascarado.de(email),
                        extras.joinToString(", "),
                    )
                } else {
                    LOG.info("Seed IAM (profile=dev) alinhou authorities de {}.", EmailMascarado.de(email))
                }
                return salvo
            }
            return usuario
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
        return usuarioRepository.save(usuario)
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(IamDevDataLoader::class.java)
    }
}
