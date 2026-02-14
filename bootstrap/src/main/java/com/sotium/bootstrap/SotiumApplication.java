package com.sotium.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(
        scanBasePackages = "com.sotium"
)
@EntityScan(basePackages = "com.sotium")
@EnableJpaRepositories(basePackages = "com.sotium")
public class SotiumApplication {

    public static void main(String[] args) {
        SpringApplication.run(SotiumApplication.class, args);
    }
}
