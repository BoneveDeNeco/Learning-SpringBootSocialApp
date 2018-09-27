package com.lucas.learningspringboot.SpringBootSocialApp.comments;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;

public interface CustomProcessor {
	
	public static final String INPUT = "input";
	public static final String OUTPUT = "emptyOutput";
	
	@Input(CustomProcessor.INPUT)
	SubscribableChannel input();
	
	@Output(CustomProcessor.OUTPUT)
	MessageChannel output();
	
}
