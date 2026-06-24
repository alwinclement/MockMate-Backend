package com.example.MockMate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MockMateApplication {

	public static void main(String[] args) {
		SpringApplication.run(MockMateApplication.class, args);
	}

}
