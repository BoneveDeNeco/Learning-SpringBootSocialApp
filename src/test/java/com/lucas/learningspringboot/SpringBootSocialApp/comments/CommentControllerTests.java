package com.lucas.learningspringboot.SpringBootSocialApp.comments;

import static org.assertj.core.api.Assertions.*;
import static com.lucas.learningspringboot.SpringBootSocialApp.AssertionUtils.*;
import static org.mockito.Mockito.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ReactiveHttpOutputMessage;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;

@RunWith(SpringRunner.class)
@WebFluxTest(CommentController.class)
@Import({ThymeleafAutoConfiguration.class})
public class CommentControllerTests {
	
	private static final String COMMENTS_MAPPING = "/comments";
	
	@Autowired
	WebTestClient webTestClient;
	
	@MockBean
	RabbitTemplate rabbitTemplate;
	
	@Test
	public void handlesPostRequesToAddComment() {
		webTestClient.post().uri(COMMENTS_MAPPING).exchange()
			.expectBody().consumeWith(response -> assertHandlerExists(response));
	}
	
	@Test
	public void addCommentHandlerRedirectsToIndex() {
		webTestClient.post().uri(COMMENTS_MAPPING).exchange()
			.expectStatus().isSeeOther()
			.expectHeader().valueEquals(HttpHeaders.LOCATION, "/");
	}
	
	@Test
	public void addCommentHandlersPublishesCommentWithRabbitTemplate() {
		Comment comment = new Comment("1", "1", "A comment");
		MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
		formData.add("id", "1");
		formData.add("imageId", "1");
		formData.add("comment", "A comment");

		webTestClient.post().uri(COMMENTS_MAPPING)
			.body(BodyInserters.fromFormData(formData)).exchange();
		
		verify(rabbitTemplate).convertAndSend("learning-spring-boot", "comments.new", comment);
	}
}
