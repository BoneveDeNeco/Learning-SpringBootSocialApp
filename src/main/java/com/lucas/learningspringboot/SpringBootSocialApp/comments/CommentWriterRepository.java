package com.lucas.learningspringboot.SpringBootSocialApp.comments;

import org.springframework.data.repository.Repository;

import reactor.core.publisher.Mono;

public interface CommentWriterRepository extends Repository<Comment, String> {
	Mono<Comment> save(Comment comment);
	
	//Needed by save() operation
	Mono<Comment> findById(String id);
}
