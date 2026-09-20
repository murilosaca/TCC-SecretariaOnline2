package br.ufpr.sept.so2.modules.iam.application

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.maps.shouldContainKey
import io.kotest.matchers.maps.shouldNotContainKey
import io.kotest.matchers.shouldBe

class MenuLinksTest : StringSpec({
    "sessao autenticada sempre tem inicio e contato" {
        val links = MenuLinks.from(emptyList())
        links["inicio"] shouldBe "/inicio"
        links["contato"] shouldBe "/contato"
        links.shouldNotContainKey("cursos")
    }

    "secretaria ganha CRUD academico e nao ganha revisao CAAF" {
        val links = MenuLinks.from(
            listOf(
                "course.manage",
                "subject.manage",
                "user.manage_students",
                "calendar.manage",
                "request.view_curso",
                "request.deliberate",
            ),
        )
        links.shouldContainKey("cursos")
        links.shouldContainKey("alunos")
        links.shouldContainKey("deliberar")
        links.shouldNotContainKey("solicitacoes")
        links.shouldNotContainKey("revisao-caaf")
        links.shouldNotContainKey("eventos-professor")
    }

    "aluno ganha formativas e certificados e nao ganha cursos" {
        val links = MenuLinks.from(
            listOf(
                "request.view_own",
                "attendance.view_open",
                "formative.view_own",
                "certificate.view_own",
            ),
        )
        links.shouldContainKey("formativas")
        links.shouldContainKey("certificados")
        links.shouldContainKey("solicitacoes")
        links.shouldNotContainKey("cursos")
        links.shouldNotContainKey("deliberar")
    }

    "professor ganha deliberar e eventos-professor" {
        val links = MenuLinks.from(listOf("request.deliberate", "event.manage", "event.host"))
        links["deliberar"] shouldBe "/solicitacoes?to=me"
        links["eventos-professor"] shouldBe "/professor/eventos"
        links.shouldNotContainKey("cursos")
    }
})
