package br.ufpr.sept.so2.modules.atendimentos.infrastructure

import br.ufpr.sept.so2.modules.atendimentos.application.ports.CategoriaAtendimentoRepository
import br.ufpr.sept.so2.modules.atendimentos.domain.CategoriaAtendimento
import br.ufpr.sept.so2.shared.infrastructure.Uuids
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Profile
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.time.OffsetDateTime

/**
 * Categorias de atendimento são lista pequena de seed (RN-F5-007-03).
 * Não existe CRUD de categoria no admin nesta fatia.
 */
@Component
@Profile("dev")
@Order(70)
class AtendimentoDevDataLoader(
    private val categoriaRepository: CategoriaAtendimentoRepository,
) : ApplicationRunner {

    override fun run(args: ApplicationArguments) {
        val agora = OffsetDateTime.now()
        val criadas = CATEGORIAS.count { nome -> garantirCategoria(nome, agora) }
        LOG.info("Categorias de atendimento prontas ({} novas de {}).", criadas, CATEGORIAS.size)
    }

    private fun garantirCategoria(nome: String, agora: OffsetDateTime): Boolean {
        if (categoriaRepository.findByNome(nome) != null) {
            return false
        }
        categoriaRepository.save(CategoriaAtendimento(Uuids.v7(), nome, true, agora, agora))
        return true
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(AtendimentoDevDataLoader::class.java)

        private val CATEGORIAS = listOf(
            "Matrícula",
            "Documentação",
            "Estágio",
            "Formativas",
            "Outros",
        )
    }
}
