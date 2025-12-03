package com.devops.projeto_ac2.infrastructure.messaging.consumers;

import com.devops.projeto_ac2.domain.events.AlunoConcluidoEvent;
import com.devops.projeto_ac2.infrastructure.messaging.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumer (Listener) de eventos AlunoConcluido
 * Simula um MICROSERVIÇO de CERTIFICADOS que gera certificados PDF
 * 
 * Demonstra como múltiplos microserviços podem consumir o mesmo tipo de evento
 */
@Component
public class CertificadoServiceConsumer {
    
    private static final Logger logger = LoggerFactory.getLogger(CertificadoServiceConsumer.class);
    
    @RabbitListener(queues = RabbitMQConfig.QUEUE_ALUNO_CONCLUIDO)
    public void processarAlunoConcluido(AlunoConcluidoEvent event) {
        try {
            logger.info("╔════════════════════════════════════════════════════════════╗");
            logger.info("║  MICROSERVIÇO: Certificado Service                        ║");
            logger.info("╠════════════════════════════════════════════════════════════╣");
            logger.info("║  Evento recebido: AlunoConcluido                          ║");
            logger.info("║  EventID: {}", String.format("%-45s", event.getEventId()) + "║");
            logger.info("║  AlunoID: {}", String.format("%-45s", event.getAlunoId()) + "║");
            logger.info("║  Nome: {}", String.format("%-48s", event.getNome()) + "║");
            logger.info("║  Média Final: {}", String.format("%-42s", event.getMediaFinal()) + "║");
            logger.info("║  Aprovado: {}", String.format("%-45s", event.isAprovado() ? "SIM" : "NÃO") + "║");
            logger.info("╠════════════════════════════════════════════════════════════╣");
            
            if (event.isAprovado()) {
                logger.info("║  AÇÃO: Gerando certificado de conclusão...                ║");
                gerarCertificado(event);
                logger.info("║  ✓ Certificado gerado com sucesso!                        ║");
                logger.info("║  📄 Arquivo: certificado_{}.pdf", String.format("%-30s", event.getAlunoId()) + "║");
            } else {
                logger.info("║  ⚠ Aluno reprovado - Certificado não será gerado         ║");
            }
            
            logger.info("╚════════════════════════════════════════════════════════════╝");
            
        } catch (Exception e) {
            logger.error("Erro ao processar evento AlunoConcluido no CertificadoService - EventID: {}", 
                        event.getEventId(), e);
        }
    }
    
    private void gerarCertificado(AlunoConcluidoEvent event) {
        // Simulação de geração de certificado
        // Em produção, integraria com:
        // - iText (geração de PDF)
        // - JasperReports
        // - Apache PDFBox
        // - Template engines (Thymeleaf, FreeMarker)
        
        logger.info("   📋 Certificado emitido para: {}", event.getNome());
        logger.info("   📋 RA: {}", event.getRegistroAcademico());
        logger.info("   📋 Média Final: {}", event.getMediaFinal());
        logger.info("   📋 Data de conclusão: {}", event.getDataConclusao());
        
        // Simula salvamento em storage
        String caminhoArquivo = String.format("certificados/certificado_%s.pdf", event.getAlunoId());
        logger.info("   💾 Salvando em: {}", caminhoArquivo);
    }
}
