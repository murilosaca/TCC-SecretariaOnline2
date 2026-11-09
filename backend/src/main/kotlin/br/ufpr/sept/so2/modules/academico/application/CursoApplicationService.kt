package br.ufpr.sept.so2.modules.academico.application

import br.ufpr.sept.so2.modules.academico.application.ports.AlunoRepository
import br.ufpr.sept.so2.modules.academico.application.ports.CursoEscopoPort
import br.ufpr.sept.so2.modules.academico.application.ports.CursoRepository
import br.ufpr.sept.so2.modules.academico.application.ports.CursoSecretarioRepository
import br.ufpr.sept.so2.modules.academico.application.ports.DisciplinaRepository
import br.ufpr.sept.so2.modules.academico.application.ports.UsuarioExistenciaPort
import br.ufpr.sept.so2.modules.academico.domain.Curso
import br.ufpr.sept.so2.modules.iam.application.ports.AuditLogPort
import br.ufpr.sept.so2.shared.domain.exception.ConflitoEstadoException
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import br.ufpr.sept.so2.shared.infrastructure.Uuids
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.UUID

@Service
@Transactional
class CursoApplicationService(
    private val cursoRepository: CursoRepository,
    private val cursoSecretarioRepository: CursoSecretarioRepository,
    private val cursoEscopoPort: CursoEscopoPort,
    private val alunoRepository: AlunoRepository,
    private val disciplinaRepository: DisciplinaRepository,
    private val usuarioExistenciaPort: UsuarioExistenciaPort,
    private val auditLogPort: AuditLogPort,
    private val objectMapper: ObjectMapper,
) {
    @Transactional(readOnly = true)
    fun listar(usuarioId: UUID, pageable: Pageable): Pair<Page<Curso>, Set<UUID>> {
        val cursoIds = cursoEscopoPort.cursoIdsDoUsuario(usuarioId)
        return cursoRepository.findAllByIds(cursoIds, pageable) to cursoIds
    }

    @Transactional(readOnly = true)
    fun buscarPorId(id: UUID, usuarioId: UUID): Curso {
        exigirNoEscopo(usuarioId, id)
        return cursoRepository.findById(id)
            .orElseThrow { RecursoNaoEncontradoException("Curso não encontrado.") }
    }

    @Transactional(readOnly = true)
    fun secretariosIds(cursoId: UUID): List<UUID> =
        cursoSecretarioRepository.findUsuarioIdsByCursoId(cursoId)

    @Transactional(readOnly = true)
    fun secretariosPorCursos(cursoIds: Collection<UUID>): Map<UUID, List<UUID>> =
        cursoSecretarioRepository.findUsuarioIdsByCursoIds(cursoIds)

    @Transactional(readOnly = true)
    fun estaNoEscopo(usuarioId: UUID, cursoId: UUID): Boolean =
        cursoId in cursoEscopoPort.cursoIdsDoUsuario(usuarioId)

    @Transactional(readOnly = true)
    fun cursoIdsDoUsuario(usuarioId: UUID): Set<UUID> = cursoEscopoPort.cursoIdsDoUsuario(usuarioId)

    fun criar(
        usuarioId: UUID,
        nome: String,
        sigla: String,
        codigo: String,
        idCoordenador: UUID?,
        horasFormativasMinimas: Int,
        secretariosIds: List<UUID>?,
        ip: String?,
    ): Curso {
        if (cursoRepository.existsBySigla(sigla)) {
            throw ConflitoEstadoException("Já existe curso com a sigla $sigla")
        }
        if (cursoRepository.existsByCodigo(codigo)) {
            throw ConflitoEstadoException("Já existe curso com o código $codigo")
        }
        exigirUsuariosExistentes(idCoordenador, secretariosIds)
        val agora = OffsetDateTime.now()
        val curso = Curso(
            id = Uuids.v7(),
            nome = nome,
            sigla = sigla.trim().uppercase(),
            codigo = codigo.trim().uppercase(),
            idCoordenador = idCoordenador,
            horasFormativasMinimas = horasFormativasMinimas,
            ativo = true,
            createdAt = agora,
            updatedAt = agora,
        )
        val persistido = cursoRepository.save(curso)
        val secretarios = linkedSetOf<UUID>()
        secretarios.addAll(secretariosIds.orEmpty())
        secretarios.add(usuarioId)
        cursoSecretarioRepository.replaceAll(persistido.id, secretarios)
        auditLogPort.appendDiff(
            "curso.criado",
            usuarioId,
            null,
            AuditMutacao.json(objectMapper, "cursoId", persistido.id, AuditMutacao.entre(emptyMap(), foto(persistido, secretarios))),
            ip,
        )
        return persistido
    }

    fun atualizar(
        id: UUID,
        usuarioId: UUID,
        nome: String?,
        sigla: String?,
        codigo: String?,
        idCoordenador: UUID?,
        horas: Int?,
        ativo: Boolean?,
        secretariosIds: List<UUID>?,
        ip: String?,
    ): Curso {
        val curso = buscarPorId(id, usuarioId)
        val antes = foto(curso, cursoSecretarioRepository.findUsuarioIdsByCursoId(id))
        val novaSigla = sigla?.trim()?.uppercase()
        val novoCodigo = codigo?.trim()?.uppercase()
        if (novaSigla != null && novaSigla != curso.sigla && cursoRepository.existsBySigla(novaSigla)) {
            throw ConflitoEstadoException("Já existe curso com a sigla $novaSigla")
        }
        if (novoCodigo != null && novoCodigo != curso.codigo && cursoRepository.existsByCodigo(novoCodigo)) {
            throw ConflitoEstadoException("Já existe curso com o código $novoCodigo")
        }
        exigirUsuariosExistentes(idCoordenador, secretariosIds)
        curso.atualizar(nome, novaSigla, novoCodigo, idCoordenador, horas, ativo)
        val persistido = cursoRepository.save(curso)
        if (secretariosIds != null) {
            val secretarios = linkedSetOf<UUID>()
            secretarios.addAll(secretariosIds)
            secretarios.add(usuarioId)
            cursoSecretarioRepository.replaceAll(persistido.id, secretarios)
        }
        val depoisSecretarios = cursoSecretarioRepository.findUsuarioIdsByCursoId(persistido.id)
        registrarDiff(usuarioId, persistido.id, antes, foto(persistido, depoisSecretarios), ip)
        return persistido
    }

    fun excluir(id: UUID, usuarioId: UUID, ip: String?) {
        buscarPorId(id, usuarioId)
        val temAlunos = alunoRepository.findByIdCursoIn(listOf(id)).isNotEmpty()
        val temDisciplinas = disciplinaRepository.findAll(id, setOf(id), PageRequest.of(0, 1)).hasContent()
        if (temAlunos || temDisciplinas) {
            throw ConflitoEstadoException(
                "Curso possui alunos ou disciplinas vinculados; desvincule antes de excluir.",
            )
        }
        cursoSecretarioRepository.deleteByCursoId(id)
        cursoRepository.deleteById(id)
        auditLogPort.append(
            "curso.excluido",
            usuarioId,
            objectMapper.writeValueAsString(mapOf("cursoId" to id.toString())),
            ip,
        )
    }

    private fun registrarDiff(
        atorId: UUID,
        id: UUID,
        antes: Map<String, String?>,
        depois: Map<String, String?>,
        ip: String?,
    ) {
        val alteracoes = AuditMutacao.entre(antes, depois)
        if (alteracoes.isEmpty()) {
            return
        }
        auditLogPort.appendDiff(
            "curso.atualizado",
            atorId,
            objectMapper.writeValueAsString(antes),
            AuditMutacao.json(objectMapper, "cursoId", id, alteracoes),
            ip,
        )
    }

    private fun foto(curso: Curso, secretarios: Collection<UUID>): Map<String, String?> = linkedMapOf(
        "nome" to curso.nome,
        "sigla" to curso.sigla,
        "codigo" to curso.codigo,
        "idCoordenador" to curso.idCoordenador?.toString(),
        "horasFormativasMinimas" to curso.horasFormativasMinimas.toString(),
        "ativo" to curso.ativo.toString(),
        "secretarios" to secretarios.map { it.toString() }.sorted().joinToString(","),
    )

    private fun exigirNoEscopo(usuarioId: UUID, cursoId: UUID) {
        if (cursoId !in cursoEscopoPort.cursoIdsDoUsuario(usuarioId)) {
            throw RecursoNaoEncontradoException("Curso não encontrado.")
        }
    }

    private fun exigirUsuariosExistentes(idCoordenador: UUID?, secretariosIds: List<UUID>?) {
        if (idCoordenador != null && !usuarioExistenciaPort.existe(idCoordenador)) {
            throw DadoInvalidoException("Coordenador informado não existe.")
        }
        val secretarios = secretariosIds.orEmpty()
        if (secretarios.isNotEmpty() && !usuarioExistenciaPort.existemTodos(secretarios)) {
            throw DadoInvalidoException("Um ou mais secretários informados não existem.")
        }
    }
}
