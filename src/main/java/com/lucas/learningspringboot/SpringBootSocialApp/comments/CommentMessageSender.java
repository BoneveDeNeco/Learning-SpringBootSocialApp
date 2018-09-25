package com.lucas.learningspringboot.SpringBootSocialApp.comments;

import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.cloud.stream.reactive.FluxSender;
import org.springframework.cloud.stream.reactive.StreamEmitter;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

@Component
@EnableBinding(Source.class)
public class CommentMessageSender {
	
	private FluxSink<Message<Comment>> commentSink;
	private Flux<Message<Comment>> commentFlux;
	
	public CommentMessageSender() {
		this.commentFlux = Flux.<Message<Comment>>create(emitter -> this.commentSink = emitter,
				FluxSink.OverflowStrategy.IGNORE)
			.publish()
			.autoConnect();
	}
	
	public FluxSink<Message<Comment>> send(Comment comment) {
		return commentSink.next(MessageBuilder.withPayload(comment)
				.build());
	}
	
	@StreamEmitter
	public void emit(@Output(Source.OUTPUT) FluxSender output) {
		output.send(commentFlux);
	}
}
