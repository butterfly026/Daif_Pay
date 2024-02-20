package com.daifubackend.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class DaifuApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(DaifuApiApplication.class, args);
	}

}
