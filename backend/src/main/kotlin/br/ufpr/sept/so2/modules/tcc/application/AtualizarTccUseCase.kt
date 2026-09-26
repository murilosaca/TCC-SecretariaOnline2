package br.ufpr.sept.so2.modules.tcc.application

import br.ufpr.sept.so2.modules.academico.application.ports.CursoEscopoPort
import br.ufpr.sept.so2.modules.academico.application.ports.UsuarioExistenciaPort
import br.ufpr.sept.so2.modules.iam.application.ports.AuditLogPort
import br.ufpr.sept.so2.modules.iam.application.ports.OutboxPort
import br.ufpr.sept.so2.modules.tcc.application.ports.TccRepository
import br.ufpr.sept.so2.modules.tcc.domain.MembroBancaTcc
import br.ufpr.sept.so2.modules.tcc.domain.PapelBancaTcc
import br.ufpr.sept.so2.modules.tcc.domain.Tcc
import br.ufpr.sept.so2.modules.tcc.domain.TccSituacao
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import br.ufpr.sept.so2.shared.infrastructure.Uuids
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

@Service
class AtualizarTccUseCase(
    private val tccRepository: TccRepository,
    private val cursoEscopoPort: CursoEscopoPort,
    private val usuarioExistenciaPort: UsuarioExistenciaPort,
    private val outboxPort: OutboxPort,
    private val auditLogPort: AuditLogPort,
    private val objectMapper: ObjectMapper,
) {
    @Transactional
    fun execute(
        atorId: UUID,
        tccId: UUID,
        alunoId: UUID,
        titulo: String,
        dataDefesa: LocalDate,
        dataEntrega: LocalDate,
        membros: List<Pair<UUID, String>>,
        ip: String?,
    ): Tcc {
        val cursos = TccEscopo.cursos(cursoEscopoPort, atorId)
        val tcc = TccEscopo.exigirTcc(cursos, tccRepository, tccId)
        if (alunoId != tcc.idAluno && tcc.situacao != TccSituacao.CONCLUIDO) {
            throw DadoInvalidoException("O aluno do TCC não pode ser trocado.")
        }
        val banca = montarBanca(tcc, membros)
        tcc.atualizarCadastro(titulo, dataDefesa, dataEntrega, banca, OffsetDateTime.now())
        val salvo = tccRepository.save(tcc)
        TccTrilha.registrar(outboxPort, auditLogPort, objectMapper, TIPO, atorId, salvo, ip)
        return salvo
    }

    private fun montarBanca(tcc: Tcc, membros: List<Pair<UUID, String>>): List<MembroBancaTcc> {
        if (membros.isEmpty()) {
            throw DadoInvalidoException("O TCC exige ao menos o orientador na banca.")
        }
        val ids = membros.map { it.first }.distinct()
        if (!usuarioExistenciaPort.existemTodos(ids)) {
            throw DadoInvalidoException("Um ou mais membros da banca não existem.")
        }
        val atuais = tcc.membros.associateBy { it.idUsuario }
        return membros.map { (idUsuario, papel) ->
            val papelEnum = PapelBancaTcc.from(papel)
            val existente = atuais[idUsuario]
            MembroBancaTcc(existente?.id ?: Uuids.v7(), idUsuario, papelEnum)
        }
    }

    companion object {
        const val TIPO = "tcc.atualizado"
    }
}
