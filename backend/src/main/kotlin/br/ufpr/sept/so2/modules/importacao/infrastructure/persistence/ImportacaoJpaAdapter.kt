package br.ufpr.sept.so2.modules.importacao.infrastructure.persistence

import br.ufpr.sept.so2.modules.importacao.application.ports.ExportacaoRepository
import br.ufpr.sept.so2.modules.importacao.application.ports.ImportacaoRepository
import br.ufpr.sept.so2.modules.importacao.domain.ExportacaoJob
import br.ufpr.sept.so2.modules.importacao.domain.ImportacaoJob
import br.ufpr.sept.so2.modules.importacao.domain.ImportacaoLinha
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.UUID

@Component
class ImportacaoJpaAdapter(
    private val jobs: ImportJobJpaRepository,
    private val linhas: ImportLinhaJpaRepository,
    private val objectMapper: ObjectMapper,
) : ImportacaoRepository {

    @Transactional
    override fun save(job: ImportacaoJob): ImportacaoJob {
        jobs.save(paraEntidade(job))
        job.linhas.forEach { linha ->
            linhas.save(
                ImportLinhaJpaEntity(
                    id = linha.id,
                    importJobId = job.id,
                    numero = linha.numero,
                    status = linha.status,
                    mensagem = linha.mensagem,
                    conteudo = objectMapper.writeValueAsString(linha.valores),
                ),
            )
        }
        return findById(job.id) ?: job
    }

    @Transactional(readOnly = true)
    override fun findById(id: UUID): ImportacaoJob? =
        jobs.findById(id).map { entidade ->
            paraDominio(entidade, linhas.findByImportJobIdOrderByNumeroAsc(id).map(::paraLinha))
        }.orElse(null)

    @Transactional(readOnly = true)
    override fun listar(operadorId: UUID, pageable: Pageable): Page<ImportacaoJob> =
        jobs.findByOperadorIdOrderByCreatedAtDesc(operadorId, pageable).map { paraDominio(it, emptyList()) }

    @Transactional
    override fun atualizarValidacao(
        id: UUID,
        linhasAtualizadas: List<ImportacaoLinha>,
        status: String,
        validCount: Int,
        errorCount: Int,
        warningCount: Int,
        mensagem: String?,
    ) {
        val job = jobs.findById(id).orElse(null) ?: return
        val porId = linhas.findByImportJobIdOrderByNumeroAsc(id).associateBy { it.id }
        linhasAtualizadas.forEach { linha ->
            val entity = porId[linha.id] ?: return@forEach
            entity.status = linha.status
            entity.mensagem = linha.mensagem
            linhas.save(entity)
        }
        job.status = status
        job.validCount = validCount
        job.errorCount = errorCount
        job.warningCount = warningCount
        job.mensagem = mensagem
        job.updatedAt = OffsetDateTime.now()
        jobs.save(job)
    }

    @Transactional
    override fun concluir(id: UUID, status: String, importadas: Int, mensagem: String?) {
        val job = jobs.findById(id).orElse(null) ?: return
        job.status = status
        job.importadas = importadas
        job.mensagem = mensagem
        job.updatedAt = OffsetDateTime.now()
        jobs.save(job)
    }

    private fun paraEntidade(job: ImportacaoJob) = ImportJobJpaEntity(
        id = job.id,
        kind = job.kind,
        status = job.status,
        operadorId = job.operadorId,
        nomeArquivo = job.nomeArquivo,
        checksumSha256 = job.checksumSha256,
        totalLinhas = job.totalLinhas,
        validCount = job.validCount,
        errorCount = job.errorCount,
        warningCount = job.warningCount,
        importadas = job.importadas,
        mensagem = job.mensagem,
        createdAt = job.createdAt,
        updatedAt = job.updatedAt,
    )

    private fun paraDominio(entity: ImportJobJpaEntity, linhasJob: List<ImportacaoLinha>) = ImportacaoJob(
        id = entity.id!!,
        kind = entity.kind,
        status = entity.status,
        operadorId = entity.operadorId!!,
        nomeArquivo = entity.nomeArquivo,
        checksumSha256 = entity.checksumSha256,
        totalLinhas = entity.totalLinhas,
        validCount = entity.validCount,
        errorCount = entity.errorCount,
        warningCount = entity.warningCount,
        importadas = entity.importadas,
        mensagem = entity.mensagem,
        createdAt = entity.createdAt!!,
        updatedAt = entity.updatedAt!!,
        linhas = linhasJob,
    )

    private fun paraLinha(entity: ImportLinhaJpaEntity) = ImportacaoLinha(
        id = entity.id!!,
        numero = entity.numero,
        status = entity.status,
        mensagem = entity.mensagem,
        valores = objectMapper.readValue(entity.conteudo, MAPA),
    )

    companion object {
        private val MAPA: TypeReference<Map<String, String>> = object : TypeReference<Map<String, String>>() {}
    }
}

