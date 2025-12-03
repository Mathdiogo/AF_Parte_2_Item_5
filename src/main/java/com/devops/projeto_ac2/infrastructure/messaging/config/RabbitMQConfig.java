package com.devops.projeto_ac2.infrastructure.messaging.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração do RabbitMQ seguindo arquitetura Event-Driven
 * 
 * Conceitos:
 * - Exchange: roteador de mensagens (tipo Topic permite roteamento por padrões)
 * - Queue: fila onde mensagens ficam armazenadas
 * - Binding: ligação entre Exchange e Queue com routing key
 * 
 * Routing Keys:
 * - aluno.criado -> eventos de criação de aluno
 * - aluno.concluido -> eventos de conclusão de curso
 * - aluno.tentativa -> eventos de tentativas de avaliação
 */
@Configuration
public class RabbitMQConfig {
    
    // Nome da Exchange (Topic Exchange permite roteamento flexível)
    public static final String EXCHANGE_NAME = "aluno.events.exchange";
    
    // Nomes das Queues (Filas)
    public static final String QUEUE_ALUNO_CRIADO = "aluno.criado.queue";
    public static final String QUEUE_ALUNO_CONCLUIDO = "aluno.concluido.queue";
    public static final String QUEUE_TENTATIVA_REGISTRADA = "aluno.tentativa.queue";
    
    // Routing Keys
    public static final String ROUTING_KEY_ALUNO_CRIADO = "aluno.criado";
    public static final String ROUTING_KEY_ALUNO_CONCLUIDO = "aluno.concluido";
    public static final String ROUTING_KEY_TENTATIVA = "aluno.tentativa";
    
    /**
     * Declara a Exchange do tipo Topic
     * Topic permite roteamento baseado em padrões (ex: aluno.*)
     */
    @Bean
    public TopicExchange alunoExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }
    
    /**
     * Fila para eventos de aluno criado
     * Durable = true: fila persiste após restart do RabbitMQ
     */
    @Bean
    public Queue alunoCriadoQueue() {
        return new Queue(QUEUE_ALUNO_CRIADO, true);
    }
    
    /**
     * Fila para eventos de conclusão de curso
     */
    @Bean
    public Queue alunoConcluidoQueue() {
        return new Queue(QUEUE_ALUNO_CONCLUIDO, true);
    }
    
    /**
     * Fila para eventos de tentativa registrada
     */
    @Bean
    public Queue tentativaRegistradaQueue() {
        return new Queue(QUEUE_TENTATIVA_REGISTRADA, true);
    }
    
    /**
     * Binding: conecta a fila alunoCriado com a exchange usando routing key
     */
    @Bean
    public Binding bindingAlunoCriado(Queue alunoCriadoQueue, TopicExchange alunoExchange) {
        return BindingBuilder
                .bind(alunoCriadoQueue)
                .to(alunoExchange)
                .with(ROUTING_KEY_ALUNO_CRIADO);
    }
    
    /**
     * Binding: conecta a fila alunoConcluido com a exchange
     */
    @Bean
    public Binding bindingAlunoConcluido(Queue alunoConcluidoQueue, TopicExchange alunoExchange) {
        return BindingBuilder
                .bind(alunoConcluidoQueue)
                .to(alunoExchange)
                .with(ROUTING_KEY_ALUNO_CONCLUIDO);
    }
    
    /**
     * Binding: conecta a fila tentativaRegistrada com a exchange
     */
    @Bean
    public Binding bindingTentativaRegistrada(Queue tentativaRegistradaQueue, TopicExchange alunoExchange) {
        return BindingBuilder
                .bind(tentativaRegistradaQueue)
                .to(alunoExchange)
                .with(ROUTING_KEY_TENTATIVA);
    }
    
    /**
     * Conversor de mensagens: converte objetos Java para JSON
     * Necessário para serializar/deserializar eventos
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    
    /**
     * Template para enviar mensagens para o RabbitMQ
     * RabbitTemplate é thread-safe e pode ser injetado em qualquer lugar
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, 
                                          MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}
