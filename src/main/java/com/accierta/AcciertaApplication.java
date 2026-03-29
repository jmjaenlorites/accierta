package com.accierta;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AcciertaApplication {

    public static void main(String[] args) {
        SpringApplication.run(AcciertaApplication.class, args);
    }
}
