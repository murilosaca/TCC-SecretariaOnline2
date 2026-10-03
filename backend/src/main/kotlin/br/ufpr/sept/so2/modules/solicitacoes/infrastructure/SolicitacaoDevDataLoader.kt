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
        val agora = OffsetDateTime.now()
        if (tipoRepository.findByCodigo(DeclaracaoSimplesSeed.CODIGO).isEmpty) {
            tipoRepository.save(DeclaracaoSimplesSeed.tipo(agora))
            LOG.info("RequestType DECLARACAO_SIMPLES publicado para o wizard.")
        }
        if (tipoRepository.findByCodigo(AutorizacaoImagemSeed.CODIGO).isEmpty) {
            tipoRepository.save(AutorizacaoImagemSeed.tipo(agora))
            LOG.info("RequestType AUTORIZACAO_IMAGEM publicado.")
        }
        if (tipoRepository.findByCodigo(SuporteTecnicoSeed.CODIGO).isEmpty) {
            tipoRepository.save(SuporteTecnicoSeed.tipo(agora))
            LOG.info("RequestType SUPORTE_TECNICO publicado.")
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(SolicitacaoDevDataLoader::class.java)
    }
}
