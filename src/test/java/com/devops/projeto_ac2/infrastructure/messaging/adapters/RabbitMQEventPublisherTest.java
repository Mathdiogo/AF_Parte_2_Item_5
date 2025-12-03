package com.devops.projeto_ac2.infrastructure.messaging.adapters;

import com.devops.projeto_ac2.domain.events.AlunoConcluidoEvent;
import com.devops.projeto_ac2.domain.events.AlunoCriadoEvent;
import com.devops.projeto_ac2.domain.events.TentativaRegistradaEvent;
import com.devops.projeto_ac2.infrastructure.messaging.config.RabbitMQConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para RabbitMQEventPublisher
 * 
 * JUSTIFICATIVA DOS MOCKS NO ADAPTER DE MENSAGERIA:
 * 
 * 1. ISOLAMENTO DO RABBITMQ: Não precisa do RabbitMQ rodando para testar
 * 2. VERIFICAÇÃO DE INTEGRAÇÃO: Confirma que o adapter chama o RabbitTemplate corretamente
 * 3. TESTES DE FALHA: Simula falhas de conexão, timeout, etc.
 * 4. RAPIDEZ: Testes executam em milissegundos
 * 5. CI/CD: Pipeline não precisa subir containers
 * 
 * DIFERENÇA ENTRE TESTES UNITÁRIOS E TESTES DE INTEGRAÇÃO:
 * - Unitários (aqui): Mockam RabbitTemplate, testam lógica do adapter
 * - Integração: Usariam RabbitMQ real (testcontainers) para validar comunicação fim-a-fim
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do RabbitMQEventPublisher (Adapter)")
class RabbitMQEventPublisherTest {
    
    @Mock
    private RabbitTemplate rabbitTemplate;
    
    @InjectMocks
    private RabbitMQEventPublisher publisher;
    
    @Test
    @DisplayName("Deve publicar evento AlunoCriado com routing key correta")
    void devePublicarEventoAlunoCriadoComRoutingKeyCorreta() {
        // Arrange
        AlunoCriadoEvent event = new AlunoCriadoEvent(1L, "João Silva", "RA123456");
        
        // Act
        publisher.publicarAlunoCriado(event);
        
        // Assert
        ArgumentCaptor<Object> messageCaptor = ArgumentCaptor.forClass(Object.class);
        verify(rabbitTemplate, times(1)).convertAndSend(
            eq(RabbitMQConfig.EXCHANGE_NAME),
            eq(RabbitMQConfig.ROUTING_KEY_ALUNO_CRIADO),
            messageCaptor.capture()
        );
        
        Object capturedMessage = messageCaptor.getValue();
        assertThat(capturedMessage).isInstanceOf(AlunoCriadoEvent.class);
        assertThat(((AlunoCriadoEvent) capturedMessage).getAlunoId()).isEqualTo(1L);
    }
    
    @Test
    @DisplayName("Deve publicar evento AlunoConcluido com routing key correta")
    void devePublicarEventoAlunoConcluidoComRoutingKeyCorreta() {
        // Arrange
        AlunoConcluidoEvent event = new AlunoConcluidoEvent(
            2L, "Maria Santos", "RA999999", 8.5, true
        );
        
        // Act
        publisher.publicarAlunoConcluido(event);
        
        // Assert
        verify(rabbitTemplate, times(1)).convertAndSend(
            eq(RabbitMQConfig.EXCHANGE_NAME),
            eq(RabbitMQConfig.ROUTING_KEY_ALUNO_CONCLUIDO),
            eq(event)
        );
    }
    
    @Test
    @DisplayName("Deve publicar evento TentativaRegistrada com routing key correta")
    void devePublicarEventoTentativaRegistradaComRoutingKeyCorreta() {
        // Arrange
        TentativaRegistradaEvent event = new TentativaRegistradaEvent(
            3L, "RA555555", 2
        );
        
        // Act
        publisher.publicarTentativaRegistrada(event);
        
        // Assert
        verify(rabbitTemplate, times(1)).convertAndSend(
            eq(RabbitMQConfig.EXCHANGE_NAME),
            eq(RabbitMQConfig.ROUTING_KEY_TENTATIVA),
            eq(event)
        );
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando RabbitTemplate falha")
    void deveLancarExcecaoQuandoRabbitTemplateFalha() {
        // Arrange
        AlunoCriadoEvent event = new AlunoCriadoEvent(1L, "Teste", "RA000000");
        
        doThrow(new RuntimeException("Conexão perdida com RabbitMQ"))
            .when(rabbitTemplate).convertAndSend(anyString(), anyString(), any());
        
        // Act & Assert
        assertThatThrownBy(() -> publisher.publicarAlunoCriado(event))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Falha ao publicar evento");
    }
    
    @Test
    @DisplayName("Deve verificar que eventos são enviados para exchange correto")
    void deveVerificarQueEventosSaoEnviadosParaExchangeCorreto() {
        // Arrange
        AlunoCriadoEvent event1 = new AlunoCriadoEvent(1L, "Aluno 1", "RA111");
        AlunoConcluidoEvent event2 = new AlunoConcluidoEvent(2L, "Aluno 2", "RA222", 7.0, true);
        TentativaRegistradaEvent event3 = new TentativaRegistradaEvent(3L, "RA333", 1);
        
        // Act
        publisher.publicarAlunoCriado(event1);
        publisher.publicarAlunoConcluido(event2);
        publisher.publicarTentativaRegistrada(event3);
        
        // Assert - Todos devem ir para o mesmo exchange
        verify(rabbitTemplate, times(3)).convertAndSend(
            eq(RabbitMQConfig.EXCHANGE_NAME),
            anyString(),
            any()
        );
    }
    
    @Test
    @DisplayName("Deve verificar que cada tipo de evento usa routing key diferente")
    void deveVerificarQueRoutingKeysSaoDiferentes() {
        // Arrange
        AlunoCriadoEvent event1 = new AlunoCriadoEvent(1L, "Test", "RA111");
        AlunoConcluidoEvent event2 = new AlunoConcluidoEvent(1L, "Test", "RA111", 7.0, true);
        TentativaRegistradaEvent event3 = new TentativaRegistradaEvent(1L, "RA111", 1);
        
        // Act
        publisher.publicarAlunoCriado(event1);
        publisher.publicarAlunoConcluido(event2);
        publisher.publicarTentativaRegistrada(event3);
        
        // Assert - Verifica routing keys diferentes
        verify(rabbitTemplate).convertAndSend(
            anyString(),
            eq(RabbitMQConfig.ROUTING_KEY_ALUNO_CRIADO),
            any()
        );
        verify(rabbitTemplate).convertAndSend(
            anyString(),
            eq(RabbitMQConfig.ROUTING_KEY_ALUNO_CONCLUIDO),
            any()
        );
        verify(rabbitTemplate).convertAndSend(
            anyString(),
            eq(RabbitMQConfig.ROUTING_KEY_TENTATIVA),
            any()
        );
    }
}
