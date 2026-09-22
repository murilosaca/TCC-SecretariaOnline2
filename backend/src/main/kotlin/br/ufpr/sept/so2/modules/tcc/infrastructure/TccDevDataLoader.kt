package br.ufpr.sept.so2.modules.tcc.infrastructure

import br.ufpr.sept.so2.modules.academico.application.ports.AlunoRepository
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.modules.iam.infrastructure.IamProperties
import br.ufpr.sept.so2.modules.tcc.application.ports.TccRepository
import br.ufpr.sept.so2.modules.tcc.domain.MembroBancaTcc
import br.ufpr.sept.so2.modules.tcc.domain.PapelBancaTcc
import br.ufpr.sept.so2.modules.tcc.domain.Tcc
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
@Order(21)
class TccDevDataLoader(
    private val properties: IamProperties,
    private val alunoRepository: AlunoRepository,
    private val usuarioRepository: UsuarioRepository,
    private val tccRepository: TccRepository,
) : ApplicationRunner {

    override fun run(args: ApplicationArguments) {
        if (!properties.seed.enabled) {
            return
        }
        val aluno = alunoRepository.findByGrr(GRR_ALUNO).orElse(null)
        if (aluno == null) {
            LOG.warn("Seed de TCC ignorado: cadastro do aluno de desenvolvimento ausente.")
            return
        }
        val orientador = usuarioRepository.findByEmail(EMAIL_ORIENTADOR).orElse(null)
        if (orientador == null) {
            LOG.warn("Seed de TCC ignorado: orientador de desenvolvimento ausente.")
            return
        }
        if (tccRepository.existsAtivoByAluno(aluno.id)) {
            return
        }
        val agora = OffsetDateTime.now()
        tccRepository.save(
            Tcc.abrir(
                Uuids.v7(),
                aluno.id,
                aluno.idCurso,
                TITULO,
                LocalDate.of(2026, 11, 12),
                LocalDate.of(2026, 10, 3),
                agora,
                listOf(MembroBancaTcc(Uuids.v7(), orientador.id, PapelBancaTcc.ORIENTADOR)),
            ),
        )
        LOG.info("TCC de desenvolvimento pronto (TADS, orientador único na banca, versão final pendente).")
    }

    companion object {
        private const val GRR_ALUNO = "GRR20240001"
        private const val EMAIL_ORIENTADOR = "professor.dev@ufpr.br"
        private const val TITULO = "Plataforma de secretaria acadêmica para o SEPT"
        private val LOG = LoggerFactory.getLogger(TccDevDataLoader::class.java)
    }
}
