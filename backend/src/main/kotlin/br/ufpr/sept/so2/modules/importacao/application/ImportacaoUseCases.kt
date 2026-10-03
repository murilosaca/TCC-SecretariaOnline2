package br.ufpr.sept.so2.modules.importacao.application

import br.ufpr.sept.so2.modules.academico.application.ports.CursoEscopoPort
import br.ufpr.sept.so2.modules.arquivos.application.ports.ObjectStoragePort
import br.ufpr.sept.so2.modules.iam.application.ports.AuditLogPort
import br.ufpr.sept.so2.modules.iam.application.ports.OutboxPort
import br.ufpr.sept.so2.modules.importacao.application.ports.ExportacaoConteudoPort
import br.ufpr.sept.so2.modules.importacao.application.ports.ExportacaoRepository
import br.ufpr.sept.so2.modules.importacao.application.ports.ImportacaoGravador
import br.ufpr.sept.so2.modules.importacao.application.ports.ImportacaoRepository
import br.ufpr.sept.so2.modules.importacao.domain.ExportacaoJob
import br.ufpr.sept.so2.modules.importacao.domain.ExportacaoKinds
import br.ufpr.sept.so2.modules.importacao.domain.ExportacaoStatus
import br.ufpr.sept.so2.modules.importacao.domain.ImportacaoJob
import br.ufpr.sept.so2.modules.importacao.domain.ImportacaoKinds
import br.ufpr.sept.so2.modules.importacao.domain.ImportacaoLinha
import br.ufpr.sept.so2.modules.importacao.domain.ImportacaoStatus
import br.ufpr.sept.so2.modules.importacao.domain.PlanilhaLeitor
import br.ufpr.sept.so2.modules.importacao.domain.executarLotes
import br.ufpr.sept.so2.shared.domain.exception.AcessoNegadoException
import br.ufpr.sept.so2.shared.domain.exception.ConflitoEstadoException
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import br.ufpr.sept.so2.shared.infrastructure.Uuids
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate
import java.security.MessageDigest
import java.time.Duration
import java.time.OffsetDateTime
import java.util.UUID

@Service
class ReceberImportacaoUseCase(
    private val repository: ImportacaoRepository,
    private val cursoEscopoPort: CursoEscopoPort,
    private val depoisDoCommit: DepoisDoCommit,
    private val worker: ImportacaoWorker,
) {
    @Transactional
    fun execute(operadorId: UUID, kind: String, nome: String, bytes: ByteArray): ImportacaoJob {
        val kindOk = ImportacaoKinds.exigir(kind)
        if (kindOk != ImportacaoKinds.USUARIOS && cursoEscopoPort.cursoIdsDoUsuario(operadorId).isEmpty()) {
            throw AcessoNegadoException("Nenhum curso vinculado à sua secretaria foi encontrado.")
        }
        if (bytes.size > PlanilhaLeitor.MAX_BYTES) {
            throw DadoInvalidoException("Arquivo excede 20 MB.")
        }
        val extensao = nome.substringAfterLast('.', "").lowercase()
        if (extensao !in setOf("csv", "txt", "xlsx")) {
            throw DadoInvalidoException("Envie um arquivo CSV ou XLSX.")
        }
        val agora = OffsetDateTime.now()
        val id = Uuids.v7()
        val registros = try {
            PlanilhaLeitor.ler(nome, bytes)
        } catch (ex: DadoInvalidoException) {
            throw ex
        } catch (_: Exception) {
            val falha = jobVazio(id, operadorId, kindOk, nome, bytes, agora, ImportacaoStatus.FAILED, "Não foi possível ler a planilha.")
            return repository.save(falha)
        }
        val linhas = registros.mapIndexed { indice, valores ->
            ImportacaoLinha(Uuids.v7(), indice + 2, ImportacaoStatus.PENDING, null, valores)
        }
        val job = ImportacaoJob(
            id = id,
            kind = kindOk,
            status = ImportacaoStatus.RECEBIDO,
            operadorId = operadorId,
            nomeArquivo = nome.ifBlank { "$kindOk.csv" },
            checksumSha256 = checksum(bytes),
            totalLinhas = linhas.size,
            validCount = 0,
            errorCount = 0,
            warningCount = 0,
            importadas = 0,
            mensagem = null,
            createdAt = agora,
            updatedAt = agora,
            linhas = linhas,
        )
        val salvo = repository.save(job)
        depoisDoCommit.executar { worker.validar(salvo.id) }
        return salvo
    }

    private fun jobVazio(
        id: UUID,
        operadorId: UUID,
        kind: String,
        nome: String,
        bytes: ByteArray,
        agora: OffsetDateTime,
        status: String,
        mensagem: String,
    ) = ImportacaoJob(
        id, kind, status, operadorId, nome.ifBlank { "$kind.csv" }, checksum(bytes),
        0, 0, 0, 0, 0, mensagem, agora, agora, emptyList(),
    )

    companion object {
        fun checksum(bytes: ByteArray): String =
            MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
    }
}

