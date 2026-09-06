package br.ufpr.sept.so2.modules.iam.infrastructure.persistence;

import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository;
import br.ufpr.sept.so2.modules.iam.domain.IdentificadorLogin;
import br.ufpr.sept.so2.modules.iam.domain.Usuario;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class UsuarioJpaAdapter implements UsuarioRepository {

    private final UsuarioJpaRepository jpaRepository;

    public UsuarioJpaAdapter(UsuarioJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Usuario save(Usuario usuario) {
        UsuarioJpaEntity entity = jpaRepository.findById(usuario.getId())
                .orElseGet(() -> UsuarioJpaEntity.fromDomain(usuario));
        entity.merge(usuario);
        return jpaRepository.save(entity).toDomain();
    }

    @Override
    public Optional<Usuario> findById(UUID id) {
        return jpaRepository.findById(id).map(UsuarioJpaEntity::toDomain);
    }

    @Override
    public Optional<Usuario> findByIdentificador(IdentificadorLogin identificador) {
        if (identificador.getTipo() == IdentificadorLogin.Tipo.GRR) {
            return jpaRepository.findByGrrIgnoreCase(identificador.getValor()).map(UsuarioJpaEntity::toDomain);
        }
        return findByEmail(identificador.getValor());
    }

    @Override
    public Optional<Usuario> findByEmail(String email) {
        return jpaRepository.findByAnyEmail(email).map(UsuarioJpaEntity::toDomain);
    }
}
