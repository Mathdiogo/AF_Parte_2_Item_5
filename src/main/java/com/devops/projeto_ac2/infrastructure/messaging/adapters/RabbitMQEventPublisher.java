package com.devops.projeto_ac2.infrastructure.messaging.adapters;

import com.devops.projeto_ac2.domain.events.AlunoConcluidoEvent;
import com.devops.projeto_ac2.domain.events.AlunoCriadoEvent;
import com.devops.projeto_ac2.domain.events.TentativaRegistradaEvent;
import com.devops.projeto_ac2.domain.ports.EventPublisher;
import com.devops.projeto_ac2.infrastructure.messaging.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Adapter (Implementação) do EventPublisher usando RabbitMQ
 * 
 * Esta classe é a ponte entre o domínio (Clean Architecture) e a infraestrutura (RabbitMQ)
 * Implementa o padrão Publisher/Producer de mensageria
 * 
 * @Component: marca como bean do Spring para injeção de dependência
 */
@Component
public class RabbitMQEventPublisher implements EventPublisher {
    
    private static final Logger logger = LoggerFactory.getLogger(RabbitMQEventPublisher.class);
    
    private final RabbitTemplate rabbitTemplate;
    
    public RabbitMQEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }
    
    @Override
    public void publicarAlunoCriado(AlunoCriadoEvent event) {
        try {
            logger.info("Publicando evento AlunoCriado para RabbitMQ - Aluno ID: {}, EventID: {}", 
                       event.getAlunoId(), event.getEventId());
            
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY_ALUNO_CRIADO,
                event
            );
            
            logger.info("Evento AlunoCriado publicado com sucesso - EventID: {}", event.getEventId());
        } catch (Exception e) {
            logger.error("Erro ao publicar evento AlunoCriado - EventID: {}", event.getEventId(), e);
            // Em produção, você poderia:
            // - Armazenar em fila de retry
            // - Enviar para Dead Letter Queue
            // - Alertar equipe de operações
            throw new RuntimeException("Falha ao publicar evento de aluno criado", e);
        }
    }
    
    @Override
    public void publicarAlunoConcluido(AlunoConcluidoEvent event) {
        try {
            logger.info("Publicando evento AlunoConcluido para RabbitMQ - Aluno ID: {}, Média: {}, EventID: {}", 
                       event.getAlunoId(), event.getMediaFinal(), event.getEventId());
            
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY_ALUNO_CONCLUIDO,
                event
            );
            
            logger.info("Evento AlunoConcluido publicado com sucesso - EventID: {}", event.getEventId());
        } catch (Exception e) {
            logger.error("Erro ao publicar evento AlunoConcluido - EventID: {}", event.getEventId(), e);
            throw new RuntimeException("Falha ao publicar evento de aluno concluído", e);
        }
    }
    
    @Override
    public void publicarTentativaRegistrada(TentativaRegistradaEvent event) {
        try {
            logger.info("Publicando evento TentativaRegistrada para RabbitMQ - Aluno ID: {}, Tentativa: {}, EventID: {}", 
                       event.getAlunoId(), event.getNumeroTentativa(), event.getEventId());
            
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY_TENTATIVA,
                event
            );
            
            logger.info("Evento TentativaRegistrada publicado com sucesso - EventID: {}", event.getEventId());
        } catch (Exception e) {
            logger.error("Erro ao publicar evento TentativaRegistrada - EventID: {}", event.getEventId(), e);
            throw new RuntimeException("Falha ao publicar evento de tentativa registrada", e);
        }
    }
}
