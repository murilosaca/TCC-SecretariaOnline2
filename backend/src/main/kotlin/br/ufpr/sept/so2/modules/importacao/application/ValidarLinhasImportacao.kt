package br.ufpr.sept.so2.modules.importacao.application

import br.ufpr.sept.so2.modules.academico.application.ports.AlunoRepository
import br.ufpr.sept.so2.modules.academico.application.ports.CursoEscopoPort
import br.ufpr.sept.so2.modules.academico.application.ports.CursoRepository
import br.ufpr.sept.so2.modules.academico.application.ports.DisciplinaRepository
import br.ufpr.sept.so2.modules.academico.domain.Aluno
import br.ufpr.sept.so2.modules.academico.domain.AlunoSituacao
import br.ufpr.sept.so2.modules.academico.domain.Disciplina
import br.ufpr.sept.so2.modules.iam.application.ports.PasswordHasher
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.modules.iam.domain.Usuario
import br.ufpr.sept.so2.modules.importacao.application.ports.AlocacaoProfessorPort
import br.ufpr.sept.so2.modules.importacao.application.ports.ImportacaoGravador
import br.ufpr.sept.so2.modules.importacao.domain.ImportacaoKinds
import br.ufpr.sept.so2.modules.importacao.domain.ImportacaoLinha
import br.ufpr.sept.so2.modules.importacao.domain.ImportacaoStatus
import br.ufpr.sept.so2.shared.domain.exception.AcessoNegadoException
import br.ufpr.sept.so2.shared.domain.valueobject.Email
import br.ufpr.sept.so2.shared.domain.valueobject.Grr
import br.ufpr.sept.so2.shared.infrastructure.Uuids
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.util.UUID

