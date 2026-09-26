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
        links.shouldNotContainKey("configurar-curso")
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
        links.shouldNotContainKey("comissoes-coe")
    }

    "orientador com internship.review ganha a fila, o pool COE e nao o cadastro do aluno" {
        val links = MenuLinks.from(listOf("internship.review"))
        links["estagios-revisao"] shouldBe "/estagios?to=me"
        links["comissoes-coe"] shouldBe "/comissoes/coe"
        links.shouldNotContainKey("estagios")
        links.shouldNotContainKey("cursos")
    }

    "secretaria nao ganha menu de estagio" {
        val links = MenuLinks.from(
            listOf("course.manage", "subject.manage", "user.manage_students", "calendar.manage"),
        )
        links.shouldNotContainKey("estagios")
        links.shouldNotContainKey("estagios-revisao")
        links.shouldNotContainKey("estagios-secretaria")
        links.shouldNotContainKey("comissoes-coe")
        links.shouldNotContainKey("tccs")
        links.shouldNotContainKey("tccs-revisao")
    }

    "secretaria com internship.manage ganha o cadastro e nao a lista do aluno" {
        val links = MenuLinks.from(
            listOf("course.manage", "user.manage_students", "internship.manage"),
        )
        links["estagios-secretaria"] shouldBe "/secretaria/estagios"
        links.shouldNotContainKey("estagios")
        links.shouldNotContainKey("estagios-revisao")
        links.shouldNotContainKey("comissoes-coe")
    }

    "aluno e professor nao ganham o cadastro da secretaria" {
        MenuLinks.from(listOf("internship.view_own")).shouldNotContainKey("estagios-secretaria")
        MenuLinks.from(listOf("internship.review")).shouldNotContainKey("estagios-secretaria")
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

    "egresso puro ganha o portal e nao as rotas de aluno" {
        val links = MenuLinks.from(listOf("alumni.view_own"))
        links["egresso-inicio"] shouldBe "/egresso/inicio"
        links["contato"] shouldBe "/contato"
        links.shouldNotContainKey("inicio")
        links.shouldNotContainKey("solicitacoes")
        links.shouldNotContainKey("formativas")
        links.shouldNotContainKey("estagios")
        links.shouldNotContainKey("tccs")
        links.shouldNotContainKey("eventos")
    }

    "aluno ativo nao ganha o portal mesmo com alumni.view_own" {
        val links = MenuLinks.from(
            listOf("alumni.view_own", "request.open", "attendance.view_open", "tcc.view_own"),
        )
        links["inicio"] shouldBe "/inicio"
        links["eventos"] shouldBe "/eventos"
        links["tccs"] shouldBe "/tccs"
        links.shouldNotContainKey("egresso-inicio")
    }

    "professor ganha deliberar e eventos-professor" {
        val links = MenuLinks.from(listOf("request.deliberate", "event.manage", "event.host"))
        links["deliberar"] shouldBe "/solicitacoes?to=me"
        links["eventos-professor"] shouldBe "/professor/eventos"
        links.shouldNotContainKey("cursos")
        links.shouldNotContainKey("configurar-curso")
    }

    "coordenador com course.config e um curso ganha configurar-curso" {
        val cursoId = java.util.UUID.fromString("01999999-0000-7000-8000-00000000c061")
        val links = MenuLinks.from(listOf("course.config", "tcc.review"), cursoId)
        links["configurar-curso"] shouldBe "/coordenacao/cursos/$cursoId/configurar"
        links.shouldNotContainKey("cursos")
    }

    "course.config sem curso coordenado nao ganha o item" {
        val links = MenuLinks.from(listOf("course.config"))
        links.shouldNotContainKey("configurar-curso")
        links.shouldNotContainKey("cursos")
    }
})
