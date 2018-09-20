package com.lucas.learningspringboot.SpringBootSocialApp;

import static org.assertj.core.api.Assertions.*;
import static com.lucas.learningspringboot.SpringBootSocialApp.AssertionUtils.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;

import com.lucas.learningspringboot.SpringBootSocialApp.HomeController;
import com.lucas.learningspringboot.SpringBootSocialApp.images.Comment;
import com.lucas.learningspringboot.SpringBootSocialApp.images.CommentReaderRepository;
import com.lucas.learningspringboot.SpringBootSocialApp.images.Image;
import com.lucas.learningspringboot.SpringBootSocialApp.images.ImageService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RunWith(SpringRunner.class)
@WebFluxTest(controllers = HomeController.class)
@Import({ThymeleafAutoConfiguration.class})
public class HomeControllerTests {

	private static final Comment A_COMMENT = new Comment("c1", "1", "A commentary");
	private static final Flux<Comment> A_COMMENT_FLUX = Flux.just(A_COMMENT);
	private static final Image AN_IMAGE = new Image("1", "Image 1");
	private static final Flux<Image> AN_IMAGE_FLUX = Flux.just(AN_IMAGE);
	private static final String FILE_NAME = "image.jpg";
	private static final String DELETE_IMAGE_PATH = HomeController.BASE_PATH + "/" + FILE_NAME;
	private static final String FILE_CONTENTS = "Test File";
	private static final String GET_IMAGE_PATH = DELETE_IMAGE_PATH+ "/raw";
	private static final String ROOT_LOCATION = "/";

	@Autowired
	WebTestClient webTestClient;

	@MockBean
	ImageService imageService;
	
	@MockBean
	CommentReaderRepository commentReaderRepository;
	
	HomeController controller;
	Model model;
	
	@Before
	public void setup() throws IOException {
		when(imageService.findImage(FILE_NAME)).thenReturn(Mono.just(new ByteArrayResource(FILE_CONTENTS.getBytes())));
		when(imageService.createImage(any())).thenReturn(Mono.empty());
		when(imageService.deleteImage(anyString())).thenReturn(Mono.empty());
		
		controller = new HomeController(imageService, commentReaderRepository);
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
		webTestClient.get().uri(GET_IMAGE_PATH).exchange()
			.expectHeader().contentLength(FILE_CONTENTS.length());
	}
	
	@Test
	public void getImageHandlerPutsImageInResponseBody() {
		webTestClient.get().uri(GET_IMAGE_PATH).exchange()
			.expectBody(String.class).isEqualTo(FILE_CONTENTS);
	}
	
	@Test
	public void getImageHandlerFailsForBadFile() throws IOException {
		Resource imageResource = mock(Resource.class);
		when(imageResource.getInputStream()).thenThrow(new IOException("Not found."));
		when(imageService.findImage(FILE_NAME)).thenReturn(Mono.just(imageResource));
		
		webTestClient.get().uri(GET_IMAGE_PATH).exchange()
			.expectStatus().isBadRequest();
	}
	
	@Test
	public void handlesRequestForCreatingImageFiles() {
		webTestClient.post().uri(HomeController.BASE_PATH).exchange()
			.expectBody().consumeWith(response -> assertHandlerExists(response));
	}
	
	@Test
	public void createImageHandlerRedirectsToHomePage() {
		webTestClient.post().uri(HomeController.BASE_PATH).exchange()
		.expectStatus().isSeeOther()
		.expectHeader().valueEquals(HttpHeaders.LOCATION, ROOT_LOCATION);
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
		assertThat(fileContents).isEqualTo(FILE_CONTENTS);
	}
	
	@Test
	public void handlesRequestForDeletingAnImage() {
		webTestClient.delete().uri(DELETE_IMAGE_PATH).exchange()
			.expectBody().consumeWith(response -> assertHandlerExists(response));
	}
	
	@Test
	public void deleteImageHandlerRedirectsToHome() {
		webTestClient.delete().uri(DELETE_IMAGE_PATH).exchange()
			.expectStatus().isSeeOther()
			.expectHeader().valueEquals(HttpHeaders.LOCATION, ROOT_LOCATION);
	}
	
	@Test
	public void deleteImageHandlerDeletesImage() {
		webTestClient.delete().uri(DELETE_IMAGE_PATH).exchange();
		
		verify(imageService).deleteImage(FILE_NAME);
	}
	
	@Test
	public void handlesRequestForGettingIndex() {
		setupIndexMocks();
		
		webTestClient.get().uri(ROOT_LOCATION).exchange()
			.expectBody().consumeWith(response -> assertHandlerExists(response));
	}
	
	@Test
	public void indexHandlerAnswersWithIndexPage() {
		setupIndexMocks();
		
		Mono<String> pageToRender = controller.index(mock(Model.class));
		
		assertThat(pageToRender.block()).isEqualTo("index");
	}
	
	@Test
	public void indexHandlerAddsImagesWithCommentsToPageModel() {
		setupIndexMocks();
		
		model = new ExtendedModelMap();
		
		controller.index(model);
		Flux<HashMap<String, Object>> imageFlux = (Flux<HashMap<String, Object>>) model.asMap().get("images");
		HashMap<String, Object> imageAttributes = (HashMap<String, Object>) imageFlux.blockFirst();
		assertThat(imageAttributes).contains(
				entry("id", "1"),
				entry("name", "Image 1"),
				entry("comments", Arrays.asList(A_COMMENT)));
	}
	
	private void setupIndexMocks() {
		when(imageService.findAllImages()).thenReturn(AN_IMAGE_FLUX);
		when(commentReaderRepository.findByImageId("1")).thenReturn(A_COMMENT_FLUX);
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
