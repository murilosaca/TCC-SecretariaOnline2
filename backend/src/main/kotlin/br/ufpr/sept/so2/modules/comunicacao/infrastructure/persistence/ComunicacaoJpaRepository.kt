package br.ufpr.sept.so2.modules.comunicacao.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
import java.util.UUID

interface ComunicacaoJpaRepository : JpaRepository<ComunicacaoJpaEntity, UUID> {
    fun findByAutorId(autorId: UUID): List<ComunicacaoJpaEntity>

    fun findByTitulo(titulo: String): Optional<ComunicacaoJpaEntity>
}

interface ComunicacaoEntregaJpaRepository : JpaRepository<ComunicacaoEntregaJpaEntity, UUID> {
    fun findByDestinatarioIdAndInAppTrue(destinatarioId: UUID): List<ComunicacaoEntregaJpaEntity>

    fun findByComunicacaoIdAndDestinatarioId(comunicacaoId: UUID, destinatarioId: UUID): Optional<ComunicacaoEntregaJpaEntity>
}

interface TurmaJpaRepository : JpaRepository<TurmaJpaEntity, UUID> {
    fun findByProfessorIdOrderByNomeAsc(professorId: UUID): List<TurmaJpaEntity>

    fun findByIdAndProfessorId(id: UUID, professorId: UUID): Optional<TurmaJpaEntity>

    fun findByProfessorIdAndCodigo(professorId: UUID, codigo: String): Optional<TurmaJpaEntity>
}

interface TurmaAlunoJpaRepository : JpaRepository<TurmaAlunoJpaEntity, TurmaAlunoId> {
    fun findByIdTurma(idTurma: UUID): List<TurmaAlunoJpaEntity>

    fun existsByIdTurmaAndIdAluno(idTurma: UUID, idAluno: UUID): Boolean
}
