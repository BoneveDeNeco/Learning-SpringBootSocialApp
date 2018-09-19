package com.lucas.learningspringboot.SpringBootSocialApp;

import java.io.IOException;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lucas.learningspringboot.SpringBootSocialApp.images.CommentReaderRepository;
import com.lucas.learningspringboot.SpringBootSocialApp.images.ImageService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
public class HomeController {
	protected static final String BASE_PATH = "/images";
	private static final String FILENAME = "{filename:.+}";
	
	private final ImageService imageService;
	private final CommentReaderRepository commentReaderRepository;
	
	@Autowired
	public HomeController(ImageService imageService, CommentReaderRepository commentReaderRepository) {
		this.imageService = imageService;
		this.commentReaderRepository = commentReaderRepository;
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
	public Mono<String> createFile(@RequestPart(name = "file") Flux<FilePart> files) {
		return imageService.createImage(files)
				.then(Mono.just("redirect:/"));
	}
	
	@DeleteMapping(value=BASE_PATH + "/" + FILENAME)
	public Mono<String> deleteFile(@PathVariable String filename) {
		return imageService.deleteImage(filename)
			.then(Mono.just("redirect:/"));
	}
	
	@GetMapping(value="/")
	public Mono<String> index(Model model) {
		model.addAttribute("images", 
				imageService.findAllImages()
				.flatMap(image -> 
					Mono.just(image)
						.zipWith(commentReaderRepository.findByImageId(image.getId())
								.collectList()))
				.map(imageAndComments -> {
					HashMap<String, Object> map = new HashMap<>();
					map.put("id", imageAndComments.getT1().getId());
					map.put("name", imageAndComments.getT1().getName());
					map.put("comments", imageAndComments.getT2());
					return map;
				}));
		return Mono.just("index");
	}
}
