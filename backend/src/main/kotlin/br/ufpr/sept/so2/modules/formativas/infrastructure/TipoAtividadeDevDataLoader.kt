package br.ufpr.sept.so2.modules.formativas.infrastructure

import br.ufpr.sept.so2.modules.academico.application.ports.CursoRepository
import br.ufpr.sept.so2.modules.formativas.application.ports.TipoAtividadeFormativaRepository
import br.ufpr.sept.so2.modules.formativas.domain.TipoAtividadeFormativa
import br.ufpr.sept.so2.modules.iam.infrastructure.IamProperties
import br.ufpr.sept.so2.shared.infrastructure.Uuids
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Profile
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.time.OffsetDateTime

@Component
@Profile("dev")
@Order(28)
class TipoAtividadeDevDataLoader(
    private val properties: IamProperties,
    private val cursoRepository: CursoRepository,
    private val tipoRepository: TipoAtividadeFormativaRepository,
) : ApplicationRunner {
    override fun run(args: ApplicationArguments) {
        if (!properties.seed.enabled) {
            return
        }
        val curso = cursoRepository.findByCodigo(CODIGO_TADS).orElse(null) ?: return
        val agora = OffsetDateTime.now()
        TIPOS.forEach { nome ->
            if (tipoRepository.findByCursoENome(curso.id, nome) == null) {
                tipoRepository.save(
                    TipoAtividadeFormativa(Uuids.v7(), curso.id, nome, true, agora, agora),
                )
            }
        }
        LOG.info("Tipos de atividade formativa de desenvolvimento prontos (TADS).")
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(TipoAtividadeDevDataLoader::class.java)
        private const val CODIGO_TADS = "TADS-SEPT"
        private val TIPOS = listOf("Curso de extensão", "Publicação", "Monitoria")
    }
}
