package com.ewp.crm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CrmApplication {

	public static void main(String[] args) {
		SpringApplication.run(CrmApplication.class, args);
	}

//	@Bean(initMethod = "init")
//	public DataInitializer initTestData() {
//		return new DataInitializer();
//	}
}
