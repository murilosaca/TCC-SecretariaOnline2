package br.ufpr.sept.so2.modules.academico.application.ports;

import br.ufpr.sept.so2.modules.academico.domain.PeriodoLetivo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PeriodoLetivoRepository {

    PeriodoLetivo save(PeriodoLetivo periodo);

    Optional<PeriodoLetivo> findById(UUID id);

    Page<PeriodoLetivo> findAll(Pageable pageable);

    List<PeriodoLetivo> findAll();

    Optional<PeriodoLetivo> findVigente(LocalDate data);

    boolean existsByAnoAndSemestre(int ano, int semestre);

    void deleteById(UUID id);
}
