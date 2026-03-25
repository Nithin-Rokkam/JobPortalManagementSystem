package com.capg.jobportal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class JpmsApplicationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(JpmsApplicationServiceApplication.class, args);
        System.out.println("JPMS-ApplicationService is running on port 8083...");
    }
}