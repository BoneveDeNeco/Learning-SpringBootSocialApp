package com.lucas.learningspringboot.SpringBootSocialApp.images;

import org.springframework.data.annotation.Id;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Comment {
	@Id private String id;
	private String imageId;
	private String comment;
}