@Component
class ValidarLinhasImportacao(
    private val cursoEscopoPort: CursoEscopoPort,
    private val cursoRepository: CursoRepository,
    private val alunoRepository: AlunoRepository,
    private val disciplinaRepository: DisciplinaRepository,
    private val usuarioRepository: UsuarioRepository,
    private val alocacao: AlocacaoProfessorPort,
) {
    fun validar(operadorId: UUID, kind: String, linhas: List<ImportacaoLinha>): List<ImportacaoLinha> {
        val cursos = cursoEscopoPort.cursoIdsDoUsuario(operadorId)
        val vistos = mutableSetOf<String>()
        return linhas.map { linha -> avaliar(kind, linha, cursos, vistos) }
    }

    private fun avaliar(
        kind: String,
        linha: ImportacaoLinha,
        cursos: Set<UUID>,
        vistos: MutableSet<String>,
    ): ImportacaoLinha {
        val erro = when (kind) {
            ImportacaoKinds.ALUNOS -> erroAluno(linha.valores, cursos, vistos)
            ImportacaoKinds.DISCIPLINAS -> erroDisciplina(linha.valores, cursos, vistos)
            ImportacaoKinds.USUARIOS -> erroUsuario(linha.valores, vistos)
            else -> erroAlocacao(linha.valores, cursos, vistos)
        }
        return if (erro == null) {
            linha.copy(status = ImportacaoStatus.VALID, mensagem = null)
        } else {
            linha.copy(status = ImportacaoStatus.INVALID, mensagem = erro)
        }
    }

    private fun erroAluno(valores: Map<String, String>, cursos: Set<UUID>, vistos: MutableSet<String>): String? {
        val nome = campo(valores, "nome")
        val grr = campo(valores, "grr")
        val email = campo(valores, "emailInstitucional")
        val sigla = campo(valores, "siglaCurso")
        if (nome.isBlank() || grr.isBlank() || email.isBlank() || sigla.isBlank()) {
            return "Nome, GRR, e-mail institucional e sigla do curso são obrigatórios."
        }
        if (!grrValido(grr) || !emailValido(email)) {
            return "GRR ou e-mail institucional inválido."
        }
        val curso = cursoNoEscopo(sigla, cursos) ?: return "Curso fora do seu escopo."
        val chave = grr.uppercase()
        if (!vistos.add(chave) || alunoRepository.existsByGrr(Grr.of(grr).value)) {
            return "GRR duplicado."
        }
        if (alunoRepository.existsByEmailInstitucional(Email.of(email).value)) {
            return "E-mail institucional já cadastrado."
        }
        val situacao = campo(valores, "situacao")
        if (situacao.isNotBlank() && runCatching { AlunoSituacao.valueOf(situacao.uppercase()) }.isFailure) {
            return "Situação inválida."
        }
        if (!curso.ativo) {
            return "Curso inativo."
        }
        return null
    }

    private fun erroDisciplina(valores: Map<String, String>, cursos: Set<UUID>, vistos: MutableSet<String>): String? {
        val codigo = campo(valores, "codigo")
        val nome = campo(valores, "nome")
        val carga = campo(valores, "cargaHoraria")
        val sigla = campo(valores, "siglaCurso")
        if (codigo.isBlank() || nome.isBlank() || carga.isBlank() || sigla.isBlank()) {
            return "Código, nome, carga horária e sigla do curso são obrigatórios."
        }
        val horas = carga.toIntOrNull()
        if (horas == null || horas <= 0) {
            return "Carga horária inválida."
        }
        val curso = cursoNoEscopo(sigla, cursos) ?: return "Curso fora do seu escopo."
        val chave = "${curso.id}:$codigo".lowercase()
        if (!vistos.add(chave) || disciplinaRepository.existsByCursoAndCodigo(curso.id, codigo)) {
            return "Disciplina já cadastrada neste curso."
        }
        return null
    }

    private fun erroUsuario(valores: Map<String, String>, vistos: MutableSet<String>): String? {
        val nome = campo(valores, "nome")
        val email = campo(valores, "email")
        val grr = campo(valores, "grr")
        if (nome.isBlank() || email.isBlank()) {
            return "Nome e e-mail são obrigatórios."
        }
        if (!emailValido(email)) {
            return "E-mail inválido."
        }
        if (grr.isNotBlank() && !grrValido(grr)) {
            return "GRR inválido."
        }
        val chave = email.lowercase()
        if (!vistos.add(chave) || usuarioRepository.findByEmail(Email.of(email).value).isPresent) {
            return "E-mail já cadastrado."
        }
        return null
    }

    private fun erroAlocacao(valores: Map<String, String>, cursos: Set<UUID>, vistos: MutableSet<String>): String? {
        val email = campo(valores, "emailProfessor")
        val codigo = campo(valores, "codigoDisciplina")
        val sigla = campo(valores, "siglaCurso")
        if (email.isBlank() || codigo.isBlank() || sigla.isBlank()) {
            return "E-mail do professor, código da disciplina e sigla do curso são obrigatórios."
        }
        val curso = cursoNoEscopo(sigla, cursos) ?: return "Curso fora do seu escopo."
        val professor = usuarioRepository.findByEmail(email).orElse(null) ?: return "Professor não encontrado."
        val disciplina = disciplinaRepository.findByCursoAndCodigo(curso.id, codigo)
            ?: return "Disciplina não encontrada no curso."
        val chave = "${professor.id}:${disciplina.id}"
        if (!vistos.add(chave) || alocacao.existe(professor.id, disciplina.id)) {
            return "Alocação já existente."
        }
        return null
    }

    private fun cursoNoEscopo(sigla: String, cursos: Set<UUID>) =
        cursoRepository.findBySigla(sigla).orElse(null)?.takeIf { it.id in cursos }

    companion object {
        fun campo(valores: Map<String, String>, nome: String): String =
            valores.entries.firstOrNull { it.key.equals(nome, ignoreCase = true) }?.value?.trim().orEmpty()

        private fun grrValido(valor: String): Boolean = runCatching { Grr.of(valor) }.isSuccess

        private fun emailValido(valor: String): Boolean = runCatching { Email.of(valor) }.isSuccess
    }
}

