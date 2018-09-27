package com.lucas.learningspringboot.SpringBootSocialApp.comments;



import java.util.concurrent.BlockingQueue;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;


import reactor.core.publisher.Flux;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class CommentMessageReceiverTests {
	
	@Autowired
	CustomProcessor channels;
	
	@Autowired
	MessageCollector collector;
	
	@Autowired
	CommentService commentService;
	
	@Test
	public void receivesCommentMessagesAndSaveInDatabase() {
		Comment comment = new Comment("1", "1", "A comment");
		SubscribableChannel input = channels.input();
		
		input.send(new GenericMessage<>(comment));
		
		Mockito.verify(commentService).save(Mockito.any(Flux.class));
	}

}
