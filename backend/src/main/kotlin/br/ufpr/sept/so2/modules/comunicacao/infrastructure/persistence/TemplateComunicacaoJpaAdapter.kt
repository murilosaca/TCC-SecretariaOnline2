package br.ufpr.sept.so2.modules.comunicacao.infrastructure.persistence

import br.ufpr.sept.so2.modules.comunicacao.application.RevisaoAtual
import br.ufpr.sept.so2.modules.comunicacao.application.RevisaoItem
import br.ufpr.sept.so2.modules.comunicacao.application.TemplateComunicacaoRepository
import br.ufpr.sept.so2.modules.comunicacao.application.TemplateDetalhe
import br.ufpr.sept.so2.modules.comunicacao.application.TemplateResumo
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import br.ufpr.sept.so2.shared.infrastructure.Uuids
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Component
class TemplateComunicacaoJpaAdapter(
    private val cabecalhos: TemplateComunicacaoJpaRepository,
    private val revisoes: TemplateRevisaoJpaRepository,
) : TemplateComunicacaoRepository {
    override fun existeNome(nome: String): Boolean = cabecalhos.existsByNome(nome)

    @Transactional
    override fun criar(nome: String, assunto: String, corpo: String, autorId: UUID): TemplateDetalhe {
        val header = TemplateComunicacaoJpaEntity()
        header.id = Uuids.v7()
        header.nome = nome
        header.assunto = assunto
        cabecalhos.save(header)
        val revisao = novaRevisao(header.id!!, 1, assunto, corpo, autorId)
        revisoes.save(revisao)
        return detalhe(header)
    }

    @Transactional
    override fun salvarRevisao(id: UUID, assunto: String, corpo: String, autorId: UUID): TemplateDetalhe {
        val header = cabecalhos.findById(id).orElseThrow { naoEncontrado() }
        val atual = revisoes.findByTemplateIdAndStatus(id, CURRENT).orElseThrow { naoEncontrado() }
        atual.status = ARCHIVED
        revisoes.saveAndFlush(atual)
        revisoes.save(novaRevisao(id, atual.versao + 1, assunto, corpo, autorId))
        header.assunto = assunto
        cabecalhos.save(header)
        return detalhe(header)
    }

    @Transactional(readOnly = true)
    override fun buscar(id: UUID): TemplateDetalhe =
        detalhe(cabecalhos.findById(id).orElseThrow { naoEncontrado() })

    @Transactional(readOnly = true)
    override fun listar(pageable: Pageable): Page<TemplateResumo> =
        cabecalhos.findAll(pageable).map { TemplateResumo(it.id!!, it.nome!!, it.assunto!!) }

    @Transactional(readOnly = true)
    override fun revisaoAtualPorNome(nome: String): RevisaoAtual? {
        val header = cabecalhos.findByNome(nome).orElse(null) ?: return null
        val atual = revisoes.findByTemplateIdAndStatus(header.id!!, CURRENT).orElse(null) ?: return null
        return RevisaoAtual(atual.assunto!!, atual.corpo!!)
    }

    private fun detalhe(header: TemplateComunicacaoJpaEntity): TemplateDetalhe {
        val itens = revisoes.findByTemplateIdOrderByVersaoDesc(header.id!!).map { paraItem(it) }
        val corrente = itens.first { it.status == CURRENT }
        return TemplateDetalhe(header.id!!, header.nome!!, corrente.assunto, corrente.corpo, itens)
    }

    private fun novaRevisao(
        templateId: UUID,
        versao: Int,
        assunto: String,
        corpo: String,
        autorId: UUID,
    ): TemplateRevisaoJpaEntity {
        val entity = TemplateRevisaoJpaEntity()
        entity.id = Uuids.v7()
        entity.templateId = templateId
        entity.versao = versao
        entity.assunto = assunto
        entity.corpo = corpo
        entity.status = CURRENT
        entity.autorId = autorId
        return entity
    }

    private fun paraItem(entity: TemplateRevisaoJpaEntity) = RevisaoItem(
        entity.versao,
        entity.assunto!!,
        entity.corpo!!,
        entity.autorId!!,
        entity.createdAt!!,
        entity.status!!,
    )

    private fun naoEncontrado() = RecursoNaoEncontradoException("Template não encontrado.")

    companion object {
        const val CURRENT = "CURRENT"
        const val ARCHIVED = "ARCHIVED"
    }
}
