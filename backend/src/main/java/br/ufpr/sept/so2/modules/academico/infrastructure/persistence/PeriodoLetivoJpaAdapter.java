package br.ufpr.sept.so2.modules.academico.infrastructure.persistence;

import br.ufpr.sept.so2.modules.academico.application.ports.PeriodoLetivoRepository;
import br.ufpr.sept.so2.modules.academico.domain.PeriodoLetivo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class PeriodoLetivoJpaAdapter implements PeriodoLetivoRepository {

    private final PeriodoLetivoJpaRepository jpaRepository;

    public PeriodoLetivoJpaAdapter(PeriodoLetivoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public PeriodoLetivo save(PeriodoLetivo periodo) {
        PeriodoLetivoJpaEntity entity = jpaRepository.findById(periodo.getId())
                .orElseGet(() -> PeriodoLetivoJpaEntity.fromDomain(periodo));
        entity.merge(periodo);
        return jpaRepository.save(entity).toDomain();
    }

    @Override
    public Optional<PeriodoLetivo> findById(UUID id) {
        return jpaRepository.findById(id).map(PeriodoLetivoJpaEntity::toDomain);
    }

    @Override
    public Page<PeriodoLetivo> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable).map(PeriodoLetivoJpaEntity::toDomain);
    }

    @Override
    public List<PeriodoLetivo> findAll() {
        return jpaRepository.findAll().stream().map(PeriodoLetivoJpaEntity::toDomain).toList();
    }

    @Override
    public Optional<PeriodoLetivo> findVigente(LocalDate data) {
        return jpaRepository.findVigente(data).map(PeriodoLetivoJpaEntity::toDomain);
    }

    @Override
    public boolean existsByAnoAndSemestre(int ano, int semestre) {
        return jpaRepository.existsByAnoAndSemestre((short) ano, (short) semestre);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}
