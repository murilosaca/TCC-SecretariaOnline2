package br.ufpr.sept.so2.modules.solicitacoes.infrastructure.persistence;

import br.ufpr.sept.so2.modules.solicitacoes.application.ports.ProtocoloSequenciaPort;
import br.ufpr.sept.so2.modules.solicitacoes.domain.Protocolo;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ProtocoloSequenciaAdapter implements ProtocoloSequenciaPort {

    private final ProtocoloSeqJpaRepository jpaRepository;

    public ProtocoloSequenciaAdapter(ProtocoloSeqJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    @Transactional
    public Protocolo proximo(int ano) {
        ProtocoloSeqJpaEntity row = jpaRepository.lockByAno(ano)
                .orElseGet(() -> jpaRepository.saveAndFlush(new ProtocoloSeqJpaEntity(ano, 0)));
        row.setUltimo(row.getUltimo() + 1);
        jpaRepository.saveAndFlush(row);
        return Protocolo.formatar(ano, row.getUltimo());
    }
}
