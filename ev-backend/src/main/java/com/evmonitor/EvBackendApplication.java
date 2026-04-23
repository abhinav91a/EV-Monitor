package com.evmonitor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EvBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(EvBackendApplication.class, args);
    }
}
