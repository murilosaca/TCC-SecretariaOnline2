package br.ufpr.sept.so2.modules.solicitacoes.application

import br.ufpr.sept.so2.modules.iam.application.ports.AuditLogPort
import br.ufpr.sept.so2.modules.solicitacoes.application.ports.SolicitacaoRepository
import br.ufpr.sept.so2.modules.solicitacoes.application.ports.TipoSolicitacaoRepository
import br.ufpr.sept.so2.modules.solicitacoes.application.ports.TipoSolicitacaoVersaoPort
import br.ufpr.sept.so2.modules.solicitacoes.domain.TipoSolicitacao
import br.ufpr.sept.so2.shared.domain.exception.ConflitoEstadoException
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import br.ufpr.sept.so2.shared.domain.exception.TipoSolicitacaoEmUsoException
import br.ufpr.sept.so2.shared.infrastructure.Uuids
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.UUID

data class TipoEditor(
    val tipo: TipoSolicitacao,
    val podeSalvar: Boolean,
    val podeExcluir: Boolean,
)

@Service
class ListarTiposSolicitacaoAdminUseCase(
    private val tipoRepository: TipoSolicitacaoRepository,
    private val versaoPort: TipoSolicitacaoVersaoPort,
    private val solicitacaoRepository: SolicitacaoRepository,
) {
    fun execute(pageable: Pageable): Page<TipoEditor> {
        val size = pageable.pageSize.coerceIn(1, 100)
        val numero = pageable.pageNumber.coerceAtLeast(0)
        return tipoRepository.findAll(PageRequest.of(numero, size)).map { editor(it) }
    }

    fun buscar(id: UUID): TipoEditor = editor(carregar(id))

    private fun editor(tipo: TipoSolicitacao): TipoEditor {
        val rascunho = tipo.status == TipoSolicitacao.DRAFT
        val semHistorico = versaoPort.count(tipo.id) == 0L &&
            solicitacaoRepository.countByTipoId(tipo.id) == 0L
        return TipoEditor(tipo, rascunho, rascunho && semHistorico)
    }

    private fun carregar(id: UUID): TipoSolicitacao =
        tipoRepository.findById(id).orElseThrow { RecursoNaoEncontradoException("Tipo de solicitação não encontrado.") }
}

@Service
class CriarTipoSolicitacaoUseCase(
    private val validator: TipoSolicitacaoSchemaValidator,
    private val gravacao: TipoSolicitacaoGravacao,
) {
    fun execute(
        codigo: String,
        nome: String,
        descricao: String?,
        prazoDias: Int,
        formSchema: String,
        workflowJson: String,
        atorId: UUID,
        ip: String?,
    ): TipoSolicitacao {
        validator.validar(formSchema, workflowJson)
        return gravacao.criar(codigo, nome, descricao, prazoDias, formSchema, workflowJson, atorId, ip)
    }
}

@Service
class SalvarRascunhoTipoSolicitacaoUseCase(
    private val validator: TipoSolicitacaoSchemaValidator,
    private val gravacao: TipoSolicitacaoGravacao,
) {
    fun execute(
        id: UUID,
        nome: String?,
        descricao: String?,
        prazoDias: Int?,
        formSchema: String,
        workflowJson: String,
        atorId: UUID,
        ip: String?,
    ): TipoSolicitacao {
        validator.validar(formSchema, workflowJson)
        return gravacao.salvarRascunho(id, nome, descricao, prazoDias, formSchema, workflowJson, atorId, ip)
    }
}

@Service
class PublicarTipoSolicitacaoUseCase(
    private val validator: TipoSolicitacaoSchemaValidator,
    private val gravacao: TipoSolicitacaoGravacao,
) {
    fun execute(id: UUID, formSchema: String, workflowJson: String, atorId: UUID, ip: String?): TipoSolicitacao {
        validator.validar(formSchema, workflowJson)
        return gravacao.publicar(id, formSchema, workflowJson, atorId, ip)
    }
}

@Service
class ExcluirTipoSolicitacaoUseCase(
    private val gravacao: TipoSolicitacaoGravacao,
) {
    fun execute(id: UUID, atorId: UUID, ip: String?) {
        gravacao.excluir(id, atorId, ip)
    }
}

