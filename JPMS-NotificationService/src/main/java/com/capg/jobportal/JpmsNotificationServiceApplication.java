package com.capg.jobportal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients      
public class JpmsNotificationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(JpmsNotificationServiceApplication.class, args);
		System.out.println("JPMS Notification Service is running on port 8085...");
	}

}
