package com.lucas.learningspringboot.SpringBootSocialApp;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import static org.mockito.Mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

@Ignore
//@RunWith(SpringRunner.class)
//@SpringBootTest
//@AutoConfigureWebTestClient
public class AbstractSpringIntegrationTest {
	
	@Autowired
	protected WebTestClient webTestClient;
	
	@MockBean
	protected FileSystemWrapper fileSystemWrapper;
	
	protected static ResourceLoader resourceLoader = mock(ResourceLoader.class);
	
	//For some reason, @MockBean is not working for ResourceLoader
	@Configuration
	public static class MockResourceLoaderConfig {
		@Bean 
		@Primary
		ResourceLoader resourceLoader() {
			return resourceLoader;
		}
	}
	
	//Emulating Spring's mock reset with @MockBean
	@After
	public void resetContainerMocks() { //Do not use the same name in child classes, will override this one
		reset(resourceLoader);
	}
}
