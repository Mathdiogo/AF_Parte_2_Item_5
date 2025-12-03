package com.devops.projeto_ac2.domain.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Evento de Domínio: representa que um aluno concluiu um curso
 * Este evento pode ser consumido por múltiplos microserviços:
 * - Serviço de Certificados (gerar certificado)
 * - Serviço de Notificações (enviar email/SMS)
 * - Serviço de Gamificação (atualizar pontos/badges)
 * - Serviço de Analytics (registrar métricas)
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlunoConcluidoEvent {
    
    private Long alunoId;
    private String nome;
    private String registroAcademico;
    private double mediaFinal;
    private boolean aprovado;
    private LocalDateTime dataConclusao;
    private String eventId; // ID único do evento para rastreamento
    
    public AlunoConcluidoEvent(Long alunoId, String nome, String registroAcademico, 
                               double mediaFinal, boolean aprovado) {
        this.alunoId = alunoId;
        this.nome = nome;
        this.registroAcademico = registroAcademico;
        this.mediaFinal = mediaFinal;
        this.aprovado = aprovado;
        this.dataConclusao = LocalDateTime.now();
        this.eventId = java.util.UUID.randomUUID().toString();
    }
}
