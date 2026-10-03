package br.ufpr.sept.so2.modules.comunicacao.application

import br.ufpr.sept.so2.modules.iam.application.ports.AuditLogPort
import br.ufpr.sept.so2.shared.domain.exception.ConflitoEstadoException
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import java.util.UUID

@Service
class CriarTemplateComunicacaoUseCase(
    private val repositorio: TemplateComunicacaoRepository,
    private val auditLogPort: AuditLogPort,
    private val transactionTemplate: TransactionTemplate,
    private val objectMapper: ObjectMapper,
) {
    fun execute(nomeBruto: String?, assuntoBruto: String?, corpoBruto: String?, autorId: UUID, ip: String?): TemplateDetalhe {
        val nome = NomeTemplate.normalizar(nomeBruto)
        val assunto = TextoTemplate.exigir(assuntoBruto, "Assunto", 200)
        val corpo = TextoTemplate.exigir(corpoBruto, "Corpo", 20_000)
        if (repositorio.existeNome(nome)) {
            throw ConflitoEstadoException("Já existe um template com este nome.")
        }
        return transactionTemplate.execute {
            val criado = repositorio.criar(nome, assunto, corpo, autorId)
            auditLogPort.append(
                "template.criado",
                autorId,
                objectMapper.writeValueAsString(
                    mapOf("templateId" to criado.id.toString(), "nome" to criado.nome, "versao" to 1),
                ),
                ip,
            )
            criado
        }!!
    }
}

@Service
class SalvarRevisaoTemplateUseCase(
    private val repositorio: TemplateComunicacaoRepository,
    private val auditLogPort: AuditLogPort,
    private val objectMapper: ObjectMapper,
) {
    @org.springframework.transaction.annotation.Transactional
    fun execute(id: UUID, assuntoBruto: String?, corpoBruto: String?, autorId: UUID, ip: String?): TemplateDetalhe {
        val assunto = TextoTemplate.exigir(assuntoBruto, "Assunto", 200)
        val corpo = TextoTemplate.exigir(corpoBruto, "Corpo", 20_000)
        val salvo = repositorio.salvarRevisao(id, assunto, corpo, autorId)
        val corrente = salvo.revisoes.first { it.status == "CURRENT" }
        auditLogPort.append(
            "template.revisao_salva",
            autorId,
            objectMapper.writeValueAsString(
                mapOf(
                    "templateId" to salvo.id.toString(),
                    "nome" to salvo.nome,
                    "versao" to corrente.versao,
                ),
            ),
            ip,
        )
        return salvo
    }
}

@Service
class ConsultarTemplateComunicacaoUseCase(
    private val repositorio: TemplateComunicacaoRepository,
) {
    fun listar(pageable: Pageable): Page<TemplateResumo> = repositorio.listar(pageable)

    fun buscar(id: UUID): TemplateDetalhe = repositorio.buscar(id)
}
