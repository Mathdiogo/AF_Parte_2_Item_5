package com.devops.projeto_ac2.application.usecases;

import com.devops.projeto_ac2.domain.entities.Aluno;
import com.devops.projeto_ac2.domain.events.TentativaRegistradaEvent;
import com.devops.projeto_ac2.domain.exceptions.AlunoNotFoundException;
import com.devops.projeto_ac2.domain.ports.EventPublisher;
import com.devops.projeto_ac2.domain.repositories.AlunoRepository;
import com.devops.projeto_ac2.domain.valueobjects.MediaFinal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case: Registrar tentativa de avaliação do aluno
 * Implementa regra de limite de 3 tentativas
 * ATUALIZADO: Agora publica eventos para arquitetura de microserviços
 */
@Service
public class RegistrarTentativaUseCase {
    
    private static final Logger logger = LoggerFactory.getLogger(RegistrarTentativaUseCase.class);
    
    private final AlunoRepository alunoRepository;
    private final EventPublisher eventPublisher;
    
    public RegistrarTentativaUseCase(AlunoRepository alunoRepository, EventPublisher eventPublisher) {
        this.alunoRepository = alunoRepository;
        this.eventPublisher = eventPublisher;
    }
    
    /**
     * Executa o registro de uma tentativa de avaliação
     * 
     * @param alunoId ID do aluno
     * @param nota Nota obtida na tentativa
     * @return O aluno atualizado
     * @throws AlunoNotFoundException se o aluno não existir
     */
    @Transactional
    public Aluno executar(Long alunoId, double nota) {
        logger.info("Registrando tentativa - AlunoID: {}, Nota: {}", alunoId, nota);
        
        // Buscar aluno
        Aluno aluno = alunoRepository.buscarPorId(alunoId)
                .orElseThrow(() -> new AlunoNotFoundException(alunoId));
        
        // Criar Value Object da média
        MediaFinal media = MediaFinal.criar(nota);
        
        // Registrar tentativa (validações são feitas na entidade)
        aluno.registrarTentativa(media);
        
        // Persistir mudanças
        Aluno alunoAtualizado = alunoRepository.salvar(aluno);
        logger.info("Tentativa registrada - AlunoID: {}, Total tentativas: {}", 
                   alunoAtualizado.getId(), alunoAtualizado.getTentativasAvaliacao());
        
        // MICROSERVIÇOS: Publicar evento para analytics e monitoramento
        // Este evento pode ser consumido por:
        // - Serviço de Analytics (métricas de tentativas)
        // - Serviço de Alertas (avisar se atingiu limite)
        // - Serviço de BI (dashboard de desempenho)
        TentativaRegistradaEvent event = new TentativaRegistradaEvent(
            alunoAtualizado.getId(),
            alunoAtualizado.getRegistroAcademico().getValor(),
            alunoAtualizado.getTentativasAvaliacao()
        );
        
        eventPublisher.publicarTentativaRegistrada(event);
        logger.info("Evento TentativaRegistrada publicado - AlunoID: {}, EventID: {}", 
                   alunoAtualizado.getId(), event.getEventId());
        
        return alunoAtualizado;
    }
}
