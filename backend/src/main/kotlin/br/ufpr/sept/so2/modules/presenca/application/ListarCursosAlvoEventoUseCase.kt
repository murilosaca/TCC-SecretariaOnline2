package br.ufpr.sept.so2.modules.presenca.application

import br.ufpr.sept.so2.modules.academico.application.ports.CursoRepository
import br.ufpr.sept.so2.modules.academico.domain.Curso
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ListarCursosAlvoEventoUseCase(
    private val cursoRepository: CursoRepository,
) {
    @Transactional(readOnly = true)
    fun execute(): List<Curso> = cursoRepository.findAtivos()
}
