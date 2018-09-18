package com.lucas.learningspringboot.SpringBootSocialApp.config;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.After;
import org.junit.Test;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.springframework.boot.test.util.EnvironmentTestUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ClassUtils;

public class WebDriverAutoConfigurationTests {
	private AnnotationConfigApplicationContext context;
	
	@After
	public void close() {
		if (this.context != null) {
			this.context.close();
		}
	}
	
	private void load(Class<?>[] configs, String... environment) {
		AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
		applicationContext.register(WebDriverAutoConfiguration.class);
		if (configs.length > 0) {
			applicationContext.register(configs);
		}
		
		EnvironmentTestUtils.addEnvironment(applicationContext, environment);
		applicationContext.refresh();
		this.context = applicationContext;
	}
	
	@Configuration
	protected static class MockFirefoxConfiguration {
		@Bean
		FirefoxDriverFactory firefoxDriverFactory() {
			FirefoxDriverFactory factory = mock(FirefoxDriverFactory.class);
			when(factory.getObject()).thenReturn(mock(FirefoxDriver.class));
			return factory;
		}
	}
	
	@Configuration
	protected static class MockChromeConfiguration {
		@Bean
		ChromeDriverFactory chromeDriverFactory() {
			ChromeDriverFactory factory = mock(ChromeDriverFactory.class);
			when(factory.getObject()).thenReturn(mock(ChromeDriver.class));
			return factory;
		}
	}
	
	@Configuration
	protected static class MockSafariConfiguration {
		@Bean
		SafariDriverFactory safariDriverFactory() {
			SafariDriverFactory factory = mock(SafariDriverFactory.class);
			when(factory.getObject()).thenReturn(mock(SafariDriver.class));
			return factory;
		}
	}
	
	@Test
	public void fallbackToNonGuiModeWhenAllBrowsersDisabled() {
		load(new Class[]{}, 
			"com.lucas.webdriver.firefox.enabled:false",
			"com.lucas.webdriver.chrome.enabled:false",
			"com.lucas.webdriver.safari.enabled:false");
		
		WebDriver driver = context.getBean(WebDriver.class);
		assertThat(ClassUtils.isAssignable(TakesScreenshot.class, driver.getClass())).isFalse();
		assertThat(ClassUtils.isAssignable(HtmlUnitDriver.class, driver.getClass())).isTrue();
	}
	
	@Test
	public void testWithMockedFirefox() {
		load(new Class[]{MockFirefoxConfiguration.class}, 
				"com.lucas.webdriver.chrome.enabled:false",
				"com.lucas.webdriver.safari.enabled:false");
		WebDriver driver = context.getBean(WebDriver.class);
		assertThat(ClassUtils.isAssignable(TakesScreenshot.class, driver.getClass())).isTrue();
		assertThat(ClassUtils.isAssignable(FirefoxDriver.class, driver.getClass())).isTrue();
	}
	
	@Test
	public void testWithMockedChrome() {
		load(new Class[]{MockChromeConfiguration.class}, 
				"com.lucas.webdriver.firefox.enabled:false",
				"com.lucas.webdriver.safari.enabled:false");
		WebDriver driver = context.getBean(WebDriver.class);
		assertThat(ClassUtils.isAssignable(TakesScreenshot.class, driver.getClass())).isTrue();
		assertThat(ClassUtils.isAssignable(ChromeDriver.class, driver.getClass())).isTrue();
	}
	
	@Test
	public void testWithMockedSafari() {
		load(new Class[]{MockSafariConfiguration.class}, 
				"com.lucas.webdriver.firefox.enabled:false",
				"com.lucas.webdriver.chrome.enabled:false");
		WebDriver driver = context.getBean(WebDriver.class);
		assertThat(ClassUtils.isAssignable(TakesScreenshot.class, driver.getClass())).isTrue();
		assertThat(ClassUtils.isAssignable(SafariDriver.class, driver.getClass())).isTrue();
	}
}
