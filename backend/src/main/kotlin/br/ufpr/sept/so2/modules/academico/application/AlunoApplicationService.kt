package br.ufpr.sept.so2.modules.academico.application

import br.ufpr.sept.so2.modules.academico.application.ports.AlunoRepository
import br.ufpr.sept.so2.modules.academico.application.ports.CursoRepository
import br.ufpr.sept.so2.modules.academico.domain.Aluno
import br.ufpr.sept.so2.modules.academico.domain.AlunoSituacao
import br.ufpr.sept.so2.shared.domain.exception.ConflitoEstadoException
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import br.ufpr.sept.so2.shared.domain.valueobject.Email
import br.ufpr.sept.so2.shared.domain.valueobject.Grr
import br.ufpr.sept.so2.shared.infrastructure.Uuids
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
) {
    @Transactional(readOnly = true)
    fun listar(idCurso: UUID?, termo: String?, pageable: Pageable): Page<Aluno> =
        alunoRepository.findAll(idCurso, termo, pageable)

    @Transactional(readOnly = true)
    fun buscarPorId(id: UUID): Aluno =
        alunoRepository.findById(id)
            .orElseThrow { RecursoNaoEncontradoException("Aluno não encontrado: $id") }

    fun criar(
        nome: String,
        nomeSocial: String?,
        grr: String,
        emailInstitucional: String,
        emailPessoal: String?,
        telefone: String?,
        idCurso: UUID,
        situacao: AlunoSituacao?,
    ): Aluno {
        cursoRepository.findById(idCurso)
            .orElseThrow { RecursoNaoEncontradoException("Curso não encontrado: $idCurso") }
        val grrVo = Grr.of(grr)
        val institucional = Email.of(emailInstitucional)
        if (alunoRepository.existsByGrr(grrVo.value)) {
            throw ConflitoEstadoException("Já existe aluno com o GRR $grrVo")
        }
        if (alunoRepository.existsByEmailInstitucional(institucional.value)) {
            throw ConflitoEstadoException("Já existe aluno com o e-mail $institucional")
        }
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
        )
        return alunoRepository.save(aluno)
    }

    fun atualizar(
        id: UUID,
        nome: String?,
        nomeSocial: String?,
        emailPessoal: String?,
        telefone: String?,
        idCurso: UUID?,
        situacao: AlunoSituacao?,
        ativo: Boolean?,
    ): Aluno {
        val aluno = buscarPorId(id)
        if (idCurso != null) {
            cursoRepository.findById(idCurso)
                .orElseThrow { RecursoNaoEncontradoException("Curso não encontrado: $idCurso") }
        }
        val pessoal = emailPessoal?.takeIf { it.isNotBlank() }?.let { Email.of(it) }
        aluno.atualizar(nome, nomeSocial, pessoal, telefone, idCurso, situacao, ativo)
        return alunoRepository.save(aluno)
    }

    fun excluir(id: UUID) {
        buscarPorId(id)
        alunoRepository.deleteById(id)
    }
}
