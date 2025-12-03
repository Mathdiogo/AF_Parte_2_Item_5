package com.devops.projeto_ac2.application.usecases;

import com.devops.projeto_ac2.domain.entities.Aluno;
import com.devops.projeto_ac2.domain.events.AlunoCriadoEvent;
import com.devops.projeto_ac2.domain.exceptions.DomainException;
import com.devops.projeto_ac2.domain.ports.EventPublisher;
import com.devops.projeto_ac2.domain.repositories.AlunoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para CriarAlunoUseCase com RabbitMQ
 * 
 * IMPORTÂNCIA DOS MOCKS EM MENSAGERIA:
 * 
 * 1. ISOLAMENTO: Testa apenas a lógica do UseCase, sem depender do RabbitMQ real
 * 2. VELOCIDADE: Testes executam rapidamente sem conexões de rede
 * 3. CONFIABILIDADE: Não falha por problemas de infraestrutura (RabbitMQ down)
 * 4. CONTROLE: Simula cenários difíceis (falhas, timeouts, etc.)
 * 5. CI/CD: Roda em pipelines sem necessidade de containers
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do CriarAlunoUseCase com EventPublisher")
class CriarAlunoUseCaseTestComMensageria {
    
    @Mock
    private AlunoRepository alunoRepository;
    
    @Mock
    private EventPublisher eventPublisher;
    
    @InjectMocks
    private CriarAlunoUseCase useCase;
    
    @BeforeEach
    void setUp() {
        // Configuração inicial se necessário
    }
    
    @Test
    @DisplayName("Deve criar aluno e publicar evento AlunoCriado")
    void deveCriarAlunoEPublicarEvento() {
        // Arrange
        String nome = "João Silva";
        String ra = "RA123456";
        
        when(alunoRepository.existePorRA(ra)).thenReturn(false);
        when(alunoRepository.salvar(any(Aluno.class))).thenAnswer(invocation -> {
            Aluno aluno = invocation.getArgument(0);
            // Simula o ID gerado pelo banco
            aluno.getClass().getDeclaredField("id").setAccessible(true);
            aluno.getClass().getDeclaredField("id").set(aluno, 1L);
            return aluno;
        });
        
        // Act
        Aluno resultado = useCase.executar(nome, ra);
        
        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getNome()).isEqualTo(nome);
        assertThat(resultado.getRegistroAcademico().getValor()).isEqualTo(ra);
        
        // Verifica se o repositório foi chamado
        verify(alunoRepository, times(1)).existePorRA(ra);
        verify(alunoRepository, times(1)).salvar(any(Aluno.class));
        
        // IMPORTANTE: Verifica se o evento foi publicado
        ArgumentCaptor<AlunoCriadoEvent> eventCaptor = ArgumentCaptor.forClass(AlunoCriadoEvent.class);
        verify(eventPublisher, times(1)).publicarAlunoCriado(eventCaptor.capture());
        
        AlunoCriadoEvent evento = eventCaptor.getValue();
        assertThat(evento).isNotNull();
        assertThat(evento.getAlunoId()).isEqualTo(1L);
        assertThat(evento.getNome()).isEqualTo(nome);
        assertThat(evento.getRegistroAcademico()).isEqualTo(ra);
        assertThat(evento.getEventId()).isNotNull();
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando RA já existe e NÃO publicar evento")
    void deveLancarExcecaoQuandoRAJaExiste() {
        // Arrange
        String nome = "Maria Santos";
        String ra = "RA999999";
        
        when(alunoRepository.existePorRA(ra)).thenReturn(true);
        
        // Act & Assert
        assertThatThrownBy(() -> useCase.executar(nome, ra))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining("Já existe um aluno cadastrado com o RA");
        
        // Verifica que o repositório foi consultado mas não salvou
        verify(alunoRepository, times(1)).existePorRA(ra);
        verify(alunoRepository, never()).salvar(any(Aluno.class));
        
        // IMPORTANTE: Verifica que o evento NÃO foi publicado
        verify(eventPublisher, never()).publicarAlunoCriado(any(AlunoCriadoEvent.class));
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando nome é inválido e NÃO publicar evento")
    void deveLancarExcecaoQuandoNomeInvalido() {
        // Arrange
        String nomeInvalido = "A"; // Nome muito curto
        String ra = "RA123456";
        
        when(alunoRepository.existePorRA(ra)).thenReturn(false);
        
        // Act & Assert
        assertThatThrownBy(() -> useCase.executar(nomeInvalido, ra))
            .isInstanceOf(DomainException.class);
        
        // IMPORTANTE: Verifica que o evento NÃO foi publicado devido ao erro
        verify(eventPublisher, never()).publicarAlunoCriado(any(AlunoCriadoEvent.class));
    }
    
    @Test
    @DisplayName("Deve criar aluno mesmo se publicação de evento falhar (não deve bloquear o processo)")
    void deveCriarAlunoMesmoSePublicacaoFalhar() {
        // Arrange
        String nome = "Pedro Oliveira";
        String ra = "RA555555";
        
        when(alunoRepository.existePorRA(ra)).thenReturn(false);
        when(alunoRepository.salvar(any(Aluno.class))).thenAnswer(invocation -> {
            Aluno aluno = invocation.getArgument(0);
            aluno.getClass().getDeclaredField("id").setAccessible(true);
            aluno.getClass().getDeclaredField("id").set(aluno, 2L);
            return aluno;
        });
        
        // Simula falha na publicação do evento
        doThrow(new RuntimeException("RabbitMQ indisponível"))
            .when(eventPublisher).publicarAlunoCriado(any(AlunoCriadoEvent.class));
        
        // Act & Assert
        // Neste cenário, a falha na mensageria deve propagar (decisão de design)
        assertThatThrownBy(() -> useCase.executar(nome, ra))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("RabbitMQ indisponível");
        
        // Verifica que tentou salvar o aluno antes da falha
        verify(alunoRepository, times(1)).salvar(any(Aluno.class));
        verify(eventPublisher, times(1)).publicarAlunoCriado(any(AlunoCriadoEvent.class));
    }
}
