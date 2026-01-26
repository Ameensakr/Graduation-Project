package com.example.jwt_demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class JwtDemoApplication {
	public static void main(String[] args) {
		SpringApplication.run(JwtDemoApplication.class, args); // Run Tomcat Server
		System.out.println("Server started on port 8083");
	}
}
