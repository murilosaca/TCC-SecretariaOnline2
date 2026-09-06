package br.ufpr.sept.so2.modules.iam.application.ports;

import br.ufpr.sept.so2.modules.iam.domain.IdentificadorLogin;
import br.ufpr.sept.so2.modules.iam.domain.Usuario;

import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository {

    Usuario save(Usuario usuario);

    Optional<Usuario> findById(UUID id);

    Optional<Usuario> findByIdentificador(IdentificadorLogin identificador);

    Optional<Usuario> findByEmail(String email);
}
