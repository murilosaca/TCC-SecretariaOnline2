package br.ufpr.sept.so2.modules.iam.application;

import br.ufpr.sept.so2.modules.iam.application.ports.AuditLogPort;
import br.ufpr.sept.so2.modules.iam.application.ports.OutboxPort;
import br.ufpr.sept.so2.modules.iam.application.ports.PasswordHasher;
import br.ufpr.sept.so2.modules.iam.application.ports.SenhaHistoricoRepository;
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository;
import br.ufpr.sept.so2.modules.iam.domain.PoliticaSenha;
import br.ufpr.sept.so2.modules.iam.domain.SenhaReutilizadaException;
import br.ufpr.sept.so2.modules.iam.domain.Usuario;
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException;
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class PrimeiroAcessoUseCase {

    private final UsuarioRepository usuarioRepository;
    private final SenhaHistoricoRepository senhaHistoricoRepository;
    private final PasswordHasher passwordHasher;
    private final AuditLogPort auditLogPort;
    private final OutboxPort outboxPort;

    public PrimeiroAcessoUseCase(
            UsuarioRepository usuarioRepository,
            SenhaHistoricoRepository senhaHistoricoRepository,
            PasswordHasher passwordHasher,
            AuditLogPort auditLogPort,
            OutboxPort outboxPort
    ) {
        this.usuarioRepository = usuarioRepository;
        this.senhaHistoricoRepository = senhaHistoricoRepository;
        this.passwordHasher = passwordHasher;
        this.auditLogPort = auditLogPort;
        this.outboxPort = outboxPort;
    }

    @Transactional
    public void execute(UUID usuarioId, String novaSenha, boolean aceiteTermos, String ip, String userAgent) {
        if (!aceiteTermos) {
            throw new DadoInvalidoException("É obrigatório aceitar a política de privacidade (LGPD).");
        }
        PoliticaSenha.validar(novaSenha);
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));
        if (passwordHasher.matches(novaSenha, usuario.getSenhaHash())) {
            throw SenhaReutilizadaException.temporaria();
        }
        OffsetDateTime agora = OffsetDateTime.now();
        String novoHash = passwordHasher.hash(novaSenha);
        senhaHistoricoRepository.append(usuario.getId(), usuario.getSenhaHash());
        usuario.completarPrimeiroAcesso(novoHash, agora, ip, userAgent);
        usuarioRepository.save(usuario);
        auditLogPort.append("iam.first_access_completed", usuario.getId(), "lgpd=true", ip);
        outboxPort.enqueue("iam.first_access_completed", "{\"usuarioId\":\"" + usuario.getId() + "\"}");
    }
}
