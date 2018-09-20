package com.lucas.learningspringboot.SpringBootSocialApp.comments;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.lucas.learningspringboot.SpringBootSocialApp.images.Comment;

import reactor.core.publisher.Mono;

@Controller
public class CommentController {
	
	private final RabbitTemplate rabbitTemplate;
	
	@Autowired
	public CommentController(RabbitTemplate rabbitTemplate) {
		this.rabbitTemplate = rabbitTemplate;
	}

	@PostMapping("/comments")
	public Mono<String> addComment(Mono<Comment> newComment) {
		return newComment.flatMap(comment ->
			Mono.fromRunnable(() -> rabbitTemplate
					.convertAndSend("learning-spring-boot", "comments.new", comment))
				.log("Comment: " + comment))
			.log("commentController-publish")
			.then(Mono.just("redirect:/"));
	}
}
