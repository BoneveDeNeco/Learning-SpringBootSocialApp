package com.lucas.learningspringboot.SpringBootSocialApp;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.reactivestreams.Subscription;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import com.lucas.learningspringboot.SpringBootSocialApp.comments.Comment;
import com.lucas.learningspringboot.SpringBootSocialApp.comments.CommentService;

import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;

@Configuration
@Profile("test")
public class TestConfiguration {
	/*
	 * This mock emulates what happens with the Flux passed to save when configuring the listener. A FluxAutoConnect is
	 * passed. I'm not sure about what goes on behind the scenes, but this flux is then used to push messages to the listener
	 */
	@Bean
	@Primary
	public CommentService commentService() {
		CommentService commentService = Mockito.mock(CommentService.class);
		Mockito.when(commentService.save(Mockito.any(Flux.class))).thenAnswer(new Answer<Flux<Void>>() {
			@Override
			public Flux<Void> answer(InvocationOnMock invocation) throws Throwable {
				Flux<Comment> fluxAutoConnect = invocation.getArgument(0);
				fluxAutoConnect.subscribe(new CoreSubscriber() {

					@Override
					public void onNext(Object t) {
						throw new RuntimeException("onNext");
					}

					@Override
					public void onError(Throwable t) {
						throw new RuntimeException("onError");
					}

					@Override
					public void onComplete() {
						throw new RuntimeException("onComplete");
					}

					@Override
					public void onSubscribe(Subscription s) {
						s.request(Integer.MAX_VALUE);
					}
				});
				
				return Flux.empty();
			}
			
		});
		return commentService;
	}
}
