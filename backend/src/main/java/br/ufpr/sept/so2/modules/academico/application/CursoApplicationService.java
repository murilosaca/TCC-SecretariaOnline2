package br.ufpr.sept.so2.modules.academico.application;

import br.ufpr.sept.so2.modules.academico.application.ports.CursoRepository;
import br.ufpr.sept.so2.modules.academico.domain.Curso;
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
public class CursoApplicationService {

    private final CursoRepository cursoRepository;

    public CursoApplicationService(CursoRepository cursoRepository) {
        this.cursoRepository = cursoRepository;
    }

    @Transactional(readOnly = true)
    public Page<Curso> listar(Pageable pageable) {
        return cursoRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Curso buscarPorId(UUID id) {
        return cursoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Curso não encontrado: " + id));
    }

    public Curso criar(String nome, String sigla, String codigo, UUID idCoordenador, int horasFormativasMinimas) {
        if (cursoRepository.existsBySigla(sigla)) {
            throw new ConflitoEstadoException("Já existe curso com a sigla " + sigla);
        }
        if (cursoRepository.existsByCodigo(codigo)) {
            throw new ConflitoEstadoException("Já existe curso com o código " + codigo);
        }
        OffsetDateTime agora = OffsetDateTime.now();
        Curso curso = new Curso(
                Uuids.v7(),
                nome,
                sigla.trim().toUpperCase(),
                codigo.trim().toUpperCase(),
                idCoordenador,
                horasFormativasMinimas,
                true,
                agora,
                agora
        );
        return cursoRepository.save(curso);
    }

    public Curso atualizar(UUID id, String nome, String sigla, String codigo, UUID idCoordenador, Integer horas, Boolean ativo) {
        Curso curso = buscarPorId(id);
        curso.atualizar(nome, sigla == null ? null : sigla.trim().toUpperCase(),
                codigo == null ? null : codigo.trim().toUpperCase(), idCoordenador, horas, ativo);
        return cursoRepository.save(curso);
    }

    public void excluir(UUID id) {
        buscarPorId(id);
        cursoRepository.deleteById(id);
    }
}
