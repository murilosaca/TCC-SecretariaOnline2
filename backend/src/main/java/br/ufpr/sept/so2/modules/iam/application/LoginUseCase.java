package br.ufpr.sept.so2.modules.iam.application;

import br.ufpr.sept.so2.modules.iam.application.ports.AuditLogPort;
import br.ufpr.sept.so2.modules.iam.application.ports.JwtTokenService;
import br.ufpr.sept.so2.modules.iam.application.ports.OpaqueTokenHasher;
import br.ufpr.sept.so2.modules.iam.application.ports.PasswordHasher;
import br.ufpr.sept.so2.modules.iam.application.ports.RefreshTokenRepository;
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository;
import br.ufpr.sept.so2.modules.iam.domain.IdentificadorLogin;
import br.ufpr.sept.so2.modules.iam.domain.RefreshSessao;
import br.ufpr.sept.so2.modules.iam.domain.Usuario;
import br.ufpr.sept.so2.shared.domain.exception.CredenciaisInvalidasException;
import br.ufpr.sept.so2.shared.infrastructure.Uuids;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class LoginUseCase {

    private final UsuarioRepository usuarioRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordHasher passwordHasher;
    private final JwtTokenService jwtTokenService;
    private final OpaqueTokenHasher opaqueTokenHasher;
    private final AuditLogPort auditLogPort;
    private final IamSettings settings;

    public LoginUseCase(
            UsuarioRepository usuarioRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordHasher passwordHasher,
            JwtTokenService jwtTokenService,
            OpaqueTokenHasher opaqueTokenHasher,
            AuditLogPort auditLogPort,
            IamSettings settings
    ) {
        this.usuarioRepository = usuarioRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordHasher = passwordHasher;
        this.jwtTokenService = jwtTokenService;
        this.opaqueTokenHasher = opaqueTokenHasher;
        this.auditLogPort = auditLogPort;
        this.settings = settings;
    }

    @Transactional
    public LoginResult execute(String identificador, String senha, String ip) {
        Optional<IdentificadorLogin> parsed = IdentificadorLogin.tryParse(identificador);
        Optional<Usuario> encontrado = parsed.flatMap(usuarioRepository::findByIdentificador);
        if (encontrado.isEmpty()) {
            recusar(null, parsed.map(IdentificadorLogin::mascarado).orElse("***"), ip);
        }

        Usuario usuario = encontrado.get();
        OffsetDateTime agora = OffsetDateTime.now();
        usuario.liberarBloqueioSeExpirado(agora);
        if (!usuario.isAtivo() || usuario.estaBloqueado(agora)) {
            recusar(usuario.getId(), mascarar(usuario), ip);
        }
        if (!passwordHasher.matches(senha, usuario.getSenhaHash())) {
            boolean bloqueou = usuario.registrarFalha(
                    agora,
                    settings.maxFalhasConsecutivas(),
                    settings.minutosBloqueio()
            );
            usuarioRepository.save(usuario);
            if (bloqueou) {
                auditLogPort.append("iam.account_blocked", usuario.getId(), mascarar(usuario), ip);
            }
            recusar(usuario.getId(), mascarar(usuario), ip);
        }

        usuario.registrarLoginOk(agora);
        usuarioRepository.save(usuario);
        String refreshRaw = UUID.randomUUID().toString();
        RefreshSessao sessao = new RefreshSessao(
                Uuids.v7(),
                usuario.getId(),
                opaqueTokenHasher.hash(refreshRaw),
                agora.plusSeconds(settings.refreshTtlSeconds()),
                false,
                false,
                agora,
                agora
        );
        refreshTokenRepository.save(sessao);
        auditLogPort.append("iam.login_success", usuario.getId(), mascarar(usuario), ip);
        return new LoginResult(
                jwtTokenService.emitAccessToken(usuario),
                refreshRaw,
                usuario.precisaPrimeiroAcesso(),
                settings.accessTtlSeconds()
        );
    }

    private void recusar(UUID atorId, String payload, String ip) {
        passwordHasher.matchesDummy();
        auditLogPort.append("iam.login_failed", atorId, payload, ip);
        throw new CredenciaisInvalidasException();
    }

    private static String mascarar(Usuario usuario) {
        if (usuario.getGrr() != null) {
            return IdentificadorLogin.tryParse(usuario.getGrr().getValue())
                    .map(IdentificadorLogin::mascarado)
                    .orElse("***");
        }
        return IdentificadorLogin.tryParse(usuario.getEmailInstitucional().getValue())
                .map(IdentificadorLogin::mascarado)
                .orElse("***");
    }
}
