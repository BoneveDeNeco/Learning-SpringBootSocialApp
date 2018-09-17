package com.lucas.learningspringboot.SpringBootSocialApp.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.annotation.ObjectIdGenerators.UUIDGenerator;
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
		this.fileSystemWrapper.createDirectory(fileSystemWrapper.getPath(UPLOAD_ROOT));
	}
	
	public Flux<Image> findAllImages() {
		return imageRepository.findAll().log("FindAll");
	}
	
	public Mono<Resource> findImage(String filename) {
		return Mono.fromSupplier(() -> 
				resourceLoader.getResource("file:" + UPLOAD_ROOT + "/" + filename));
	}
	
	public Mono<Void> createImage(Flux<FilePart> files) {
		return files.flatMap(file -> {
			Mono<Image> saveImageToDatabase = imageRepository.save(
					new Image(UUID.randomUUID().toString(), file.filename()))
					.log("createImage-save");
			
			Mono<Void> copyFile = Mono.just(fileSystemWrapper.getPath(UPLOAD_ROOT)
					.resolve(file.filename()).toFile())
				.log("createImage-picktarget")
				.map(destFile -> {
					try {
						destFile.createNewFile();
						return destFile;
					} catch (IOException e) {
						throw new RuntimeException(destFile.getAbsolutePath(), e);
					}
				})
				.log("createImage-newfile")
				.flatMap(file::transferTo)
				.log("createImage-copy");
			
			return Mono.when(saveImageToDatabase, copyFile);
		})
		.then();
	}
	
	public Mono<Void> deleteImage(String filename) {
		Mono<Void> deleteDatabaseRecord = imageRepository
				.findByName(filename)
				.flatMap(imageRepository::delete);
		
		Mono<Void> deleteFile = Mono.fromRunnable(() -> {
				fileSystemWrapper.deleteIfExists(fileSystemWrapper.getPath(UPLOAD_ROOT)
						.resolve(filename));
		});
		
		return Mono.when(deleteDatabaseRecord, deleteFile).then();
	}
}
