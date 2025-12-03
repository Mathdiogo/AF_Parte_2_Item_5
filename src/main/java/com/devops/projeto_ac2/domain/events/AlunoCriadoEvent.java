package com.devops.projeto_ac2.domain.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Evento de Domínio: representa que um aluno criou sua conta
 * Eventos são imutáveis e representam fatos que ocorreram no passado
 * Seguindo DDD Event-Driven Architecture
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlunoCriadoEvent {
    
    private Long alunoId;
    private String nome;
    private String registroAcademico;
    private LocalDateTime dataCriacao;
    private String eventId; // ID único do evento para rastreamento
    
    public AlunoCriadoEvent(Long alunoId, String nome, String registroAcademico) {
        this.alunoId = alunoId;
        this.nome = nome;
        this.registroAcademico = registroAcademico;
        this.dataCriacao = LocalDateTime.now();
        this.eventId = java.util.UUID.randomUUID().toString();
    }
}
