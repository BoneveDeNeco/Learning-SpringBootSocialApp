package com.lucas.learningspringboot.SpringBootSocialApp;

import static org.mockito.Mockito.*;
import static com.lucas.learningspringboot.SpringBootSocialApp.AssertionUtils.assertHandlerExists;

import java.io.IOException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;

import com.lucas.learningspringboot.SpringBootSocialApp.images.ImageService;

import reactor.core.publisher.Mono;

@Ignore
public class HomeControllerIntegrationTests extends AbstractSpringIntegrationTest {

	private static final String FILE_NAME = "image.jpg";
	private static final String DELETE_IMAGE_PATH = HomeController.BASE_PATH + "/" + FILE_NAME;
	private static final String FILE_CONTENTS = "Test File";
	private static final String GET_IMAGE_PATH = DELETE_IMAGE_PATH+ "/raw";
	private static final String ROOT_LOCATION = "/";
	
	@Before
	public void setup() {
		when(resourceLoader.getResource("file:" + ImageService.UPLOAD_ROOT + "/" + FILE_NAME))
			.thenReturn(new ByteArrayResource(FILE_CONTENTS.getBytes()));
	}
	
	@Test
	public void handlesRequestForGettingSingleImage() {
		webTestClient.get().uri(GET_IMAGE_PATH).exchange()
			.expectBody().consumeWith(response -> assertHandlerExists(response));
	}
	

	@Test
	public void getImageHandlerAnswersWithJpegContentType() throws IOException {
		webTestClient.get().uri(GET_IMAGE_PATH).exchange()
			.expectHeader().contentType(MediaType.IMAGE_JPEG_VALUE);
	}
	
	@Test
	public void getImageHandlerPutsImageInResponseBody() {
		webTestClient.get().uri(GET_IMAGE_PATH).exchange()
			.expectBody(String.class).isEqualTo(FILE_CONTENTS);
	}
	
	@Test
	public void getImageHandlerAnswersWithContentLength() throws IOException {
		webTestClient.get().uri(GET_IMAGE_PATH).exchange()
			.expectHeader().contentLength(FILE_CONTENTS.length());
	}
	
	@Test
	public void getImageHandlerFailsForBadFile() throws IOException {
		Resource missingImageResource = mock(Resource.class);
		when(missingImageResource.getInputStream()).thenThrow(new IOException("Not found."));
		when(resourceLoader.getResource("file:" + ImageService.UPLOAD_ROOT + "/" + FILE_NAME))
			.thenReturn(missingImageResource);
		
		webTestClient.get().uri(GET_IMAGE_PATH).exchange()
			.expectStatus().isBadRequest();
	}
	
	@Test
	public void handlesRequestForCreatingImageFiles() {
		postImage()
			.expectBody().consumeWith(response -> assertHandlerExists(response));
	}
	
	@Test
	public void createImageHandlerRedirectsToHomePage() {
		postImage()
			.expectStatus().isSeeOther()
			.expectHeader().valueEquals(HttpHeaders.LOCATION, ROOT_LOCATION);
	}
	
	private ResponseSpec postImage() {
		MultiValueMap<String, Object> multipartData = new LinkedMultiValueMap<>();
		multipartData.add("file", FILE_CONTENTS);
		
		return webTestClient.post().uri(HomeController.BASE_PATH)
			.body(BodyInserters.fromMultipartData(multipartData)).exchange();
	}
}