@Service
class TipoSolicitacaoGravacao(
    private val tipoRepository: TipoSolicitacaoRepository,
    private val versaoPort: TipoSolicitacaoVersaoPort,
    private val solicitacaoRepository: SolicitacaoRepository,
    private val auditLogPort: AuditLogPort,
) {
    @Transactional
    fun criar(
        codigo: String,
        nome: String,
        descricao: String?,
        prazoDias: Int,
        formSchema: String,
        workflowJson: String,
        atorId: UUID,
        ip: String?,
    ): TipoSolicitacao {
        val codigoOk = codigoTipo(codigo)
        if (tipoRepository.findByCodigo(codigoOk).isPresent) {
            throw ConflitoEstadoException("Já existe um tipo com este código.")
        }
        val agora = OffsetDateTime.now()
        val tipo = TipoSolicitacao(
            Uuids.v7(),
            codigoOk,
            nomeTipo(nome),
            descricao?.trim()?.takeIf { it.isNotEmpty() },
            TipoSolicitacao.DRAFT,
            formSchema,
            workflowJson,
            prazo(prazoDias),
            0,
            agora,
            agora,
        )
        val salvo = tipoRepository.save(tipo)
        auditLogPort.append("request_type.criado", atorId, """{"codigo":"$codigoOk"}""", ip)
        return salvo
    }

    @Transactional
    fun salvarRascunho(
        id: UUID,
        nome: String?,
        descricao: String?,
        prazoDias: Int?,
        formSchema: String,
        workflowJson: String,
        atorId: UUID,
        ip: String?,
    ): TipoSolicitacao {
        val atual = carregar(id)
        if (atual.status != TipoSolicitacao.DRAFT) {
            throw ConflitoEstadoException("Tipo publicado não é editado no lugar. Publique uma nova versão.")
        }
        val salvo = tipoRepository.save(
            copia(atual, nomeTipo(nome ?: atual.nome), descricao, prazoDias ?: atual.prazoDias, formSchema, workflowJson, atual.versao, atual.status),
        )
        auditLogPort.append("request_type.rascunho", atorId, """{"id":"$id"}""", ip)
        return salvo
    }

    @Transactional
    fun publicar(id: UUID, formSchema: String, workflowJson: String, atorId: UUID, ip: String?): TipoSolicitacao {
        val atual = carregar(id)
        val maior = versaoPort.maior(id)
        val nova = maxOf(atual.versao, maior) + 1
        val agora = OffsetDateTime.now()
        versaoPort.inserir(id, nova, formSchema, workflowJson, atorId, agora)
        val salvo = tipoRepository.save(
            copia(atual, atual.nome, atual.descricao, atual.prazoDias, formSchema, workflowJson, nova, TipoSolicitacao.PUBLISHED),
        )
        auditLogPort.append("request_type.publicado", atorId, """{"id":"$id","versao":$nova}""", ip)
        return salvo
    }

    @Transactional
    fun excluir(id: UUID, atorId: UUID, ip: String?) {
        val atual = carregar(id)
        if (atual.status != TipoSolicitacao.DRAFT) {
            throw TipoSolicitacaoEmUsoException("Tipo publicado não pode ser excluído.")
        }
        val versoes = versaoPort.count(id)
        if (versoes > 0) {
            throw TipoSolicitacaoEmUsoException("Há histórico de versão.")
        }
        val solicitacoes = solicitacaoRepository.countByTipoId(id)
        if (solicitacoes > 0) {
            throw TipoSolicitacaoEmUsoException("$solicitacoes solicitações vinculadas")
        }
        auditLogPort.append("request_type.excluido", atorId, """{"codigo":"${atual.codigo}"}""", ip)
        tipoRepository.deleteById(id)
    }

    private fun carregar(id: UUID): TipoSolicitacao =
        tipoRepository.findById(id).orElseThrow { RecursoNaoEncontradoException("Tipo de solicitação não encontrado.") }

    private fun copia(
        atual: TipoSolicitacao,
        nome: String,
        descricao: String?,
        prazoDias: Int,
        formSchema: String,
        workflowJson: String,
        versao: Int,
        status: String,
    ): TipoSolicitacao =
        TipoSolicitacao(
            atual.id,
            atual.codigo,
            nome,
            descricao,
            status,
            formSchema,
            workflowJson,
            prazo(prazoDias),
            versao,
            atual.createdAt,
            OffsetDateTime.now(),
        )

    private fun codigoTipo(codigo: String): String {
        val limpo = codigo.trim().uppercase()
        if (!CODIGO.matches(limpo)) {
            throw DadoInvalidoException("Código do tipo em MAIUSCULAS_COM_UNDERSCORE.")
        }
        return limpo
    }

    private fun nomeTipo(nome: String): String {
        val limpo = nome.trim()
        if (limpo.length < 3 || limpo.length > 200) {
            throw DadoInvalidoException("Nome do tipo inválido.")
        }
        return limpo
    }

    private fun prazo(dias: Int): Int {
        if (dias < 1 || dias > 365) {
            throw DadoInvalidoException("Prazo em dias deve ficar entre 1 e 365.")
        }
        return dias
    }

    companion object {
        private val CODIGO = Regex("^[A-Z][A-Z0-9_]{2,79}$")
    }
}
