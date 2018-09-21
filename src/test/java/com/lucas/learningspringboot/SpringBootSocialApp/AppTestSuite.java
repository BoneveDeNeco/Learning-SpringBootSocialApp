package com.lucas.learningspringboot.SpringBootSocialApp;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.googlecode.junittoolbox.ParallelSuite;
import com.lucas.learningspringboot.SpringBootSocialApp.comments.CommentControllerTests;
import com.lucas.learningspringboot.SpringBootSocialApp.comments.CommentServiceTests;
import com.lucas.learningspringboot.SpringBootSocialApp.config.WebDriverAutoConfigurationTests;
import com.lucas.learningspringboot.SpringBootSocialApp.images.EmbededImageRepositoryTests;
import com.lucas.learningspringboot.SpringBootSocialApp.images.ImageServiceTests;

@RunWith(Suite.class)
@SuiteClasses({HomeControllerTests.class,
		CommentControllerTests.class,
		CommentServiceTests.class,
		//WebDriverAutoConfigurationTests.class,
		EmbededImageRepositoryTests.class,
		ImageServiceTests.class
})
public class AppTestSuite {

}
