package com.devops.projeto_ac2.infrastructure.messaging.consumers;

import com.devops.projeto_ac2.domain.events.AlunoCriadoEvent;
import com.devops.projeto_ac2.infrastructure.messaging.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumer (Listener) de eventos AlunoCriado
 * Simula um MICROSERVIÇO de EMAIL que envia emails de boas-vindas
 * 
 * Este é um exemplo de arquitetura orientada a eventos:
 * - Publisher: CriarAlunoUseCase publica o evento
 * - Consumer: Este listener consome o evento e executa ação
 * 
 * Em produção, cada consumer seria um microserviço separado
 */
@Component
public class EmailServiceConsumer {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailServiceConsumer.class);
    
    /**
     * Escuta eventos da fila de aluno criado
     * @RabbitListener: anotação que marca este método como consumer
     */
    @RabbitListener(queues = RabbitMQConfig.QUEUE_ALUNO_CRIADO)
    public void processarAlunoCriado(AlunoCriadoEvent event) {
        try {
            logger.info("╔════════════════════════════════════════════════════════════╗");
            logger.info("║  MICROSERVIÇO: Email Service                              ║");
            logger.info("╠════════════════════════════════════════════════════════════╣");
            logger.info("║  Evento recebido: AlunoCriado                             ║");
            logger.info("║  EventID: {}", String.format("%-45s", event.getEventId()) + "║");
            logger.info("║  AlunoID: {}", String.format("%-45s", event.getAlunoId()) + "║");
            logger.info("║  Nome: {}", String.format("%-48s", event.getNome()) + "║");
            logger.info("║  RA: {}", String.format("%-50s", event.getRegistroAcademico()) + "║");
            logger.info("╠════════════════════════════════════════════════════════════╣");
            logger.info("║  AÇÃO: Enviando email de boas-vindas...                   ║");
            
            // Simula envio de email (em produção, integraria com SendGrid, SES, etc.)
            enviarEmailBoasVindas(event);
            
            logger.info("║  ✓ Email de boas-vindas enviado com sucesso!              ║");
            logger.info("╚════════════════════════════════════════════════════════════╝");
            
        } catch (Exception e) {
            logger.error("Erro ao processar evento AlunoCriado no EmailService - EventID: {}", 
                        event.getEventId(), e);
            // Em produção: enviar para Dead Letter Queue (DLQ)
        }
    }
    
    private void enviarEmailBoasVindas(AlunoCriadoEvent event) {
        // Simulação de envio de email
        // Em produção, integraria com:
        // - AWS SES
        // - SendGrid
        // - Mailgun
        // - SMTP
        
        String destinatario = event.getRegistroAcademico() + "@faculdade.edu.br";
        String assunto = "Bem-vindo(a) à Plataforma!";
        String corpo = String.format("""
            Olá %s,
            
            Seja bem-vindo(a) à nossa plataforma de ensino!
            Seu registro acadêmico é: %s
            
            Estamos felizes em ter você conosco.
            
            Atenciosamente,
            Equipe Acadêmica
            """, event.getNome(), event.getRegistroAcademico());
        
        logger.info("   📧 Para: {}", destinatario);
        logger.info("   📧 Assunto: {}", assunto);
        logger.info("   📧 Corpo: {} caracteres", corpo.length());
    }
}
