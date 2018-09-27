package com.lucas.learningspringboot.SpringBootSocialApp.comments;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class CommentServiceTests {
	private static final Comment A_COMMENT = new Comment("1", "Image1", "A comment");
	
	Flux<Comment> fluxComment;
	CommentWriterRepository repository;
	CommentService service;
	MeterRegistry meterRegistry;
	@Before
	public void setup() {
		fluxComment = Flux.just(A_COMMENT);
		repository = mock(CommentWriterRepository.class);
		when(repository.saveAll(any())).thenReturn(fluxComment);
		
		meterRegistry = new SimpleMeterRegistry();
		service = new CommentService(repository, meterRegistry);
	}
	
	@Test
	public void savesCommentsToCommentRepository() {
		Flux<Comment> comments = Flux.just(A_COMMENT);
		service.save(comments);
		
		verify(repository).saveAll(comments);
	}
	
	@Test
	public void keepsTrackOfNumberOfCommentsConsumed() {
		service.save(Flux.just(A_COMMENT)).blockFirst();
		
		assertThat(meterRegistry.counter("comments.consumed", "imageId", A_COMMENT.getImageId()).count())
			.isEqualTo(1.0);
		//meterRegistry.close();
	}
}
