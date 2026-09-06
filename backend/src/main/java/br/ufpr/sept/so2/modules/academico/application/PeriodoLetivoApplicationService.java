package br.ufpr.sept.so2.modules.academico.application;

import br.ufpr.sept.so2.modules.academico.application.ports.PeriodoLetivoRepository;
import br.ufpr.sept.so2.modules.academico.domain.PeriodoLetivo;
import br.ufpr.sept.so2.shared.domain.exception.ConflitoEstadoException;
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException;
import br.ufpr.sept.so2.shared.infrastructure.Uuids;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@Transactional
public class PeriodoLetivoApplicationService {

    private final PeriodoLetivoRepository periodoLetivoRepository;

    public PeriodoLetivoApplicationService(PeriodoLetivoRepository periodoLetivoRepository) {
        this.periodoLetivoRepository = periodoLetivoRepository;
    }

    @Transactional(readOnly = true)
    public Page<PeriodoLetivo> listar(Pageable pageable) {
        return periodoLetivoRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public PeriodoLetivo buscarPorId(UUID id) {
        return periodoLetivoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Período letivo não encontrado: " + id));
    }

    @Transactional(readOnly = true)
    public PeriodoLetivo buscarVigente(LocalDate data) {
        return periodoLetivoRepository.findVigente(data)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Não há período letivo vigente em " + data + "."));
    }

    public PeriodoLetivo criar(int ano, int semestre, LocalDate inicio, LocalDate fim) {
        if (periodoLetivoRepository.existsByAnoAndSemestre(ano, semestre)) {
            throw new ConflitoEstadoException("Já existe período letivo para " + ano + "/" + semestre + ".");
        }
        OffsetDateTime agora = OffsetDateTime.now();
        PeriodoLetivo periodo = new PeriodoLetivo(Uuids.v7(), ano, semestre, inicio, fim, true, agora, agora);
        garantirSemSobreposicao(periodo);
        return periodoLetivoRepository.save(periodo);
    }

    public PeriodoLetivo atualizar(UUID id, Integer ano, Integer semestre, LocalDate inicio, LocalDate fim, Boolean ativo) {
        PeriodoLetivo periodo = buscarPorId(id);
        int anoAlvo = ano != null ? ano : periodo.getAno();
        int semestreAlvo = semestre != null ? semestre : periodo.getSemestre();
        if ((anoAlvo != periodo.getAno() || semestreAlvo != periodo.getSemestre())
                && periodoLetivoRepository.existsByAnoAndSemestre(anoAlvo, semestreAlvo)) {
            throw new ConflitoEstadoException("Já existe período letivo para " + anoAlvo + "/" + semestreAlvo + ".");
        }
        periodo.atualizar(ano, semestre, inicio, fim, ativo);
        garantirSemSobreposicao(periodo);
        return periodoLetivoRepository.save(periodo);
    }

    public void excluir(UUID id) {
        buscarPorId(id);
        periodoLetivoRepository.deleteById(id);
    }

    private void garantirSemSobreposicao(PeriodoLetivo candidato) {
        boolean sobrepoe = periodoLetivoRepository.findAll().stream()
                .filter(existente -> !existente.getId().equals(candidato.getId()))
                .anyMatch(candidato::sobrepoe);
        if (sobrepoe) {
            throw new ConflitoEstadoException("O intervalo informado sobrepõe outro período letivo.");
        }
    }
}
