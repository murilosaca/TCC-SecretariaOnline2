package br.ufpr.sept.so2.modules.iam.infrastructure;

import br.ufpr.sept.so2.modules.iam.application.ports.PasswordHasher;
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository;
import br.ufpr.sept.so2.modules.iam.domain.Usuario;
import br.ufpr.sept.so2.shared.domain.valueobject.Email;
import br.ufpr.sept.so2.shared.domain.valueobject.Grr;
import br.ufpr.sept.so2.shared.infrastructure.Uuids;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;

@Component
@Profile("dev")
@Order(10)
public class IamDevDataLoader implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(IamDevDataLoader.class);

    private final IamProperties properties;
    private final UsuarioRepository usuarioRepository;
    private final PasswordHasher passwordHasher;

    public IamDevDataLoader(
            IamProperties properties,
            UsuarioRepository usuarioRepository,
            PasswordHasher passwordHasher
    ) {
        this.properties = properties;
        this.usuarioRepository = usuarioRepository;
        this.passwordHasher = passwordHasher;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!properties.getSeed().isEnabled()) {
            return;
        }
        String senha = properties.getSeed().getPassword();
        if (senha == null || senha.isBlank()) {
            senha = "TroqueEstaSenha1!";
            LOG.warn("IAM_DEV_SEED_PASSWORD ausente; usando senha local de bootstrap (apenas profile=dev).");
        }
        String hash = passwordHasher.hash(senha);
        OffsetDateTime agora = OffsetDateTime.now();
        criarSeAusente(
                "aluno.dev@ufpr.br",
                "GRR20240001",
                hash,
                true,
                agora,
                agora,
                authoritiesAluno()
        );
        criarSeAusente(
                "novo.dev@ufpr.br",
                "GRR20240002",
                hash,
                false,
                null,
                agora,
                authoritiesAluno()
        );
        criarSeAusente(
                "professor.dev@ufpr.br",
                "GRR20240003",
                hash,
                true,
                agora,
                agora,
                authoritiesProfessor()
        );
        LOG.info("Usuários de desenvolvimento IAM prontos (aluno.dev / novo.dev / professor.dev).");
    }

    private void criarSeAusente(
            String email,
            String grr,
            String hash,
            boolean senhaAlterada,
            OffsetDateTime lgpd,
            OffsetDateTime agora,
            List<String> authorities
    ) {
        var existente = usuarioRepository.findByEmail(email);
        if (existente.isPresent()) {
            Usuario usuario = existente.get();
            if (usuario.concederAuthorities(authorities, agora)) {
                usuarioRepository.save(usuario);
            }
            return;
        }
        Usuario usuario = new Usuario(
                Uuids.v7(),
                Email.of(email),
                null,
                Grr.of(grr),
                hash,
                senhaAlterada,
                lgpd,
                lgpd == null ? null : "127.0.0.1",
                lgpd == null ? null : "so2-dev-seed",
                true,
                0,
                null,
                authorities,
                agora,
                agora
        );
        usuarioRepository.save(usuario);
    }

    private static List<String> authoritiesAluno() {
        return List.of(
                "dashboard.view_own",
                "request.view_own",
                "request.open",
                "attendance.view_open",
                "attendance.check_in"
        );
    }

    private static List<String> authoritiesProfessor() {
        return List.of(
                "dashboard.view_own",
                "event.manage",
                "event.host"
        );
    }
}
