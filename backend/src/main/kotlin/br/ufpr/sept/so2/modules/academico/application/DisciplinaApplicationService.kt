package br.ufpr.sept.so2.modules.academico.application

import br.ufpr.sept.so2.modules.academico.application.ports.CursoEscopoPort
import br.ufpr.sept.so2.modules.academico.application.ports.CursoRepository
import br.ufpr.sept.so2.modules.academico.application.ports.DisciplinaRepository
import br.ufpr.sept.so2.modules.academico.domain.Disciplina
import br.ufpr.sept.so2.modules.iam.application.ports.AuditLogPort
import br.ufpr.sept.so2.shared.domain.exception.ConflitoEstadoException
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import br.ufpr.sept.so2.shared.infrastructure.Uuids
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.UUID

@Service
@Transactional
class DisciplinaApplicationService(
    private val disciplinaRepository: DisciplinaRepository,
    private val cursoRepository: CursoRepository,
    private val cursoEscopoPort: CursoEscopoPort,
    private val auditLogPort: AuditLogPort,
    private val objectMapper: ObjectMapper,
) {
    @Transactional(readOnly = true)
    fun listar(usuarioId: UUID, idCurso: UUID?, pageable: Pageable): Pair<Page<Disciplina>, Set<UUID>> {
        val cursoIds = cursoEscopoPort.cursoIdsDoUsuario(usuarioId)
        val filtro = when {
            idCurso == null -> cursoIds
            idCurso in cursoIds -> setOf(idCurso)
            else -> emptySet()
        }
        val idCursoEfetivo = idCurso?.takeIf { it in cursoIds }
        return disciplinaRepository.findAll(idCursoEfetivo, filtro, pageable) to cursoIds
    }

    @Transactional(readOnly = true)
    fun buscarPorId(id: UUID, usuarioId: UUID): Disciplina {
        val disciplina = disciplinaRepository.findById(id)
            ?: throw RecursoNaoEncontradoException("Disciplina não encontrada.")
        exigirCursoNoEscopo(usuarioId, disciplina.idCurso)
        return disciplina
    }

    @Transactional(readOnly = true)
    fun cursoIdsDoUsuario(usuarioId: UUID): Set<UUID> = cursoEscopoPort.cursoIdsDoUsuario(usuarioId)

    fun criar(
        usuarioId: UUID,
        idCurso: UUID,
        codigo: String,
        nome: String,
        periodo: Int,
        cargaHorariaTotal: Int,
        creditos: Int,
        vagas: Int,
        ip: String?,
    ): Disciplina {
        exigirCursoNoEscopo(usuarioId, idCurso)
        cursoRepository.findById(idCurso)
            .orElseThrow { RecursoNaoEncontradoException("Curso não encontrado.") }
        val codigoNormalizado = codigo.trim().uppercase()
        if (disciplinaRepository.existsByCursoAndCodigo(idCurso, codigoNormalizado)) {
            throw ConflitoEstadoException("Já existe disciplina com o código $codigoNormalizado neste curso")
        }
        val agora = OffsetDateTime.now()
        val disciplina = Disciplina(
            id = Uuids.v7(),
            idCurso = idCurso,
            codigo = codigoNormalizado,
            nome = nome,
            periodo = periodo,
            cargaHorariaTotal = cargaHorariaTotal,
            creditos = creditos,
            ativa = true,
            createdAt = agora,
            updatedAt = agora,
            vagas = vagas,
        )
        val salva = disciplinaRepository.save(disciplina)
        auditLogPort.appendDiff(
            "disciplina.criada",
            usuarioId,
            null,
            AuditMutacao.json(objectMapper, "disciplinaId", salva.id, AuditMutacao.entre(emptyMap(), foto(salva))),
            ip,
        )
        return salva
    }

    fun atualizar(
        id: UUID,
        usuarioId: UUID,
        codigo: String?,
        nome: String?,
        periodo: Int?,
        carga: Int?,
        creditos: Int?,
        ativa: Boolean?,
        vagas: Int?,
        ip: String?,
    ): Disciplina {
        val disciplina = buscarPorId(id, usuarioId)
        val antes = foto(disciplina)
        disciplina.atualizar(codigo?.trim()?.uppercase(), nome, periodo, carga, creditos, ativa, vagas)
        val salva = disciplinaRepository.save(disciplina)
        registrarDiff(usuarioId, salva.id, antes, foto(salva), ip)
        return salva
    }

    fun excluir(id: UUID, usuarioId: UUID, ip: String?) {
        val disciplina = buscarPorId(id, usuarioId)
        disciplinaRepository.deleteById(id)
        auditLogPort.append(
            "disciplina.excluida",
            usuarioId,
            objectMapper.writeValueAsString(mapOf("disciplinaId" to disciplina.id.toString())),
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
            "disciplina.atualizada",
            atorId,
            objectMapper.writeValueAsString(antes),
            AuditMutacao.json(objectMapper, "disciplinaId", id, alteracoes),
            ip,
        )
    }

    private fun foto(disciplina: Disciplina): Map<String, String?> = linkedMapOf(
        "codigo" to disciplina.codigo,
        "nome" to disciplina.nome,
        "periodo" to disciplina.periodo.toString(),
        "cargaHorariaTotal" to disciplina.cargaHorariaTotal.toString(),
        "creditos" to disciplina.creditos.toString(),
        "ativa" to disciplina.ativa.toString(),
        "vagas" to disciplina.vagas.toString(),
    )

    private fun exigirCursoNoEscopo(usuarioId: UUID, idCurso: UUID) {
        if (idCurso !in cursoEscopoPort.cursoIdsDoUsuario(usuarioId)) {
            throw RecursoNaoEncontradoException("Disciplina não encontrada.")
        }
    }
}
