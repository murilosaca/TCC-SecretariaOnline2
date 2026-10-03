package br.ufpr.sept.so2.modules.suporte.infrastructure

import br.ufpr.sept.so2.modules.suporte.infrastructure.persistence.FaqItemJpaEntity
import br.ufpr.sept.so2.modules.suporte.infrastructure.persistence.FaqItemJpaRepository
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.util.UUID

@Component
@Order(40)
class FaqSeedInitializer(
    private val repositorio: FaqItemJpaRepository,
) : ApplicationRunner {
    override fun run(args: ApplicationArguments) {
        if (repositorio.count() > 0) {
            return
        }
        itens().forEach { repositorio.save(it) }
    }

    companion object {
        fun itens(): List<FaqItemJpaEntity> = listOf(
            item(
                "018f0000-0000-7000-8000-000000000361",
                "Como redefinir minha senha?",
                "Em /recuperar-senha informe o e-mail da conta. O link chega pelo canal já usado na recuperação de senha e vale por 24 horas.",
                1,
            ),
            item(
                "018f0000-0000-7000-8000-000000000362",
                "Prazo de deliberação?",
                "O prazo é o do tipo de solicitação publicado. A fila da secretaria marca o atraso quando esse prazo vence.",
                2,
            ),
        )

        private fun item(id: String, pergunta: String, resposta: String, ordem: Int): FaqItemJpaEntity {
            val entity = FaqItemJpaEntity()
            entity.id = UUID.fromString(id)
            entity.pergunta = pergunta
            entity.resposta = resposta
            entity.ordem = ordem
            return entity
        }
    }
}
