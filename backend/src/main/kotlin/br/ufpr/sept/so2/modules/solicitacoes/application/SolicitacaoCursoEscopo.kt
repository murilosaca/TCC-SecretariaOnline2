package br.ufpr.sept.so2.modules.solicitacoes.application

import br.ufpr.sept.so2.modules.academico.application.ports.AlunoRepository
import br.ufpr.sept.so2.modules.academico.application.ports.CursoEscopoPort
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.modules.iam.domain.IdentificadorLogin
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class SolicitacaoCursoEscopo(
    private val cursoEscopoPort: CursoEscopoPort,
    private val alunoRepository: AlunoRepository,
    private val usuarioRepository: UsuarioRepository,
) {
    fun solicitanteIdsNoEscopo(usuarioId: UUID): Set<UUID> {
        val cursoIds = cursoEscopoPort.cursoIdsDoUsuario(usuarioId)
        if (cursoIds.isEmpty()) {
            return emptySet()
        }
        val ids = mutableSetOf<UUID>()
        for (aluno in alunoRepository.findByIdCursoIn(cursoIds)) {
            val porGrr = IdentificadorLogin.tryParse(aluno.grr.value)
                ?.let { usuarioRepository.findByIdentificador(it) }
            if (porGrr != null && porGrr.isPresent) {
                ids.add(porGrr.get().id)
            }
            usuarioRepository.findByEmail(aluno.emailInstitucional.value).ifPresent { ids.add(it.id) }
        }
        return ids
    }

    fun solicitanteNoEscopo(atorId: UUID, solicitanteId: UUID): Boolean {
        val cursoIds = cursoEscopoPort.cursoIdsDoUsuario(atorId)
        if (cursoIds.isEmpty()) {
            return false
        }
        val usuario = usuarioRepository.findById(solicitanteId).orElse(null) ?: return false
        val cursoId = cursoEscopoPort.cursoIdDoAlunoPorGrrOuEmail(
            usuario.grr?.value,
            usuario.emailInstitucional.value,
        )
        return cursoId != null && cursoId in cursoIds
    }
}
