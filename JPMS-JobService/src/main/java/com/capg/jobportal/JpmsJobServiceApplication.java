package com.capg.jobportal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class JpmsJobServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(JpmsJobServiceApplication.class, args);
		System.out.println("JPMS-JobService is running on port 8082...");
	}

}
