package com.lucas.learningspringboot.SpringBootSocialApp.config;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.springframework.beans.factory.ObjectFactory;

public class ChromeDriverFactory implements ObjectFactory<ChromeDriver> {
	
	private WebDriverConfigurationProperties properties;
	
	ChromeDriverFactory(WebDriverConfigurationProperties properties) {
		this.properties = properties;
	}

	@Override
	public ChromeDriver getObject() {
		if (properties.getChrome().isEnabled()) {
			try {
				return new ChromeDriver();
			} catch (WebDriverException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

}