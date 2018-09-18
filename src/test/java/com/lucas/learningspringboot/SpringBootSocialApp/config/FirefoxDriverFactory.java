package com.lucas.learningspringboot.SpringBootSocialApp.config;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.springframework.beans.factory.ObjectFactory;

public class FirefoxDriverFactory implements ObjectFactory<FirefoxDriver> {
	
	private WebDriverConfigurationProperties properties;
	
	FirefoxDriverFactory(WebDriverConfigurationProperties properties) {
		this.properties = properties;
	}

	@Override
	public FirefoxDriver getObject() {
		if (properties.getFirefox().isEnabled()) {
			try {
				return new FirefoxDriver();
			} catch (WebDriverException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

}
