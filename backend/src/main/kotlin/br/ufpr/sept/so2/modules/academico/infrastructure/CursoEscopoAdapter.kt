package br.ufpr.sept.so2.modules.academico.infrastructure

import br.ufpr.sept.so2.modules.academico.application.ports.AlunoRepository
import br.ufpr.sept.so2.modules.academico.application.ports.CursoEscopoPort
import br.ufpr.sept.so2.modules.academico.application.ports.CursoRepository
import br.ufpr.sept.so2.modules.academico.application.ports.CursoSecretarioRepository
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class CursoEscopoAdapter(
    private val cursoSecretarioRepository: CursoSecretarioRepository,
    private val cursoRepository: CursoRepository,
    private val alunoRepository: AlunoRepository,
) : CursoEscopoPort {
    override fun cursoIdsDoUsuario(usuarioId: UUID): Set<UUID> {
        val comoSecretario = cursoSecretarioRepository.findCursoIdsByUsuarioId(usuarioId)
        val comoCoordenador = cursoRepository.findIdsByCoordenador(usuarioId)
        return comoSecretario + comoCoordenador
    }

    override fun cursoIdDoAlunoPorGrrOuEmail(grr: String?, emailInstitucional: String?): UUID? {
        if (!grr.isNullOrBlank()) {
            val porGrr = alunoRepository.findByGrr(grr)
            if (porGrr.isPresent) {
                return porGrr.get().idCurso
            }
        }
        if (!emailInstitucional.isNullOrBlank()) {
            val porEmail = alunoRepository.findByEmailInstitucional(emailInstitucional)
            if (porEmail.isPresent) {
                return porEmail.get().idCurso
            }
        }
        return null
    }
}
