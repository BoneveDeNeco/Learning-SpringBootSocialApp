package com.lucas.learningspringboot.SpringBootSocialApp;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.FileUrlResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.FileSystemUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.jimfs.Jimfs;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ImageServiceTests {
	
	private static final String DIFFERENT_FILENAME = "filename";
	private static final String A_FILENAME = "bazinga.png";
	
	@Autowired
	ImageService imageService;
	
	@MockBean
	ResourceLoader resourceLoader;
	
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
		when(resourceLoader.getResource("file:" + ImageService.UPLOAD_ROOT + "/" + A_FILENAME))
			.thenReturn(new FileUrlResource(uploadRootPath.resolve(A_FILENAME).toUri().toURL()));
		Mono<Resource> image = imageService.findImage(A_FILENAME);
		
		byte[] output = new byte[100];
		image.block().getInputStream().read(output);
		String fileContents = String.valueOf(output);
		assertThat(fileContents, is("Test File 3"));
		//assertThat(image.block().getFilename(), is(A_FILENAME));
	}
	
	@Test
	public void createsImages() {
		FilePart file = mock(FilePart.class);
		when(file.filename()).thenReturn(DIFFERENT_FILENAME);
		when(file.transferTo(any())).thenReturn(Mono.empty());
		Flux<FilePart> files = Flux.just(file);
		
		Path mockRootPath = mock(Path.class);
		Path resolvedMockPath = mock(Path.class);
		when(mockRootPath.resolve(anyString())).thenReturn(resolvedMockPath);
		when(resolvedMockPath.toFile()).thenReturn(mock(File.class));
		imageService.setUploadRootPath(mockRootPath);
		
		Mono<Void> mono = imageService.createImage(files);
		mono.subscribe();
		
		verify(file).transferTo(any());
	}
	
	@Test
	public void deletesImages() {
		Mono<Void> handle = imageService.deleteImage(A_FILENAME);
		
		handle.subscribe();
		
		assertThat(Files.exists(uploadRootPath.resolve(A_FILENAME)), is(false));
	}
}
