package br.ufpr.sept.so2.modules.iam.application;

import br.ufpr.sept.so2.modules.iam.application.ports.AuditLogPort;
import br.ufpr.sept.so2.modules.iam.application.ports.JwtTokenService;
import br.ufpr.sept.so2.modules.iam.application.ports.OutboxPort;
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository;
import br.ufpr.sept.so2.modules.iam.domain.IdentificadorLogin;
import br.ufpr.sept.so2.modules.iam.domain.Usuario;
import br.ufpr.sept.so2.shared.domain.valueobject.Email;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class RecuperarSenhaUseCase {

    private final UsuarioRepository usuarioRepository;
    private final JwtTokenService jwtTokenService;
    private final OutboxPort outboxPort;
    private final AuditLogPort auditLogPort;
    private final IamSettings settings;

    public RecuperarSenhaUseCase(
            UsuarioRepository usuarioRepository,
            JwtTokenService jwtTokenService,
            OutboxPort outboxPort,
            AuditLogPort auditLogPort,
            IamSettings settings
    ) {
        this.usuarioRepository = usuarioRepository;
        this.jwtTokenService = jwtTokenService;
        this.outboxPort = outboxPort;
        this.auditLogPort = auditLogPort;
        this.settings = settings;
    }

    @Transactional
    public void execute(String email, String ip) {
        String normalizado = Email.of(email).getValue();
        String mascarado = IdentificadorLogin.tryParse(normalizado)
                .map(IdentificadorLogin::mascarado)
                .orElse("***");
        Optional<Usuario> encontrado = usuarioRepository.findByEmail(normalizado)
                .filter(Usuario::isAtivo);
        if (encontrado.isEmpty()) {
            auditLogPort.append("iam.password_reset_requested", null, mascarado, ip);
            return;
        }
        Usuario usuario = encontrado.get();
        String token = jwtTokenService.emitResetToken(usuario);
        String url = settings.frontendBaseUrl() + "/nova-senha?token=" + token;
        String payload = "{\"email\":\"" + mascarado + "\",\"resetUrl\":\"" + url + "\"}";
        outboxPort.enqueue("PASSWORD_RESET", payload);
        auditLogPort.append("iam.password_reset_requested", usuario.getId(), mascarado, ip);
    }
}
