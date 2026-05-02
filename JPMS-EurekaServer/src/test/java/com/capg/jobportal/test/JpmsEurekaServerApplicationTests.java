package com.capg.jobportal.test;

<<<<<<< HEAD
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
=======
import org.mockito.MockedStatic;
import org.junit.jupiter.api.Test;
import com.capg.jobportal.JpmsEurekaServerApplication;
import org.springframework.boot.SpringApplication;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import org.springframework.context.ConfigurableApplicationContext;

>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)
class JpmsEurekaServerApplicationTests {

	@Test
	void contextLoads() {
<<<<<<< HEAD
=======
		try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {
			springApplication
					.when(() -> SpringApplication.run(eq(JpmsEurekaServerApplication.class), any(String[].class)))
					.thenReturn(mock(ConfigurableApplicationContext.class));

			assertDoesNotThrow(
					() -> JpmsEurekaServerApplication.main(new String[] { "--spring.main.web-application-type=none" }));
		}
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)
	}

}
