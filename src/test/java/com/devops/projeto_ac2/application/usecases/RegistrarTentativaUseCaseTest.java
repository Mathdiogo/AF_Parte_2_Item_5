package com.devops.projeto_ac2.application.usecases;

import com.devops.projeto_ac2.domain.entities.Aluno;
import com.devops.projeto_ac2.domain.exceptions.AlunoNotFoundException;
import com.devops.projeto_ac2.domain.ports.EventPublisher;
import com.devops.projeto_ac2.domain.repositories.AlunoRepository;
import com.devops.projeto_ac2.domain.valueobjects.NomeAluno;
import com.devops.projeto_ac2.domain.valueobjects.RegistroAcademico;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para RegistrarTentativaUseCase (com EventPublisher mockado)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do Use Case Registrar Tentativa")
class RegistrarTentativaUseCaseTest {
    
    @Mock
    private AlunoRepository alunoRepository;
    
    @Mock
    private EventPublisher eventPublisher;
    
    @InjectMocks
    private RegistrarTentativaUseCase useCase;
    
    @Test
    @DisplayName("Deve registrar tentativa com sucesso")
    void deveRegistrarTentativaComSucesso() {
        // Arrange
        Long alunoId = 1L;
        double nota = 7.5;
        
        Aluno aluno = criarAlunoTeste(alunoId);
        
        when(alunoRepository.buscarPorId(alunoId)).thenReturn(Optional.of(aluno));
        when(alunoRepository.salvar(any(Aluno.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        Aluno resultado = useCase.executar(alunoId, nota);
        
        // Assert
        assertThat(resultado.getTentativasAvaliacao()).isEqualTo(1);
        assertThat(resultado.getMediaFinal()).isEqualTo(7.5);
        
        verify(alunoRepository, times(1)).buscarPorId(alunoId);
        verify(alunoRepository, times(1)).salvar(aluno);
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando aluno não existe")
    void deveLancarExcecaoQuandoAlunoNaoExiste() {
        // Arrange
        Long alunoId = 999L;
        double nota = 7.5;
        
        when(alunoRepository.buscarPorId(alunoId)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> useCase.executar(alunoId, nota))
                .isInstanceOf(AlunoNotFoundException.class);
        
        verify(alunoRepository, times(1)).buscarPorId(alunoId);
        verify(alunoRepository, never()).salvar(any());
    }
    
    @Test
    @DisplayName("Deve permitir múltiplas tentativas até o limite")
    void devePermitirMultiplasTentativas() {
        // Arrange
        Long alunoId = 1L;
        Aluno aluno = criarAlunoTeste(alunoId);
        
        when(alunoRepository.buscarPorId(alunoId)).thenReturn(Optional.of(aluno));
        when(alunoRepository.salvar(any(Aluno.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act - registrar 3 tentativas
        useCase.executar(alunoId, 5.0);
        useCase.executar(alunoId, 6.0);
        useCase.executar(alunoId, 7.0);
        
        // Assert
        assertThat(aluno.getTentativasAvaliacao()).isEqualTo(3);
        assertThat(aluno.getMediaFinal()).isEqualTo(7.0);
        verify(alunoRepository, times(3)).salvar(aluno);
    }
    
    private Aluno criarAlunoTeste(Long id) {
        return Aluno.criar(
                NomeAluno.criar("João Silva"), 
                RegistroAcademico.criar("12345")
        );
    }
}