@Component
class ImportacaoGravadorAdapter(
    private val cursoEscopoPort: CursoEscopoPort,
    private val cursoRepository: CursoRepository,
    private val alunoRepository: AlunoRepository,
    private val disciplinaRepository: DisciplinaRepository,
    private val usuarioRepository: UsuarioRepository,
    private val alocacao: AlocacaoProfessorPort,
    private val passwordHasher: PasswordHasher,
) : ImportacaoGravador {
    override fun gravar(operadorId: UUID, kind: String, linhas: List<ImportacaoLinha>) {
        val cursos = cursoEscopoPort.cursoIdsDoUsuario(operadorId)
        val agora = OffsetDateTime.now()
        linhas.forEach { linha ->
            when (kind) {
                ImportacaoKinds.ALUNOS -> gravarAluno(linha.valores, cursos, agora)
                ImportacaoKinds.DISCIPLINAS -> gravarDisciplina(linha.valores, cursos, agora)
                ImportacaoKinds.USUARIOS -> gravarUsuario(linha.valores, agora)
                else -> gravarAlocacao(linha.valores, cursos, agora)
            }
        }
    }

    private fun gravarAluno(valores: Map<String, String>, cursos: Set<UUID>, agora: OffsetDateTime) {
        val curso = cursoRepository.findBySigla(ValidarLinhasImportacao.campo(valores, "siglaCurso")).orElse(null)
            ?: throw AcessoNegadoException("Curso fora do seu escopo.")
        if (curso.id !in cursos) {
            throw AcessoNegadoException("Curso fora do seu escopo.")
        }
        val situacaoBruta = ValidarLinhasImportacao.campo(valores, "situacao")
        val situacao = if (situacaoBruta.isBlank()) {
            AlunoSituacao.MATRICULADO
        } else {
            AlunoSituacao.valueOf(situacaoBruta.uppercase())
        }
        alunoRepository.save(
            Aluno(
                id = Uuids.v7(),
                nome = ValidarLinhasImportacao.campo(valores, "nome"),
                nomeSocial = null,
                grr = Grr.of(ValidarLinhasImportacao.campo(valores, "grr")),
                emailInstitucional = Email.of(ValidarLinhasImportacao.campo(valores, "emailInstitucional")),
                emailPessoal = null,
                telefone = null,
                idCurso = curso.id,
                situacao = situacao,
                ativo = true,
                createdAt = agora,
                updatedAt = agora,
            ),
        )
    }

    private fun gravarDisciplina(valores: Map<String, String>, cursos: Set<UUID>, agora: OffsetDateTime) {
        val curso = cursoRepository.findBySigla(ValidarLinhasImportacao.campo(valores, "siglaCurso")).orElse(null)
            ?: throw AcessoNegadoException("Curso fora do seu escopo.")
        if (curso.id !in cursos) {
            throw AcessoNegadoException("Curso fora do seu escopo.")
        }
        val carga = ValidarLinhasImportacao.campo(valores, "cargaHoraria").toInt()
        val periodo = ValidarLinhasImportacao.campo(valores, "periodo").toIntOrNull() ?: 1
        val creditos = ValidarLinhasImportacao.campo(valores, "creditos").toIntOrNull() ?: 1
        disciplinaRepository.save(
            Disciplina(
                id = Uuids.v7(),
                idCurso = curso.id,
                codigo = ValidarLinhasImportacao.campo(valores, "codigo"),
                nome = ValidarLinhasImportacao.campo(valores, "nome"),
                periodo = periodo,
                cargaHorariaTotal = carga,
                creditos = creditos,
                ativa = true,
                createdAt = agora,
                updatedAt = agora,
            ),
        )
    }

    private fun gravarUsuario(valores: Map<String, String>, agora: OffsetDateTime) {
        val grrBruto = ValidarLinhasImportacao.campo(valores, "grr")
        usuarioRepository.save(
            Usuario(
                id = Uuids.v7(),
                nome = ValidarLinhasImportacao.campo(valores, "nome"),
                emailInstitucional = Email.of(ValidarLinhasImportacao.campo(valores, "email")),
                emailPessoal = null,
                grr = if (grrBruto.isBlank()) null else Grr.of(grrBruto),
                senhaHash = passwordHasher.hash(Uuids.v7().toString() + "Aa1!"),
                senhaAlterada = false,
                lgpdAceiteEm = null,
                lgpdAceiteIp = null,
                lgpdAceiteUserAgent = null,
                ativo = true,
                falhasConsecutivas = 0,
                bloqueadoAte = null,
                authorities = emptyList(),
                createdAt = agora,
                updatedAt = agora,
            ),
        )
    }

    private fun gravarAlocacao(valores: Map<String, String>, cursos: Set<UUID>, agora: OffsetDateTime) {
        val curso = cursoRepository.findBySigla(ValidarLinhasImportacao.campo(valores, "siglaCurso")).orElse(null)
            ?: throw AcessoNegadoException("Curso fora do seu escopo.")
        if (curso.id !in cursos) {
            throw AcessoNegadoException("Curso fora do seu escopo.")
        }
        val professor = usuarioRepository.findByEmail(ValidarLinhasImportacao.campo(valores, "emailProfessor"))
            .orElseThrow { IllegalStateException("Professor não encontrado.") }
        val disciplina = disciplinaRepository.findByCursoAndCodigo(
            curso.id,
            ValidarLinhasImportacao.campo(valores, "codigoDisciplina"),
        ) ?: throw IllegalStateException("Disciplina não encontrada.")
        alocacao.salvar(Uuids.v7(), professor.id, disciplina.id, curso.id, agora)
    }
}
