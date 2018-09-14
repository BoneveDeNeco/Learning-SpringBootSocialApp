package com.lucas.learningspringboot.SpringBootSocialApp.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;

import com.lucas.learningspringboot.SpringBootSocialApp.Image;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ImageService {
	
	public static final String UPLOAD_ROOT = "upload-dir";
	
	private ResourceLoader resourceLoader;
	private Path uploadRootPath = Paths.get(UPLOAD_ROOT);
	
	@Autowired
	public ImageService(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}
	
	public Flux<Image> findAllImages() {
		try {
			return Flux.fromIterable(
					Files.newDirectoryStream(uploadRootPath))
				.map(path -> 
						new Image(String.valueOf(path.hashCode()), path.getFileName().toString()));
		} catch (IOException e) {
			e.printStackTrace();
			return Flux.empty();
		}
	}
	
	public Mono<Resource> findImage(String filename) {
		return Mono.fromSupplier(() -> 
				resourceLoader.getResource("file:" + UPLOAD_ROOT + "/" + filename));
	}
	
	public Mono<Void> createImage(Flux<FilePart> files) {
		return files.flatMap(file -> 
				file.transferTo(uploadRootPath.resolve(file.filename()).toFile()))
			.then();
	}
	
	public Mono<Void> deleteImage(String filename) {
		return Mono.fromRunnable(() -> {
			try {
				Files.deleteIfExists(uploadRootPath.resolve(filename));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
	}
	
	public void setUploadRootPath(Path uploadRootPath) {
		this.uploadRootPath = uploadRootPath;
	}
	
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}
	
	/*@Bean
	CommandLineRunner setUp() {
		return (args) -> {
			FileSystemUtils.deleteRecursively(new File(ImageService.UPLOAD_ROOT));
			
			Files.createDirectory(Paths.get(ImageService.UPLOAD_ROOT));
			
			FileCopyUtils.copy("Test File", 
					new FileWriter(ImageService.UPLOAD_ROOT + "/learning-spring-boot-cover.jpg"));
			
			FileCopyUtils.copy("Test File 2", 
					new FileWriter(ImageService.UPLOAD_ROOT + "/learning-spring-boot-2nd-edition-cover.jpg"));
			
			FileCopyUtils.copy("Test File 3", 
					new FileWriter(ImageService.UPLOAD_ROOT + "/bazinga.png"));
		};
	}*/
}