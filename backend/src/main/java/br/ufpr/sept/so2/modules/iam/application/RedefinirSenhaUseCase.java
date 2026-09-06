package br.ufpr.sept.so2.modules.iam.application;

import br.ufpr.sept.so2.modules.iam.application.ports.AuditLogPort;
import br.ufpr.sept.so2.modules.iam.application.ports.JtiBlacklistRepository;
import br.ufpr.sept.so2.modules.iam.application.ports.JwtTokenService;
import br.ufpr.sept.so2.modules.iam.application.ports.PasswordHasher;
import br.ufpr.sept.so2.modules.iam.application.ports.RefreshTokenRepository;
import br.ufpr.sept.so2.modules.iam.application.ports.SenhaHistoricoRepository;
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository;
import br.ufpr.sept.so2.modules.iam.domain.PoliticaSenha;
import br.ufpr.sept.so2.modules.iam.domain.SenhaReutilizadaException;
import br.ufpr.sept.so2.modules.iam.domain.Usuario;
import br.ufpr.sept.so2.shared.domain.exception.TokenResetInvalidoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class RedefinirSenhaUseCase {

    private final JwtTokenService jwtTokenService;
    private final JtiBlacklistRepository jtiBlacklistRepository;
    private final UsuarioRepository usuarioRepository;
    private final SenhaHistoricoRepository senhaHistoricoRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordHasher passwordHasher;
    private final AuditLogPort auditLogPort;
    private final IamSettings settings;

    public RedefinirSenhaUseCase(
            JwtTokenService jwtTokenService,
            JtiBlacklistRepository jtiBlacklistRepository,
            UsuarioRepository usuarioRepository,
            SenhaHistoricoRepository senhaHistoricoRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordHasher passwordHasher,
            AuditLogPort auditLogPort,
            IamSettings settings
    ) {
        this.jwtTokenService = jwtTokenService;
        this.jtiBlacklistRepository = jtiBlacklistRepository;
        this.usuarioRepository = usuarioRepository;
        this.senhaHistoricoRepository = senhaHistoricoRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordHasher = passwordHasher;
        this.auditLogPort = auditLogPort;
        this.settings = settings;
    }

    @Transactional
    public void execute(String token, String novaSenha, String ip) {
        JwtTokenService.ResetTokenClaims claims = lerToken(token);
        if (jtiBlacklistRepository.contains(claims.jti())) {
            throw new TokenResetInvalidoException();
        }
        PoliticaSenha.validar(novaSenha);
        Usuario usuario = usuarioRepository.findById(claims.userId())
                .orElseThrow(TokenResetInvalidoException::new);
        rejeitarSeReutilizada(usuario, novaSenha);

        OffsetDateTime agora = OffsetDateTime.now();
        String novoHash = passwordHasher.hash(novaSenha);
        senhaHistoricoRepository.append(usuario.getId(), usuario.getSenhaHash());
        usuario.redefinirSenha(novoHash, agora);
        usuarioRepository.save(usuario);
        refreshTokenRepository.revokeAllByUsuarioId(usuario.getId());
        jtiBlacklistRepository.add(claims.jti(), agora.plusSeconds(settings.resetTtlSeconds()));
        auditLogPort.append("iam.password_reset_completed", usuario.getId(), "ok", ip);
    }

    public void validarToken(String token) {
        JwtTokenService.ResetTokenClaims claims = lerToken(token);
        if (jtiBlacklistRepository.contains(claims.jti())) {
            throw new TokenResetInvalidoException();
        }
        usuarioRepository.findById(claims.userId()).orElseThrow(TokenResetInvalidoException::new);
    }

    private JwtTokenService.ResetTokenClaims lerToken(String token) {
        try {
            return jwtTokenService.parseResetToken(token);
        } catch (RuntimeException ex) {
            throw new TokenResetInvalidoException();
        }
    }

    private void rejeitarSeReutilizada(Usuario usuario, String novaSenha) {
        List<String> hashes = new ArrayList<>();
        hashes.add(usuario.getSenhaHash());
        hashes.addAll(senhaHistoricoRepository.findLastHashes(usuario.getId(), 3));
        for (String hash : hashes) {
            if (passwordHasher.matches(novaSenha, hash)) {
                throw SenhaReutilizadaException.historico();
            }
        }
    }
}
