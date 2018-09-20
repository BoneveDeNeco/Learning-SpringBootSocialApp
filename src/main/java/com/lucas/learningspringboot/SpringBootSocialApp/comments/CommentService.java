package com.lucas.learningspringboot.SpringBootSocialApp.comments;

import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Service
public class CommentService {
	
	CommentWriterRepository repository;
	
	public CommentService(CommentWriterRepository repository) {
		this.repository = repository;
	}
	
	@RabbitListener(id = "save", bindings = @QueueBinding(
		value = @Queue,
		exchange = @Exchange(value = "learning-spring-boot"),
		key = "comments.new"
	))
	public void save(Comment comment) {
		repository.save(comment)
		.subscribe();
	}
	
	@Bean
	Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
		return new Jackson2JsonMessageConverter();
	}
}
