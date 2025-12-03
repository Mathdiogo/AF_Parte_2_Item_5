package com.devops.projeto_ac2.application.usecases;

import com.devops.projeto_ac2.domain.entities.Aluno;
import com.devops.projeto_ac2.domain.events.AlunoCriadoEvent;
import com.devops.projeto_ac2.domain.exceptions.DomainException;
import com.devops.projeto_ac2.domain.ports.EventPublisher;
import com.devops.projeto_ac2.domain.repositories.AlunoRepository;
import com.devops.projeto_ac2.domain.valueobjects.NomeAluno;
import com.devops.projeto_ac2.domain.valueobjects.RegistroAcademico;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case: Criar um novo aluno
 * Seguindo Clean Architecture, encapsula toda a lógica de criação
 * ATUALIZADO: Agora publica eventos para arquitetura de microserviços
 */
@Service
public class CriarAlunoUseCase {
    
    private static final Logger logger = LoggerFactory.getLogger(CriarAlunoUseCase.class);
    
    private final AlunoRepository alunoRepository;
    private final EventPublisher eventPublisher;
    
    public CriarAlunoUseCase(AlunoRepository alunoRepository, EventPublisher eventPublisher) {
        this.alunoRepository = alunoRepository;
        this.eventPublisher = eventPublisher;
    }
    
    /**
     * Executa o caso de uso de criar um aluno
     * 
     * @param nome Nome do aluno
     * @param ra RA do aluno
     * @return O aluno criado
     * @throws DomainException se o RA já existir ou dados forem inválidos
     */
    @Transactional
    public Aluno executar(String nome, String ra) {
        logger.info("Iniciando criação de aluno - Nome: {}, RA: {}", nome, ra);
        
        // Validar se RA já existe
        if (alunoRepository.existePorRA(ra)) {
            throw new DomainException("Já existe um aluno cadastrado com o RA: " + ra);
        }
        
        // Criar Value Objects (validações são feitas nos VOs)
        NomeAluno nomeVO = NomeAluno.criar(nome);
        RegistroAcademico raVO = RegistroAcademico.criar(ra);
        
        // Criar entidade usando factory method
        Aluno aluno = Aluno.criar(nomeVO, raVO);
        
        // Persistir
        Aluno alunoSalvo = alunoRepository.salvar(aluno);
        logger.info("Aluno criado com sucesso - ID: {}", alunoSalvo.getId());
        
        // MICROSERVIÇOS: Publicar evento para outros sistemas consumirem
        // Este evento pode ser consumido por:
        // - Serviço de Email (enviar boas-vindas)
        // - Serviço de Analytics (registrar métrica)
        // - Serviço de Gamificação (criar perfil inicial)
        AlunoCriadoEvent event = new AlunoCriadoEvent(
            alunoSalvo.getId(),
            alunoSalvo.getNome(),
            alunoSalvo.getRegistroAcademico().getValor()
        );
        
        eventPublisher.publicarAlunoCriado(event);
        logger.info("Evento AlunoCriado publicado - AlunoID: {}, EventID: {}", 
                   alunoSalvo.getId(), event.getEventId());
        
        return alunoSalvo;
    }
}
