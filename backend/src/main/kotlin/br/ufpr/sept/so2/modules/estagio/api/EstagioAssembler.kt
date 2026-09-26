package br.ufpr.sept.so2.modules.estagio.api

import br.ufpr.sept.so2.modules.estagio.api.dto.DocumentoEstagioResponse
import br.ufpr.sept.so2.modules.estagio.api.dto.EstagioResponse
import br.ufpr.sept.so2.modules.estagio.api.dto.ParecerEstagioResponse
import br.ufpr.sept.so2.modules.estagio.application.EstagioAcesso
import br.ufpr.sept.so2.modules.estagio.application.ports.AlunoEstagioPort
import br.ufpr.sept.so2.modules.estagio.application.ports.AutorEstagioPort
import br.ufpr.sept.so2.modules.estagio.domain.DocumentoEstagio
import br.ufpr.sept.so2.modules.estagio.domain.Estagio
import br.ufpr.sept.so2.modules.estagio.domain.EstagioSituacao
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class EstagioAssembler(
    private val alunoEstagioPort: AlunoEstagioPort,
    private val autorEstagioPort: AutorEstagioPort,
) {
    fun from(estagio: Estagio, usuarioId: UUID, alunoId: UUID?, authorities: List<String>): EstagioResponse {
        val dono = alunoId != null && estagio.pertenceAoAluno(alunoId)
        val revisor = EstagioAcesso.REVIEW in authorities && estagio.orientadoPor(usuarioId)
        val self = "/estagios/${estagio.id}"
        val links = linkedMapOf("self" to self)
        if (revisor && estagio.documentos.any { it.podeRevisar() }) {
            links["revisar"] = self
        }
        if (revisor && estagio.podeArquivar()) {
            links["arquivar"] = "$self/encerrar"
        }
        if (EstagioAcesso.MANAGE in authorities && estagio.situacao == EstagioSituacao.ATIVO) {
            links["editar"] = self
        }
        val tipos = estagio.documentos.associate { it.id to it.tipo.name }
        return EstagioResponse(
            estagio.id,
            estagio.idAluno,
            alunoEstagioPort.nomeDe(estagio.idAluno),
            estagio.idCurso,
            estagio.idOrientador,
            estagio.idOrientador?.let { autorEstagioPort.rotulo(it) },
            estagio.empresa,
            estagio.supervisor,
            estagio.inicio,
            estagio.fim,
            estagio.situacao.name,
            estagio.documentoPendente(),
            estagio.documentos.map { documento(it, estagio, dono, revisor) },
            estagio.pareceres().map { parecer ->
                ParecerEstagioResponse(
                    parecer.id,
                    parecer.idDocumento,
                    tipos[parecer.idDocumento] ?: "",
                    parecer.idAutor,
                    autorEstagioPort.rotulo(parecer.idAutor),
                    parecer.acao,
                    parecer.texto,
                    parecer.createdAt,
                )
            },
            links,
        )
    }

    private fun documento(
        documento: DocumentoEstagio,
        estagio: Estagio,
        dono: Boolean,
        revisor: Boolean,
    ): DocumentoEstagioResponse {
        val links = linkedMapOf<String, String>()
        val base = "/estagios/${estagio.id}"
        if (dono && estagio.situacao != EstagioSituacao.CONCLUIDO && documento.podeEnviar()) {
            links["upload"] = "$base/documentos"
        }
        if (revisor && documento.podeRevisar()) {
            val parecer = "$base/documentos/${documento.id}/parecer"
            links["revisar"] = base
            links["aprovar"] = parecer
            links["reprovar"] = parecer
        }
        return DocumentoEstagioResponse(
            documento.id,
            documento.tipo.name,
            documento.obrigatorio,
            documento.estado.name,
            documento.nomeArquivo,
            documento.tamanho,
            documento.enviadoEm,
            links,
        )
    }
}
