package br.ufpr.sept.so2.modules.iam.application;

import br.ufpr.sept.so2.modules.iam.application.ports.AuditLogPort;
import br.ufpr.sept.so2.modules.iam.application.ports.JwtTokenService;
import br.ufpr.sept.so2.modules.iam.application.ports.OpaqueTokenHasher;
import br.ufpr.sept.so2.modules.iam.application.ports.RefreshTokenRepository;
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository;
import br.ufpr.sept.so2.modules.iam.domain.RefreshSessao;
import br.ufpr.sept.so2.modules.iam.domain.Usuario;
import br.ufpr.sept.so2.shared.domain.exception.CredenciaisInvalidasException;
import br.ufpr.sept.so2.shared.infrastructure.Uuids;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class RefreshTokenUseCase {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UsuarioRepository usuarioRepository;
    private final JwtTokenService jwtTokenService;
    private final OpaqueTokenHasher opaqueTokenHasher;
    private final AuditLogPort auditLogPort;
    private final IamSettings settings;

    public RefreshTokenUseCase(
            RefreshTokenRepository refreshTokenRepository,
            UsuarioRepository usuarioRepository,
            JwtTokenService jwtTokenService,
            OpaqueTokenHasher opaqueTokenHasher,
            AuditLogPort auditLogPort,
            IamSettings settings
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.usuarioRepository = usuarioRepository;
        this.jwtTokenService = jwtTokenService;
        this.opaqueTokenHasher = opaqueTokenHasher;
        this.auditLogPort = auditLogPort;
        this.settings = settings;
    }

    @Transactional
    public LoginResult execute(String refreshRaw, String ip) {
        if (refreshRaw == null || refreshRaw.isBlank()) {
            throw new CredenciaisInvalidasException();
        }
        OffsetDateTime agora = OffsetDateTime.now();
        RefreshSessao sessao = refreshTokenRepository.lockByTokenHash(opaqueTokenHasher.hash(refreshRaw))
                .orElseThrow(CredenciaisInvalidasException::new);

        if (sessao.reutilizadaOuRevogada()) {
            refreshTokenRepository.revokeAllByUsuarioId(sessao.getUsuarioId());
            auditLogPort.append("iam.suspicious_token_reuse", sessao.getUsuarioId(), "reuse", ip);
            throw new CredenciaisInvalidasException();
        }
        if (sessao.expirada(agora)) {
            sessao.revogar(agora);
            refreshTokenRepository.save(sessao);
            throw new CredenciaisInvalidasException();
        }

        Usuario usuario = usuarioRepository.findById(sessao.getUsuarioId())
                .orElseThrow(CredenciaisInvalidasException::new);
        if (!usuario.isAtivo()) {
            throw new CredenciaisInvalidasException();
        }

        sessao.marcarUsada(agora);
        refreshTokenRepository.save(sessao);

        String novoRaw = UUID.randomUUID().toString();
        RefreshSessao nova = new RefreshSessao(
                Uuids.v7(),
                usuario.getId(),
                opaqueTokenHasher.hash(novoRaw),
                agora.plusSeconds(settings.refreshTtlSeconds()),
                false,
                false,
                agora,
                agora
        );
        refreshTokenRepository.save(nova);
        return new LoginResult(
                jwtTokenService.emitAccessToken(usuario),
                novoRaw,
                usuario.precisaPrimeiroAcesso(),
                settings.accessTtlSeconds()
        );
    }
}
