package br.ufpr.sept.so2.modules.importacao.domain

import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException

object ImportacaoKinds {
    const val ALUNOS = "alunos"
    const val DISCIPLINAS = "disciplinas"
    const val USUARIOS = "usuarios"
    const val ALOCACAO = "alocacao_professor"

    val TODOS: Set<String> = setOf(ALUNOS, DISCIPLINAS, USUARIOS, ALOCACAO)

    fun exigir(kind: String): String {
        val limpo = kind.trim().lowercase()
        if (limpo !in TODOS) {
            throw DadoInvalidoException("Tipo de importação desconhecido.")
        }
        return limpo
    }

    fun modeloCsv(kind: String): String =
        when (exigir(kind)) {
            ALUNOS ->
                "nome,grr,emailInstitucional,siglaCurso,situacao\n" +
                    "Ana Exemplo,GRR20240099,ana.exemplo@ufpr.br,TADS,MATRICULADO\n"
            DISCIPLINAS ->
                "codigo,nome,cargaHoraria,siglaCurso,periodo,creditos\n" +
                    "DS101,Disciplina exemplo,60,TADS,1,4\n"
            USUARIOS ->
                "nome,email,grr\n" +
                    "Bruno Exemplo,bruno.exemplo@ufpr.br,GRR20240098\n"
            else ->
                "emailProfessor,codigoDisciplina,siglaCurso\n" +
                    "professor.dev@ufpr.br,DS101,TADS\n"
        }
}

object ExportacaoKinds {
    val TODOS: List<String> = listOf(
        "alunos",
        "solicitacoes",
        "presencas",
        "certificados",
        "egressos",
        "formativas",
    )

    fun exigir(kind: String): String {
        val limpo = kind.trim().lowercase()
        if (limpo !in TODOS) {
            throw DadoInvalidoException("Tipo de exportação desconhecido.")
        }
        return limpo
    }

    fun titulo(kind: String): String =
        when (kind) {
            "alunos" -> "Alunos"
            "solicitacoes" -> "Solicitações"
            "presencas" -> "Presenças"
            "certificados" -> "Certificados"
            "egressos" -> "Egressos"
            "formativas" -> "Formativas"
            else -> kind
        }
}
