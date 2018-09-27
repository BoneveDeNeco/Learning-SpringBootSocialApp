package com.lucas.learningspringboot.SpringBootSocialApp.comments;

import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import io.micrometer.core.instrument.MeterRegistry;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class CommentService {
	
	CommentWriterRepository repository;
	MeterRegistry meterRegistry;
	
	public CommentService(CommentWriterRepository repository, MeterRegistry meterRegistry) {
		this.repository = repository;
		this.meterRegistry = meterRegistry;
	}
	
	public Flux<Void> save(Flux<Comment> comments) {
		return repository.saveAll(comments)
		.log("commentService-save")
		.flatMap(savedComment -> {
			meterRegistry.counter("comments.consumed", "imageId", savedComment.getImageId())
				.increment();
			return Mono.empty();
		});
	}
}
