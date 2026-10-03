package br.ufpr.sept.so2.modules.iam.infrastructure.persistence

import br.ufpr.sept.so2.modules.iam.application.AtribuicaoPerfis
import br.ufpr.sept.so2.modules.iam.application.AuthorityResumo
import br.ufpr.sept.so2.modules.iam.application.CapabilityCache
import br.ufpr.sept.so2.modules.iam.application.CatalogoPapeis
import br.ufpr.sept.so2.modules.iam.application.MatrizFgac
import br.ufpr.sept.so2.modules.iam.application.PerfilResumo
import br.ufpr.sept.so2.modules.iam.application.ports.AuditLogPort
import br.ufpr.sept.so2.modules.iam.application.ports.PerfilAcessoPort
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.shared.domain.exception.ConflitoEstadoException
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import br.ufpr.sept.so2.shared.domain.exception.PerfilEmUsoException
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager
import java.time.OffsetDateTime
import java.util.UUID

@Component
class PerfilAcessoJpaAdapter(
    private val perfilRepo: PerfilJpaRepository,
    private val authorityRepo: AuthorityJpaRepository,
    private val vinculoRepo: UsuarioPerfilJpaRepository,
    private val usuarioRepository: UsuarioRepository,
    private val auditLogPort: AuditLogPort,
    private val capabilityCache: CapabilityCache,
) : PerfilAcessoPort {

    override fun uniao(usuarioId: UUID): List<String>? {
        val vinculos = vinculoRepo.findByUsuario(usuarioId)
        if (vinculos.isEmpty()) {
            return null
        }
        val ids = vinculos.mapNotNull { it.id.perfilId }
        return perfilRepo.findAllById(ids)
            .flatMap { it.authorities }
            .distinct()
            .sorted()
    }

    override fun listar(q: String?, pageable: Pageable): Page<PerfilResumo> {
        val termo = q?.trim()?.lowercase()?.takeIf { it.isNotEmpty() }
        val todos = perfilRepo.findAll()
            .filter { termo == null || it.nome!!.lowercase().contains(termo) }
            .sortedBy { it.nome }
        val size = pageable.pageSize.coerceIn(1, 100)
        val numero = pageable.pageNumber.coerceAtLeast(0)
        val from = (numero * size).coerceAtMost(todos.size)
        val ate = (from + size).coerceAtMost(todos.size)
        val fatia = todos.subList(from, ate).map { resumo(it) }
        return PageImpl(fatia, PageRequest.of(numero, size), todos.size.toLong())
    }

    override fun buscar(id: UUID): PerfilResumo = resumo(carregar(id))

    @Transactional
    override fun criar(
        nome: String,
        descricao: String?,
        authorities: List<String>,
        atorId: UUID,
        ip: String?,
    ): PerfilResumo {
        val normalizado = nomeCustom(nome)
        if (perfilRepo.findByNomeIgnoreCase(normalizado).isPresent) {
            throw ConflitoEstadoException("Já existe um perfil com este nome.")
        }
        val conjunto = conferirAuthorities(authorities)
        val entity = PerfilJpaEntity()
        entity.nome = normalizado
        entity.descricao = limparDescricao(descricao)
        entity.tipo = CatalogoPapeis.CUSTOM
        entity.authorities = conjunto.toMutableSet()
        val salvo = perfilRepo.save(entity)
        auditLogPort.append(
            "iam.perfil.criado",
            atorId,
            """{"nome":"$normalizado","authorities":${jsonLista(conjunto)}}""",
            ip,
        )
        return resumo(salvo)
    }

    @Transactional
    override fun atualizar(
        id: UUID,
        descricao: String?,
        authorities: List<String>,
        atorId: UUID,
        ip: String?,
    ): PerfilResumo {
        val entity = carregar(id)
        val conjunto = conferirAuthorities(authorities)
        val antes = entity.authorities.toSet()
        entity.descricao = limparDescricao(descricao)
        entity.authorities.clear()
        entity.authorities.addAll(conjunto)
        perfilRepo.save(entity)
        val afetados = materializarUsuarios(id)
        auditLogPort.appendDiff(
            "iam.perfil.atualizado",
            atorId,
            """{"saiu":${jsonLista(antes - conjunto.toSet())}}""",
            """{"entrou":${jsonLista(conjunto.toSet() - antes)}}""",
            ip,
        )
        publicarCache(afetados)
        return resumo(entity)
    }

    @Transactional
    override fun excluir(id: UUID, atorId: UUID, ip: String?) {
        val entity = carregar(id)
        if (entity.tipo == CatalogoPapeis.SYSTEM) {
            throw DadoInvalidoException("Perfil de sistema não pode ser excluído.")
        }
        val ativos = vinculoRepo.contarUsuariosAtivos(id)
        if (ativos > 0) {
            throw PerfilEmUsoException(ativos.toInt())
        }
        val nome = entity.nome
        vinculoRepo.deleteByPerfil(id)
        auditLogPort.append("iam.perfil.excluido", atorId, """{"nome":"$nome"}""", ip)
        perfilRepo.deleteById(id)
    }

    override fun listarAutoridades(): MatrizFgac {
        val authorities = authorityRepo.findAll()
            .sortedBy { it.nome }
            .map { authorityResumo(it) }
        val perfis = perfilRepo.findAll().sortedBy { it.nome }.map { resumo(it) }
        return MatrizFgac(authorities, perfis)
    }

    @Transactional
    override fun atualizarDescricao(nome: String, descricao: String, atorId: UUID, ip: String?): AuthorityResumo {
        val entity = authorityRepo.findById(nome)
            .orElseThrow { RecursoNaoEncontradoException("Authority não encontrada.") }
        val texto = limparDescricao(descricao) ?: throw DadoInvalidoException("Descrição é obrigatória.")
        entity.descricao = texto
        authorityRepo.save(entity)
        auditLogPort.append("iam.authority.descricao", atorId, """{"nome":"$nome"}""", ip)
        return authorityResumo(entity)
    }

    @Transactional
    override fun salvarMatriz(vinculos: Map<UUID, List<String>>, atorId: UUID, ip: String?) {
        val afetados = linkedMapOf<UUID, List<String>>()
        vinculos.forEach { (perfilId, authorities) ->
            val entity = carregar(perfilId)
            val conjunto = conferirAuthorities(authorities)
            entity.authorities.clear()
            entity.authorities.addAll(conjunto)
            perfilRepo.save(entity)
            materializarUsuarios(perfilId).forEach { (usuarioId, caps) -> afetados[usuarioId] = caps }
        }
        auditLogPort.append(
            "iam.matriz.atualizada",
            atorId,
            """{"perfis":${vinculos.size}}""",
            ip,
        )
        publicarCache(afetados)
    }

    override fun atribuicaoDe(usuarioId: UUID): AtribuicaoPerfis {
        exigirUsuario(usuarioId)
        val selecionados = vinculoRepo.findByUsuario(usuarioId).mapNotNull { it.id.perfilId }
        val perfis = perfilRepo.findAll().sortedBy { it.nome }.map { resumo(it) }
        return AtribuicaoPerfis(usuarioId, selecionados, perfis)
    }

    @Transactional
    override fun substituirPerfis(
        usuarioId: UUID,
        perfilIds: List<UUID>,
        atorId: UUID,
        ip: String?,
    ): AtribuicaoPerfis {
        exigirUsuario(usuarioId)
        val antes = nomesDe(usuarioId)
        val destinos = perfilIds.distinct().map { carregar(it) }
        val depois = destinos.map { it.nome!! }.sorted()
        vinculoRepo.deleteByUsuario(usuarioId)
        destinos.forEach { perfil ->
            vinculoRepo.save(UsuarioPerfilJpaEntity(UsuarioPerfilId(usuarioId, perfil.id)))
        }
        val caps = materializar(usuarioId)
        auditLogPort.appendDiff(
            "iam.perfis.atribuidos",
            atorId,
            """{"usuarioId":"$usuarioId","saiu":${jsonLista(antes - depois.toSet())}}""",
            """{"usuarioId":"$usuarioId","entrou":${jsonLista(depois.toSet() - antes)}}""",
            ip,
        )
        if (caps != null) {
            publicarCache(mapOf(usuarioId to caps))
        }
        return atribuicaoDe(usuarioId)
    }

    @Transactional
    override fun garantirVinculo(usuarioId: UUID, nomes: List<String>) {
        val destinos = nomes.distinct().map { nome ->
            perfilRepo.findByNomeIgnoreCase(nome)
                .orElseThrow { RecursoNaoEncontradoException("Perfil $nome não encontrado.") }
        }
        val atuais = vinculoRepo.findByUsuario(usuarioId).mapNotNull { it.id.perfilId }.toSet()
        val desejados = destinos.mapNotNull { it.id }.toSet()
        if (atuais != desejados) {
            vinculoRepo.deleteByUsuario(usuarioId)
            destinos.forEach { perfil ->
                vinculoRepo.save(UsuarioPerfilJpaEntity(UsuarioPerfilId(usuarioId, perfil.id)))
            }
        }
        materializar(usuarioId)
    }

    private fun materializarUsuarios(perfilId: UUID): Map<UUID, List<String>> {
        val mapa = linkedMapOf<UUID, List<String>>()
        vinculoRepo.idsUsuarios(perfilId).distinct().forEach { usuarioId ->
            val caps = materializar(usuarioId)
            if (caps != null) {
                mapa[usuarioId] = caps
            }
        }
        return mapa
    }

    private fun materializar(usuarioId: UUID): List<String>? {
        val efetivas = uniao(usuarioId) ?: return null
        val usuario = usuarioRepository.findById(usuarioId).orElse(null) ?: return efetivas
        if (usuario.substituirAuthorities(efetivas, OffsetDateTime.now())) {
            usuarioRepository.save(usuario)
        }
        return usuario.authorities
    }

    private fun publicarCache(afetados: Map<UUID, List<String>>) {
        if (afetados.isEmpty()) {
            return
        }
        val copia = afetados.toMap()
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            copia.forEach { (id, caps) -> capabilityCache.put(id, caps) }
            return
        }
        TransactionSynchronizationManager.registerSynchronization(object : TransactionSynchronization {
            override fun afterCommit() {
                copia.forEach { (id, caps) -> capabilityCache.put(id, caps) }
            }
        })
    }

    private fun nomesDe(usuarioId: UUID): Set<String> {
        val ids = vinculoRepo.findByUsuario(usuarioId).mapNotNull { it.id.perfilId }
        return perfilRepo.findAllById(ids).mapNotNull { it.nome }.toSet()
    }

    private fun carregar(id: UUID): PerfilJpaEntity =
        perfilRepo.findById(id).orElseThrow { RecursoNaoEncontradoException("Perfil não encontrado.") }

    private fun exigirUsuario(usuarioId: UUID) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw RecursoNaoEncontradoException("Usuário não encontrado.")
        }
    }

    private fun resumo(entity: PerfilJpaEntity): PerfilResumo =
        PerfilResumo(
            entity.id!!,
            entity.nome!!,
            entity.descricao,
            entity.tipo!!,
            entity.authorities.sorted(),
            vinculoRepo.contarUsuariosAtivos(entity.id!!).toInt(),
        )

    private fun authorityResumo(entity: AuthorityJpaEntity): AuthorityResumo =
        AuthorityResumo(entity.nome!!, entity.descricao!!, entity.modulo!!, entity.sistema)

    private fun conferirAuthorities(authorities: List<String>): List<String> {
        val conjunto = authorities.map { it.trim() }.filter { it.isNotEmpty() }.distinct().sorted()
        val desconhecida = conjunto.firstOrNull { !authorityRepo.existsById(it) }
        if (desconhecida != null) {
            throw DadoInvalidoException("Authority desconhecida: $desconhecida")
        }
        return conjunto
    }

    private fun nomeCustom(nome: String): String {
        val limpo = nome.trim()
        if (!NOME.matches(limpo)) {
            throw DadoInvalidoException("Nome de perfil em snake_case.")
        }
        if (CatalogoPapeis.SISTEMA.contains(limpo.uppercase())) {
            throw ConflitoEstadoException("Nome reservado a perfil de sistema.")
        }
        return limpo
    }

    private fun limparDescricao(descricao: String?): String? {
        val texto = descricao?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        if (texto.length > 300) {
            throw DadoInvalidoException("Descrição acima de 300 caracteres.")
        }
        return texto
    }

    private fun jsonLista(valores: Collection<String>): String =
        valores.sorted().joinToString(prefix = "[", postfix = "]") { "\"$it\"" }

    companion object {
        private val NOME = Regex("^[a-z][a-z0-9_]{1,79}$")
    }
}
