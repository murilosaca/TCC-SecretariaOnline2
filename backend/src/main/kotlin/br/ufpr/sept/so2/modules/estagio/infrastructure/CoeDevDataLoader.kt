package br.ufpr.sept.so2.modules.estagio.infrastructure

import br.ufpr.sept.so2.modules.academico.application.ports.AlunoRepository
import br.ufpr.sept.so2.modules.academico.application.ports.CursoRepository
import br.ufpr.sept.so2.modules.academico.infrastructure.AcademicoDevDataLoader
import br.ufpr.sept.so2.modules.estagio.application.ports.CoeMembroPort
import br.ufpr.sept.so2.modules.estagio.application.ports.EstagioRepository
import br.ufpr.sept.so2.modules.estagio.domain.DocumentoEstagio
import br.ufpr.sept.so2.modules.estagio.domain.Estagio
import br.ufpr.sept.so2.modules.estagio.domain.TipoDocumentoEstagio
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.modules.iam.infrastructure.IamProperties
import br.ufpr.sept.so2.shared.infrastructure.Uuids
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Profile
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.OffsetDateTime

@Component
@Profile("dev")
@Order(25)
class CoeDevDataLoader(
    private val properties: IamProperties,
    private val cursoRepository: CursoRepository,
    private val alunoRepository: AlunoRepository,
    private val usuarioRepository: UsuarioRepository,
    private val coeMembroPort: CoeMembroPort,
    private val estagioRepository: EstagioRepository,
) : ApplicationRunner {

    override fun run(args: ApplicationArguments) {
        if (!properties.seed.enabled) {
            return
        }
        val curso = cursoRepository.findByCodigo(AcademicoDevDataLoader.CODIGO_TADS).orElse(null)
        if (curso == null) {
            LOG.warn("Seed COE ignorado: curso TADS ausente.")
            return
        }
        listOf(EMAIL_PROFESSOR, EMAIL_COE).forEach { email ->
            usuarioRepository.findByEmail(email).ifPresent { usuario ->
                coeMembroPort.adicionarSeAusente(curso.id, usuario.id)
            }
        }
        if (estagioRepository.existsSemOrientador(curso.id)) {
            LOG.info("Pool COE de desenvolvimento pronto (TADS, professor.dev membro).")
            return
        }
        val aluno = alunoRepository.findByGrr(GRR_ALUNO).orElse(null)
        if (aluno == null) {
            LOG.warn("Seed do pool COE sem estágio: aluno de desenvolvimento ausente.")
            return
        }
        val agora = OffsetDateTime.now()
        estagioRepository.save(
            Estagio.abrir(
                Uuids.v7(),
                aluno.id,
                aluno.idCurso,
                null,
                EMPRESA,
                SUPERVISOR,
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 12, 15),
                agora,
                listOf(
                    DocumentoEstagio.pendente(Uuids.v7(), TipoDocumentoEstagio.TCE),
                    DocumentoEstagio.pendente(Uuids.v7(), TipoDocumentoEstagio.RELATORIO_FINAL),
                ),
            ),
        )
        LOG.info("Pool COE de desenvolvimento pronto (TADS, estágio sem orientador).")
    }

    companion object {
        private const val EMAIL_PROFESSOR = "professor.dev@ufpr.br"
        private const val EMAIL_COE = "coe.dev@ufpr.br"
        private const val GRR_ALUNO = "GRR20240001"
        private const val EMPRESA = "Pool COE SEPT"
        private const val SUPERVISOR = "Ana Supervisora"
        private val LOG = LoggerFactory.getLogger(CoeDevDataLoader::class.java)
    }
}
