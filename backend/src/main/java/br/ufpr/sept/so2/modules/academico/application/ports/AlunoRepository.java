package br.ufpr.sept.so2.modules.academico.application.ports;

import br.ufpr.sept.so2.modules.academico.domain.Aluno;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface AlunoRepository {

    Aluno save(Aluno aluno);

    Optional<Aluno> findById(UUID id);

    Page<Aluno> findAll(UUID idCurso, String termo, Pageable pageable);

    boolean existsByGrr(String grr);

    boolean existsByEmailInstitucional(String email);

    void deleteById(UUID id);
}
