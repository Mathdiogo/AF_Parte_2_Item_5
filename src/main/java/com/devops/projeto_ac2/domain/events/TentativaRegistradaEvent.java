package com.devops.projeto_ac2.domain.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Evento de Domínio: representa que um aluno registrou uma tentativa de avaliação
 * Usado para monitoramento e analytics
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TentativaRegistradaEvent {
    
    private Long alunoId;
    private String registroAcademico;
    private int numeroTentativa;
    private LocalDateTime dataRegistro;
    private String eventId;
    
    public TentativaRegistradaEvent(Long alunoId, String registroAcademico, int numeroTentativa) {
        this.alunoId = alunoId;
        this.registroAcademico = registroAcademico;
        this.numeroTentativa = numeroTentativa;
        this.dataRegistro = LocalDateTime.now();
        this.eventId = java.util.UUID.randomUUID().toString();
    }
}
