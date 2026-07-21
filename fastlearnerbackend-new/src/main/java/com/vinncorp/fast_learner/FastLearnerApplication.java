package com.vinncorp.fast_learner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync(proxyTargetClass = true)
@SpringBootApplication
@EnableScheduling
public class FastLearnerApplication {

	public static void main(String[] args) {
		SpringApplication.run(FastLearnerApplication.class, args);
	}
}
