package br.ufpr.sept.so2.modules.formativas.infrastructure

import br.ufpr.sept.so2.modules.academico.application.ports.AlunoRepository
import br.ufpr.sept.so2.modules.academico.application.ports.CursoRepository
import br.ufpr.sept.so2.modules.academico.infrastructure.AcademicoDevDataLoader
import br.ufpr.sept.so2.modules.formativas.application.ports.ComissaoMembroPort
import br.ufpr.sept.so2.modules.formativas.application.ports.FormativaRepository
import br.ufpr.sept.so2.modules.formativas.domain.Formativa
import br.ufpr.sept.so2.modules.formativas.domain.FormativaEstado
import br.ufpr.sept.so2.modules.formativas.domain.TipoComissao
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.modules.iam.infrastructure.IamProperties
import br.ufpr.sept.so2.shared.infrastructure.Uuids
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Profile
import org.springframework.core.annotation.Order
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.util.UUID

@Component
@Profile("dev")
@Order(26)
class CaafDevDataLoader(
    private val properties: IamProperties,
    private val cursoRepository: CursoRepository,
    private val alunoRepository: AlunoRepository,
    private val usuarioRepository: UsuarioRepository,
    private val comissaoMembroPort: ComissaoMembroPort,
    private val formativaRepository: FormativaRepository,
) : ApplicationRunner {

    override fun run(args: ApplicationArguments) {
        if (!properties.seed.enabled) {
            return
        }
        val curso = cursoRepository.findByCodigo(AcademicoDevDataLoader.CODIGO_TADS).orElse(null)
        if (curso == null) {
            LOG.warn("Seed CAAF ignorado: curso TADS ausente.")
            return
        }
        listOf(EMAIL_CAAF, EMAIL_COLEGA).forEach { email ->
            usuarioRepository.findByEmail(email).ifPresent { usuario ->
                comissaoMembroPort.adicionarSeAusente(curso.id, usuario.id, TipoComissao.CAAF)
            }
        }
        val aluno = alunoRepository.findByGrr(GRR_ALUNO).orElse(null)
        if (aluno == null) {
            LOG.warn("Seed do pool CAAF sem formativa: aluno de desenvolvimento ausente.")
            return
        }
        val jaHaPool = formativaRepository.findByEstado(
            FormativaEstado.AGUARDANDO_CAAF,
            PageRequest.of(0, 1),
        ).hasContent()
        if (jaHaPool) {
            LOG.info("Pool CAAF de desenvolvimento pronto (TADS, caaf.dev membro).")
            return
        }
        val agora = OffsetDateTime.now()
        listOf(
            EVENTO_POOL_1 to "Pool CAAF SEPT — oficina com presença",
            EVENTO_POOL_2 to "Pool CAAF SEPT — palestra com presença",
        ).forEach { (eventoId, titulo) ->
            if (formativaRepository.findByEventoAndAluno(eventoId, aluno.id) != null) {
                return@forEach
            }
            val formativa = Formativa.viaPresenca(
                Uuids.v7(),
                aluno.id,
                eventoId,
                titulo,
                4,
                agora,
            )
            formativa.confirmar(agora)
            formativaRepository.save(formativa)
        }
        LOG.info("Pool CAAF de desenvolvimento pronto (TADS, formativas AGUARDANDO_CAAF).")
    }

    companion object {
        private const val EMAIL_CAAF = "caaf.dev@ufpr.br"
        private const val EMAIL_COLEGA = "caaf.colegadev@ufpr.br"
        private const val GRR_ALUNO = "GRR20240001"
        private val EVENTO_POOL_1: UUID = UUID.fromString("01999999-caaf-7000-8000-0000000000e1")
        private val EVENTO_POOL_2: UUID = UUID.fromString("01999999-caaf-7000-8000-0000000000e2")
        private val LOG = LoggerFactory.getLogger(CaafDevDataLoader::class.java)
    }
}
