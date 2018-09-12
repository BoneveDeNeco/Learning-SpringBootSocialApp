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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
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

	@Autowired
	WebTestClient webTestClient;

	@SpyBean
	ImageService imageService;

	@Test
	public void handlesRequestForOneRawImageWithVirtualFilesystem() throws IOException {
		FileSystem filesystem = Jimfs.newFileSystem();
		Path uploadRootPath = filesystem.getPath(ImageService.UPLOAD_ROOT);

		Files.createDirectory(uploadRootPath);

		Files.write(uploadRootPath.resolve("image.jpg"), ImmutableList.of("Test File"), StandardCharsets.UTF_8);

		ResourceLoader resourceLoader = mock(ResourceLoader.class);
		when(resourceLoader.getResource(anyString()))
				.thenReturn(new FileUrlResource(uploadRootPath.resolve("image.jpg").toUri().toURL()));

		imageService.setResourceLoader(resourceLoader);

		webTestClient.get().uri(HomeController.BASE_PATH + "/image.jpg/raw").exchange()
		.expectStatus().is2xxSuccessful()
		.expectHeader().contentType(MediaType.IMAGE_JPEG_VALUE)
		.expectBody()
		.consumeWith(response -> assertThat(new String(response.getResponseBody()), containsString("Test File")));
	}

	@Test
	public void handlesRequestForOneRawImageWithMocks() throws IOException {
		InputStream mockInputStream = mock(InputStream.class);
		when(mockInputStream.read(any())).thenReturn(-1);
		when(mockInputStream.read(any(), anyInt(), anyInt())).thenReturn(-1);
		when(mockInputStream.read()).thenReturn(-1);
		Resource imageResource = mock(Resource.class);
		when(imageResource.getInputStream()).thenReturn(mockInputStream);
		when(imageService.findImage("image.jpg")).thenReturn(Mono.just(imageResource));

		webTestClient.get().uri(HomeController.BASE_PATH + "/image.jpg/raw").exchange().expectStatus().is2xxSuccessful()
				.expectHeader().contentType(MediaType.IMAGE_JPEG_VALUE);
	}
}
