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

import com.lucas.learningspringboot.SpringBootSocialApp.FileSystemWrapper;
import com.lucas.learningspringboot.SpringBootSocialApp.Image;
import com.lucas.learningspringboot.SpringBootSocialApp.repositories.ImageRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ImageService {
	
	public static final String UPLOAD_ROOT = "upload-dir";
	
	private final ResourceLoader resourceLoader;
	private final ImageRepository imageRepository;
	private final FileSystemWrapper fileSystemWrapper;
	
	@Autowired
	public ImageService(FileSystemWrapper fileSystemWrapper, ResourceLoader resourceLoader, 
			ImageRepository imageRepository) {
		this.fileSystemWrapper = fileSystemWrapper;
		this.resourceLoader = resourceLoader;
		this.imageRepository = imageRepository;
	}
	
	public Flux<Image> findAllImages() {
		return imageRepository.findAll();
	}
	
	public Mono<Resource> findImage(String filename) {
		return Mono.fromSupplier(() -> 
				resourceLoader.getResource("file:" + UPLOAD_ROOT + "/" + filename));
	}
	
	public Mono<Void> createImage(Flux<FilePart> files) {
		return files.flatMap(file -> 
				file.transferTo(fileSystemWrapper.getPath(UPLOAD_ROOT)
						.resolve(file.filename()).toFile()))
			.then();
	}
	
	public Mono<Void> deleteImage(String filename) {
		return Mono.fromRunnable(() -> {
			try {
				fileSystemWrapper.deleteIfExists(fileSystemWrapper.getPath(UPLOAD_ROOT)
						.resolve(filename));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
	}
}
