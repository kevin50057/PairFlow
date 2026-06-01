package com.pairflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class PairflowApplication {

    public static void main(String[] args) {
        SpringApplication.run(PairflowApplication.class, args);
    }
}
