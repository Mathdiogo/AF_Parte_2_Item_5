package com.devops.projeto_ac2.infrastructure.messaging.consumers;

import com.devops.projeto_ac2.domain.events.TentativaRegistradaEvent;
import com.devops.projeto_ac2.infrastructure.messaging.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumer (Listener) de eventos TentativaRegistrada
 * Simula um MICROSERVIÇO de ANALYTICS que registra métricas e estatísticas
 */
@Component
public class AnalyticsServiceConsumer {
    
    private static final Logger logger = LoggerFactory.getLogger(AnalyticsServiceConsumer.class);
    
    @RabbitListener(queues = RabbitMQConfig.QUEUE_TENTATIVA_REGISTRADA)
    public void processarTentativaRegistrada(TentativaRegistradaEvent event) {
        try {
            logger.info("╔════════════════════════════════════════════════════════════╗");
            logger.info("║  MICROSERVIÇO: Analytics Service                          ║");
            logger.info("╠════════════════════════════════════════════════════════════╣");
            logger.info("║  Evento recebido: TentativaRegistrada                     ║");
            logger.info("║  EventID: {}", String.format("%-45s", event.getEventId()) + "║");
            logger.info("║  AlunoID: {}", String.format("%-45s", event.getAlunoId()) + "║");
            logger.info("║  RA: {}", String.format("%-50s", event.getRegistroAcademico()) + "║");
            logger.info("║  Tentativa nº: {}", String.format("%-42s", event.getNumeroTentativa()) + "║");
            logger.info("╠════════════════════════════════════════════════════════════╣");
            logger.info("║  AÇÃO: Registrando métricas de analytics...                ║");
            
            registrarMetricas(event);
            
            logger.info("║  ✓ Métricas registradas com sucesso!                      ║");
            logger.info("╚════════════════════════════════════════════════════════════╝");
            
        } catch (Exception e) {
            logger.error("Erro ao processar evento TentativaRegistrada no AnalyticsService - EventID: {}", 
                        event.getEventId(), e);
        }
    }
    
    private void registrarMetricas(TentativaRegistradaEvent event) {
        // Simulação de registro de métricas
        // Em produção, integraria com:
        // - Elasticsearch + Kibana
        // - Prometheus + Grafana
        // - Data Lake (S3 + Athena)
        // - BigQuery
        
        logger.info("   📊 Registrando métrica: tentativa_avaliacao");
        logger.info("   📊 Aluno: {}", event.getAlunoId());
        logger.info("   📊 Número da tentativa: {}", event.getNumeroTentativa());
        logger.info("   📊 Timestamp: {}", event.getDataRegistro());
        
        // Simula envio para sistema de analytics
        if (event.getNumeroTentativa() >= 3) {
            logger.warn("   ⚠️ ALERTA: Aluno {} atingiu o limite de tentativas!", event.getAlunoId());
            // Poderia disparar alertas, emails, etc.
        }
        
        logger.info("   💾 Dados enviados para data warehouse");
    }
}
