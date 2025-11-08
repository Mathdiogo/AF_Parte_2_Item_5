package com.devops.projeto_ac2.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "tb_alunos")
public class Aluno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    private String nome;

    private double mediaFinal;

    private boolean concluiu;

    @Embedded
    private AlunoRA AlunoRA;

    @Builder.Default
    private Integer cursosAdicionais = 0;

}
