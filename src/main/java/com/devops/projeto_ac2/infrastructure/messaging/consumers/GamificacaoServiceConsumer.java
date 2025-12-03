package com.devops.projeto_ac2.infrastructure.messaging.consumers;

import com.devops.projeto_ac2.domain.events.AlunoConcluidoEvent;
import com.devops.projeto_ac2.infrastructure.messaging.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumer (Listener) de eventos AlunoConcluido
 * Simula um MICROSERVIÇO de GAMIFICAÇÃO que atribui pontos e badges
 * 
 * Este é outro exemplo de consumer do mesmo evento (AlunoConcluido)
 * Demonstra como um evento pode disparar múltiplas ações em sistemas diferentes
 */
@Component
public class GamificacaoServiceConsumer {
    
    private static final Logger logger = LoggerFactory.getLogger(GamificacaoServiceConsumer.class);
    
    @RabbitListener(queues = RabbitMQConfig.QUEUE_ALUNO_CONCLUIDO)
    public void processarAlunoConcluido(AlunoConcluidoEvent event) {
        try {
            logger.info("╔════════════════════════════════════════════════════════════╗");
            logger.info("║  MICROSERVIÇO: Gamificação Service                        ║");
            logger.info("╠════════════════════════════════════════════════════════════╣");
            logger.info("║  Evento recebido: AlunoConcluido                          ║");
            logger.info("║  EventID: {}", String.format("%-45s", event.getEventId()) + "║");
            logger.info("║  AlunoID: {}", String.format("%-45s", event.getAlunoId()) + "║");
            logger.info("║  Nome: {}", String.format("%-48s", event.getNome()) + "║");
            logger.info("║  Média Final: {}", String.format("%-42s", event.getMediaFinal()) + "║");
            logger.info("╠════════════════════════════════════════════════════════════╣");
            logger.info("║  AÇÃO: Calculando recompensas de gamificação...            ║");
            
            calcularRecompensas(event);
            
            logger.info("║  ✓ Recompensas atribuídas com sucesso!                    ║");
            logger.info("╚════════════════════════════════════════════════════════════╝");
            
        } catch (Exception e) {
            logger.error("Erro ao processar evento AlunoConcluido no GamificacaoService - EventID: {}", 
                        event.getEventId(), e);
        }
    }
    
    private void calcularRecompensas(AlunoConcluidoEvent event) {
        // Simulação de sistema de gamificação
        // Em produção, integraria com banco de dados de pontos/badges
        
        int pontos = calcularPontos(event.getMediaFinal());
        String badge = determinarBadge(event.getMediaFinal());
        
        logger.info("   🎮 Pontos ganhos: {}", pontos);
        logger.info("   🏆 Badge conquistado: {}", badge);
        logger.info("   ⭐ Novo nível: {}", determinarNivel(pontos));
        
        // Simula salvamento no banco de gamificação
        logger.info("   💾 Atualizando perfil de gamificação do aluno {}", event.getAlunoId());
    }
    
    private int calcularPontos(double media) {
        // Lógica de pontuação baseada na média
        if (media >= 9.0) return 1000;
        if (media >= 8.0) return 800;
        if (media >= 7.0) return 600;
        if (media >= 6.0) return 400;
        return 200;
    }
    
    private String determinarBadge(double media) {
        if (media >= 9.5) return "🥇 Excelência Máxima";
        if (media >= 9.0) return "🥈 Desempenho Excepcional";
        if (media >= 8.0) return "🥉 Alto Desempenho";
        if (media >= 7.0) return "⭐ Bom Desempenho";
        return "✓ Concluído";
    }
    
    private String determinarNivel(int pontos) {
        if (pontos >= 1000) return "Mestre";
        if (pontos >= 800) return "Avançado";
        if (pontos >= 600) return "Intermediário";
        return "Iniciante";
    }
}
