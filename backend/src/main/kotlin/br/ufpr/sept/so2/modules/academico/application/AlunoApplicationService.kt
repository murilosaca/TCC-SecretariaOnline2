package br.ufpr.sept.so2.modules.academico.application

import br.ufpr.sept.so2.modules.academico.application.ports.AlunoRepository
import br.ufpr.sept.so2.modules.academico.application.ports.ContaAlunoPort
import br.ufpr.sept.so2.modules.academico.application.ports.CursoEscopoPort
import br.ufpr.sept.so2.modules.academico.application.ports.CursoRepository
import br.ufpr.sept.so2.modules.academico.application.ports.ProvisionarContaAluno
import br.ufpr.sept.so2.modules.academico.domain.Aluno
import br.ufpr.sept.so2.modules.academico.domain.AlunoSituacao
import br.ufpr.sept.so2.modules.iam.application.ports.AuditLogPort
import br.ufpr.sept.so2.shared.domain.exception.ConflitoEstadoException
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import br.ufpr.sept.so2.shared.domain.valueobject.Email
import br.ufpr.sept.so2.shared.domain.valueobject.Grr
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
class AlunoApplicationService(
    private val alunoRepository: AlunoRepository,
    private val cursoRepository: CursoRepository,
    private val cursoEscopoPort: CursoEscopoPort,
    private val contaAlunoPort: ContaAlunoPort,
    private val auditLogPort: AuditLogPort,
    private val objectMapper: ObjectMapper,
) {
    @Transactional(readOnly = true)
    fun listar(usuarioId: UUID, idCurso: UUID?, termo: String?, pageable: Pageable): Pair<Page<Aluno>, Set<UUID>> {
        val cursoIds = cursoEscopoPort.cursoIdsDoUsuario(usuarioId)
        val buscaLivre = !termo.isNullOrBlank() && idCurso == null
        val filtroCurso = when {
            buscaLivre -> null
            idCurso == null -> cursoIds
            idCurso in cursoIds -> setOf(idCurso)
            else -> emptySet()
        }
        val idCursoEfetivo = if (buscaLivre) null else idCurso?.takeIf { it in cursoIds }
        return alunoRepository.findAll(idCursoEfetivo, termo, filtroCurso, pageable) to cursoIds
    }

    @Transactional(readOnly = true)
    fun buscarPorId(id: UUID, usuarioId: UUID): Aluno {
        val aluno = alunoRepository.findById(id)
            .orElseThrow { RecursoNaoEncontradoException("Aluno não encontrado.") }
        exigirCursoNoEscopo(usuarioId, aluno.idCurso)
        return aluno
    }

    @Transactional(readOnly = true)
    fun cursoIdsDoUsuario(usuarioId: UUID): Set<UUID> = cursoEscopoPort.cursoIdsDoUsuario(usuarioId)

    fun criar(
        usuarioId: UUID,
        nome: String,
        nomeSocial: String?,
        grr: String,
        emailInstitucional: String,
        emailPessoal: String?,
        telefone: String?,
        idCurso: UUID,
        situacao: AlunoSituacao?,
        ip: String?,
    ): Aluno {
        exigirCursoNoEscopo(usuarioId, idCurso)
        cursoRepository.findById(idCurso)
            .orElseThrow { RecursoNaoEncontradoException("Curso não encontrado.") }
        val grrVo = Grr.of(grr)
        val institucional = Email.of(emailInstitucional)
        if (alunoRepository.existsByGrr(grrVo.value)) {
            throw ConflitoEstadoException("Já existe aluno com o GRR $grrVo")
        }
        if (alunoRepository.existsByEmailInstitucional(institucional.value)) {
            throw ConflitoEstadoException("Já existe aluno com o e-mail $institucional")
        }
        val conta = contaAlunoPort.provisionar(
            ProvisionarContaAluno(
                usuarioId,
                nome,
                grrVo.value,
                institucional.value,
                emailPessoal,
                ip,
            ),
        )
        val agora = OffsetDateTime.now()
        val aluno = Aluno(
            id = Uuids.v7(),
            nome = nome,
            nomeSocial = nomeSocial,
            grr = grrVo,
            emailInstitucional = institucional,
            emailPessoal = emailPessoal?.takeIf { it.isNotBlank() }?.let { Email.of(it) },
            telefone = telefone,
            idCurso = idCurso,
            situacao = situacao ?: AlunoSituacao.MATRICULADO,
            ativo = true,
            createdAt = agora,
            updatedAt = agora,
            idUsuario = conta.usuarioId,
        )
        val salvo = alunoRepository.save(aluno)
        auditLogPort.append(
            "aluno.criado",
            usuarioId,
            objectMapper.writeValueAsString(
                mapOf(
                    "alunoId" to salvo.id.toString(),
                    "usuarioId" to conta.usuarioId.toString(),
                    "vinculado" to conta.vinculada,
                ),
            ),
            ip,
        )
        return salvo
    }

    fun atualizar(
        id: UUID,
        usuarioId: UUID,
        nome: String?,
        nomeSocial: String?,
        emailPessoal: String?,
        telefone: String?,
        idCurso: UUID?,
        situacao: AlunoSituacao?,
        ativo: Boolean?,
        ip: String?,
    ): Aluno {
        val aluno = buscarPorId(id, usuarioId)
        if (idCurso != null) {
            exigirCursoNoEscopo(usuarioId, idCurso)
            cursoRepository.findById(idCurso)
                .orElseThrow { RecursoNaoEncontradoException("Curso não encontrado.") }
        }
        val antes = foto(aluno)
        val pessoal = emailPessoal?.takeIf { it.isNotBlank() }?.let { Email.of(it) }
        aluno.atualizar(nome, nomeSocial, pessoal, telefone, idCurso, situacao, ativo)
        val salvo = alunoRepository.save(aluno)
        registrarDiff("aluno.atualizado", usuarioId, salvo.id, "alunoId", antes, foto(salvo), ip)
        return salvo
    }

    fun resetarSenha(id: UUID, operadorId: UUID, ip: String?) {
        val aluno = buscarPorId(id, operadorId)
        val usuarioId = aluno.idUsuario ?: vincularConta(aluno)
        contaAlunoPort.resetarSenha(operadorId, usuarioId, ip)
    }

    fun excluir(id: UUID, usuarioId: UUID, ip: String?) {
        val aluno = buscarPorId(id, usuarioId)
        alunoRepository.deleteById(id)
        auditLogPort.append(
            "aluno.excluido",
            usuarioId,
            objectMapper.writeValueAsString(mapOf("alunoId" to aluno.id.toString())),
            ip,
        )
    }

    private fun vincularConta(aluno: Aluno): UUID {
        val usuarioId = contaAlunoPort.localizar(aluno.grr.value, aluno.emailInstitucional.value)
            ?: throw DadoInvalidoException("Este aluno não tem conta de acesso para redefinir a senha.")
        aluno.vincularUsuario(usuarioId)
        alunoRepository.save(aluno)
        return usuarioId
    }

    private fun exigirCursoNoEscopo(usuarioId: UUID, idCurso: UUID) {
        if (idCurso !in cursoEscopoPort.cursoIdsDoUsuario(usuarioId)) {
            throw RecursoNaoEncontradoException("Aluno não encontrado.")
        }
    }

    private fun registrarDiff(
        tipo: String,
        atorId: UUID,
        id: UUID,
        chave: String,
        antes: Map<String, String?>,
        depois: Map<String, String?>,
        ip: String?,
    ) {
        val alteracoes = AuditMutacao.entre(antes, depois)
        if (alteracoes.isEmpty()) {
            return
        }
        auditLogPort.appendDiff(
            tipo,
            atorId,
            objectMapper.writeValueAsString(antes),
            AuditMutacao.json(objectMapper, chave, id, alteracoes),
            ip,
        )
    }

    private fun foto(aluno: Aluno): Map<String, String?> = linkedMapOf(
        "nome" to aluno.nome,
        "nomeSocial" to aluno.nomeSocial,
        "emailPessoal" to aluno.emailPessoal?.value,
        "telefone" to aluno.telefone,
        "idCurso" to aluno.idCurso.toString(),
        "situacao" to aluno.situacao.name,
        "ativo" to aluno.ativo.toString(),
    )
}
