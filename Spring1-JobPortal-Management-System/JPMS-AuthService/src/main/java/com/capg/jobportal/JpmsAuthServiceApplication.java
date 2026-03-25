package com.capg.jobportal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class JpmsAuthServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(JpmsAuthServiceApplication.class, args);
		System.out.println("JPMS-AuthService is running on port 8081...");
	}

}
