package br.ufpr.sept.so2.modules.solicitacoes.infrastructure;

import br.ufpr.sept.so2.modules.solicitacoes.application.ports.TipoSolicitacaoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
@Profile("dev")
@Order(20)
public class SolicitacaoDevDataLoader implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(SolicitacaoDevDataLoader.class);

    private final TipoSolicitacaoRepository tipoRepository;

    public SolicitacaoDevDataLoader(TipoSolicitacaoRepository tipoRepository) {
        this.tipoRepository = tipoRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (tipoRepository.findByCodigo(DeclaracaoSimplesSeed.CODIGO).isPresent()) {
            return;
        }
        tipoRepository.save(DeclaracaoSimplesSeed.tipo(OffsetDateTime.now()));
        LOG.info("RequestType DECLARACAO_SIMPLES publicado para o wizard.");
    }
}
