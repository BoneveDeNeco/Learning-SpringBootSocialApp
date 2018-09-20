package com.lucas.learningspringboot.SpringBootSocialApp.comments;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;

import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;

public class CommentServiceTests {
	private static final Comment A_COMMENT = new Comment("1", "Image1", "A comment");
	
	@Test
	public void savesCommentsToCommentRepository() {
		Mono<Comment> monoComment = mock(Mono.class);
		CommentWriterRepository repository = mock(CommentWriterRepository.class);
		when(repository.save(A_COMMENT)).thenReturn(monoComment);
		CommentService service = new CommentService(repository);
		
		service.save(A_COMMENT);
		
		verify(repository).save(A_COMMENT);
		verify(monoComment).subscribe((CoreSubscriber<Comment>) any());
	}
}
