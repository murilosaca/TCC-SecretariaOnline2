package br.ufpr.sept.so2.modules.academico.infrastructure.persistence;

import br.ufpr.sept.so2.modules.academico.application.ports.CursoRepository;
import br.ufpr.sept.so2.modules.academico.domain.Curso;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class CursoJpaAdapter implements CursoRepository {

    private final CursoJpaRepository jpaRepository;

    public CursoJpaAdapter(CursoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Curso save(Curso curso) {
        CursoJpaEntity entity = jpaRepository.findById(curso.getId())
                .orElseGet(() -> CursoJpaEntity.fromDomain(curso));
        entity.merge(curso);
        return jpaRepository.save(entity).toDomain();
    }

    @Override
    public Optional<Curso> findById(UUID id) {
        return jpaRepository.findById(id).map(CursoJpaEntity::toDomain);
    }

    @Override
    public Page<Curso> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable).map(CursoJpaEntity::toDomain);
    }

    @Override
    public boolean existsBySigla(String sigla) {
        return jpaRepository.existsBySiglaIgnoreCase(sigla);
    }

    @Override
    public boolean existsByCodigo(String codigo) {
        return jpaRepository.existsByCodigoIgnoreCase(codigo);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}