@Component
class ExportacaoJpaAdapter(
    private val jobs: ExportJobJpaRepository,
) : ExportacaoRepository {

    override fun save(job: ExportacaoJob): ExportacaoJob {
        jobs.save(paraEntidade(job))
        return findById(job.id) ?: job
    }

    override fun findById(id: UUID): ExportacaoJob? =
        jobs.findById(id).map(::paraDominio).orElse(null)

    override fun listar(operadorId: UUID, pageable: Pageable): Page<ExportacaoJob> =
        jobs.findByOperadorIdOrderByCreatedAtDesc(operadorId, pageable).map(::paraDominio)

    @Transactional
    override fun marcarPronto(id: UUID, storageKey: String, nomeArquivo: String, expiresAt: OffsetDateTime) {
        val job = jobs.findById(id).orElse(null) ?: return
        job.status = br.ufpr.sept.so2.modules.importacao.domain.ExportacaoStatus.PRONTO
        job.storageKey = storageKey
        job.nomeArquivo = nomeArquivo
        job.expiresAt = expiresAt
        job.updatedAt = OffsetDateTime.now()
        jobs.save(job)
    }

    @Transactional
    override fun marcarFalha(id: UUID, mensagem: String) {
        val job = jobs.findById(id).orElse(null) ?: return
        job.status = br.ufpr.sept.so2.modules.importacao.domain.ExportacaoStatus.FAILED
        job.mensagem = mensagem
        job.updatedAt = OffsetDateTime.now()
        jobs.save(job)
    }

    @Transactional
    override fun marcarExpirado(id: UUID) {
        val job = jobs.findById(id).orElse(null) ?: return
        job.status = br.ufpr.sept.so2.modules.importacao.domain.ExportacaoStatus.EXPIRADO
        job.updatedAt = OffsetDateTime.now()
        jobs.save(job)
    }

    private fun paraEntidade(job: ExportacaoJob) = ExportJobJpaEntity(
        id = job.id,
        kind = job.kind,
        status = job.status,
        operadorId = job.operadorId,
        filtros = job.filtrosJson,
        storageKey = job.storageKey,
        nomeArquivo = job.nomeArquivo,
        expiresAt = job.expiresAt,
        mensagem = job.mensagem,
        createdAt = job.createdAt,
        updatedAt = job.updatedAt,
    )

    private fun paraDominio(entity: ExportJobJpaEntity) = ExportacaoJob(
        id = entity.id!!,
        kind = entity.kind,
        status = entity.status,
        operadorId = entity.operadorId!!,
        filtrosJson = entity.filtros,
        storageKey = entity.storageKey,
        nomeArquivo = entity.nomeArquivo,
        expiresAt = entity.expiresAt,
        mensagem = entity.mensagem,
        createdAt = entity.createdAt!!,
        updatedAt = entity.updatedAt!!,
    )
}
