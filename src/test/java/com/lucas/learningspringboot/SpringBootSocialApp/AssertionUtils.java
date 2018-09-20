package com.lucas.learningspringboot.SpringBootSocialApp;

import static org.assertj.core.api.Assertions.*;

import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.EntityExchangeResult;

public class AssertionUtils {
	public static void assertHandlerExists(EntityExchangeResult<byte[]> response) {
		assertThat(response.getStatus()).isNotEqualTo(HttpStatus.NOT_FOUND);
	}
}
