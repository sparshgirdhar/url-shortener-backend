package com.example.urlshortener;

import org.springframework.boot.SpringApplication;

public class TestUrlShortnerApplication {

	public static void main(String[] args) {
		SpringApplication.from(UrlShortnerApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