@Service
class ValidarImportacaoUseCase(
    private val repository: ImportacaoRepository,
    private val validador: ValidarLinhasImportacao,
) {
    @Transactional
    fun execute(jobId: UUID) {
        val job = repository.findById(jobId) ?: return
        if (job.status != ImportacaoStatus.RECEBIDO && job.status != ImportacaoStatus.VALIDANDO) {
            return
        }
        try {
            val avaliadas = validador.validar(job.operadorId, job.kind, job.linhas)
            val erros = avaliadas.count { it.status == ImportacaoStatus.INVALID }
            val avisos = avaliadas.count { it.status == ImportacaoStatus.WARNING }
            val validas = avaliadas.count { it.status == ImportacaoStatus.VALID }
            repository.atualizarValidacao(
                job.id,
                avaliadas,
                ImportacaoStatus.VALIDATED,
                validas,
                erros,
                avisos,
                if (erros > 0) "Corrija os $erros erros antes de confirmar." else null,
            )
        } catch (ex: Exception) {
            repository.atualizarValidacao(
                job.id,
                emptyList(),
                ImportacaoStatus.FAILED,
                0,
                job.totalLinhas,
                0,
                ex.message ?: "Falha na validação.",
            )
        }
    }
}

@Component
class ImportacaoWorker(
    private val validarImportacaoUseCase: ValidarImportacaoUseCase,
) {
    @Async("so2TaskExecutor")
    fun validar(jobId: UUID) {
        validarImportacaoUseCase.execute(jobId)
    }
}

@Service
class ObterImportacaoUseCase(
    private val repository: ImportacaoRepository,
) {
    @Transactional(readOnly = true)
    fun execute(operadorId: UUID, id: UUID): ImportacaoJob =
        proprio(repository.findById(id), operadorId)

    @Transactional(readOnly = true)
    fun listar(operadorId: UUID, pageable: Pageable): Page<ImportacaoJob> =
        repository.listar(operadorId, pageable)

    companion object {
        fun proprio(job: ImportacaoJob?, operadorId: UUID): ImportacaoJob {
            if (job == null || job.operadorId != operadorId) {
                throw RecursoNaoEncontradoException("Importação não encontrada.")
            }
            return job
        }
    }
}

@Service
class ConfirmarImportacaoUseCase(
    transactionManager: PlatformTransactionManager,
    private val repository: ImportacaoRepository,
    private val gravador: ImportacaoGravador,
    private val outboxPort: OutboxPort,
    private val auditLogPort: AuditLogPort,
    private val objectMapper: ObjectMapper,
) {
    private val tx = TransactionTemplate(transactionManager).apply {
        propagationBehavior = TransactionDefinition.PROPAGATION_REQUIRES_NEW
    }

    fun execute(operadorId: UUID, jobId: UUID, ip: String?): ImportacaoJob {
        val job = tx.execute { ObterImportacaoUseCase.proprio(repository.findById(jobId), operadorId) }!!
        if (job.status != ImportacaoStatus.VALIDATED || job.errorCount > 0) {
            throw DadoInvalidoException("Corrija os erros antes de confirmar.")
        }
        val validas = job.linhas
            .filter { it.status == ImportacaoStatus.VALID || it.status == ImportacaoStatus.WARNING }
            .sortedBy { it.numero }
        val resultado = executarLotes(validas.chunked(LOTE)) { lote ->
            tx.executeWithoutResult { gravador.gravar(operadorId, job.kind, lote) }
        }
        val mensagem = when (resultado.status) {
            ImportacaoStatus.SUCCESS -> "${resultado.importadas} registros importados com sucesso."
            else -> "${resultado.importadas} importados, ${resultado.naoProcessadas} não processados. ${resultado.mensagem.orEmpty()}".trim()
        }
        return tx.execute {
            repository.concluir(job.id, resultado.status, resultado.importadas, mensagem)
            val payload = objectMapper.writeValueAsString(
                mapOf(
                    "entidade" to "importacao",
                    "operadorId" to operadorId.toString(),
                    "kind" to job.kind,
                    "checksum" to job.checksumSha256,
                    "totalLinhas" to job.totalLinhas,
                    "importadas" to resultado.importadas,
                    "status" to resultado.status,
                ),
            )
            auditLogPort.append("importacao.concluida", operadorId, payload, ip)
            outboxPort.enqueue("importacao.concluida", payload)
            repository.findById(job.id)!!
        }!!
    }

    companion object {
        const val LOTE: Int = 1000
    }
}

