package com.edutrack.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan; // 추가

@SpringBootApplication
@EntityScan(basePackages = "com.edutrack.domain") // 추가
public class EdutrackApplication {

	public static void main(String[] args) {
		SpringApplication.run(EdutrackApplication.class, args);
    System.out.println("hello, world! Welcome EduTrack API!!");
    System.out.println("create develop branch");
    System.out.println("test PR");
	}
}
