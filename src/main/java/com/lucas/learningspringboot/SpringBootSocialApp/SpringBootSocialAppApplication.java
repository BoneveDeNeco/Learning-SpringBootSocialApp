package com.lucas.learningspringboot.SpringBootSocialApp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.filter.reactive.HiddenHttpMethodFilter;

@SpringBootApplication
public class SpringBootSocialAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringBootSocialAppApplication.class, args);
	}
	
	//Delete is not a valid action in HTML5 form, but this makes thymeleaf do a small workaround to support it
	@Bean
	HiddenHttpMethodFilter hiddenHttpMethodFilter() {
		return new HiddenHttpMethodFilter();
	}
}