@Service
class SolicitarExportacaoUseCase(
    private val repository: ExportacaoRepository,
    private val cursoEscopoPort: CursoEscopoPort,
    private val objectMapper: ObjectMapper,
    private val depoisDoCommit: DepoisDoCommit,
    private val worker: ExportacaoWorker,
) {
    @Transactional
    fun execute(operadorId: UUID, kind: String, filtros: Map<String, String?>): ExportacaoJob {
        val kindOk = ExportacaoKinds.exigir(kind)
        if (cursoEscopoPort.cursoIdsDoUsuario(operadorId).isEmpty()) {
            throw AcessoNegadoException("Nenhum curso vinculado à sua secretaria foi encontrado.")
        }
        val agora = OffsetDateTime.now()
        val job = ExportacaoJob(
            id = Uuids.v7(),
            kind = kindOk,
            status = ExportacaoStatus.PROCESSANDO,
            operadorId = operadorId,
            filtrosJson = objectMapper.writeValueAsString(filtros),
            storageKey = null,
            nomeArquivo = null,
            expiresAt = null,
            mensagem = null,
            createdAt = agora,
            updatedAt = agora,
        )
        val salvo = repository.save(job)
        depoisDoCommit.executar { worker.gerar(salvo.id) }
        return salvo
    }
}

@Service
class GerarExportacaoUseCase(
    private val repository: ExportacaoRepository,
    private val conteudo: ExportacaoConteudoPort,
    private val storage: ObjectStoragePort,
    private val outboxPort: OutboxPort,
    private val auditLogPort: AuditLogPort,
    private val objectMapper: ObjectMapper,
) {
    @Transactional
    fun execute(jobId: UUID) {
        val job = repository.findById(jobId) ?: return
        if (job.status != ExportacaoStatus.PROCESSANDO) {
            return
        }
        try {
            val filtros = lerFiltros(job.filtrosJson)
            val bytes = conteudo.csv(job.operadorId, job.kind, filtros)
            val nome = "${job.kind}.csv"
            val key = "exportacoes/${job.id}/$nome"
            storage.putObject(key, "text/csv", bytes)
            val expira = OffsetDateTime.now().plusDays(7)
            repository.marcarPronto(job.id, key, nome, expira)
            val payload = objectMapper.writeValueAsString(
                mapOf(
                    "entidade" to "exportacao",
                    "exportacaoId" to job.id.toString(),
                    "kind" to job.kind,
                    "operadorId" to job.operadorId.toString(),
                ),
            )
            outboxPort.enqueue("exportacao.pronta", payload)
            auditLogPort.append("exportacao.pronta", job.operadorId, payload, null)
        } catch (ex: Exception) {
            repository.marcarFalha(job.id, ex.message ?: "Falha ao gerar a exportação.")
        }
    }

    private fun lerFiltros(json: String?): Map<String, String?> {
        if (json.isNullOrBlank()) {
            return emptyMap()
        }
        val node = objectMapper.readTree(json)
        return node.fieldNames().asSequence().associateWith { nome ->
            val valor = node.path(nome).asText(null)
            if (valor.isNullOrBlank()) null else valor
        }
    }
}

@Component
class ExportacaoWorker(
    private val gerarExportacaoUseCase: GerarExportacaoUseCase,
) {
    @Async("so2TaskExecutor")
    fun gerar(jobId: UUID) {
        gerarExportacaoUseCase.execute(jobId)
    }
}

@Service
class ConsultarExportacaoUseCase(
    private val repository: ExportacaoRepository,
    private val storage: ObjectStoragePort,
) {
    @Transactional
    fun listar(operadorId: UUID, pageable: Pageable): Page<ExportacaoJob> {
        repository.listar(operadorId, pageable).content.forEach { expirarSePreciso(it) }
        return repository.listar(operadorId, pageable)
    }

    @Transactional
    fun obter(operadorId: UUID, id: UUID): ExportacaoJob {
        val job = proprio(id, operadorId)
        expirarSePreciso(job)
        return repository.findById(id) ?: job
    }

    fun url(operadorId: UUID, id: UUID): String {
        val job = obter(operadorId, id)
        if (job.status != ExportacaoStatus.PRONTO || job.storageKey.isNullOrBlank()) {
            throw ConflitoEstadoException("Exportação indisponível para download.")
        }
        return storage.presignGetUrl(job.storageKey, Duration.ofMinutes(15), job.nomeArquivo)
    }

    private fun proprio(id: UUID, operadorId: UUID): ExportacaoJob {
        val job = repository.findById(id) ?: throw RecursoNaoEncontradoException("Exportação não encontrada.")
        if (job.operadorId != operadorId) {
            throw RecursoNaoEncontradoException("Exportação não encontrada.")
        }
        return job
    }

    private fun expirarSePreciso(job: ExportacaoJob) {
        if (job.expirada(OffsetDateTime.now())) {
            repository.marcarExpirado(job.id)
        }
    }
}
