package com.sotium.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
        scanBasePackages = "com.sotium"
)
public class SotiumApplication {

    public static void main(String[] args) {
        SpringApplication.run(SotiumApplication.class, args);
    }
}
