package br.ufpr.sept.so2.modules.iam.api

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class NativeClientTest : StringSpec({
    "body ganha do cookie quando os dois existem" {
        NativeClient.resolveRefreshToken("cookie", "body") shouldBe "body"
    }

    "cai no cookie se o body vem vazio" {
        NativeClient.resolveRefreshToken("cookie", "  ") shouldBe "cookie"
        NativeClient.resolveRefreshToken("cookie", null) shouldBe "cookie"
    }

    "nulo se nenhum token chegar" {
        NativeClient.resolveRefreshToken(null, null) shouldBe null
        NativeClient.resolveRefreshToken(" ", "") shouldBe null
    }
})
