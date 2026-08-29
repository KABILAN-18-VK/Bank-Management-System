package com.bank.banking_management_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.bank")
@EnableJpaRepositories("com.bank.repository")
@EntityScan("com.bank.entity")
public class BankingManagementSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(BankingManagementSystemApplication.class, args);
	}

}
