package org.ledokol.testrunner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class AutoRunnerApplication {

	public static void main(String[] args) {
		SpringApplication.run(AutoRunnerApplication.class, args);
	}

}
