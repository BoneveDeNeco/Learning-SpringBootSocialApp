package com.lucas.learningspringboot.SpringBootSocialApp.comments;

import org.springframework.data.annotation.Id;

import lombok.Data;

@Data
public class Comment {
	@Id private String id;
	private String imageId;
	private String comment;
	
	public Comment(String id, String imageId, String comment) {
		this.id = id;
		this.imageId = imageId;
		this.comment = comment;
	}
}
