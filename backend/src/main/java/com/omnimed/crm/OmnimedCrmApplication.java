package com.omnimed.crm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class OmnimedCrmApplication {

    public static void main(String[] args) {
        SpringApplication.run(OmnimedCrmApplication.class, args);
    }
}
