package com.devops.projeto_ac2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.devops.projeto_ac2.entity.Aluno;


public interface Aluno_Repository extends JpaRepository<Aluno, Long> {
}