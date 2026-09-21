package br.ufpr.sept.so2.modules.estagio.infrastructure

import br.ufpr.sept.so2.modules.academico.application.ports.AlunoRepository
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
@Order(20)
class EstagioDevDataLoader(
    private val properties: IamProperties,
    private val alunoRepository: AlunoRepository,
    private val usuarioRepository: UsuarioRepository,
    private val estagioRepository: EstagioRepository,
) : ApplicationRunner {

    override fun run(args: ApplicationArguments) {
        if (!properties.seed.enabled) {
            return
        }
        val aluno = alunoRepository.findByGrr(GRR_ALUNO).orElse(null)
        if (aluno == null) {
            LOG.warn("Seed de estágio ignorado: cadastro do aluno de desenvolvimento ausente.")
            return
        }
        val orientador = usuarioRepository.findByEmail(EMAIL_ORIENTADOR).orElse(null)
        if (orientador == null) {
            LOG.warn("Seed de estágio ignorado: orientador de desenvolvimento ausente.")
            return
        }
        if (estagioRepository.existsByAluno(aluno.id)) {
            return
        }
        val agora = OffsetDateTime.now()
        estagioRepository.save(
            Estagio.abrir(
                Uuids.v7(),
                aluno.id,
                aluno.idCurso,
                orientador.id,
                EMPRESA,
                SUPERVISOR,
                LocalDate.of(2026, 3, 2),
                LocalDate.of(2026, 11, 30),
                agora,
                listOf(
                    DocumentoEstagio.pendente(Uuids.v7(), TipoDocumentoEstagio.TCE),
                    DocumentoEstagio.pendente(Uuids.v7(), TipoDocumentoEstagio.RELATORIO_FINAL),
                ),
            ),
        )
        LOG.info("Estágio de desenvolvimento pronto (TADS, empresa fictícia, documentos pendentes de envio).")
    }

    companion object {
        private const val GRR_ALUNO = "GRR20240001"
        private const val EMAIL_ORIENTADOR = "professor.dev@ufpr.br"
        private const val EMPRESA = "Empresa Fictícia SEPT"
        private const val SUPERVISOR = "Carla Supervisora"
        private val LOG = LoggerFactory.getLogger(EstagioDevDataLoader::class.java)
    }
}
