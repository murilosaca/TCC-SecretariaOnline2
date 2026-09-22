package br.ufpr.sept.so2.modules.estagio.domain

import br.ufpr.sept.so2.shared.domain.exception.ConflitoEstadoException
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

class Estagio(
    val id: UUID,
    val idAluno: UUID,
    val idCurso: UUID,
    idOrientador: UUID?,
    empresa: String,
    supervisor: String,
    val inicio: LocalDate,
    val fim: LocalDate,
    situacao: EstagioSituacao,
    val createdAt: OffsetDateTime,
    updatedAt: OffsetDateTime,
    documentos: List<DocumentoEstagio>,
) {
    val empresa: String = empresa.trim()
    val supervisor: String = supervisor.trim()

    var idOrientador: UUID? = idOrientador
        private set

    var situacao: EstagioSituacao = situacao
        private set

    var updatedAt: OffsetDateTime = updatedAt
        private set

    private val _documentos: MutableList<DocumentoEstagio> = documentos.toMutableList()

    val documentos: List<DocumentoEstagio>
        get() = _documentos.toList()

    init {
        validar(this.empresa, this.supervisor, inicio, fim, _documentos)
    }

    fun pertenceAoAluno(alunoId: UUID): Boolean = idAluno == alunoId

    fun orientadoPor(usuarioId: UUID): Boolean = idOrientador == usuarioId

    fun semOrientador(): Boolean = idOrientador == null

    fun podeAtribuir(atorId: UUID): Boolean =
        situacao != EstagioSituacao.CONCLUIDO && (idOrientador == null || idOrientador == atorId)

    fun atribuirOrientador(novoId: UUID, agora: OffsetDateTime) {
        garantirMutavel()
        if (idOrientador == novoId) {
            throw ConflitoEstadoException("Este estágio já está atribuído a este orientador.")
        }
        idOrientador = novoId
        updatedAt = agora
    }

    fun documentoPendente(): String? =
        _documentos
            .filter { it.estado == EstadoDocumentoEstagio.AGUARDANDO_PARECER }
            .maxWithOrNull(compareBy<DocumentoEstagio> { it.enviadoEm ?: OffsetDateTime.MIN }.thenBy { it.tipo.name })
            ?.tipo
            ?.name

    fun podeArquivar(): Boolean {
        if (situacao != EstagioSituacao.ATIVO) {
            return false
        }
        val obrigatorios = _documentos.filter { it.obrigatorio }
        return obrigatorios.isNotEmpty() && obrigatorios.all { it.estado == EstadoDocumentoEstagio.APROVADO }
    }

    fun pareceres(): List<ParecerEstagio> =
        _documentos.flatMap { it.pareceres }.sortedByDescending { it.createdAt }

    fun enviarDocumento(
        tipo: TipoDocumentoEstagio,
        nome: String,
        contentType: String?,
        bytes: ByteArray,
        agora: OffsetDateTime,
    ) {
        garantirMutavel()
        val documento = _documentos.find { it.tipo == tipo }
            ?: throw DadoInvalidoException("Tipo de documento não exigido neste estágio.")
        documento.registrarEnvio(nome, contentType, bytes, agora)
        updatedAt = agora
    }

    fun emitirParecer(
        documentoId: UUID,
        acaoBruta: String?,
        parecer: String?,
        autorId: UUID,
        parecerId: UUID,
        agora: OffsetDateTime,
    ): ParecerEstagio {
        garantirMutavel()
        val documento = _documentos.find { it.id == documentoId }
            ?: throw DadoInvalidoException("Documento de estágio não encontrado.")
        val registrado = documento.receberParecer(
            AcaoParecerEstagio.from(acaoBruta),
            parecer,
            autorId,
            parecerId,
            agora,
        )
        updatedAt = agora
        return registrado
    }

    fun encerrar(agora: OffsetDateTime) {
        if (situacao == EstagioSituacao.CONCLUIDO) {
            throw ConflitoEstadoException("Estágio concluído é imutável.")
        }
        if (!podeArquivar()) {
            throw DadoInvalidoException("Há documentos obrigatórios ainda não aprovados.")
        }
        situacao = EstagioSituacao.CONCLUIDO
        updatedAt = agora
    }

    private fun garantirMutavel() {
        if (situacao == EstagioSituacao.CONCLUIDO) {
            throw ConflitoEstadoException("Estágio concluído é imutável.")
        }
    }

    companion object {
        fun abrir(
            id: UUID,
            idAluno: UUID,
            idCurso: UUID,
            idOrientador: UUID?,
            empresa: String,
            supervisor: String,
            inicio: LocalDate,
            fim: LocalDate,
            agora: OffsetDateTime,
            documentos: List<DocumentoEstagio>,
        ): Estagio = Estagio(
            id,
            idAluno,
            idCurso,
            idOrientador,
            empresa,
            supervisor,
            inicio,
            fim,
            EstagioSituacao.ATIVO,
            agora,
            agora,
            documentos,
        )

        private fun validar(
            empresa: String,
            supervisor: String,
            inicio: LocalDate,
            fim: LocalDate,
            documentos: List<DocumentoEstagio>,
        ) {
            if (empresa.isEmpty() || empresa.length > 200) {
                throw DadoInvalidoException("Informe a empresa do estágio.")
            }
            if (supervisor.isEmpty() || supervisor.length > 200) {
                throw DadoInvalidoException("Informe o supervisor do estágio.")
            }
            if (!fim.isAfter(inicio)) {
                throw DadoInvalidoException("A vigência do estágio precisa terminar depois do início.")
            }
            if (documentos.isEmpty()) {
                throw DadoInvalidoException("O estágio exige ao menos um documento.")
            }
            if (documentos.map { it.tipo }.distinct().size != documentos.size) {
                throw DadoInvalidoException("Há tipo de documento repetido no estágio.")
            }
        }
    }
}
