package br.ufpr.sept.so2.modules.academico.infrastructure

import br.ufpr.sept.so2.modules.academico.application.ports.AlunoRepository
import br.ufpr.sept.so2.modules.academico.application.ports.CursoRepository
import br.ufpr.sept.so2.modules.academico.domain.Aluno
import br.ufpr.sept.so2.modules.academico.domain.AlunoSituacao
import br.ufpr.sept.so2.modules.academico.domain.Curso
import br.ufpr.sept.so2.shared.domain.valueobject.Email
import br.ufpr.sept.so2.shared.domain.valueobject.Grr
import br.ufpr.sept.so2.shared.infrastructure.Uuids
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Profile
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.util.UUID

@Component
@Profile("dev")
@Order(15)
class AcademicoDevDataLoader(
    private val cursoRepository: CursoRepository,
    private val alunoRepository: AlunoRepository,
) : ApplicationRunner {

    override fun run(args: ApplicationArguments) {
        val agora = OffsetDateTime.now()
        val curso = cursoRepository.findByCodigo(CODIGO_TADS).orElseGet {
            cursoRepository.save(
                Curso(
                    Uuids.v7(),
                    "Análise e Desenvolvimento de Sistemas",
                    "TADS",
                    CODIGO_TADS,
                    null,
                    120,
                    true,
                    agora,
                    agora,
                ),
            )
        }
        criarAlunoSeAusente("Aluno Dev", "GRR20240001", "aluno.dev@ufpr.br", curso.id, agora)
        criarAlunoSeAusente("Novo Dev", "GRR20240002", "novo.dev@ufpr.br", curso.id, agora)
        LOG.info("Cadastro acadêmico de desenvolvimento pronto (TADS, aluno.dev, novo.dev).")
    }

    private fun criarAlunoSeAusente(
        nome: String,
        grr: String,
        email: String,
        idCurso: UUID,
        agora: OffsetDateTime,
    ) {
        if (alunoRepository.findByGrr(grr).isPresent) {
            return
        }
        alunoRepository.save(
            Aluno(
                Uuids.v7(),
                nome,
                null,
                Grr.of(grr),
                Email.of(email),
                null,
                null,
                idCurso,
                AlunoSituacao.MATRICULADO,
                true,
                agora,
                agora,
            ),
        )
    }

    companion object {
        const val CODIGO_TADS = "TADS-SEPT"
        private val LOG = LoggerFactory.getLogger(AcademicoDevDataLoader::class.java)
    }
}
