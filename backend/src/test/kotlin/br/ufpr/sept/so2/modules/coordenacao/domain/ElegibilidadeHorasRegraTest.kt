package br.ufpr.sept.so2.modules.coordenacao.domain

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class ElegibilidadeHorasRegraTest : StringSpec({
    "aluno que ja atingiu o limiar antigo permanece elegivel depois da alta" {
        ElegibilidadeHorasRegra.deveCongelar(125, 120, false) shouldBe true
        ElegibilidadeHorasRegra.elegivel(125, 150, 120) shouldBe true
        ElegibilidadeHorasRegra.requeridas(125, 150, 120) shouldBe 120
    }

    "aluno abaixo do limiar antigo passa a ser medido pelo novo" {
        ElegibilidadeHorasRegra.deveCongelar(100, 120, false) shouldBe false
        ElegibilidadeHorasRegra.elegivel(100, 150, null) shouldBe false
        ElegibilidadeHorasRegra.requeridas(100, 150, null) shouldBe 150
    }

    "linha ja congelada nao e recalculada" {
        ElegibilidadeHorasRegra.deveCongelar(125, 120, true) shouldBe false
        ElegibilidadeHorasRegra.requeridas(200, 150, 120) shouldBe 150
    }
})
