package com.lucas.learningspringboot.SpringBootSocialApp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;

import org.springframework.stereotype.Component;

@Component
public class FileSystemWrapper {
	
	public boolean deleteIfExists(Path path) {
		try {
			return Files.deleteIfExists(path);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public Path getPath(String first, String... more) {
		return Paths.get(first, more);
	}
	
	public Path createDirectory(Path dir, FileAttribute<?>... attrs) {
		try {
			return Files.createDirectories(dir, attrs);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
