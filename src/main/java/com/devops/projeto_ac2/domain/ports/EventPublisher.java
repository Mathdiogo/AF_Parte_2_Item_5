package com.devops.projeto_ac2.domain.ports;

import com.devops.projeto_ac2.domain.events.AlunoConcluidoEvent;
import com.devops.projeto_ac2.domain.events.AlunoCriadoEvent;
import com.devops.projeto_ac2.domain.events.TentativaRegistradaEvent;

/**
 * Port (Interface) para publicação de eventos
 * 
 * Clean Architecture: o domínio não conhece detalhes de implementação
 * Esta interface será implementada por um adapter na camada de infraestrutura
 * 
 * Isso permite:
 * - Trocar a implementação (RabbitMQ, Kafka, MQTT) sem afetar o domínio
 * - Testar com mocks facilmente
 * - Manter o domínio isolado de frameworks
 */
public interface EventPublisher {
    
    /**
     * Publica evento de aluno criado
     */
    void publicarAlunoCriado(AlunoCriadoEvent event);
    
    /**
     * Publica evento de aluno concluído
     */
    void publicarAlunoConcluido(AlunoConcluidoEvent event);
    
    /**
     * Publica evento de tentativa registrada
     */
    void publicarTentativaRegistrada(TentativaRegistradaEvent event);
}
