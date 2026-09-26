package br.ufpr.sept.so2.modules.tcc.domain

import br.ufpr.sept.so2.shared.domain.exception.ConflitoEstadoException
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

class Tcc(
    val id: UUID,
    val idAluno: UUID,
    val idCurso: UUID,
    titulo: String,
    situacao: TccSituacao,
    estado: TccEstado,
    dataDefesa: LocalDate,
    dataEntrega: LocalDate,
    nomeArquivo: String? = null,
    contentType: String? = null,
    storageKey: String? = null,
    tamanho: Int? = null,
    enviadoEm: OffsetDateTime? = null,
    val createdAt: OffsetDateTime,
    updatedAt: OffsetDateTime,
    membros: List<MembroBancaTcc>,
    avaliacoes: List<AvaliacaoTcc> = emptyList(),
) {
    var titulo: String = titulo.trim()
        private set

    var situacao: TccSituacao = situacao
        private set

    var estado: TccEstado = estado
        private set

    var dataDefesa: LocalDate = dataDefesa
        private set

    var dataEntrega: LocalDate = dataEntrega
        private set

    var nomeArquivo: String? = nomeArquivo
        private set

    var contentType: String? = contentType
        private set

    var storageKey: String? = storageKey
        private set

    var tamanho: Int? = tamanho
        private set

    var enviadoEm: OffsetDateTime? = enviadoEm
        private set

    var updatedAt: OffsetDateTime = updatedAt
        private set

    private val _membros: MutableList<MembroBancaTcc> = membros.toMutableList()

    private val _avaliacoes: MutableList<AvaliacaoTcc> = avaliacoes.toMutableList()

    val membros: List<MembroBancaTcc>
        get() = _membros.toList()

    val avaliacoes: List<AvaliacaoTcc>
        get() = _avaliacoes.sortedByDescending { it.createdAt }

    init {
        validar(this.titulo, this.dataDefesa, this.dataEntrega, _membros)
    }

    fun pertenceAoAluno(alunoId: UUID): Boolean = idAluno == alunoId

    fun membroDe(usuarioId: UUID): MembroBancaTcc? = _membros.find { it.idUsuario == usuarioId }

    fun idOrientador(): UUID = _membros.first { it.papel == PapelBancaTcc.ORIENTADOR }.idUsuario

    fun temArquivo(): Boolean = !storageKey.isNullOrBlank()

    fun podeEnviar(): Boolean = situacao == TccSituacao.ATIVO && estado.podeEnviar()

    fun podeAvaliar(usuarioId: UUID): Boolean =
        situacao == TccSituacao.ATIVO && estado.podeAvaliar() && membroDe(usuarioId) != null

    fun enviarVersaoFinal(
        nome: String,
        contentType: String?,
        bytes: ByteArray,
        storageKey: String,
        agora: OffsetDateTime,
    ) {
        exigirAtivo()
        if (!estado.podeEnviar()) {
            throw ConflitoEstadoException("Este TCC não aceita envio no estado atual.")
        }
        if (storageKey.isBlank()) {
            throw DadoInvalidoException("Chave de armazenamento do TCC é obrigatória.")
        }
        this.nomeArquivo = VersaoFinalTcc.validar(nome, contentType, bytes)
        this.contentType = "application/pdf"
        this.storageKey = storageKey
        this.tamanho = bytes.size
        this.enviadoEm = agora
        this.estado = TccEstado.SUBMETIDO
        this.updatedAt = agora
    }

    fun avaliar(
        acaoBruta: String?,
        nota: BigDecimal?,
        parecer: String?,
        autorId: UUID,
        avaliacaoId: UUID,
        agora: OffsetDateTime,
    ): AvaliacaoTcc {
        exigirAtivo()
        if (!estado.podeAvaliar()) {
            throw ConflitoEstadoException("O TCC não está aguardando avaliação.")
        }
        val membro = membroDe(autorId)
            ?: throw DadoInvalidoException("Avaliador não integra a banca deste TCC.")
        val acao = AcaoAvaliacaoTcc.from(acaoBruta)
        val registrada = AvaliacaoTcc(
            avaliacaoId,
            autorId,
            membro.papel,
            acao,
            acao.resultado(),
            AvaliacaoTcc.validarNota(nota),
            AvaliacaoTcc.validarTexto(acao, parecer),
            agora,
        )
        _avaliacoes.add(registrada)
        estado = registrada.resultado
        updatedAt = agora
        return registrada
    }

    fun atualizarCadastro(
        titulo: String,
        dataDefesa: LocalDate,
        dataEntrega: LocalDate,
        membros: List<MembroBancaTcc>,
        agora: OffsetDateTime,
    ) {
        exigirAtivo()
        val tituloLimpo = titulo.trim()
        validar(tituloLimpo, dataDefesa, dataEntrega, membros)
        this.titulo = tituloLimpo
        this.dataDefesa = dataDefesa
        this.dataEntrega = dataEntrega
        _membros.clear()
        _membros.addAll(membros)
        updatedAt = agora
    }

    private fun exigirAtivo() {
        if (situacao == TccSituacao.CONCLUIDO) {
            throw ConflitoEstadoException("TCC concluído é imutável.")
        }
    }

    companion object {
        fun abrir(
            id: UUID,
            idAluno: UUID,
            idCurso: UUID,
            titulo: String,
            dataDefesa: LocalDate,
            dataEntrega: LocalDate,
            agora: OffsetDateTime,
            membros: List<MembroBancaTcc>,
        ): Tcc = Tcc(
            id,
            idAluno,
            idCurso,
            titulo,
            TccSituacao.ATIVO,
            TccEstado.EM_ELABORACAO,
            dataDefesa,
            dataEntrega,
            null,
            null,
            null,
            null,
            null,
            agora,
            agora,
            membros,
        )

        private fun validar(
            titulo: String,
            dataDefesa: LocalDate,
            dataEntrega: LocalDate,
            membros: List<MembroBancaTcc>,
        ) {
            if (titulo.length < 3 || titulo.length > 200) {
                throw DadoInvalidoException("Informe o título do TCC.")
            }
            if (dataDefesa.isBefore(dataEntrega)) {
                throw DadoInvalidoException("A defesa não pode ser anterior ao limite de entrega.")
            }
            if (membros.isEmpty()) {
                throw DadoInvalidoException("O TCC exige ao menos o orientador na banca.")
            }
            if (membros.count { it.papel == PapelBancaTcc.ORIENTADOR } != 1) {
                throw DadoInvalidoException("O TCC exige exatamente um orientador.")
            }
            if (membros.map { it.idUsuario }.distinct().size != membros.size) {
                throw DadoInvalidoException("Há membro repetido na banca.")
            }
        }
    }
}
