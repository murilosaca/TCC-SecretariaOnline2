package br.ufpr.sept.so2.modules.coordenacao.application

import br.ufpr.sept.so2.modules.coordenacao.api.dto.ConfigCursoResponse
import br.ufpr.sept.so2.modules.coordenacao.application.ports.CursoConfigPort
import br.ufpr.sept.so2.modules.coordenacao.application.ports.CursoConfigPort.CursoResumo
import br.ufpr.sept.so2.modules.coordenacao.application.ports.CursoConfigPort.ParametrosBanca
import br.ufpr.sept.so2.modules.coordenacao.application.ports.ElegibilidadeHorasPort
import br.ufpr.sept.so2.modules.coordenacao.domain.ConfigCursoPatch
import br.ufpr.sept.so2.modules.iam.application.ports.AuditLogPort
import br.ufpr.sept.so2.shared.domain.exception.AcessoNegadoException
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import java.util.UUID

class ConfigCursoUseCaseTest : StringSpec({
    val cursoId = UUID.randomUUID()
    val outroId = UUID.randomUUID()
    val coordenadorId = UUID.randomUUID()
    val caps = listOf("course.config")
    val curso = CursoResumo(cursoId, "TADS", "TADS", coordenadorId, 120, true)

    "coordenador le a config e ganha _links.update" {
        val port = FakeCursoConfig(curso)
        val config = ObterConfigCursoUseCase(port).execute(cursoId, coordenadorId, caps)
        val response = ConfigCursoResponse.from(config, true)
        config.horasFormativasMinimas shouldBe 120
        config.duracaoCalendario shouldBe 15
        config.bancaMembrosExternos shouldBe 1
        config.bancaModalidade.name shouldBe "PRESENCIAL"
        response.links["self"] shouldBe "/coordenacao/cursos/$cursoId/config"
        response.links["update"] shouldBe "/coordenacao/cursos/$cursoId/config"
    }

    "patch de horas grava audit e congela quem ja atingiu o limiar" {
        val port = FakeCursoConfig(curso)
        val eleg = FakeElegibilidade()
        val audit = FakeAudit()
        val atualizado = AtualizarConfigCursoUseCase(port, eleg, audit, ObjectMapper()).execute(
            cursoId,
            coordenadorId,
            caps,
            ConfigCursoPatch(horasFormativasMinimas = 150),
            "127.0.0.1",
        )
        atualizado.horasFormativasMinimas shouldBe 150
        port.horasSalvas shouldBe 150
        eleg.congelados shouldContainExactly listOf(cursoId to 120)
        audit.tipos shouldContainExactly listOf("curso.config.atualizada")
        audit.payloads.single().contains("\"campo\":\"horasFormativasMinimas\"") shouldBe true
        audit.payloads.single().contains("\"de\":120") shouldBe true
        audit.payloads.single().contains("\"para\":150") shouldBe true
    }

    "patch so de banca nao mexe no limiar de horas" {
        val port = FakeCursoConfig(curso)
        val eleg = FakeElegibilidade()
        AtualizarConfigCursoUseCase(port, eleg, FakeAudit(), ObjectMapper()).execute(
            cursoId,
            coordenadorId,
            caps,
            ConfigCursoPatch(bancaMembrosExternos = 2),
            null,
        )
        port.horasSalvas shouldBe null
        eleg.congelados.size shouldBe 0
        port.parametrosSalvos?.bancaMembrosExternos shouldBe 2
    }

    "outro curso e sem cap devolvem 403 sem gravar" {
        val port = FakeCursoConfig(curso)
        val obter = ObterConfigCursoUseCase(port)
        shouldThrow<AcessoNegadoException> {
            obter.execute(outroId, coordenadorId, caps)
        }.message shouldBe CursoConfigAcesso.MSG_OUTRO_CURSO
        shouldThrow<AcessoNegadoException> {
            obter.execute(cursoId, coordenadorId, listOf("course.manage"))
        }.message shouldBe CursoConfigAcesso.MSG_SEM_CAP

        val atualizar = AtualizarConfigCursoUseCase(port, FakeElegibilidade(), FakeAudit(), ObjectMapper())
        shouldThrow<AcessoNegadoException> {
            atualizar.execute(outroId, coordenadorId, caps, ConfigCursoPatch(horasFormativasMinimas = 150), null)
        }
        shouldThrow<AcessoNegadoException> {
            atualizar.execute(cursoId, coordenadorId, emptyList(), ConfigCursoPatch(horasFormativasMinimas = 150), null)
        }
        port.horasSalvas shouldBe null
    }

    "horas fora de 0-1000 devolvem 422" {
        val atualizar = AtualizarConfigCursoUseCase(
            FakeCursoConfig(curso),
            FakeElegibilidade(),
            FakeAudit(),
            ObjectMapper(),
        )
        shouldThrow<DadoInvalidoException> {
            atualizar.execute(cursoId, coordenadorId, caps, ConfigCursoPatch(horasFormativasMinimas = -10), null)
        }.message shouldBe "Valor deve ser entre 0 e 1000"
        shouldThrow<DadoInvalidoException> {
            atualizar.execute(cursoId, coordenadorId, caps, ConfigCursoPatch(horasFormativasMinimas = 1001), null)
        }
    }
})

private class FakeCursoConfig(
    private val curso: CursoResumo,
) : CursoConfigPort {
    var horasSalvas: Int? = null
    var parametrosSalvos: ParametrosBanca? = null

    override fun obterCurso(id: UUID, bloquear: Boolean): CursoResumo? =
        if (id == curso.id) curso.copy(horasFormativasMinimas = horasSalvas ?: curso.horasFormativasMinimas) else null

    override fun obterParametros(id: UUID): ParametrosBanca? = parametrosSalvos

    override fun salvarHoras(id: UUID, horas: Int) {
        horasSalvas = horas
    }

    override fun salvarParametros(parametros: ParametrosBanca) {
        parametrosSalvos = parametros
    }
}

private class FakeElegibilidade : ElegibilidadeHorasPort {
    val congelados = mutableListOf<Pair<UUID, Int>>()

    override fun congelarQuemJaAtingiu(idCurso: UUID, limiarAnterior: Int) {
        congelados += idCurso to limiarAnterior
    }

    override fun requeridas(alunoId: UUID, limiarAtual: Int, horasValidadas: Int): Int = limiarAtual
}

private class FakeAudit : AuditLogPort {
    val tipos = mutableListOf<String>()
    val payloads = mutableListOf<String>()

    override fun append(tipo: String, atorId: UUID?, payload: String?, ip: String?) {
        tipos += tipo
        payloads += payload.orEmpty()
    }
}
