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
 * Testes unitários para ConcluirCursoUseCase (com EventPublisher mockado)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do Use Case Concluir Curso")
class ConcluirCursoUseCaseTest {
    
    @Mock
    private AlunoRepository alunoRepository;
    
    @Mock
    private EventPublisher eventPublisher;
    
    @InjectMocks
    private ConcluirCursoUseCase useCase;
    
    @Test
    @DisplayName("Deve concluir curso com sucesso e adicionar cursos extras quando aprovado")
    void deveConcluirCursoComSucessoQuandoAprovado() {
        // Arrange
        Long alunoId = 1L;
        double mediaFinal = 8.5;
        
        Aluno aluno = criarAlunoTeste(alunoId);
        
        when(alunoRepository.buscarPorId(alunoId)).thenReturn(Optional.of(aluno));
        when(alunoRepository.salvar(any(Aluno.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        Aluno resultado = useCase.executar(alunoId, mediaFinal);
        
        // Assert
        assertThat(resultado.isConcluiu()).isTrue();
        assertThat(resultado.getMediaFinal()).isEqualTo(8.5);
        assertThat(resultado.getCursosAdicionais()).isEqualTo(3);
        assertThat(resultado.getDataConclusao()).isNotNull();
        
        verify(alunoRepository, times(1)).buscarPorId(alunoId);
        verify(alunoRepository, times(1)).salvar(aluno);
    }
    
    @Test
    @DisplayName("Deve concluir curso sem adicionar cursos extras quando reprovado")
    void deveConcluirCursoSemCursosExtrasQuandoReprovado() {
        // Arrange
        Long alunoId = 1L;
        double mediaFinal = 4.0;
        
        Aluno aluno = criarAlunoTeste(alunoId);
        
        when(alunoRepository.buscarPorId(alunoId)).thenReturn(Optional.of(aluno));
        when(alunoRepository.salvar(any(Aluno.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        Aluno resultado = useCase.executar(alunoId, mediaFinal);
        
        // Assert
        assertThat(resultado.isConcluiu()).isTrue();
        assertThat(resultado.getMediaFinal()).isEqualTo(4.0);
        assertThat(resultado.getCursosAdicionais()).isEqualTo(0);
        
        verify(alunoRepository, times(1)).buscarPorId(alunoId);
        verify(alunoRepository, times(1)).salvar(aluno);
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando aluno não existe")
    void deveLancarExcecaoQuandoAlunoNaoExiste() {
        // Arrange
        Long alunoId = 999L;
        double mediaFinal = 8.0;
        
        when(alunoRepository.buscarPorId(alunoId)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> useCase.executar(alunoId, mediaFinal))
                .isInstanceOf(AlunoNotFoundException.class)
                .hasMessage("Aluno não encontrado com ID: " + alunoId);
        
        verify(alunoRepository, times(1)).buscarPorId(alunoId);
        verify(alunoRepository, never()).salvar(any());
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando média é inválida")
    void deveLancarExcecaoQuandoMediaInvalida() {
        // Arrange
        Long alunoId = 1L;
        double mediaInvalida = -1.0;
        
        Aluno aluno = criarAlunoTeste(alunoId);
        
        when(alunoRepository.buscarPorId(alunoId)).thenReturn(Optional.of(aluno));
        
        // Act & Assert
        assertThatThrownBy(() -> useCase.executar(alunoId, mediaInvalida))
                .isInstanceOf(Exception.class);
        
        verify(alunoRepository, never()).salvar(any());
    }
    
    private Aluno criarAlunoTeste(Long id) {
        NomeAluno nome = NomeAluno.criar("João Silva");
        RegistroAcademico ra = RegistroAcademico.criar("12345");
        return Aluno.criar(nome, ra);
    }
}
