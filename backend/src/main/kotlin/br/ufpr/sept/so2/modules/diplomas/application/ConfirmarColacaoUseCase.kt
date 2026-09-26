package br.ufpr.sept.so2.modules.diplomas.application

import br.ufpr.sept.so2.modules.academico.application.ports.AlunoRepository
import br.ufpr.sept.so2.modules.academico.application.ports.CursoRepository
import br.ufpr.sept.so2.modules.academico.application.ports.PeriodoLetivoRepository
import br.ufpr.sept.so2.modules.academico.domain.Aluno
import br.ufpr.sept.so2.modules.academico.domain.AlunoSituacao
import br.ufpr.sept.so2.modules.coordenacao.application.ports.ElegibilidadeHorasPort
import br.ufpr.sept.so2.modules.diplomas.application.ports.DiplomaRepository
import br.ufpr.sept.so2.modules.diplomas.domain.AvaliarElegibilidadeColacao
import br.ufpr.sept.so2.modules.diplomas.domain.Diploma
import br.ufpr.sept.so2.modules.formativas.application.ports.FormativaRepository
import br.ufpr.sept.so2.modules.formativas.domain.FormativaEstado
import br.ufpr.sept.so2.modules.iam.application.ports.AuditLogPort
import br.ufpr.sept.so2.modules.iam.application.ports.OutboxPort
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.modules.iam.domain.IdentificadorLogin
import br.ufpr.sept.so2.modules.tcc.application.ports.TccRepository
import br.ufpr.sept.so2.modules.tcc.domain.TccEstado
import br.ufpr.sept.so2.shared.domain.exception.ConflitoEstadoException
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import br.ufpr.sept.so2.shared.infrastructure.Uuids
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.UUID

data class ConfirmarColacaoComando(
    val cursoId: UUID,
    val periodoId: UUID,
    val alunoIds: List<UUID>,
    val dataColacao: OffsetDateTime,
    val livro: String,
    val folha: String,
    val turma: String?,
)

@Service
class ConfirmarColacaoUseCase(
    private val cursoRepository: CursoRepository,
    private val periodoLetivoRepository: PeriodoLetivoRepository,
    private val alunoRepository: AlunoRepository,
    private val diplomaRepository: DiplomaRepository,
    private val tccRepository: TccRepository,
    private val formativaRepository: FormativaRepository,
    private val elegibilidadeHorasPort: ElegibilidadeHorasPort,
    private val usuarioRepository: UsuarioRepository,
    private val outboxPort: OutboxPort,
    private val auditLogPort: AuditLogPort,
    private val objectMapper: ObjectMapper,
) {
    @Transactional
    fun execute(comando: ConfirmarColacaoComando, atorId: UUID, ip: String?): List<Diploma> {
        if (comando.alunoIds.isEmpty()) {
            throw DadoInvalidoException("Selecione ao menos um aluno elegível.")
        }
        val ids = comando.alunoIds.distinct()
        if (ids.size != comando.alunoIds.size) {
            throw DadoInvalidoException("Lista de alunos contém duplicatas.")
        }

        val curso = cursoRepository.findById(comando.cursoId)
            .orElseThrow { RecursoNaoEncontradoException("Curso não encontrado.") }
        periodoLetivoRepository.findById(comando.periodoId)
            .orElseThrow { RecursoNaoEncontradoException("Período letivo não encontrado.") }

        val limiar = curso.horasFormativasMinimas
        val agora = OffsetDateTime.now()
        val diplomas = mutableListOf<Diploma>()
        var sequencia = 1

        for (alunoId in ids) {
            val aluno = alunoRepository.findById(alunoId)
                .orElseThrow { RecursoNaoEncontradoException("Aluno não encontrado: $alunoId") }
            if (aluno.idCurso != comando.cursoId) {
                throw DadoInvalidoException("Aluno ${aluno.grr.value} não pertence ao curso selecionado.")
            }
            if (aluno.situacao == AlunoSituacao.EGRESSO || diplomaRepository.existsByAluno(aluno.id)) {
                throw ConflitoEstadoException("Aluno ${aluno.grr.value} já é egresso ou já possui diploma.")
            }
            garantirElegivel(aluno, limiar)

            val numero = numeroDiploma(comando.livro, comando.folha, sequencia++)
            val diploma = Diploma.registrar(
                Uuids.v7(),
                aluno.id,
                comando.cursoId,
                comando.periodoId,
                numero,
                comando.dataColacao,
                comando.livro,
                comando.folha,
                comando.turma,
                agora,
            )
            val salvo = diplomaRepository.save(diploma)
            promoverEgresso(aluno, agora)
            DiplomaTrilha.registrar(
                outboxPort,
                auditLogPort,
                objectMapper,
                TIPO,
                atorId,
                salvo,
                ip,
                mapOf(
                    "dataColacao" to salvo.dataColacao.toString(),
                    "livro" to salvo.livro,
                    "folha" to salvo.folha,
                ),
            )
            diplomas.add(salvo)
        }
        return diplomas
    }

    private fun garantirElegivel(aluno: Aluno, limiarAtual: Int) {
        val tccAprovado = tccRepository
            .findByAluno(aluno.id, TccEstado.APROVADO, PageRequest.of(0, 1))
            .hasContent()
        val horas = formativaRepository.somarCargaHoraria(aluno.id, FormativaEstado.APROVADA)
        val requeridas = elegibilidadeHorasPort.requeridas(aluno.id, limiarAtual, horas)
        val resultado = AvaliarElegibilidadeColacao.avaliar(
            aluno.id,
            aluno.nome,
            aluno.grr.value,
            tccAprovado,
            horas,
            requeridas,
        )
        if (!resultado.elegivel) {
            val razao = resultado.bloqueio?.razao ?: "Aluno inelegível para colação."
            throw ConflitoEstadoException("Aluno ${aluno.grr.value} inelegível: $razao")
        }
    }

    private fun promoverEgresso(aluno: Aluno, agora: OffsetDateTime) {
        aluno.atualizar(null, aluno.nomeSocial, aluno.emailPessoal, aluno.telefone, null, AlunoSituacao.EGRESSO, null)
        alunoRepository.save(aluno)

        val usuario = usuarioRepository.findByIdentificador(IdentificadorLogin.tryParse(aluno.grr.value)!!)
            .or { usuarioRepository.findByEmail(aluno.emailInstitucional.value) }
            .orElse(null)
            ?: throw RecursoNaoEncontradoException(
                "Usuário IAM não encontrado para o aluno ${aluno.grr.value}.",
            )
        usuario.substituirAuthorities(AUTHORITIES_EGRESSO, agora)
        usuarioRepository.save(usuario)
    }

    companion object {
        const val TIPO = "egressos.graduated"
        private val AUTHORITIES_EGRESSO = listOf("alumni.view_own")

        private fun numeroDiploma(livro: String, folha: String, sequencia: Int): String {
            val seq = sequencia.toString().padStart(3, '0')
            return "L${livro.trim()}-F${folha.trim()}-$seq".take(40)
        }
    }
}
