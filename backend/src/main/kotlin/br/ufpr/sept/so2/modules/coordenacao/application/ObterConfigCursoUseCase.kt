package br.ufpr.sept.so2.modules.coordenacao.application

import br.ufpr.sept.so2.modules.coordenacao.application.ports.CursoConfigPort
import br.ufpr.sept.so2.modules.coordenacao.domain.ConfigCurso
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ObterConfigCursoUseCase(
    private val cursoConfigPort: CursoConfigPort,
) {
    @Transactional(readOnly = true)
    fun execute(cursoId: UUID, usuarioId: UUID, authorities: Collection<String>): ConfigCurso {
        CursoConfigAcesso.exigirCap(authorities)
        val curso = CursoConfigAcesso.exigirDono(
            cursoConfigPort.obterCurso(cursoId, bloquear = false),
            usuarioId,
        )
        return ConfigCursoMontagem.de(curso, cursoConfigPort.obterParametros(cursoId))
    }
}
