package com.eripy.banking_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling 
public class BankingSystemApplication {
	public static void main(String[] args) {
		SpringApplication.run(BankingSystemApplication.class, args);
	}

}
