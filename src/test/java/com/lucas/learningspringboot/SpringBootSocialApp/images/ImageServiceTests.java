package com.lucas.learningspringboot.SpringBootSocialApp.images;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.codec.multipart.FilePart;

import com.lucas.learningspringboot.SpringBootSocialApp.FileSystemWrapper;
import com.lucas.learningspringboot.SpringBootSocialApp.images.ImageRepository;
import com.lucas.learningspringboot.SpringBootSocialApp.images.ImageService;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ImageServiceTests {
	
	private static final String A_FILENAME = "bazinga.png";
	private static final long MOCK_FILE_SIZE = 100;
	
	ResourceLoader resourceLoader;
	ImageRepository imageRepository;
	FileSystemWrapper filesystemWrapper;
	ImageService imageService;
	
	Path uploadRootPath = Paths.get(ImageService.UPLOAD_ROOT);
	Flux<FilePart> files;
	File mockFile;
	FilePart file;
	
	MeterRegistry meterRegistry;
	
	@Before
	public void setup() throws IOException {
		resourceLoader = mock(ResourceLoader.class);
		
		imageRepository = mock(ImageRepository.class);
		when(imageRepository.save(any()))
			.thenReturn(Mono.just(new Image("1", A_FILENAME)));
		
		filesystemWrapper = mock(FileSystemWrapper.class);
		when(filesystemWrapper.getPath(ImageService.UPLOAD_ROOT)).thenReturn(uploadRootPath);
		
		meterRegistry = new SimpleMeterRegistry();
		
		imageService = new ImageService(filesystemWrapper, resourceLoader, imageRepository, meterRegistry);
	}
	
	@Test
	public void createsUploadDirectoryOnInstantiation() {
		verify(filesystemWrapper).createDirectory(uploadRootPath);
	}
	
	@Test
	public void findsAllImages() {
		when(imageRepository.findAll()).thenReturn(Flux.just(new Image("1", "1"), new Image("2", "2")));
		
		Flux<Image> images = imageService.findAllImages();
		
		assertThat(images.collectList().block().size(), is(2));
	}
	
	@Test
	public void fetchesSingleImage() throws IOException {
		Mono<Resource> image = imageService.findImage(A_FILENAME);
		
		image.block();
		
		verify(resourceLoader).getResource("file:" + ImageService.UPLOAD_ROOT + "/" + A_FILENAME);
	}
	
	@Test
	public void createsImagesAndCopiesToServer() throws IOException {
		setupMocksForFileCreation();
		
		Mono<Void> mono = imageService.createImage(files);
		mono.subscribe();
		
		verify(mockFile).createNewFile();
		verify(file).transferTo(mockFile);
	}
	
	@Test
	public void savesImageToRepository() {
		setupMocksForFileCreation();
		
		Mono<Void> handler = imageService.createImage(files);
		handler.subscribe();
		
		ArgumentCaptor<Image> imageArgument = ArgumentCaptor.forClass(Image.class);
		verify(imageRepository).save(imageArgument.capture());
		assertThat(imageArgument.getValue().getName(), is(A_FILENAME));
	}
	
	@Test
	public void deletesImagesFromFilesystem() throws IOException {
		setupMocksForFileDelete();
		
		Mono<Void> handle = imageService.deleteImage(A_FILENAME);
		handle.subscribe();
		
		verify(filesystemWrapper).deleteIfExists(uploadRootPath.resolve(A_FILENAME));
	}
	
	@Test
	public void deletesImageRecordFromDb() {
		Image image = setupMocksForFileDelete();
		
		Mono<Void> handle = imageService.deleteImage(A_FILENAME);
		handle.subscribe();
		
		verify(imageRepository).delete(image);
	}
	
	@Test
	public void countsTotalBytesSavedCreatingImages() {
		setupMocksForFileCreation();
		
		Mono<Void> handle = imageService.createImage(files);
		handle.subscribe();
		
		//assertThat(meterRegistry.get("files.uploaded.bytes").summary().takeSnapshot().total(), is((double)MOCK_FILE_SIZE));
		assertThat(meterRegistry.summary("files.uploaded.bytes").totalAmount(), is((double)MOCK_FILE_SIZE));
	}
	
	private void setupMocksForFileCreation() {
		file = mock(FilePart.class);
		when(file.filename()).thenReturn(A_FILENAME);
		when(file.transferTo(any())).thenReturn(Mono.empty());
		files = Flux.just(file);
		
		Path mockUploadRootPath = mock(Path.class);
		when(filesystemWrapper.getPath(ImageService.UPLOAD_ROOT)).thenReturn(mockUploadRootPath);
		Path resolvedMockPath = mock(Path.class);
		when(mockUploadRootPath.resolve(A_FILENAME)).thenReturn(resolvedMockPath);
		when(filesystemWrapper.getPath(ImageService.UPLOAD_ROOT, A_FILENAME)).thenReturn(resolvedMockPath);
		mockFile = mock(File.class);
		when(mockFile.length()).thenReturn(MOCK_FILE_SIZE);
		when(resolvedMockPath.toFile()).thenReturn(mockFile);
	}
	
	private Image setupMocksForFileDelete() {
		Image image = new Image("1", A_FILENAME);
		when(imageRepository.findByName(A_FILENAME))
			.thenReturn(Mono.just(image));
		when(imageRepository.delete(any())).thenReturn(Mono.empty());
		return image;
	}
}
