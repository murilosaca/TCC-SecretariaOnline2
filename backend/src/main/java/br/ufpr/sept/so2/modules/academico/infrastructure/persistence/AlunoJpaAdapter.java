package br.ufpr.sept.so2.modules.academico.infrastructure.persistence;

import br.ufpr.sept.so2.modules.academico.application.ports.AlunoRepository;
import br.ufpr.sept.so2.modules.academico.domain.Aluno;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class AlunoJpaAdapter implements AlunoRepository {

    private final AlunoJpaRepository jpaRepository;

    public AlunoJpaAdapter(AlunoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Aluno save(Aluno aluno) {
        AlunoJpaEntity entity = jpaRepository.findById(aluno.getId())
                .orElseGet(() -> AlunoJpaEntity.fromDomain(aluno));
        entity.merge(aluno);
        return jpaRepository.save(entity).toDomain();
    }

    @Override
    public Optional<Aluno> findById(UUID id) {
        return jpaRepository.findById(id).map(AlunoJpaEntity::toDomain);
    }

    @Override
    public Page<Aluno> findAll(UUID idCurso, String termo, Pageable pageable) {
        Specification<AlunoJpaEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (idCurso != null) {
                predicates.add(cb.equal(root.get("idCurso"), idCurso));
            }
            if (termo != null && !termo.isBlank()) {
                String like = "%" + termo.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("nome")), like),
                        cb.like(cb.lower(root.get("grr")), like),
                        cb.like(cb.lower(root.get("emailInstitucional")), like)
                ));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
        return jpaRepository.findAll(spec, pageable).map(AlunoJpaEntity::toDomain);
    }

    @Override
    public boolean existsByGrr(String grr) {
        return jpaRepository.existsByGrrIgnoreCase(grr);
    }

    @Override
    public boolean existsByEmailInstitucional(String email) {
        return jpaRepository.existsByEmailInstitucionalIgnoreCase(email);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}
