package com.lucas.learningspringboot.SpringBootSocialApp;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import reactor.core.publisher.Mono;

@Controller
public class HomeController {
	protected static final String BASE_PATH = "/images";
	private static final String FILENAME = "{filename:.+}";
	
	private final ImageService imageService;
	
	@Autowired
	public HomeController(ImageService imageService) {
		this.imageService = imageService;
	}
	
	@GetMapping(value = BASE_PATH + "/" + FILENAME + "/raw",
			produces = MediaType.IMAGE_JPEG_VALUE)
	@ResponseBody
	public Mono<ResponseEntity<?>> oneRawImage(
			@PathVariable String filename) {
		return imageService.findImage(filename)
				.map(resource -> {
					try {
						return ResponseEntity.ok()
								.contentLength(resource.contentLength())
								.body(new InputStreamResource(
										resource.getInputStream()));
					} catch (IOException e) {
						return ResponseEntity.badRequest()
							.body("Couldn't find " + filename + 
									" => " + e.getStackTrace());
					}
				});
	}
	
	@PostMapping(value=BASE_PATH)
	public Mono<String> createFile() {
		return Mono.just("redirect:/");
	}
}
