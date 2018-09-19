package com.lucas.learningspringboot.SpringBootSocialApp.images;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;

import com.lucas.learningspringboot.SpringBootSocialApp.Image;
import com.lucas.learningspringboot.SpringBootSocialApp.images.ImageRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class ImageRepositoryTests {
	@Autowired
	ImageRepository imageRepository;
	
	@Autowired
	MongoOperations mongoOperations;
	
	@Before
	public void setup() {
		mongoOperations.dropCollection(Image.class);
		
		mongoOperations.insert(new Image("1", "learning-spring-boot-cover.jpg"));
		mongoOperations.insert(new Image("2", "learning-spring-boot-2nd-edition-cover.jpg"));
		mongoOperations.insert(new Image("3", "bazinga.jpg"));
		
		mongoOperations.findAll(Image.class).forEach(image -> {
			System.out.println(image.toString());
		});
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void findAllShouldWork() {
		Flux<Image> images = imageRepository.findAll();
		StepVerifier.create(images)
			.recordWith(ArrayList::new)
			.expectNextCount(3)
			.consumeRecordedWith(results -> {
				assertThat(results.size(), is(3));
				assertThat(results, containsInAnyOrder(
						withName("learning-spring-boot-cover.jpg"),
						withName("learning-spring-boot-2nd-edition-cover.jpg"),
						withName("bazinga.jpg")));
			})
			.expectComplete()
			.verify();
	}
	
	@Test
	public void findByNameShouldWork() {
		Mono<Image> image = imageRepository.findByName("bazinga.jpg");
		StepVerifier.create(image)
			.expectNextMatches(results -> {
				assertThat(results.getId(), is("3"));
				return true;
			});
	}
	
	public <T> Matcher<T> withName(String name) {
		return hasProperty("name", is(name));
	}
}
