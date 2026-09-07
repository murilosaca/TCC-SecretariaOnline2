package br.ufpr.sept.so2.modules.bff.infrastructure;

import br.ufpr.sept.so2.modules.academico.application.ports.PeriodoLetivoRepository;
import br.ufpr.sept.so2.modules.bff.application.ports.PeriodoVigenteQueryPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Component
public class PeriodoVigenteQueryAdapter implements PeriodoVigenteQueryPort {

    private final PeriodoLetivoRepository periodoLetivoRepository;

    public PeriodoVigenteQueryAdapter(PeriodoLetivoRepository periodoLetivoRepository) {
        this.periodoLetivoRepository = periodoLetivoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PeriodoVigenteResumo> consultar(LocalDate data) {
        return periodoLetivoRepository.findVigente(data)
                .map(periodo -> new PeriodoVigenteResumo(
                        periodo.getId(),
                        periodo.getAno(),
                        periodo.getSemestre(),
                        periodo.getInicio(),
                        periodo.getFim()
                ));
    }
}
