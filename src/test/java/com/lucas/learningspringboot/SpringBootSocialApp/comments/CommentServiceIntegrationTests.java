package com.lucas.learningspringboot.SpringBootSocialApp.comments;

import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.PublisherCallbackChannel.Listener;
import org.springframework.amqp.rabbit.test.RabbitListenerTest;
import org.springframework.amqp.rabbit.test.RabbitListenerTestHarness;
import org.springframework.amqp.rabbit.test.TestRabbitTemplate;
import org.springframework.amqp.rabbit.test.mockito.LatchCountDownAndCallRealMethodAnswer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

import com.lucas.learningspringboot.SpringBootSocialApp.SpringBootSocialAppApplication;
import com.rabbitmq.client.Channel;

import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;

@Ignore //Couldn't make it work
//@RunWith(SpringRunner.class)
//@SpringBootTest(classes = {SpringBootSocialAppApplication.class, CommentServiceIntegrationTests.RabbitMqTestConfig.class})
public class CommentServiceIntegrationTests {
	
	private static final Comment A_COMMENT = new Comment("1", "Image1", "A comment");
	
	@Autowired
	RabbitListenerTestHarness harness;
	
	@Autowired
	RabbitTemplate rabbitTemplate;
	
	@MockBean
	CommentWriterRepository repository/* = mock(CommentWriterRepository.class)*/;
	
	@Test
	public void listensForMessagesForSavingComment() throws IOException {
		Mono<Comment> monoComment = mock(Mono.class);
		when(repository.save(A_COMMENT)).thenReturn(monoComment);
		
		CommentService service = harness.getSpy("save");
		LatchCountDownAndCallRealMethodAnswer answer = new LatchCountDownAndCallRealMethodAnswer(2);
		doAnswer(answer).when(service).save(any());
		
		rabbitTemplate.convertAndSend("learning-spring-boot", "comments.new", A_COMMENT);
		
		verify(service).save(any());
	}
	
	
	@Configuration
	@RabbitListenerTest
	public static class RabbitMqTestConfig {
		@Autowired
		CommentService service;
	}

}
/*
Try with TestRabbitTemplate didn't work out as expected, it is too limited
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {SpringBootSocialAppApplication.class, CommentServiceTests.TestRabbitConfiguration.class})
public class CommentServiceTests {
	
	@Autowired
	TestRabbitTemplate template;
	
	@MockBean
	CommentWriterRepository repository;
	
	@Test
	//@Ignore
	/**
	 * Works if annotation is 
	 * @RabbitListener(queues = "comments.new")
	 * 
	 * But not for
	 * @RabbitListener(bindings = @QueueBinding(
		value = @Queue,
		exchange = @Exchange(value = "learning-spring-boot"),
		key = "comments.new"
	))
	 * @throws IOException
	 */
/*
	public void listensForMessagesForSavingComment() throws IOException {
		setupRepositoryMock();
		
		template.convertAndSend("learning-spring-boot", "comments.new", A_COMMENT);
		
		verify(repository).save(A_COMMENT);
	}
	
	private void setupRepositoryMock() {
		when(repository.save(A_COMMENT)).thenReturn(monoComment);
	}
	
	@Configuration
	@EnableRabbit
	public static class TestRabbitConfiguration {
		
		@Bean
		public ConnectionFactory connectionFactory() {
			ConnectionFactory factory = mock(ConnectionFactory.class);
			Connection connection = mock(Connection.class);
			Channel channel = mock(Channel.class);
			when(factory.createConnection()).thenReturn(connection);
			when(connection.createChannel(anyBoolean())).thenReturn(channel);
			when(channel.isOpen()).thenReturn(true);
			return factory;
		}
		
		@Bean
		public TestRabbitTemplate template() {
			return new TestRabbitTemplate(connectionFactory());
		}
		
		@Bean
		public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory() {
			SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
			factory.setConnectionFactory(connectionFactory());
			return factory;
		}
	}
}
*/
