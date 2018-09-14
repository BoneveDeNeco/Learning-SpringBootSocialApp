package com.lucas.learningspringboot.SpringBootSocialApp.controllers;

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
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.FileUrlResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;

import com.google.common.collect.ImmutableList;
import com.google.common.jimfs.Jimfs;
import com.lucas.learningspringboot.SpringBootSocialApp.Image;
import com.lucas.learningspringboot.SpringBootSocialApp.controllers.HomeController;
import com.lucas.learningspringboot.SpringBootSocialApp.services.ImageService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureWebTestClient
public class HomeControllerTests {

	private static final String FILE_NAME = "image.jpg";
	private static final String DELETE_IMAGE_PATH = HomeController.BASE_PATH + "/" + FILE_NAME;
	private static final String FILE_CONTENTS = "Test File";
	private static final String GET_IMAGE_PATH = DELETE_IMAGE_PATH+ "/raw";
	private static final String ROOT_LOCATION = "/";

	@Autowired
	WebTestClient webTestClient;

	@MockBean
	ImageService imageService;
	
	Resource imageResource;
	HomeController controller;
	Model model;
	
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
		when(imageService.createImage(any())).thenReturn(Mono.empty());
		when(imageService.deleteImage(anyString())).thenReturn(Mono.empty());
		
		controller = new HomeController(imageService);
		model = mock(Model.class);
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
	public void getImageHandlerAnswersWithContentLength() throws IOException {
		when(imageResource.contentLength()).thenReturn(10l);
		
		webTestClient.get().uri(GET_IMAGE_PATH).exchange()
			.expectHeader().contentLength(10);
	}
	
	@Test
	public void getImageHandlerPutsImageInResponseBody() {
		webTestClient.get().uri(GET_IMAGE_PATH).exchange()
			.expectBody().consumeWith(response -> assertThat(new String(response.getResponseBody()), is(FILE_CONTENTS)));;
	}
	
	@Test
	public void handlesRequestForCreatingImageFiles() {
		webTestClient.post().uri(HomeController.BASE_PATH).exchange()
			.expectBody().consumeWith(response -> assertHandlerExists(response));
	}
	
	@Test
	public void createImageHandlerRedirectsToHomePage() {
		webTestClient.post().uri(HomeController.BASE_PATH).exchange()
			.expectBody().consumeWith(response -> assertRedirectionLocation(response, ROOT_LOCATION));
	}
	
	@Test
	public void createImageHandlerCreatesNewFile() throws IOException {
		MultiValueMap<String, Object> multipartData = new LinkedMultiValueMap<>();
		multipartData.add("file", FILE_CONTENTS);
		
		webTestClient.post().uri(HomeController.BASE_PATH)
			.body(BodyInserters.fromMultipartData(multipartData))
			.exchange();
		
		ArgumentCaptor<Flux<FilePart>> filesArgCaptor = ArgumentCaptor.forClass(Flux.class);
		verify(imageService).createImage(filesArgCaptor.capture());
		byte[] buffer = new byte[100];
		((Part) filesArgCaptor.getValue().blockFirst()).content()
			.blockFirst().asInputStream().read(buffer);
		String fileContents = new String(buffer).trim();
		assertThat(fileContents, is(FILE_CONTENTS));
	}
	
	@Test
	public void handlesRequestForDeletingAnImage() {
		webTestClient.delete().uri(DELETE_IMAGE_PATH).exchange()
			.expectBody().consumeWith(response -> assertHandlerExists(response));
	}
	
	@Test
	public void deleteImageHandlerRedirectsToHome() {
		webTestClient.delete().uri(DELETE_IMAGE_PATH).exchange()
			.expectBody().consumeWith(response -> assertRedirectionLocation(response, ROOT_LOCATION));
	}
	
	@Test
	public void deleteImageHandlerDeletesImage() {
		webTestClient.delete().uri(DELETE_IMAGE_PATH).exchange();
		
		verify(imageService).deleteImage(FILE_NAME);
	}
	
	@Test
	public void handlesRequestForGettingIndex() {
		webTestClient.get().uri(ROOT_LOCATION).exchange()
			.expectBody().consumeWith(response -> assertHandlerExists(response));
	}
	
	@Test
	public void indexHandlerAnswersWithIndexPage() {
		Mono<String> pageToRender = controller.index(model);
		
		assertThat(pageToRender.block(), is("index"));
	}
	
	@Test
	public void indexHandlerAddsImagesToPageModel() {
		Flux<Image> images = Flux.just(new Image("1", "Image 1"));
		when(imageService.findAllImages()).thenReturn(images);
		
		controller.index(model);
		
		verify(model).addAttribute("images", images);
	}
	
	private void assertHandlerExists(EntityExchangeResult<byte[]> response) {
		assertThat(response.getStatus(), is(not(HttpStatus.NOT_FOUND)));
	}
	
	private void assertRedirectionLocation(EntityExchangeResult<byte[]> response, String location) {
		assertTrue("Should be Redirection status (3xx), but was " + response.getStatus(),
				response.getStatus().is3xxRedirection());
		assertThat(response.getResponseHeaders().getLocation().toString(), is(location));
	}
	
	//@Test
	//Not a good test. Tries to test too much. I'm leaving it here for example purposes
	/*public void handlesRequestForOneRawImageWithVirtualFilesystem() throws IOException {
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
	}*/
}
