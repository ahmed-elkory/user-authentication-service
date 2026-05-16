package com.ahmed.authservice;

import com.ahmed.authservice.security.config.DotenvInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class UserAuthenticationServiceApplication {
	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(UserAuthenticationServiceApplication.class);
		// TODO: Remove DotenvInitializer in production (use environment variables instead)
		if (System.getenv("SPRING_PROFILES_ACTIVE") == null ||
				System.getenv("SPRING_PROFILES_ACTIVE").equals("dev")) {
			app.addInitializers(new DotenvInitializer());
		}
		app.run(args);

	}
}
