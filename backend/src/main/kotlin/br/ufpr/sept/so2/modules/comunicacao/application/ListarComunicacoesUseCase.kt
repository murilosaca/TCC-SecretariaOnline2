package br.ufpr.sept.so2.modules.comunicacao.application

import br.ufpr.sept.so2.modules.comunicacao.application.ports.ComunicacaoEntregaRepository
import br.ufpr.sept.so2.modules.comunicacao.application.ports.ComunicacaoRepository
import br.ufpr.sept.so2.modules.comunicacao.domain.Comunicacao
import br.ufpr.sept.so2.modules.comunicacao.domain.ComunicacaoEntrega
import br.ufpr.sept.so2.modules.comunicacao.domain.TipoComunicacao
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.UUID

data class ComunicacaoItem(
    val id: UUID,
    val tipo: TipoComunicacao,
    val titulo: String,
    val corpo: String,
    val prioridade: String,
    val data: OffsetDateTime,
    val lida: Boolean,
    val podeMarcarLida: Boolean,
    val acaoHref: String?,
)

data class ContagemNaoLidas(
    val todos: Long,
    val institucional: Long,
    val turma: Long,
    val inbox: Long,
)

data class ListaComunicacoes(
    val itens: List<ComunicacaoItem>,
    val total: Long,
    val page: Int,
    val size: Int,
    val badges: ContagemNaoLidas,
)

@Service
class ListarComunicacoesUseCase(
    private val comunicacaoRepository: ComunicacaoRepository,
    private val entregaRepository: ComunicacaoEntregaRepository,
) {
    fun execute(
        usuarioId: UUID,
        aba: String?,
        tipo: String?,
        lido: Boolean?,
        pageable: org.springframework.data.domain.Pageable,
        agora: OffsetDateTime = OffsetDateTime.now(),
    ): ListaComunicacoes {
        val filtro = tipoEfetivo(aba, tipo)
        val entregas = entregaRepository.findVisiveis(usuarioId)
        val mensagens = comunicacaoRepository.findByIds(entregas.map { it.comunicacaoId }).associateBy { it.id }
        val pares = entregas.mapNotNull { entrega ->
            mensagens[entrega.comunicacaoId]?.let { entrega to it }
        }
        val idsVisiveis = pares.map { it.second.id }.toSet()
        val doAutor = comunicacaoRepository.findByAutor(usuarioId).filter { it.id !in idsVisiveis }
        val itens = pares.map { (entrega, mensagem) -> deEntrega(entrega, mensagem, agora) } +
            doAutor.map { deAutor(it) }
        val badges = contagem(pares, agora)
        val filtrados = if (filtro.vazio) {
            emptyList()
        } else {
            itens.filter { combina(it, filtro.tipo, lido) }
                .sortedWith(compareByDescending<ComunicacaoItem> { it.data }.thenBy { it.id })
        }
        val size = pageable.pageSize.coerceIn(1, 100)
        val page = pageable.pageNumber.coerceAtLeast(0)
        val from = page * size
        val fatia = if (from >= filtrados.size) emptyList() else filtrados.drop(from).take(size)
        return ListaComunicacoes(fatia, filtrados.size.toLong(), page, size, badges)
    }

    private fun deEntrega(entrega: ComunicacaoEntrega, mensagem: Comunicacao, agora: OffsetDateTime): ComunicacaoItem {
        val expirada = mensagem.expirada(agora)
        val lida = entrega.readAt != null || expirada
        return ComunicacaoItem(
            mensagem.id,
            mensagem.tipo,
            mensagem.titulo,
            mensagem.corpo,
            mensagem.prioridade.name,
            mensagem.createdAt,
            lida,
            podeMarcarLida = entrega.readAt == null && !expirada,
            acaoHref = entrega.acaoHref?.takeIf { it.isNotBlank() },
        )
    }

    private fun deAutor(mensagem: Comunicacao): ComunicacaoItem =
        ComunicacaoItem(
            mensagem.id,
            mensagem.tipo,
            mensagem.titulo,
            mensagem.corpo,
            mensagem.prioridade.name,
            mensagem.createdAt,
            lida = true,
            podeMarcarLida = false,
            acaoHref = null,
        )

    private fun combina(item: ComunicacaoItem, tipo: TipoComunicacao?, lido: Boolean?): Boolean {
        if (tipo != null && item.tipo != tipo) {
            return false
        }
        if (lido == null) {
            return true
        }
        return item.lida == lido
    }

    private fun contagem(pares: List<Pair<ComunicacaoEntrega, Comunicacao>>, agora: OffsetDateTime): ContagemNaoLidas {
        val naoLidas = pares.filter { (entrega, mensagem) ->
            entrega.readAt == null && !mensagem.expirada(agora)
        }
        val porTipo = naoLidas.groupingBy { it.second.tipo }.eachCount()
        return ContagemNaoLidas(
            todos = naoLidas.size.toLong(),
            institucional = porTipo[TipoComunicacao.INSTITUCIONAL]?.toLong() ?: 0,
            turma = porTipo[TipoComunicacao.TURMA]?.toLong() ?: 0,
            inbox = porTipo[TipoComunicacao.INBOX]?.toLong() ?: 0,
        )
    }

    private fun tipoEfetivo(aba: String?, tipo: String?): FiltroTipo {
        val daAba = when (aba?.trim()?.uppercase()) {
            null, "", "TODOS" -> null
            else -> TipoComunicacao.parseFiltro(aba)
        }
        val doFiltro = tipo?.takeIf { it.isNotBlank() }?.let { TipoComunicacao.parseFiltro(it) }
        if (daAba != null && doFiltro != null && daAba != doFiltro) {
            return FiltroTipo(vazio = true, tipo = null)
        }
        return FiltroTipo(vazio = false, tipo = doFiltro ?: daAba)
    }

    private data class FiltroTipo(val vazio: Boolean, val tipo: TipoComunicacao?)
}

@Service
class MarcarComunicacaoLidaUseCase(
    private val entregaRepository: ComunicacaoEntregaRepository,
) {
    @Transactional
    fun execute(comunicacaoId: UUID, usuarioId: UUID, agora: OffsetDateTime = OffsetDateTime.now()) {
        val entrega = entregaRepository.findByComunicacaoEDestinatario(comunicacaoId, usuarioId)
            ?: throw RecursoNaoEncontradoException("Comunicação não encontrada.")
        if (entrega.readAt == null) {
            entrega.readAt = agora
            entregaRepository.save(entrega)
        }
    }
}
