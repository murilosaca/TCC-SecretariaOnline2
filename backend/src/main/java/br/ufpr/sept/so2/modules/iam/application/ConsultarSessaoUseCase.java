package br.ufpr.sept.so2.modules.iam.application;

import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository;
import br.ufpr.sept.so2.modules.iam.domain.Usuario;
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ConsultarSessaoUseCase {

    private final UsuarioRepository usuarioRepository;

    public ConsultarSessaoUseCase(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public SessaoAtual execute(UUID usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));
        return new SessaoAtual(usuario.getId(), usuario.precisaPrimeiroAcesso(), usuario.getAuthorities());
    }

    public record SessaoAtual(UUID id, boolean mustChangePassword, List<String> authorities) {
    }
}
