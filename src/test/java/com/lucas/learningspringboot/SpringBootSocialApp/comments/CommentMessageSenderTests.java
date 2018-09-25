package com.lucas.learningspringboot.SpringBootSocialApp.comments;

import static org.assertj.core.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.util.concurrent.BlockingQueue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.messaging.Message;
import org.springframework.messaging.converter.CompositeMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CommentMessageSenderTests {

	@Autowired
	private Source channels;
	
	@Autowired
	private MessageCollector collector;
	
	@Autowired
	private CommentMessageSender sender;
	
	@Autowired
	private CompositeMessageConverter converter;
	
	@Test
	public void sendsCommentMessage() throws IOException, ClassNotFoundException {
		Comment comment = new Comment("1", "1", "A comment");
		
		sender.send(comment);
		
		BlockingQueue<Message<?>> messages = collector.forChannel(channels.output());
		Message<?> message = messages.poll();
		Comment sentComment = (Comment) converter.fromMessage(message, Comment.class);
		assertThat(sentComment).isEqualTo(comment);
	}
}
