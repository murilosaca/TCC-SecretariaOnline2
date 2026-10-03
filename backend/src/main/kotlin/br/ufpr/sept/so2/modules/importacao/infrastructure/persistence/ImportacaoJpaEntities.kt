package br.ufpr.sept.so2.modules.importacao.infrastructure.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "import_job")
class ImportJobJpaEntity(
    @Id
    @Column(columnDefinition = "uuid", nullable = false, updatable = false)
    var id: UUID? = null,
    @Column(nullable = false, length = 40)
    var kind: String = "",
    @Column(nullable = false, length = 20)
    var status: String = "",
    @Column(name = "operador_id", nullable = false)
    var operadorId: UUID? = null,
    @Column(name = "nome_arquivo", nullable = false)
    var nomeArquivo: String = "",
    @Column(name = "checksum_sha256", nullable = false, length = 64)
    var checksumSha256: String = "",
    @Column(name = "total_linhas", nullable = false)
    var totalLinhas: Int = 0,
    @Column(name = "valid_count", nullable = false)
    var validCount: Int = 0,
    @Column(name = "error_count", nullable = false)
    var errorCount: Int = 0,
    @Column(name = "warning_count", nullable = false)
    var warningCount: Int = 0,
    @Column(nullable = false)
    var importadas: Int = 0,
    @Column(columnDefinition = "TEXT")
    var mensagem: String? = null,
    @Column(name = "created_at", nullable = false)
    var createdAt: OffsetDateTime? = null,
    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime? = null,
)

@Entity
@Table(name = "import_linha")
class ImportLinhaJpaEntity(
    @Id
    @Column(columnDefinition = "uuid", nullable = false, updatable = false)
    var id: UUID? = null,
    @Column(name = "import_job_id", nullable = false)
    var importJobId: UUID? = null,
    @Column(nullable = false)
    var numero: Int = 0,
    @Column(nullable = false, length = 20)
    var status: String = "",
    @Column(columnDefinition = "TEXT")
    var mensagem: String? = null,
    @Column(nullable = false, columnDefinition = "TEXT")
    var conteudo: String = "",
)

@Entity
@Table(name = "export_job")
class ExportJobJpaEntity(
    @Id
    @Column(columnDefinition = "uuid", nullable = false, updatable = false)
    var id: UUID? = null,
    @Column(nullable = false, length = 40)
    var kind: String = "",
    @Column(nullable = false, length = 20)
    var status: String = "",
    @Column(name = "operador_id", nullable = false)
    var operadorId: UUID? = null,
    @Column(columnDefinition = "TEXT")
    var filtros: String? = null,
    @Column(name = "storage_key", length = 512)
    var storageKey: String? = null,
    @Column(name = "nome_arquivo")
    var nomeArquivo: String? = null,
    @Column(name = "expires_at")
    var expiresAt: OffsetDateTime? = null,
    @Column(columnDefinition = "TEXT")
    var mensagem: String? = null,
    @Column(name = "created_at", nullable = false)
    var createdAt: OffsetDateTime? = null,
    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime? = null,
)

@Entity
@Table(name = "alocacao_professor")
class AlocacaoProfessorJpaEntity(
    @Id
    @Column(columnDefinition = "uuid", nullable = false, updatable = false)
    var id: UUID? = null,
    @Column(name = "id_usuario", nullable = false)
    var idUsuario: UUID? = null,
    @Column(name = "id_disciplina", nullable = false)
    var idDisciplina: UUID? = null,
    @Column(name = "id_curso", nullable = false)
    var idCurso: UUID? = null,
    @Column(name = "created_at", nullable = false)
    var createdAt: OffsetDateTime? = null,
)
