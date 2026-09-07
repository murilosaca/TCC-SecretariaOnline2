package br.ufpr.sept.so2.modules.solicitacoes.infrastructure

import br.ufpr.sept.so2.modules.solicitacoes.application.ports.TipoSolicitacaoRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Profile
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.time.OffsetDateTime

@Component
@Profile("dev")
@Order(20)
class SolicitacaoDevDataLoader(
    private val tipoRepository: TipoSolicitacaoRepository,
) : ApplicationRunner {

    override fun run(args: ApplicationArguments) {
        if (tipoRepository.findByCodigo(DeclaracaoSimplesSeed.CODIGO).isPresent) {
            return
        }
        tipoRepository.save(DeclaracaoSimplesSeed.tipo(OffsetDateTime.now()))
        LOG.info("RequestType DECLARACAO_SIMPLES publicado para o wizard.")
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(SolicitacaoDevDataLoader::class.java)
    }
}
