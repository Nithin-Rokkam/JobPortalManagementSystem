package com.capg.jobportal.test;

import org.mockito.MockedStatic;
import org.junit.jupiter.api.Test;
import com.capg.jobportal.JpmsApplicationServiceApplication;
import org.springframework.boot.SpringApplication;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import org.springframework.context.ConfigurableApplicationContext;

class JpmsApplicationServiceApplicationTests {

	@Test
	void contextLoads() {
		try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {
			springApplication
					.when(() -> SpringApplication.run(eq(JpmsApplicationServiceApplication.class), any(String[].class)))
					.thenReturn(mock(ConfigurableApplicationContext.class));

			assertDoesNotThrow(() -> JpmsApplicationServiceApplication
					.main(new String[] { "--spring.main.web-application-type=none" }));
		}
	}

}
