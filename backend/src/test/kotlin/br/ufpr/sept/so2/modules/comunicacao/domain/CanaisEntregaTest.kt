package br.ufpr.sept.so2.modules.comunicacao.domain

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import java.time.LocalTime

class CanaisEntregaTest : StringSpec({
    val meioDia = LocalTime.of(12, 0)
    val dnd = PreferenciaEntrega(true, true, LocalTime.of(11, 0), LocalTime.of(13, 0), false)

    "CRITICAL ignora DND, digest e canal desligado" {
        val bloqueado = PreferenciaEntrega(false, false, LocalTime.of(0, 0), LocalTime.of(23, 59), true)
        CanaisEntrega.resolver(PrioridadeComunicacao.CRITICAL, bloqueado, meioDia) shouldContainExactlyInAnyOrder
            listOf(CanalEntrega.EMAIL, CanalEntrega.IN_APP)
    }

    "DND segura o e-mail e mantém o hub" {
        CanaisEntrega.resolver(PrioridadeComunicacao.LOW, dnd, meioDia) shouldContainExactlyInAnyOrder
            listOf(CanalEntrega.IN_APP)
    }

    "fora do DND envia e-mail e in-app" {
        CanaisEntrega.resolver(PrioridadeComunicacao.HIGH, dnd, LocalTime.of(15, 0)) shouldContainExactlyInAnyOrder
            listOf(CanalEntrega.EMAIL, CanalEntrega.IN_APP)
    }

    "digest RESUMO não dispara e-mail imediato" {
        val resumo = PreferenciaEntrega(true, true, null, null, true)
        CanaisEntrega.resolver(PrioridadeComunicacao.MEDIUM, resumo, meioDia) shouldContainExactlyInAnyOrder
            listOf(CanalEntrega.IN_APP)
    }

    "canal desligado não entra" {
        val soEmail = PreferenciaEntrega(true, false, null, null, false)
        CanaisEntrega.resolver(PrioridadeComunicacao.LOW, soEmail, meioDia) shouldContainExactlyInAnyOrder
            listOf(CanalEntrega.EMAIL)
    }

    "sem preferência gravada usa e-mail e in-app" {
        CanaisEntrega.resolver(PrioridadeComunicacao.MEDIUM, null, meioDia) shouldContainExactlyInAnyOrder
            listOf(CanalEntrega.EMAIL, CanalEntrega.IN_APP)
    }
})
