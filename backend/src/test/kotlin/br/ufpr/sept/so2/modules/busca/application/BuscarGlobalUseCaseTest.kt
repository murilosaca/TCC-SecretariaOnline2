package br.ufpr.sept.so2.modules.busca.application

import br.ufpr.sept.so2.modules.academico.application.ports.CursoEscopoPort
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import java.util.UUID

class BuscarGlobalUseCaseTest : StringSpec({
    "sem user.manage_all nao consulta usuarios" {
        val consulta = Contagem()
        val useCase = BuscarGlobalUseCase(
            consulta,
            cursoVazio,
            IdentidadeBuscaPort { IdentidadeBusca("GRR20240001", "aluno@ufpr.br") },
            SolicitantesNoEscopoPort { emptySet() },
        )
        val resultado = useCase.execute(UUID.randomUUID(), listOf("request.view_own"), "joao")
        consulta.chamadasUsuarios shouldBe 0
        resultado.usuarios.shouldBeEmpty()
    }

    "um caractere nao consulta indice" {
        val consulta = Contagem()
        val useCase = BuscarGlobalUseCase(
            consulta,
            cursoVazio,
            IdentidadeBuscaPort { null },
            SolicitantesNoEscopoPort { emptySet() },
        )
        useCase.execute(UUID.randomUUID(), listOf("user.manage_all", "request.view_own"), "a")
        consulta.chamadasUsuarios shouldBe 0
        consulta.chamadasAlunos shouldBe 0
    }
}) {
    private class Contagem : BuscaConsultaPort {
        var chamadasUsuarios: Int = 0
        var chamadasAlunos: Int = 0

        override fun alunosPorCursos(termo: String, cursos: Set<UUID>) = emptyList<BuscaRegistro>()

        override fun alunoProprio(termo: String, grr: String?, email: String): List<BuscaRegistro> {
            chamadasAlunos += 1
            return emptyList()
        }

        override fun solicitacoes(termo: String, solicitantes: Set<UUID>) = emptyList<BuscaRegistro>()

        override fun eventosPorCursos(termo: String, cursos: Set<UUID>) = emptyList<BuscaRegistro>()

        override fun eventosDoAnfitriao(termo: String, anfitriaoId: UUID) = emptyList<BuscaRegistro>()

        override fun eventosAbertos(termo: String) = emptyList<BuscaRegistro>()

        override fun usuarios(termo: String): List<BuscaRegistro> {
            chamadasUsuarios += 1
            return emptyList()
        }
    }

    companion object {
        private val cursoVazio = object : CursoEscopoPort {
            override fun cursoIdsDoUsuario(usuarioId: UUID): Set<UUID> = emptySet()

            override fun cursoIdDoAlunoPorGrrOuEmail(grr: String?, emailInstitucional: String?): UUID? = null
        }
    }
}
