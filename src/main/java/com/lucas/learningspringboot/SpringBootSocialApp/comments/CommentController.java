package com.lucas.learningspringboot.SpringBootSocialApp.comments;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.cloud.stream.reactive.FluxSender;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

import io.micrometer.core.instrument.MeterRegistry;
import reactor.core.publisher.Mono;

@Controller
@EnableBinding(Source.class)
public class CommentController {
	
	private final RabbitTemplate rabbitTemplate;
	private final MeterRegistry meterRegistry;
	private final CommentMessageSender sender;
	
	@Autowired
	public CommentController(RabbitTemplate rabbitTemplate, MeterRegistry meterRegistry, CommentMessageSender sender) {
		this.rabbitTemplate = rabbitTemplate;
		this.meterRegistry = meterRegistry;
		this.sender = sender;
	}

	@PostMapping("/comments")
	public Mono<String> addComment(Mono<Comment> newComment) {
		return newComment.map(comment -> {
				sender.send(comment);
				return comment;
			})
			.log("commentController-publish:")
			.flatMap(comment -> {
				meterRegistry.counter("comments.produced", "imageId", comment.getImageId()).increment();
				return Mono.just("redirect:/");
			});
	}
}
