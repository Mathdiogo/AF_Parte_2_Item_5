package com.devops.projeto_ac2.dto;

import lombok.Data;

@Data
public class AlunoDTO {

    private Long id;
    private String nome;
    private String ra;
    private Double mediaFinal;
    private Boolean concluiu;
    private Integer cursosAdicionais;
}
