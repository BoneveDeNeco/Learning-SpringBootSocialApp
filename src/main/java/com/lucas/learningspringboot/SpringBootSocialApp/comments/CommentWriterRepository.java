package com.lucas.learningspringboot.SpringBootSocialApp.comments;

import org.springframework.data.repository.Repository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CommentWriterRepository extends Repository<Comment, String> {
	Mono<Comment> save(Comment comment);
	
	Flux<Comment> saveAll(Flux<Comment> comments);
	
	//Needed by save() operation
	Mono<Comment> findById(String id);
}
