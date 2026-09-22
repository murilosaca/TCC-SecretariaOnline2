package br.ufpr.sept.so2.modules.coordenacao.domain

import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException

object ConfigCursoRegras {
    const val HORAS_MIN: Int = 0
    const val HORAS_MAX: Int = 1000
    const val REGIMENTO_MAX: Int = 10_000
    private val CALENDARIOS = setOf(15, 18)
    private val MEMBROS = setOf(1, 2)
    const val MSG_HORAS: String = "Valor deve ser entre 0 e 1000"

    fun aplicar(atual: ConfigCurso, patch: ConfigCursoPatch): ConfigCurso {
        validar(patch)
        return atual.copy(
            horasFormativasMinimas = patch.horasFormativasMinimas ?: atual.horasFormativasMinimas,
            duracaoCalendario = patch.duracaoCalendario ?: atual.duracaoCalendario,
            bancaMembrosExternos = patch.bancaMembrosExternos ?: atual.bancaMembrosExternos,
            bancaModalidade = patch.bancaModalidade?.let { BancaModalidade.from(it) } ?: atual.bancaModalidade,
            regimento = patch.regimento ?: atual.regimento,
        )
    }

    fun alteracoes(antes: ConfigCurso, depois: ConfigCurso): List<CampoAlterado> {
        val itens = mutableListOf<CampoAlterado>()
        if (antes.horasFormativasMinimas != depois.horasFormativasMinimas) {
            itens += CampoAlterado(
                "horasFormativasMinimas",
                antes.horasFormativasMinimas,
                depois.horasFormativasMinimas,
            )
        }
        if (antes.duracaoCalendario != depois.duracaoCalendario) {
            itens += CampoAlterado("duracaoCalendario", antes.duracaoCalendario, depois.duracaoCalendario)
        }
        if (antes.bancaMembrosExternos != depois.bancaMembrosExternos) {
            itens += CampoAlterado(
                "bancaMembrosExternos",
                antes.bancaMembrosExternos,
                depois.bancaMembrosExternos,
            )
        }
        if (antes.bancaModalidade != depois.bancaModalidade) {
            itens += CampoAlterado(
                "bancaModalidade",
                antes.bancaModalidade.name,
                depois.bancaModalidade.name,
            )
        }
        if (antes.regimento != depois.regimento) {
            itens += CampoAlterado("regimento", antes.regimento, depois.regimento)
        }
        return itens
    }

    private fun validar(patch: ConfigCursoPatch) {
        if (patch.vazio()) {
            throw DadoInvalidoException("Informe ao menos um campo.")
        }
        validarHoras(patch.horasFormativasMinimas)
        validarCalendario(patch.duracaoCalendario)
        validarMembros(patch.bancaMembrosExternos)
        if (patch.bancaModalidade != null) {
            BancaModalidade.from(patch.bancaModalidade)
        }
        validarRegimento(patch.regimento)
    }

    private fun validarHoras(horas: Int?) {
        if (horas != null && horas !in HORAS_MIN..HORAS_MAX) {
            throw DadoInvalidoException(MSG_HORAS)
        }
    }

    private fun validarCalendario(duracao: Int?) {
        if (duracao != null && duracao !in CALENDARIOS) {
            throw DadoInvalidoException("Duração do calendário deve ser 15 ou 18 semanas.")
        }
    }

    private fun validarMembros(membros: Int?) {
        if (membros != null && membros !in MEMBROS) {
            throw DadoInvalidoException("A banca deve ter 1 ou 2 membros externos.")
        }
    }

    private fun validarRegimento(regimento: String?) {
        if (regimento != null && regimento.length > REGIMENTO_MAX) {
            throw DadoInvalidoException("Regimento deve ter no máximo 10000 caracteres.")
        }
    }
}
