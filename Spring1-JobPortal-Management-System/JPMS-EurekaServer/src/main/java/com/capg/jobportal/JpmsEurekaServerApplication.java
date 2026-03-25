package com.capg.jobportal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class JpmsEurekaServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(JpmsEurekaServerApplication.class, args);
		System.out.println("JPMS-EurekaServer service running on port 8761...");
	}

}
