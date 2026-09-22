package br.ufpr.sept.so2.modules.coordenacao.application

import br.ufpr.sept.so2.modules.coordenacao.application.ports.CursoConfigPort
import br.ufpr.sept.so2.modules.coordenacao.application.ports.CursoConfigPort.ParametrosBanca
import br.ufpr.sept.so2.modules.coordenacao.application.ports.ElegibilidadeHorasPort
import br.ufpr.sept.so2.modules.coordenacao.domain.CampoAlterado
import br.ufpr.sept.so2.modules.coordenacao.domain.ConfigCurso
import br.ufpr.sept.so2.modules.coordenacao.domain.ConfigCursoPatch
import br.ufpr.sept.so2.modules.coordenacao.domain.ConfigCursoRegras
import br.ufpr.sept.so2.modules.iam.application.ports.AuditLogPort
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Grava calendário, banca, regimento e o limiar de horas do curso do coordenador.
 * Não envia e-mail. Não recalcula para inelegível quem já tinha atingido o limiar anterior.
 */
@Service
class AtualizarConfigCursoUseCase(
    private val cursoConfigPort: CursoConfigPort,
    private val elegibilidadeHorasPort: ElegibilidadeHorasPort,
    private val auditLogPort: AuditLogPort,
    private val objectMapper: ObjectMapper,
) {
    @Transactional
    fun execute(
        cursoId: UUID,
        usuarioId: UUID,
        authorities: Collection<String>,
        patch: ConfigCursoPatch,
        ip: String?,
    ): ConfigCurso {
        CursoConfigAcesso.exigirCap(authorities)
        val curso = CursoConfigAcesso.exigirDono(
            cursoConfigPort.obterCurso(cursoId, bloquear = true),
            usuarioId,
        )
        val atual = ConfigCursoMontagem.de(curso, cursoConfigPort.obterParametros(cursoId))
        val novo = ConfigCursoRegras.aplicar(atual, patch)
        val diffs = ConfigCursoRegras.alteracoes(atual, novo)
        if (diffs.isEmpty()) {
            return atual
        }
        persistir(cursoId, atual, novo, diffs)
        diffs.forEach { diff -> auditLogPort.append(TIPO, usuarioId, json(cursoId, diff), ip) }
        return novo
    }

    private fun persistir(
        cursoId: UUID,
        atual: ConfigCurso,
        novo: ConfigCurso,
        diffs: List<CampoAlterado>,
    ) {
        if (atual.horasFormativasMinimas != novo.horasFormativasMinimas) {
            elegibilidadeHorasPort.congelarQuemJaAtingiu(cursoId, atual.horasFormativasMinimas)
            cursoConfigPort.salvarHoras(cursoId, novo.horasFormativasMinimas)
        }
        if (diffs.any { it.campo != "horasFormativasMinimas" }) {
            cursoConfigPort.salvarParametros(
                ParametrosBanca(
                    cursoId,
                    novo.duracaoCalendario,
                    novo.bancaMembrosExternos,
                    novo.bancaModalidade.name,
                    novo.regimento,
                ),
            )
        }
    }

    private fun json(cursoId: UUID, diff: CampoAlterado): String =
        objectMapper.writeValueAsString(
            mapOf(
                "campo" to diff.campo,
                "de" to diff.de,
                "para" to diff.para,
                "cursoId" to cursoId.toString(),
            ),
        )

    companion object {
        const val TIPO: String = "curso.config.atualizada"
    }
}
