package com.socialnetwork.boardrift;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class BoardRiftApplication {

	public static void main(String[] args) {
		SpringApplication.run(BoardRiftApplication.class, args);
	}
}
