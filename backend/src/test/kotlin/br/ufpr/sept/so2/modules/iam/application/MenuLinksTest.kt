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
        links.shouldNotContainKey("estagios")
        links.shouldNotContainKey("estagios-revisao")
        links.shouldNotContainKey("tccs")
        links.shouldNotContainKey("tccs-revisao")
    }

    "aluno com internship.view_own ganha estagios e nao a fila" {
        val links = MenuLinks.from(listOf("internship.view_own"))
        links["estagios"] shouldBe "/estagios"
        links.shouldNotContainKey("estagios-revisao")
    }

    "orientador com internship.review ganha a fila e nao o cadastro do aluno" {
        val links = MenuLinks.from(listOf("internship.review"))
        links["estagios-revisao"] shouldBe "/estagios?to=me"
        links.shouldNotContainKey("estagios")
        links.shouldNotContainKey("cursos")
    }

    "secretaria nao ganha menu de estagio" {
        val links = MenuLinks.from(
            listOf("course.manage", "subject.manage", "user.manage_students", "calendar.manage"),
        )
        links.shouldNotContainKey("estagios")
        links.shouldNotContainKey("estagios-revisao")
        links.shouldNotContainKey("tccs")
        links.shouldNotContainKey("tccs-revisao")
    }

    "aluno com tcc.view_own ganha tccs e nao a fila" {
        val links = MenuLinks.from(listOf("tcc.view_own"))
        links["tccs"] shouldBe "/tccs"
        links.shouldNotContainKey("tccs-revisao")
    }

    "orientador com tcc.review ganha a fila e nao o cadastro do aluno" {
        val links = MenuLinks.from(listOf("tcc.review"))
        links["tccs-revisao"] shouldBe "/tccs?to=me"
        links.shouldNotContainKey("tccs")
        links.shouldNotContainKey("cursos")
    }

    "professor ganha deliberar e eventos-professor" {
        val links = MenuLinks.from(listOf("request.deliberate", "event.manage", "event.host"))
        links["deliberar"] shouldBe "/solicitacoes?to=me"
        links["eventos-professor"] shouldBe "/professor/eventos"
        links.shouldNotContainKey("cursos")
    }
})
