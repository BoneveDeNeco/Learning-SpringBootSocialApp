package com.lucas.learningspringboot.SpringBootSocialApp.comments;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

import org.junit.Before;
import org.junit.Test;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import reactor.core.publisher.Mono;

public class CommentServiceTests {
	private static final Comment A_COMMENT = new Comment("1", "Image1", "A comment");
	
	Mono<Comment> monoComment;
	CommentWriterRepository repository;
	CommentService service;
	MeterRegistry meterRegistry;
	@Before
	public void setup() {
		monoComment = Mono.just(A_COMMENT);
		repository = mock(CommentWriterRepository.class);
		when(repository.save(A_COMMENT)).thenReturn(monoComment);
		
		meterRegistry = new SimpleMeterRegistry();
		service = new CommentService(repository, meterRegistry);
	}
	
	@Test
	public void savesCommentsToCommentRepository() {
		service.save(A_COMMENT);
		
		verify(repository).save(A_COMMENT);
	}
	
	@Test
	public void keepsTrackOfNumberOfCommentsConsumed() {
		service.save(A_COMMENT);
		
		assertThat(meterRegistry.counter("comments.consumed", "imageId", A_COMMENT.getImageId()).count())
			.isEqualTo(1.0);
		//meterRegistry.close();
	}
}
