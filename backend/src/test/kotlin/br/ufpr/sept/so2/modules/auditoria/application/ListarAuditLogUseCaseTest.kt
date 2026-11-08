package br.ufpr.sept.so2.modules.auditoria.application

import br.ufpr.sept.so2.modules.auditoria.application.ports.AuditLogConsultaPort
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.springframework.data.domain.PageRequest

class ListarAuditLogUseCaseTest : StringSpec({
    "filtra a trilha pela entidade além de ator, ação e período" {
        val consulta = mockk<AuditLogConsultaPort>()
        every { consulta.contar(null, null, "curso", any(), any()) } returns 0
        every { consulta.listar(null, null, "curso", any(), any(), 0, 50) } returns emptyList()
        val useCase = ListarAuditLogUseCase(consulta)

        val pagina = useCase.execute(null, null, null, null, PageRequest.of(0, 50), "curso")

        pagina.total shouldBe 0
    }
})
