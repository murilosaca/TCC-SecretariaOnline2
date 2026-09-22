package br.ufpr.sept.so2.modules.egresso.infrastructure

import br.ufpr.sept.so2.modules.academico.application.ports.AlunoRepository
import br.ufpr.sept.so2.modules.academico.application.ports.CursoRepository
import br.ufpr.sept.so2.modules.academico.domain.Aluno
import br.ufpr.sept.so2.modules.academico.domain.AlunoSituacao
import br.ufpr.sept.so2.modules.certificados.application.ports.CertificadoRepository
import br.ufpr.sept.so2.modules.egresso.application.CertificadoDoEgresso
import br.ufpr.sept.so2.modules.egresso.application.PdfDoEgresso
import br.ufpr.sept.so2.modules.egresso.application.ports.EgressoConsultaPort
import br.ufpr.sept.so2.modules.egresso.application.ports.EgressoConsultaPort.Cadastro
import br.ufpr.sept.so2.modules.formativas.application.ports.FormativaRepository
import br.ufpr.sept.so2.modules.formativas.domain.FormativaEstado
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.modules.iam.domain.Usuario
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Component
class EgressoConsultaAdapter(
    private val usuarioRepository: UsuarioRepository,
    private val alunoRepository: AlunoRepository,
    private val cursoRepository: CursoRepository,
    private val formativaRepository: FormativaRepository,
    private val certificadoRepository: CertificadoRepository,
) : EgressoConsultaPort {

    @Transactional(readOnly = true)
    override fun consultar(usuarioId: UUID): Cadastro? {
        val usuario = usuarioRepository.findById(usuarioId).orElse(null) ?: return null
        val aluno = resolverAluno(usuario) ?: return null
        val curso = cursoRepository.findById(aluno.idCurso).orElse(null)?.let { item ->
            val nome = item.nome.trim()
            if (nome.isNotEmpty()) nome else item.sigla
        }
        val horas = formativaRepository.somarCargaHoraria(aluno.id, FormativaEstado.APROVADA)
        val pagina = certificadoRepository.findByAluno(
            aluno.id,
            PageRequest.of(0, PAGE, Sort.by(Sort.Direction.DESC, "emitidoEm")),
        )
        val certificados = pagina.content.map { item ->
            CertificadoDoEgresso(
                item.id,
                item.titulo,
                item.tipo.name,
                item.emitidoEm,
                item.hashSha256,
            )
        }
        val total = pagina.totalElements.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
        return Cadastro(
            aluno.id,
            aluno.situacao == AlunoSituacao.EGRESSO,
            nomePublico(aluno),
            curso,
            horas,
            total,
            certificados,
        )
    }

    @Transactional(readOnly = true)
    override fun certificado(certificadoId: UUID, alunoId: UUID): PdfDoEgresso? {
        val certificado = certificadoRepository.findById(certificadoId) ?: return null
        if (!certificado.pertenceAoAluno(alunoId)) {
            return null
        }
        return PdfDoEgresso(
            certificado.id,
            certificado.titulo,
            certificado.pdf,
            certificado.hashSha256,
        )
    }

    private fun resolverAluno(usuario: Usuario): Aluno? {
        val porGrr = usuario.grr?.let { alunoRepository.findByGrr(it.value).orElse(null) }
        if (porGrr != null) {
            return porGrr
        }
        return alunoRepository.findByEmailInstitucional(usuario.emailInstitucional.value).orElse(null)
    }

    companion object {
        private const val PAGE = 100

        private fun nomePublico(aluno: Aluno): String {
            val social = aluno.nomeSocial?.trim().orEmpty()
            if (social.isNotEmpty()) {
                return social
            }
            val nome = aluno.nome.trim()
            return if (nome.isEmpty()) "Egresso" else nome
        }
    }
}
