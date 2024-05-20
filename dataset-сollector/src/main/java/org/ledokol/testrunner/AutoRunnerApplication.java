package org.ledokol.testrunner;

import org.ledokol.testrunner.utils.DatasetCollector;
import org.ledokol.testrunner.utils.DatasetDevider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class AutoRunnerApplication {
	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(AutoRunnerApplication.class, args);

		DatasetCollector collector = context.getBean(DatasetCollector.class);
		collector.collectDataSet();
	}
}
