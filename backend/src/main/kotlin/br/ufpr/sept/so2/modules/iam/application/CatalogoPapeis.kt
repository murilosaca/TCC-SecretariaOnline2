package br.ufpr.sept.so2.modules.iam.application

/**
 * Authorities já usadas no código e os perfis de sistema (F7.2).
 * O seed de dev e o inicializador leem daqui para não divergir.
 */
object CatalogoPapeis {
    const val PERFIL = "user.update_own_profile"
    const val ALUNO = "ALUNO"
    const val PROFESSOR = "PROFESSOR"
    const val SECRETARIA = "SECRETARIA"
    const val COORDENADOR = "COORDENADOR"
    const val EGRESSO = "EGRESSO"
    const val ADMIN = "ADMIN"
    const val SYSTEM = "SYSTEM"
    const val CUSTOM = "CUSTOM"

    val SISTEMA: Set<String> = setOf(ALUNO, PROFESSOR, SECRETARIA, COORDENADOR, EGRESSO, ADMIN)

    val aluno: List<String> = listOf(
        "dashboard.view_own",
        "request.view_own",
        "request.open",
        "attendance.view_open",
        "attendance.check_in",
        "formative.view_own",
        "formative.confirm_own",
        "formative.submit",
        "certificate.view_own",
        "internship.view_own",
        "tcc.view_own",
        "communication.read",
        "service_record.view_own",
        PERFIL,
    )

    val professor: List<String> = listOf(
        "dashboard.view_self_professor",
        "event.manage",
        "event.host",
        "request.deliberate",
        "internship.review",
        "tcc.review",
        "communication.read",
        "communication.publish_class",
        PERFIL,
    )

    val coordenador: List<String> = listOf(
        "course.config",
        "report.view_coordinator",
        PERFIL,
    )

    val secretaria: List<String> = listOf(
        "course.manage",
        "subject.manage",
        "user.manage_students",
        "calendar.manage",
        "internship.manage",
        "tcc.manage",
        "diploma.register",
        "request.view_curso",
        "request.triage",
        "request.deliberate",
        "report.view_secretary",
        "dashboard.view_secretary",
        "service_record.create",
        "alumni.list",
        "event.manage",
        "event.host",
        "event.view_curso",
        "request.internal_open",
        "image_authorization.review",
        "import.run",
        "export.run",
        PERFIL,
    )

    val egresso: List<String> = listOf("alumni.view_own", PERFIL)

    val admin: List<String> = listOf(
        "dashboard.view_own",
        "user.manage_all",
        "user.reset_password",
        "audit.read",
        "system.observe",
        "iam.manage_roles",
        "iam.manage_authorities",
        "request_type.manage",
        "communication.manage_templates",
        PERFIL,
    )

    /** CAAF não é perfil de sistema. O seed grava a lista direto em usuario_authority. */
    val caaf: List<String> = listOf(
        "dashboard.view_own",
        "formative.review",
        PERFIL,
    )

    val papeis: Map<String, List<String>> = linkedMapOf(
        ALUNO to aluno,
        PROFESSOR to professor,
        SECRETARIA to secretaria,
        COORDENADOR to coordenador,
        EGRESSO to egresso,
        ADMIN to admin,
    )

    val descricoes: Map<String, String> = mapOf(
        "dashboard.view_own" to "Ver o painel do próprio usuário",
        "dashboard.view_self_professor" to "Ver o painel do professor",
        "dashboard.view_secretary" to "Ver o painel da secretaria",
        "request.view_own" to "Ver as próprias solicitações",
        "request.open" to "Abrir solicitação no wizard",
        "request.view_curso" to "Ver a fila do curso",
        "request.triage" to "Triar solicitações",
        "request.deliberate" to "Deliberar solicitação",
        "request.internal_open" to "Abrir solicitação interna",
        "attendance.view_open" to "Ver eventos abertos para presença",
        "attendance.check_in" to "Registrar presença",
        "formative.view_own" to "Ver as próprias formativas",
        "formative.confirm_own" to "Confirmar formativa própria",
        "formative.submit" to "Enviar formativa com comprovante",
        "formative.review" to "Revisar formativa (CAAF)",
        "certificate.view_own" to "Ver os próprios certificados",
        "internship.view_own" to "Ver o próprio estágio",
        "internship.review" to "Emitir parecer de estágio",
        "internship.manage" to "Cadastrar estágio",
        "tcc.view_own" to "Ver o próprio TCC",
        "tcc.review" to "Avaliar TCC",
        "tcc.manage" to "Cadastrar TCC",
        "communication.read" to "Ler o hub de comunicação",
        "communication.publish_class" to "Publicar comunicado",
        "service_record.view_own" to "Ver os próprios atendimentos",
        "service_record.create" to "Registrar atendimento",
        "user.update_own_profile" to "Editar o próprio perfil",
        "user.manage_students" to "Gerenciar alunos",
        "user.manage_all" to "Gerenciar usuários",
        "user.reset_password" to "Disparar reset de senha",
        "course.manage" to "Gerenciar cursos",
        "course.config" to "Configurar o curso (coordenação)",
        "subject.manage" to "Gerenciar disciplinas",
        "calendar.manage" to "Gerenciar períodos letivos",
        "diploma.register" to "Registrar diploma e colação",
        "alumni.list" to "Listar egressos",
        "alumni.view_own" to "Ver o próprio painel de egresso",
        "event.manage" to "Gerenciar eventos",
        "event.host" to "Operar evento como anfitrião",
        "event.view_curso" to "Ver eventos do curso",
        "report.view_coordinator" to "Ver relatórios da coordenação",
        "report.view_secretary" to "Ver estatísticas da secretaria",
        "image_authorization.review" to "Deliberar autorização de imagem",
        "import.run" to "Executar importação",
        "export.run" to "Executar exportação",
        "audit.read" to "Ler a trilha de auditoria",
        "system.observe" to "Observar Outbox e jobs",
        "iam.manage_roles" to "Gerenciar perfis",
        "iam.manage_authorities" to "Gerenciar authorities e a matriz",
        "request_type.manage" to "Editar tipos de solicitação",
        "communication.manage_templates" to "Editar templates de comunicação",
    )

    fun uniao(vararg grupos: List<String>): List<String> =
        grupos.asSequence().flatten().distinct().sorted().toList()

    fun modulo(nome: String): String = nome.substringBefore('.')
}
