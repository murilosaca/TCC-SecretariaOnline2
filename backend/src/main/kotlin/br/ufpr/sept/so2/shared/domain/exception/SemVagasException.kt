package br.ufpr.sept.so2.shared.domain.exception

import java.util.UUID

/** F5.6-ERRO-02: 422 type=no-vacancies. A matrícula inteira faz rollback. */
class SemVagasException(
    val disciplinaId: UUID,
    val disciplinaNome: String,
) : DadoInvalidoException("A disciplina $disciplinaNome não tem vagas disponíveis.")
