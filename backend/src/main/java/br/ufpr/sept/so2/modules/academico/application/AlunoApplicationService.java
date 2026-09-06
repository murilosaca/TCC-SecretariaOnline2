package br.ufpr.sept.so2.modules.academico.application;

import br.ufpr.sept.so2.modules.academico.application.ports.AlunoRepository;
import br.ufpr.sept.so2.modules.academico.application.ports.CursoRepository;
import br.ufpr.sept.so2.modules.academico.domain.Aluno;
import br.ufpr.sept.so2.modules.academico.domain.AlunoSituacao;
import br.ufpr.sept.so2.shared.domain.exception.ConflitoEstadoException;
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException;
import br.ufpr.sept.so2.shared.domain.valueobject.Email;
import br.ufpr.sept.so2.shared.domain.valueobject.Grr;
import br.ufpr.sept.so2.shared.infrastructure.Uuids;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@Transactional
public class AlunoApplicationService {

    private final AlunoRepository alunoRepository;
    private final CursoRepository cursoRepository;

    public AlunoApplicationService(AlunoRepository alunoRepository, CursoRepository cursoRepository) {
        this.alunoRepository = alunoRepository;
        this.cursoRepository = cursoRepository;
    }

    @Transactional(readOnly = true)
    public Page<Aluno> listar(UUID idCurso, String termo, Pageable pageable) {
        return alunoRepository.findAll(idCurso, termo, pageable);
    }

    @Transactional(readOnly = true)
    public Aluno buscarPorId(UUID id) {
        return alunoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Aluno não encontrado: " + id));
    }

    public Aluno criar(
            String nome,
            String nomeSocial,
            String grr,
            String emailInstitucional,
            String emailPessoal,
            String telefone,
            UUID idCurso,
            AlunoSituacao situacao
    ) {
        cursoRepository.findById(idCurso)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Curso não encontrado: " + idCurso));
        Grr grrVo = Grr.of(grr);
        Email institucional = Email.of(emailInstitucional);
        if (alunoRepository.existsByGrr(grrVo.getValue())) {
            throw new ConflitoEstadoException("Já existe aluno com o GRR " + grrVo);
        }
        if (alunoRepository.existsByEmailInstitucional(institucional.getValue())) {
            throw new ConflitoEstadoException("Já existe aluno com o e-mail " + institucional);
        }
        OffsetDateTime agora = OffsetDateTime.now();
        Aluno aluno = new Aluno(
                Uuids.v7(),
                nome,
                nomeSocial,
                grrVo,
                institucional,
                emailPessoal == null || emailPessoal.isBlank() ? null : Email.of(emailPessoal),
                telefone,
                idCurso,
                situacao == null ? AlunoSituacao.MATRICULADO : situacao,
                true,
                agora,
                agora
        );
        return alunoRepository.save(aluno);
    }

    public Aluno atualizar(
            UUID id,
            String nome,
            String nomeSocial,
            String emailPessoal,
            String telefone,
            UUID idCurso,
            AlunoSituacao situacao,
            Boolean ativo
    ) {
        Aluno aluno = buscarPorId(id);
        if (idCurso != null) {
            cursoRepository.findById(idCurso)
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Curso não encontrado: " + idCurso));
        }
        Email pessoal = emailPessoal == null || emailPessoal.isBlank() ? null : Email.of(emailPessoal);
        aluno.atualizar(nome, nomeSocial, pessoal, telefone, idCurso, situacao, ativo);
        return alunoRepository.save(aluno);
    }

    public void excluir(UUID id) {
        buscarPorId(id);
        alunoRepository.deleteById(id);
    }
}
