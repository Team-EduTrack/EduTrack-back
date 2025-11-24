package com.edutrack.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"com.edutrack", "com.edutrack.global.security"})
@EnableJpaRepositories(basePackages = "com.edutrack.domain")
@EntityScan(basePackages = "com.edutrack.domain")
public class EdutrackApplication {

	public static void main(String[] args) {
		SpringApplication.run(EdutrackApplication.class, args);
    System.out.println("hello, world! Welcome EduTrack API!!");
    System.out.println("create develop branch");
    System.out.println("test PR");
	}
}
