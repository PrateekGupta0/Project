package com.tross.linkedinprofileapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class LinkedInProfileApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(LinkedInProfileApiApplication.class, args);
    }
}
