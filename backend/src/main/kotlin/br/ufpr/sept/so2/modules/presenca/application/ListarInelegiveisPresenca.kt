package br.ufpr.sept.so2.modules.presenca.application

import br.ufpr.sept.so2.modules.academico.application.ports.AlunoRepository
import br.ufpr.sept.so2.modules.presenca.application.ports.PresencaRepository
import br.ufpr.sept.so2.modules.presenca.domain.Evento
import br.ufpr.sept.so2.modules.presenca.domain.EventoEstado
import br.ufpr.sept.so2.modules.presenca.domain.FasePresenca
import org.springframework.stereotype.Service

/**
 * No modo duplo, quem não registrou a entrada não pode confirmar a saída.
 * A lista só existe depois que o evento saiu de AGENDADO e tem curso.
 */
@Service
class ListarInelegiveisPresenca(
    private val alunoRepository: AlunoRepository,
    private val presencaRepository: PresencaRepository,
) {
    fun nomes(evento: Evento): List<String> {
        val cursoId = evento.idCurso
        if (!evento.attendanceMode.isDual() || evento.estado == EventoEstado.AGENDADO || cursoId == null) {
            return emptyList()
        }
        val comEntrada = presencaRepository.usuariosComFase(evento.id, FasePresenca.ENTRADA)
        return alunoRepository.findByIdCursoIn(listOf(cursoId))
            .filter { aluno -> aluno.idUsuario == null || aluno.idUsuario !in comEntrada }
            .map { aluno -> aluno.nomeSocial?.takeIf { it.isNotBlank() } ?: aluno.nome }
            .sorted()
    }
}
