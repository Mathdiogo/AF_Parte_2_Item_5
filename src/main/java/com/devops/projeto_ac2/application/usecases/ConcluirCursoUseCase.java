package com.devops.projeto_ac2.application.usecases;

import com.devops.projeto_ac2.domain.entities.Aluno;
import com.devops.projeto_ac2.domain.events.AlunoConcluidoEvent;
import com.devops.projeto_ac2.domain.exceptions.AlunoNotFoundException;
import com.devops.projeto_ac2.domain.ports.EventPublisher;
import com.devops.projeto_ac2.domain.repositories.AlunoRepository;
import com.devops.projeto_ac2.domain.valueobjects.MediaFinal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case: Concluir curso de um aluno
 * Aplica regras de negócio relacionadas à conclusão
 * ATUALIZADO: Agora publica eventos para arquitetura de microserviços
 */
@Service
public class ConcluirCursoUseCase {
    
    private static final Logger logger = LoggerFactory.getLogger(ConcluirCursoUseCase.class);
    
    private final AlunoRepository alunoRepository;
    private final EventPublisher eventPublisher;
    
    public ConcluirCursoUseCase(AlunoRepository alunoRepository, EventPublisher eventPublisher) {
        this.alunoRepository = alunoRepository;
        this.eventPublisher = eventPublisher;
    }
    
    /**
     * Executa o caso de uso de concluir o curso
     * 
     * @param alunoId ID do aluno
     * @param mediaFinal Média final obtida
     * @return O aluno atualizado
     * @throws AlunoNotFoundException se o aluno não existir
     */
    @Transactional
    public Aluno executar(Long alunoId, double mediaFinal) {
        logger.info("Iniciando conclusão de curso - AlunoID: {}, Média: {}", alunoId, mediaFinal);
        
        // Buscar aluno
        Aluno aluno = alunoRepository.buscarPorId(alunoId)
                .orElseThrow(() -> new AlunoNotFoundException(alunoId));
        
        // Criar Value Object da média (validação é feita no VO)
        MediaFinal mediaVO = MediaFinal.criar(mediaFinal);
        
        // Executar comportamento de negócio (na entidade)
        aluno.concluirCurso(mediaVO);
        
        // Persistir mudanças
        Aluno alunoAtualizado = alunoRepository.salvar(aluno);
        logger.info("Curso concluído com sucesso - AlunoID: {}, Aprovado: {}", 
                   alunoAtualizado.getId(), alunoAtualizado.isConcluiu());
        
        // MICROSERVIÇOS: Publicar evento para outros sistemas
        // Este evento pode ser consumido por:
        // - Serviço de Certificados (gerar certificado PDF)
        // - Serviço de Email (enviar parabéns/certificado)
        // - Serviço de Gamificação (atribuir badges/pontos)
        // - Serviço de Recomendação (sugerir próximos cursos)
        // - Serviço de Analytics (atualizar dashboards)
        AlunoConcluidoEvent event = new AlunoConcluidoEvent(
            alunoAtualizado.getId(),
            alunoAtualizado.getNome(),
            alunoAtualizado.getRegistroAcademico().getValor(),
            alunoAtualizado.getMediaFinal(),
            alunoAtualizado.isConcluiu()
        );
        
        eventPublisher.publicarAlunoConcluido(event);
        logger.info("Evento AlunoConcluido publicado - AlunoID: {}, EventID: {}", 
                   alunoAtualizado.getId(), event.getEventId());
        
        return alunoAtualizado;
    }
}
