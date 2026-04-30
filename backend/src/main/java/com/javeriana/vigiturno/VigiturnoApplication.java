package com.javeriana.vigiturno;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VigiturnoApplication {

	public static void main(String[] args) {
		SpringApplication.run(VigiturnoApplication.class, args);
	}

}
