package com.sotium.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.sotium")
public class SotiumApplication {

    public static void main(String[] args) {
        SpringApplication.run(SotiumApplication.class, args);
    }

}
