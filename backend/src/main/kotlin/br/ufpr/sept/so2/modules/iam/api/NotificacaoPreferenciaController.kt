package br.ufpr.sept.so2.modules.iam.api

import br.ufpr.sept.so2.modules.iam.api.dto.NotificacaoPreferenciaResponse
import br.ufpr.sept.so2.modules.iam.application.AtualizarNotificacaoPreferenciaUseCase
import br.ufpr.sept.so2.modules.iam.application.CampoPatch
import br.ufpr.sept.so2.modules.iam.application.ConsultarNotificacaoPreferenciaUseCase
import br.ufpr.sept.so2.modules.iam.application.NotificacaoPreferenciaPatch
import br.ufpr.sept.so2.modules.iam.domain.CanalNotificacao
import br.ufpr.sept.so2.modules.iam.domain.ModoDigest
import br.ufpr.sept.so2.modules.iam.domain.PrioridadeNotificacao
import br.ufpr.sept.so2.modules.iam.infrastructure.security.IamPrincipal
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import com.fasterxml.jackson.databind.JsonNode
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalTime
import java.time.format.DateTimeParseException

@RestController
@RequestMapping("/me/notifications")
@PreAuthorize("hasAuthority('user.update_own_profile')")
@Tag(name = "Notificações", description = "Preferências de canal, DND e digest (F1.5)")
class NotificacaoPreferenciaController(
    private val consultarUseCase: ConsultarNotificacaoPreferenciaUseCase,
    private val atualizarUseCase: AtualizarNotificacaoPreferenciaUseCase,
) {
    @GetMapping
    @Operation(summary = "Ler preferências de notificação do usuário autenticado")
    fun obter(authentication: Authentication): NotificacaoPreferenciaResponse =
        NotificacaoPreferenciaResponse.from(consultarUseCase.execute(principal(authentication).userId))

    @PatchMapping
    @Operation(summary = "Salvar preferências (CRITICAL não pode ser desabilitada)")
    fun atualizar(
        @RequestBody body: JsonNode,
        authentication: Authentication,
        http: HttpServletRequest,
    ): NotificacaoPreferenciaResponse {
        if (!body.isObject) {
            throw DadoInvalidoException("Corpo da requisição inválido.")
        }
        val salva = atualizarUseCase.execute(principal(authentication).userId, patchDe(body), ClienteIp.de(http))
        return NotificacaoPreferenciaResponse.from(salva)
    }

    private fun patchDe(body: JsonNode): NotificacaoPreferenciaPatch =
        NotificacaoPreferenciaPatch(
            canaisDe(body),
            hora(body, "dndInicio"),
            hora(body, "dndFim"),
            digestDe(body),
        )

    private fun canaisDe(body: JsonNode): Map<PrioridadeNotificacao, Map<CanalNotificacao, Boolean>>? {
        if (!body.has("canais") || body.get("canais").isNull) {
            return null
        }
        val node = body.get("canais")
        if (!node.isObject) {
            throw DadoInvalidoException("Matriz de canais inválida.")
        }
        val resultado = linkedMapOf<PrioridadeNotificacao, Map<CanalNotificacao, Boolean>>()
        for (prioridade in PrioridadeNotificacao.entries) {
            val bloco = node.get(prioridade.name) ?: continue
            if (!bloco.isObject) {
                throw DadoInvalidoException("Matriz de canais inválida.")
            }
            val canais = linkedMapOf<CanalNotificacao, Boolean>()
            canal(bloco, "email")?.let { canais[CanalNotificacao.EMAIL] = it }
            canal(bloco, "inApp")?.let { canais[CanalNotificacao.IN_APP] = it }
            if (canais.isNotEmpty()) {
                resultado[prioridade] = canais
            }
        }
        return resultado
    }

    private fun canal(bloco: JsonNode, nome: String): Boolean? {
        if (!bloco.has(nome) || bloco.get(nome).isNull) {
            return null
        }
        val node = bloco.get(nome)
        if (!node.isBoolean) {
            throw DadoInvalidoException("Canal $nome deve ser verdadeiro ou falso.")
        }
        return node.booleanValue()
    }

    private fun hora(body: JsonNode, campo: String): CampoPatch<LocalTime?> {
        if (!body.has(campo)) {
            return CampoPatch.ausente()
        }
        val node = body.get(campo)
        if (node.isNull || node.asText().isBlank()) {
            return CampoPatch.de(null)
        }
        return try {
            CampoPatch.de(LocalTime.parse(node.asText()))
        } catch (_: DateTimeParseException) {
            throw DadoInvalidoException("Horário de não perturbe inválido.")
        }
    }

    private fun digestDe(body: JsonNode): ModoDigest? {
        if (!body.has("digest") || body.get("digest").isNull || body.get("digest").asText().isBlank()) {
            return null
        }
        val texto = body.get("digest").asText()
        return try {
            ModoDigest.valueOf(texto)
        } catch (_: IllegalArgumentException) {
            throw DadoInvalidoException("Modo de digest inválido.")
        }
    }

    private fun principal(authentication: Authentication): IamPrincipal =
        authentication.principal as IamPrincipal
}
