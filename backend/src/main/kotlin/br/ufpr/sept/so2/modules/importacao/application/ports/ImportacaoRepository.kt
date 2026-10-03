package br.ufpr.sept.so2.modules.importacao.application.ports

import br.ufpr.sept.so2.modules.importacao.domain.ExportacaoJob
import br.ufpr.sept.so2.modules.importacao.domain.ImportacaoJob
import br.ufpr.sept.so2.modules.importacao.domain.ImportacaoLinha
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.OffsetDateTime
import java.util.UUID

interface ImportacaoRepository {
    fun save(job: ImportacaoJob): ImportacaoJob

    fun findById(id: UUID): ImportacaoJob?

    fun listar(operadorId: UUID, pageable: Pageable): Page<ImportacaoJob>

    fun atualizarValidacao(
        id: UUID,
        linhas: List<ImportacaoLinha>,
        status: String,
        validCount: Int,
        errorCount: Int,
        warningCount: Int,
        mensagem: String?,
    )

    fun concluir(id: UUID, status: String, importadas: Int, mensagem: String?)
}

interface ExportacaoRepository {
    fun save(job: ExportacaoJob): ExportacaoJob

    fun findById(id: UUID): ExportacaoJob?

    fun listar(operadorId: UUID, pageable: Pageable): Page<ExportacaoJob>

    fun marcarPronto(id: UUID, storageKey: String, nomeArquivo: String, expiresAt: OffsetDateTime)

    fun marcarFalha(id: UUID, mensagem: String)

    fun marcarExpirado(id: UUID)
}

interface ImportacaoGravador {
    fun gravar(operadorId: UUID, kind: String, linhas: List<ImportacaoLinha>)
}

interface ExportacaoConteudoPort {
    fun csv(operadorId: UUID, kind: String, filtros: Map<String, String?>): ByteArray
}

interface AlocacaoProfessorPort {
    fun existe(idUsuario: UUID, idDisciplina: UUID): Boolean

    fun salvar(id: UUID, idUsuario: UUID, idDisciplina: UUID, idCurso: UUID, createdAt: OffsetDateTime)
}
