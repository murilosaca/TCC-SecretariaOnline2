package br.ufpr.sept.so2.modules.comunicacao.application

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class PlaceholdersTemplateTest : StringSpec({
    "substitui placeholders do payload e preserva o desconhecido" {
        val texto = PlaceholdersTemplate.aplicar(
            "Olá {{nome}}, protocolo {{protocolo}}. {{aluno_email}}",
            mapOf("nome" to "Ana", "protocolo" to "PROT-2026-00001"),
        )
        texto shouldBe "Olá Ana, protocolo PROT-2026-00001. {{aluno_email}}"
    }
})
