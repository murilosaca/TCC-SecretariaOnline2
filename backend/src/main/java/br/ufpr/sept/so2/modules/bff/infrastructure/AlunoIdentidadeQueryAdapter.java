package br.ufpr.sept.so2.modules.bff.infrastructure;

import br.ufpr.sept.so2.modules.academico.application.ports.AlunoRepository;
import br.ufpr.sept.so2.modules.academico.application.ports.CursoRepository;
import br.ufpr.sept.so2.modules.academico.domain.Aluno;
import br.ufpr.sept.so2.modules.bff.application.ports.AlunoIdentidadeQueryPort;
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository;
import br.ufpr.sept.so2.modules.iam.domain.Usuario;
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Component
public class AlunoIdentidadeQueryAdapter implements AlunoIdentidadeQueryPort {

    private final UsuarioRepository usuarioRepository;
    private final AlunoRepository alunoRepository;
    private final CursoRepository cursoRepository;

    public AlunoIdentidadeQueryAdapter(
            UsuarioRepository usuarioRepository,
            AlunoRepository alunoRepository,
            CursoRepository cursoRepository
    ) {
        this.usuarioRepository = usuarioRepository;
        this.alunoRepository = alunoRepository;
        this.cursoRepository = cursoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public AlunoIdentidade consultar(UUID usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário autenticado não encontrado."));
        Optional<Aluno> aluno = Optional.empty();
        if (usuario.getGrr() != null) {
            aluno = alunoRepository.findByGrr(usuario.getGrr().getValue());
        }
        if (aluno.isEmpty() && usuario.getEmailInstitucional() != null) {
            aluno = alunoRepository.findByEmailInstitucional(usuario.getEmailInstitucional().getValue());
        }
        String nome = aluno.map(AlunoIdentidadeQueryAdapter::nomeExibicao)
                .orElseGet(() -> nomeDoEmail(usuario.getEmailInstitucional().getValue()));
        String curso = aluno
                .flatMap(encontrado -> cursoRepository.findById(encontrado.getIdCurso()))
                .map(item -> item.getSigla() != null && !item.getSigla().isBlank() ? item.getSigla() : item.getNome())
                .orElse(null);
        return new AlunoIdentidade(nome, curso);
    }

    private static String nomeExibicao(Aluno aluno) {
        if (aluno.getNomeSocial() != null && !aluno.getNomeSocial().isBlank()) {
            return aluno.getNomeSocial().trim();
        }
        return aluno.getNome();
    }

    private static String nomeDoEmail(String email) {
        String local = email.substring(0, email.indexOf('@')).replace('.', ' ').trim();
        if (local.isEmpty()) {
            return "Aluno";
        }
        String[] partes = local.split("\\s+");
        StringBuilder builder = new StringBuilder();
        for (String parte : partes) {
            if (parte.isEmpty()) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(parte.charAt(0)));
            if (parte.length() > 1) {
                builder.append(parte.substring(1));
            }
        }
        return builder.toString();
    }
}
