package com.lucas.learningspringboot.SpringBootSocialApp.comments;

import static org.assertj.core.api.Assertions.*;
import static com.lucas.learningspringboot.SpringBootSocialApp.AssertionUtils.*;
import static org.mockito.Mockito.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.export.simple.SimpleMetricsExportAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;

import com.lucas.learningspringboot.SpringBootSocialApp.images.Comment;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import reactor.core.publisher.Mono;

@RunWith(SpringRunner.class)
@WebFluxTest(CommentController.class)
@ImportAutoConfiguration({ThymeleafAutoConfiguration.class, 
	MetricsAutoConfiguration.class, SimpleMetricsExportAutoConfiguration.class //MeterRegistry
})
public class CommentControllerTests {
	
	private static final Comment A_COMMENT = new Comment("1", "1", "A comment");

	private static final String COMMENTS_MAPPING = "/comments";
	
	@Autowired
	WebTestClient webTestClient;
	
	@MockBean
	RabbitTemplate rabbitTemplate;
	
	@Test
	public void handlesPostRequesToAddComment() {
		postComment(A_COMMENT)
			.expectBody().consumeWith(response -> assertHandlerExists(response));
	}
	
	@Test
	public void addCommentHandlerRedirectsToIndex() {
		postComment(A_COMMENT)
			.expectStatus().isSeeOther()
			.expectHeader().valueEquals(HttpHeaders.LOCATION, "/");
	}
	
	@Test
	public void addCommentHandlersPublishesCommentWithRabbitTemplate() {
		postComment(A_COMMENT);
		
		verify(rabbitTemplate).convertAndSend("learning-spring-boot", "comments.new", A_COMMENT);
	}
	
	@Test
	public void keepsTrackOfNumberOfCommentsProduced() {
		//MeterRegistry persists count throughout the tests, making the count uncertain. This test needs a brand new MeterRegistry
		MeterRegistry meterRegistry = new SimpleMeterRegistry();
		CommentController controller = new CommentController(rabbitTemplate, meterRegistry);
		
		controller.addComment(Mono.just(A_COMMENT)).subscribe();
		
		assertThat(meterRegistry.counter("comments.produced", "imageId", A_COMMENT.getImageId())
				.count()).isEqualTo(1.0);
	}
	
	private ResponseSpec postComment(Comment comment) {
		MultiValueMap<String, String> formData = getCommentFormData(comment);
		
		return webTestClient.post().uri(COMMENTS_MAPPING)
			.body(BodyInserters.fromFormData(formData)).exchange();
	}
	
	private MultiValueMap<String, String> getCommentFormData(Comment comment) {
		MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
		formData.add("id", comment.getId());
		formData.add("imageId", comment.getImageId());
		formData.add("comment", comment.getComment());
		return formData;
	}
}
