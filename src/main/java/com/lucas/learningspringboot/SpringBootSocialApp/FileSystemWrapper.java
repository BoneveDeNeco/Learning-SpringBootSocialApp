package com.lucas.learningspringboot.SpringBootSocialApp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.stereotype.Component;

@Component
public class FileSystemWrapper {
	
	public boolean deleteIfExists(Path path) throws IOException {
			return Files.deleteIfExists(path);
	}
}
