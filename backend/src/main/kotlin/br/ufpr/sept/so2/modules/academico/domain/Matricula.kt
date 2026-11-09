package br.ufpr.sept.so2.modules.academico.domain

import java.time.OffsetDateTime
import java.util.UUID

class Matricula(
    val id: UUID,
    val idAluno: UUID,
    val idDisciplina: UUID,
    val idPeriodoLetivo: UUID,
    val createdAt: OffsetDateTime,
)
