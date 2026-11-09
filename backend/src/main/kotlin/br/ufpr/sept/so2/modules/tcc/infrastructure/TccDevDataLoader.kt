package br.ufpr.sept.so2.modules.tcc.infrastructure

import br.ufpr.sept.so2.modules.academico.application.ports.AlunoRepository
import br.ufpr.sept.so2.modules.academico.domain.Aluno
import br.ufpr.sept.so2.modules.arquivos.application.ports.ObjectStoragePort
import br.ufpr.sept.so2.modules.arquivos.domain.StorageKey
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.modules.iam.domain.Usuario
import br.ufpr.sept.so2.modules.iam.infrastructure.IamProperties
import br.ufpr.sept.so2.modules.tcc.application.ports.TccRepository
import br.ufpr.sept.so2.modules.tcc.domain.MembroBancaTcc
import br.ufpr.sept.so2.modules.tcc.domain.PapelBancaTcc
import br.ufpr.sept.so2.modules.tcc.domain.Tcc
import br.ufpr.sept.so2.modules.tcc.domain.TccEstado
import br.ufpr.sept.so2.shared.infrastructure.Uuids
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Profile
import org.springframework.core.annotation.Order
import org.springframework.data.domain.PageRequest
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
    private val objectStoragePort: ObjectStoragePort,
) : ApplicationRunner {

    override fun run(args: ApplicationArguments) {
        if (!properties.seed.enabled) {
            return
        }
        val orientador = usuarioRepository.findByEmail(EMAIL_ORIENTADOR).orElse(null)
        if (orientador == null) {
            LOG.warn("Seed de TCC ignorado: orientador de desenvolvimento ausente.")
            return
        }
        val aluno = alunoRepository.findByGrr(GRR_ALUNO).orElse(null)
        val colega = alunoRepository.findByGrr(GRR_COLEGA).orElse(null)
        if (aluno == null && colega == null) {
            LOG.warn("Seed de TCC ignorado: cadastro dos alunos de desenvolvimento ausente.")
            return
        }
        var submetidos = 0
        if (aluno != null) {
            submetidos += garantirSubmetido(aluno, orientador, TITULO_ALUNO)
        }
        if (colega != null) {
            submetidos += garantirSubmetido(colega, orientador, TITULO_COLEGA)
        }
        LOG.info(
            "TCCs de desenvolvimento prontos para revisão ({} submetido(s), orientador {}).",
            submetidos,
            EMAIL_ORIENTADOR,
        )
    }

    private fun garantirSubmetido(aluno: Aluno, orientador: Usuario, titulo: String): Int {
        val existente = tccRepository.findByAluno(aluno.id, null, PageRequest.of(0, 1)).content.firstOrNull()
        val tcc = existente ?: tccRepository.save(
            Tcc.abrir(
                Uuids.v7(),
                aluno.id,
                aluno.idCurso,
                titulo,
                LocalDate.of(2026, 11, 12),
                LocalDate.of(2026, 10, 3),
                OffsetDateTime.now(),
                listOf(MembroBancaTcc(Uuids.v7(), orientador.id, PapelBancaTcc.ORIENTADOR)),
            ),
        )
        if (!tcc.podeEnviar()) {
            return if (tcc.estado == TccEstado.SUBMETIDO) 1 else 0
        }
        val agora = OffsetDateTime.now()
        val pdf = PDF_DEV
        val storageKey = StorageKey.tccVersaoFinal(tcc.id).value
        tcc.enviarVersaoFinal(NOME_ARQUIVO, "application/pdf", pdf, storageKey, agora)
        objectStoragePort.putObject(storageKey, "application/pdf", pdf)
        tccRepository.save(tcc)
        return 1
    }

    companion object {
        private const val GRR_ALUNO = "GRR20240001"
        private const val GRR_COLEGA = "GRR20240002"
        private const val EMAIL_ORIENTADOR = "professor.dev@ufpr.br"
        private const val TITULO_ALUNO = "Plataforma de secretaria acadêmica para o SEPT"
        private const val TITULO_COLEGA = "Automação de trâmites da secretaria do SEPT"
        private const val NOME_ARQUIVO = "tcc-dev.pdf"
        private val PDF_DEV = "%PDF-1.4\n1 0 obj<<>>endobj\ntrailer<<>>\n%%EOF\n".toByteArray()
        private val LOG = LoggerFactory.getLogger(TccDevDataLoader::class.java)
    }
}
