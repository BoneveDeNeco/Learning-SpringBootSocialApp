package com.lucas.learningspringboot.SpringBootSocialApp.repositories;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.lucas.learningspringboot.SpringBootSocialApp.Image;

import reactor.core.publisher.Mono;

public interface ImageRepository extends ReactiveCrudRepository<Image, String> {
	
	Mono<Image> findByName(String name);
}
