package br.ufpr.sept.so2.modules.tcc.application

import br.ufpr.sept.so2.modules.academico.application.ports.CursoEscopoPort
import br.ufpr.sept.so2.modules.tcc.application.ports.TccRepository
import br.ufpr.sept.so2.modules.tcc.domain.Tcc
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ListarTccsDoEscopoUseCase(
    private val tccRepository: TccRepository,
    private val cursoEscopoPort: CursoEscopoPort,
) {
    @Transactional(readOnly = true)
    fun execute(atorId: UUID, situacao: String?, pageable: Pageable): Page<Tcc> {
        val pagina = TccPagina.de(pageable)
        val cursos = TccEscopo.cursos(cursoEscopoPort, atorId)
        if (cursos.isEmpty()) {
            return Page.empty(pagina)
        }
        return tccRepository.findByCursos(cursos, TccPagina.situacao(situacao), pagina)
    }
}
