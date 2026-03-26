package com.capg.jobportal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import jakarta.annotation.PostConstruct;

@SpringBootApplication
@EnableDiscoveryClient
public class JpmsAuthServiceApplication {
	
	@Value("${AUTH_SERVICE_DB_USERNAME}")
    private String user;

	public static void main(String[] args) {
		SpringApplication.run(JpmsAuthServiceApplication.class, args);
		System.out.println("JPMS-AuthService is running on port 8081...");
	}

	 @PostConstruct
	    public void check() {
	        System.out.println("USERNAME = " + user);
	    }
}
