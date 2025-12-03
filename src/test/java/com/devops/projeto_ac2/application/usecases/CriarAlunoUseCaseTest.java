package com.devops.projeto_ac2.application.usecases;

import com.devops.projeto_ac2.domain.entities.Aluno;
import com.devops.projeto_ac2.domain.exceptions.DomainException;
import com.devops.projeto_ac2.domain.ports.EventPublisher;
import com.devops.projeto_ac2.domain.repositories.AlunoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para CriarAlunoUseCase
 * Usando Mockito para mockar dependências (incluindo EventPublisher)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do Use Case Criar Aluno")
class CriarAlunoUseCaseTest {
    
    @Mock
    private AlunoRepository alunoRepository;
    
    @Mock
    private EventPublisher eventPublisher;
    
    @InjectMocks
    private CriarAlunoUseCase useCase;
    
    @BeforeEach
    void setUp() {
        // Configuração comum para todos os testes
    }
    
    @Test
    @DisplayName("Deve criar aluno com sucesso quando dados são válidos")
    void deveCriarAlunoComSucesso() {
        // Arrange
        String nome = "João Silva";
        String ra = "12345ABC";
        
        when(alunoRepository.existePorRA(ra)).thenReturn(false);
        when(alunoRepository.salvar(any(Aluno.class))).thenAnswer(invocation -> {
            Aluno aluno = invocation.getArgument(0);
            return aluno;
        });
        
        // Act
        Aluno resultado = useCase.executar(nome, ra);
        
        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getNome()).isEqualTo(nome);
        assertThat(resultado.getRegistroAcademico().getValor()).isEqualTo(ra);
        assertThat(resultado.isConcluiu()).isFalse();
        assertThat(resultado.getMediaFinal()).isEqualTo(0.0);
        
        verify(alunoRepository, times(1)).existePorRA(ra);
        verify(alunoRepository, times(1)).salvar(any(Aluno.class));
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando RA já existe")
    void deveLancarExcecaoQuandoRAJaExiste() {
        // Arrange
        String nome = "João Silva";
        String ra = "12345ABC";
        
        when(alunoRepository.existePorRA(ra)).thenReturn(true);
        
        // Act & Assert
        assertThatThrownBy(() -> useCase.executar(nome, ra))
                .isInstanceOf(DomainException.class)
                .hasMessage("Já existe um aluno cadastrado com o RA: " + ra);
        
        verify(alunoRepository, times(1)).existePorRA(ra);
        verify(alunoRepository, never()).salvar(any());
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando nome é inválido")
    void deveLancarExcecaoQuandoNomeInvalido() {
        // Arrange
        String nomeInvalido = "Jo"; // muito curto
        String ra = "12345ABC";
        
        when(alunoRepository.existePorRA(ra)).thenReturn(false);
        
        // Act & Assert
        assertThatThrownBy(() -> useCase.executar(nomeInvalido, ra))
                .isInstanceOf(Exception.class);
        
        verify(alunoRepository, never()).salvar(any());
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando RA é inválido")
    void deveLancarExcecaoQuandoRAInvalido() {
        // Arrange
        String nome = "João Silva";
        String raInvalido = "123"; // muito curto
        
        when(alunoRepository.existePorRA(raInvalido)).thenReturn(false);
        
        // Act & Assert
        assertThatThrownBy(() -> useCase.executar(nome, raInvalido))
                .isInstanceOf(Exception.class);
        
        verify(alunoRepository, never()).salvar(any());
    }
}
