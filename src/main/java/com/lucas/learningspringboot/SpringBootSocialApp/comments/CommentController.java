package com.lucas.learningspringboot.SpringBootSocialApp.comments;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

import io.micrometer.core.instrument.MeterRegistry;
import reactor.core.publisher.Mono;

@Controller
public class CommentController {
	
	private final RabbitTemplate rabbitTemplate;
	private final MeterRegistry meterRegistry;
	
	@Autowired
	public CommentController(RabbitTemplate rabbitTemplate, MeterRegistry meterRegistry) {
		this.rabbitTemplate = rabbitTemplate;
		this.meterRegistry = meterRegistry;
	}

	@PostMapping("/comments")
	public Mono<String> addComment(Mono<Comment> newComment) {
		return newComment.flatMap(comment ->
			Mono.fromRunnable(() -> rabbitTemplate
					.convertAndSend("learning-spring-boot", "comments.new", comment))
				.log("Comment: " + comment)
				.then(Mono.just(comment)))
			.log("commentController-publish:")
			.flatMap(comment -> {
				meterRegistry.counter("comments.produced", "imageId", comment.getImageId()).increment();
				return Mono.just("redirect:/");
			});
	}
}
