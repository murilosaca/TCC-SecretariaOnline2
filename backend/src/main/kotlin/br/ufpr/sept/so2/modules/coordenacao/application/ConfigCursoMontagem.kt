package br.ufpr.sept.so2.modules.coordenacao.application

import br.ufpr.sept.so2.modules.coordenacao.application.ports.CursoConfigPort.CursoResumo
import br.ufpr.sept.so2.modules.coordenacao.application.ports.CursoConfigPort.ParametrosBanca
import br.ufpr.sept.so2.modules.coordenacao.domain.BancaModalidade
import br.ufpr.sept.so2.modules.coordenacao.domain.ConfigCurso

object ConfigCursoMontagem {
    fun de(curso: CursoResumo, parametros: ParametrosBanca?): ConfigCurso {
        if (parametros == null) {
            return padrao(curso)
        }
        return ConfigCurso(
            curso.id,
            curso.nome,
            curso.sigla,
            curso.horasFormativasMinimas,
            parametros.duracaoCalendario,
            parametros.bancaMembrosExternos,
            BancaModalidade.from(parametros.bancaModalidade),
            parametros.regimento,
        )
    }

    private fun padrao(curso: CursoResumo) = ConfigCurso(
        curso.id,
        curso.nome,
        curso.sigla,
        curso.horasFormativasMinimas,
        ConfigCurso.DURACAO_PADRAO,
        ConfigCurso.MEMBROS_PADRAO,
        BancaModalidade.PRESENCIAL,
        ConfigCurso.REGIMENTO_PADRAO,
    )
}
