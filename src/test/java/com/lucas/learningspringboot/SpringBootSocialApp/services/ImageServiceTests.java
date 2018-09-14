package com.lucas.learningspringboot.SpringBootSocialApp.services;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.FileUrlResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import com.google.common.collect.ImmutableList;
import com.google.common.jimfs.Jimfs;
import com.lucas.learningspringboot.SpringBootSocialApp.Image;
import com.lucas.learningspringboot.SpringBootSocialApp.services.ImageService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ImageServiceTests {
	
	private static final String DIFFERENT_FILENAME = "filename";
	private static final String A_FILENAME = "bazinga.png";
	
	@MockBean
	ResourceLoader resourceLoader;
	
	@Autowired
	ImageService imageService;
	
	Path uploadRootPath;
	
	@Before
	public void setup() throws IOException {
		FileSystem filesystem = Jimfs.newFileSystem();
		uploadRootPath = filesystem.getPath(ImageService.UPLOAD_ROOT);
		imageService.setUploadRootPath(uploadRootPath);
		
		Files.createDirectory(uploadRootPath);
		
		Files.write(uploadRootPath.resolve("learning-spring-boot-cover.jpg"), ImmutableList.of("Test File"), 
				StandardCharsets.UTF_8);
		
		Files.write(uploadRootPath.resolve("learning-spring-boot-2nd-edition-cover.jpg"), ImmutableList.of("Test File 2"), 
				StandardCharsets.UTF_8);
		
		Files.write(uploadRootPath.resolve(A_FILENAME), ImmutableList.of("Test File 3"), 
				StandardCharsets.UTF_8);
	}
	
	@Test
	public void findsAllImages() {
		Flux<Image> images = imageService.findAllImages();
		
		assertThat(images.collectList().block().size(), is(3));
	}
	
	@Test
	public void fetchesSingleImage() throws IOException {
		//For some reason, the autowired imageService does not get the mocked ResourceLoader injected
		imageService.setResourceLoader(resourceLoader);
		
		when(resourceLoader.getResource(anyString()))
			.thenReturn(new FileUrlResource(uploadRootPath.resolve(A_FILENAME).toUri().toURL()));
		Mono<Resource> image = imageService.findImage(A_FILENAME);
		
		byte[] output = new byte[100];
		image.block().getInputStream().read(output);
		String fileContents = new String(output);
		assertThat(fileContents, containsString("Test File 3"));
	}
	
	@Test
	public void createsImages() {
		//I have to use mocks here because toFile() in Jimfs is unsupported
		FilePart file = mock(FilePart.class);
		when(file.filename()).thenReturn(DIFFERENT_FILENAME);
		when(file.transferTo(any())).thenReturn(Mono.empty());
		Flux<FilePart> files = Flux.just(file);
		
		Path mockRootPath = mock(Path.class);
		Path resolvedMockPath = mock(Path.class);
		when(mockRootPath.resolve(anyString())).thenReturn(resolvedMockPath);
		File mockFile = mock(File.class);
		when(resolvedMockPath.toFile()).thenReturn(mockFile);
		imageService.setUploadRootPath(mockRootPath);
		
		Mono<Void> mono = imageService.createImage(files);
		mono.subscribe();
		
		ArgumentCaptor<File> fileArgument = ArgumentCaptor.forClass(File.class);
		verify(file).transferTo(fileArgument.capture());
		assertThat(fileArgument.getValue(), is(mockFile));
	}
	
	@Test
	public void deletesImages() {
		Mono<Void> handle = imageService.deleteImage(A_FILENAME);
		
		handle.subscribe();
		
		assertThat(Files.exists(uploadRootPath.resolve(A_FILENAME)), is(false));
	}
}
