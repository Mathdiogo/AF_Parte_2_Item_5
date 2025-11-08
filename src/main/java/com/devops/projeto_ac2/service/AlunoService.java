package com.devops.projeto_ac2.service;

import com.devops.projeto_ac2.dto.AlunoConcluirDTO;
import com.devops.projeto_ac2.dto.AlunoCriarDTO;
import com.devops.projeto_ac2.entity.Aluno;
import com.devops.projeto_ac2.entity.AlunoRA;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.devops.projeto_ac2.dto.AlunoDTO;
import com.devops.projeto_ac2.repository.Aluno_Repository;

@Service
public class AlunoService {

    @Autowired
    private Aluno_Repository aluno_repository;

    public AlunoDTO salvar(AlunoCriarDTO alunoCreateDTO) {
        Aluno aluno = Aluno.builder()
                .nome(alunoCreateDTO.getNome())
                .AlunoRA(new AlunoRA(alunoCreateDTO.getRa()))
                .concluiu(false)
                .mediaFinal(0)
                .cursosAdicionais(0)
                .build();

        Aluno savedAluno = aluno_repository.save(aluno);
        return mapToDTO(savedAluno);
    }

    public AlunoDTO concluirCurso(Long alunoId, AlunoConcluirDTO concluirDTO) {
        Aluno aluno = findAlunoById(alunoId);

        if (aluno.isConcluiu()) {
            throw new IllegalStateException("aluno " + alunoId + " ja concluiu o curso");
        }

        aluno.setConcluiu(true);
        aluno.setMediaFinal(concluirDTO.getMediaFinal());

        if (aluno.getMediaFinal() >= 7.0) {
            aluno.setCursosAdicionais(aluno.getCursosAdicionais() + 3);
        }

        Aluno alunoAtualizado = aluno_repository.save(aluno);
        return mapToDTO(alunoAtualizado);
    }

    private AlunoDTO findById(Long id) {
        Aluno aluno = findAlunoById(id);
        return mapToDTO(aluno);
    }

    private Aluno findAlunoById(Long id) {
            return aluno_repository.findById(id).orElseThrow(() -> new EntityNotFoundException("id nao encontrado: " + id));
        }


    private AlunoDTO mapToDTO(Aluno aluno) {
        AlunoDTO dto = new AlunoDTO();
        dto.setId(aluno.getId());
        dto.setNome(aluno.getNome());
        dto.setRa(aluno.getAlunoRA().getRegistroMatricula());
        dto.setMediaFinal(aluno.getMediaFinal());
        dto.setConcluiu(aluno.isConcluiu());
        dto.setCursosAdicionais(aluno.getCursosAdicionais());
        return dto;
    }
}
