package br.ufpr.sept.so2.modules.academico.infrastructure.persistence;

import br.ufpr.sept.so2.modules.academico.application.ports.DisciplinaRepository;
import br.ufpr.sept.so2.modules.academico.domain.Disciplina;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class DisciplinaJpaAdapter implements DisciplinaRepository {

    private final DisciplinaJpaRepository jpaRepository;

    public DisciplinaJpaAdapter(DisciplinaJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Disciplina save(Disciplina disciplina) {
        DisciplinaJpaEntity entity = jpaRepository.findById(disciplina.getId())
                .orElseGet(() -> DisciplinaJpaEntity.fromDomain(disciplina));
        entity.merge(disciplina);
        return jpaRepository.save(entity).toDomain();
    }

    @Override
    public Optional<Disciplina> findById(UUID id) {
        return jpaRepository.findById(id).map(DisciplinaJpaEntity::toDomain);
    }

    @Override
    public Page<Disciplina> findAll(UUID idCurso, Pageable pageable) {
        if (idCurso == null) {
            return jpaRepository.findAll(pageable).map(DisciplinaJpaEntity::toDomain);
        }
        return jpaRepository.findByIdCurso(idCurso, pageable).map(DisciplinaJpaEntity::toDomain);
    }

    @Override
    public boolean existsByCursoAndCodigo(UUID idCurso, String codigo) {
        return jpaRepository.existsByIdCursoAndCodigoIgnoreCase(idCurso, codigo);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}
