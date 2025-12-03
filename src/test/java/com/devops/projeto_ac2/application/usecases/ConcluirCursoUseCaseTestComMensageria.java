package com.devops.projeto_ac2.application.usecases;

import com.devops.projeto_ac2.domain.entities.Aluno;
import com.devops.projeto_ac2.domain.events.AlunoConcluidoEvent;
import com.devops.projeto_ac2.domain.exceptions.AlunoNotFoundException;
import com.devops.projeto_ac2.domain.ports.EventPublisher;
import com.devops.projeto_ac2.domain.repositories.AlunoRepository;
import com.devops.projeto_ac2.domain.valueobjects.NomeAluno;
import com.devops.projeto_ac2.domain.valueobjects.RegistroAcademico;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para ConcluirCursoUseCase com RabbitMQ
 * 
 * JUSTIFICATIVA DOS MOCKS EM MENSAGERIA:
 * 
 * 1. DESACOPLAMENTO: Testa o UseCase sem depender da infraestrutura externa
 * 2. DETERMINISMO: Resultados previsíveis e reproduzíveis
 * 3. PERFORMANCE: Testes instantâneos sem I/O de rede
 * 4. FLEXIBILIDADE: Simula cenários de sucesso e falha facilmente
 * 5. FOCO: Testa a lógica de negócio, não a infraestrutura
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do ConcluirCursoUseCase com EventPublisher")
class ConcluirCursoUseCaseTestComMensageria {
    
    @Mock
    private AlunoRepository alunoRepository;
    
    @Mock
    private EventPublisher eventPublisher;
    
    @InjectMocks
    private ConcluirCursoUseCase useCase;
    
    @Test
    @DisplayName("Deve concluir curso e publicar evento quando aluno aprovado")
    void deveConcluirCursoEPublicarEventoQuandoAprovado() {
        // Arrange
        Long alunoId = 1L;
        double mediaFinal = 8.5;
        
        Aluno aluno = Aluno.criar(
            NomeAluno.criar("Ana Costa"),
            RegistroAcademico.criar("RA111111")
        );
        
        when(alunoRepository.buscarPorId(alunoId)).thenReturn(Optional.of(aluno));
        when(alunoRepository.salvar(any(Aluno.class))).thenReturn(aluno);
        
        // Act
        Aluno resultado = useCase.executar(alunoId, mediaFinal);
        
        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.isConcluiu()).isTrue();
        assertThat(resultado.getMediaFinal()).isEqualTo(mediaFinal);
        
        // Verifica que o evento foi publicado
        ArgumentCaptor<AlunoConcluidoEvent> eventCaptor = ArgumentCaptor.forClass(AlunoConcluidoEvent.class);
        verify(eventPublisher, times(1)).publicarAlunoConcluido(eventCaptor.capture());
        
        AlunoConcluidoEvent evento = eventCaptor.getValue();
        assertThat(evento).isNotNull();
        assertThat(evento.getNome()).isEqualTo("Ana Costa");
        assertThat(evento.getMediaFinal()).isEqualTo(mediaFinal);
        assertThat(evento.isAprovado()).isTrue();
        assertThat(evento.getEventId()).isNotNull();
    }
    
    @Test
    @DisplayName("Deve concluir curso e publicar evento mesmo quando aluno reprovado")
    void deveConcluirCursoEPublicarEventoQuandoReprovado() {
        // Arrange
        Long alunoId = 2L;
        double mediaFinal = 4.0;
        
        Aluno aluno = Aluno.criar(
            NomeAluno.criar("Bruno Dias"),
            RegistroAcademico.criar("RA222222")
        );
        
        when(alunoRepository.buscarPorId(alunoId)).thenReturn(Optional.of(aluno));
        when(alunoRepository.salvar(any(Aluno.class))).thenReturn(aluno);
        
        // Act
        Aluno resultado = useCase.executar(alunoId, mediaFinal);
        
        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.isConcluiu()).isFalse(); // Reprovado
        assertThat(resultado.getMediaFinal()).isEqualTo(mediaFinal);
        
        // IMPORTANTE: Evento deve ser publicado mesmo para reprovação
        ArgumentCaptor<AlunoConcluidoEvent> eventCaptor = ArgumentCaptor.forClass(AlunoConcluidoEvent.class);
        verify(eventPublisher, times(1)).publicarAlunoConcluido(eventCaptor.capture());
        
        AlunoConcluidoEvent evento = eventCaptor.getValue();
        assertThat(evento.isAprovado()).isFalse();
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando aluno não existe e NÃO publicar evento")
    void deveLancarExcecaoQuandoAlunoNaoExiste() {
        // Arrange
        Long alunoIdInexistente = 999L;
        double mediaFinal = 7.0;
        
        when(alunoRepository.buscarPorId(alunoIdInexistente)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> useCase.executar(alunoIdInexistente, mediaFinal))
            .isInstanceOf(AlunoNotFoundException.class);
        
        // IMPORTANTE: Evento NÃO deve ser publicado
        verify(eventPublisher, never()).publicarAlunoConcluido(any(AlunoConcluidoEvent.class));
    }
    
    @Test
    @DisplayName("Deve verificar que evento contém todos os dados necessários")
    void deveVerificarQueMúltiplosConsumidoresPodemProcessarEvento() {
        // Arrange
        Long alunoId = 3L;
        double mediaFinal = 9.5;
        
        Aluno aluno = Aluno.criar(
            NomeAluno.criar("Carla Mendes"),
            RegistroAcademico.criar("RA333333")
        );
        
        when(alunoRepository.buscarPorId(alunoId)).thenReturn(Optional.of(aluno));
        when(alunoRepository.salvar(any(Aluno.class))).thenReturn(aluno);
        
        // Act
        useCase.executar(alunoId, mediaFinal);
        
        // Assert
        ArgumentCaptor<AlunoConcluidoEvent> eventCaptor = ArgumentCaptor.forClass(AlunoConcluidoEvent.class);
        verify(eventPublisher, times(1)).publicarAlunoConcluido(eventCaptor.capture());
        
        AlunoConcluidoEvent evento = eventCaptor.getValue();
        
        // Verifica que o evento tem todos os dados para múltiplos consumers:
        // - CertificadoService precisa: nome, RA, média, aprovação
        // - EmailService precisa: nome, RA
        // - GamificacaoService precisa: alunoId, média
        // - AnalyticsService precisa: todos os dados
        assertThat(evento.getAlunoId()).isNotNull();
        assertThat(evento.getNome()).isNotBlank();
        assertThat(evento.getRegistroAcademico()).isNotBlank();
        assertThat(evento.getMediaFinal()).isPositive();
        assertThat(evento.getDataConclusao()).isNotNull();
        assertThat(evento.getEventId()).isNotNull(); // Para rastreamento
    }
}
