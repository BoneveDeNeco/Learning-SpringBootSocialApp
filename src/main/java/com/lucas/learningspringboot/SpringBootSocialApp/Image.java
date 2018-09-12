package com.lucas.learningspringboot.SpringBootSocialApp;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class Image {
	
	private String id;
	private String name;
	
	public Image(String id, String name) {
		this.id = id;
		this.name = name;
	}
}
