package com.lucas.learningspringboot.SpringBootSocialApp;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.core.io.FileUrlResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.google.common.collect.ImmutableList;
import com.google.common.jimfs.Jimfs;

import reactor.core.publisher.Mono;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureWebTestClient
public class HomeControllerTests {

	private static final String FILE_NAME = "image.jpg";
	private static final String FILE_CONTENTS = "Test File";
	private static final String GET_IMAGE_PATH = HomeController.BASE_PATH + "/"+ FILE_NAME +"/raw";

	@Autowired
	WebTestClient webTestClient;

	@MockBean
	ImageService imageService;
	
	Resource imageResource;
	
	@Before
	public void setup() throws IOException {
		InputStream mockInputStream = mock(InputStream.class);

		when(mockInputStream.read(any(), anyInt(), anyInt()))
		.thenAnswer(invocation -> {
			byte[] buffer = invocation.getArgument(0);
			byte[] fileContents = FILE_CONTENTS.getBytes();
			System.arraycopy(fileContents, 0, buffer, 0, 
					fileContents.length < buffer.length ? fileContents.length : buffer.length);
			return fileContents.length;
		})
		.thenReturn(-1); //EOF on second call

		imageResource = mock(Resource.class);
		when(imageResource.getInputStream()).thenReturn(mockInputStream);
		when(imageService.findImage(FILE_NAME)).thenReturn(Mono.just(imageResource));
	}
	
	@Test
	public void handlesRequestForGettingSingleImage() throws IOException {
		webTestClient.get().uri(GET_IMAGE_PATH).exchange()
		.expectStatus().is2xxSuccessful()
		.expectHeader().contentType(MediaType.IMAGE_JPEG_VALUE);
	}
	
	@Test
	public void getImageHandlerAnswersWithContentLength() throws IOException {
		when(imageResource.contentLength()).thenReturn(10l);
		
		webTestClient.get().uri(GET_IMAGE_PATH).exchange()
		.expectHeader().contentLength(10);
	}
	
	@Test
	public void getImageHandlerPutsImageInResponseBody() {
		webTestClient.get().uri(GET_IMAGE_PATH).exchange()
		.expectBody()
		.consumeWith(response -> assertThat(new String(response.getResponseBody()), containsString(FILE_CONTENTS)));;
	}
	
	@Test
	public void handlesRequestForCreatingImageFiles() {
		webTestClient.post().uri(HomeController.BASE_PATH).exchange();
	}
	
	@Test
	public void createFileHandlerRedirectsToHomePage() {
		webTestClient.post().uri(HomeController.BASE_PATH).exchange()
		.expectStatus().is3xxRedirection();
	}
	
	@Test
	public void createFileHandlerCreatesNewFile() {
		webTestClient.post().uri(HomeController.BASE_PATH).exchange();
	}
	
	//@Test
	//Not a good test. Tries to test too much
	public void handlesRequestForOneRawImageWithVirtualFilesystem() throws IOException {
		FileSystem filesystem = Jimfs.newFileSystem();
		Path uploadRootPath = filesystem.getPath(ImageService.UPLOAD_ROOT);

		Files.createDirectory(uploadRootPath);

		Files.write(uploadRootPath.resolve(FILE_NAME), ImmutableList.of(FILE_CONTENTS), StandardCharsets.UTF_8);

		ResourceLoader resourceLoader = mock(ResourceLoader.class);
		when(resourceLoader.getResource(anyString()))
				.thenReturn(new FileUrlResource(uploadRootPath.resolve(FILE_NAME).toUri().toURL()));

		imageService.setResourceLoader(resourceLoader);
		when(imageService.findImage(anyString())).thenCallRealMethod();

		webTestClient.get().uri(GET_IMAGE_PATH)
		.exchange()
		.expectStatus().is2xxSuccessful()
		.expectHeader().contentType(MediaType.IMAGE_JPEG_VALUE)
		.expectBody()
		.consumeWith(response -> assertThat(new String(response.getResponseBody()), containsString(FILE_CONTENTS)));
	}
}
