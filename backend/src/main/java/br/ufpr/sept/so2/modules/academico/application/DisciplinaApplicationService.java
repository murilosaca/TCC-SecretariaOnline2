package br.ufpr.sept.so2.modules.academico.application;

import br.ufpr.sept.so2.modules.academico.application.ports.CursoRepository;
import br.ufpr.sept.so2.modules.academico.application.ports.DisciplinaRepository;
import br.ufpr.sept.so2.modules.academico.domain.Disciplina;
import br.ufpr.sept.so2.shared.domain.exception.ConflitoEstadoException;
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException;
import br.ufpr.sept.so2.shared.infrastructure.Uuids;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@Transactional
public class DisciplinaApplicationService {

    private final DisciplinaRepository disciplinaRepository;
    private final CursoRepository cursoRepository;

    public DisciplinaApplicationService(DisciplinaRepository disciplinaRepository, CursoRepository cursoRepository) {
        this.disciplinaRepository = disciplinaRepository;
        this.cursoRepository = cursoRepository;
    }

    @Transactional(readOnly = true)
    public Page<Disciplina> listar(UUID idCurso, Pageable pageable) {
        return disciplinaRepository.findAll(idCurso, pageable);
    }

    @Transactional(readOnly = true)
    public Disciplina buscarPorId(UUID id) {
        return disciplinaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Disciplina não encontrada: " + id));
    }

    public Disciplina criar(UUID idCurso, String codigo, String nome, int periodo, int cargaHorariaTotal, int creditos) {
        cursoRepository.findById(idCurso)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Curso não encontrado: " + idCurso));
        String codigoNormalizado = codigo.trim().toUpperCase();
        if (disciplinaRepository.existsByCursoAndCodigo(idCurso, codigoNormalizado)) {
            throw new ConflitoEstadoException("Já existe disciplina com o código " + codigoNormalizado + " neste curso");
        }
        OffsetDateTime agora = OffsetDateTime.now();
        Disciplina disciplina = new Disciplina(
                Uuids.v7(),
                idCurso,
                codigoNormalizado,
                nome,
                periodo,
                cargaHorariaTotal,
                creditos,
                true,
                agora,
                agora
        );
        return disciplinaRepository.save(disciplina);
    }

    public Disciplina atualizar(UUID id, String codigo, String nome, Integer periodo, Integer carga, Integer creditos, Boolean ativa) {
        Disciplina disciplina = buscarPorId(id);
        disciplina.atualizar(codigo == null ? null : codigo.trim().toUpperCase(), nome, periodo, carga, creditos, ativa);
        return disciplinaRepository.save(disciplina);
    }

    public void excluir(UUID id) {
        buscarPorId(id);
        disciplinaRepository.deleteById(id);
    }
}
