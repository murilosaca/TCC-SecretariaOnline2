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
                """
Você pode trocar a senha de dois jeitos:

1) Se já estiver dentro do sistema
• No menu lateral, abra Configurações → Segurança.
• Em “Trocar senha”, informe a senha atual, digite a nova senha (ela precisa ser forte) e confirme.
• Clique em “Salvar senha”. A troca vale na hora.

2) Se não conseguir entrar (esqueceu a senha)
• Na tela de login, use a opção de recuperar senha.
• Informe o e-mail institucional (@ufpr.br), o e-mail pessoal cadastrado ou o GRR.
• Você receberá um link para criar uma nova senha. Esse link vale por 24 horas.
• Abra o link, defina a nova senha e volte ao login.

Dica: se o primeiro acesso ainda pedir troca de senha, siga as instruções da própria tela até concluir.
                """.trimIndent().trim(),
                1,
            ),
            item(
                "018f0000-0000-7000-8000-000000000362",
                "Qual é o prazo de deliberação?",
                """
O prazo depende do tipo da solicitação. Conta a partir do momento em que o pedido é aberto e aparece na tela Solicitações, na coluna de prazo.

Prazos atuais no sistema:
• Declaração simples — 15 dias
• Autorização de uso de imagem — 15 dias
• Suporte técnico — 5 dias

Outros tipos que a secretaria publicar no futuro terão o próprio prazo. Se a data passar sem resposta, a solicitação aparece em atraso (SLA) para a equipe que delibera. Você continua acompanhando o andamento em Solicitações.
                """.trimIndent().trim(),
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
