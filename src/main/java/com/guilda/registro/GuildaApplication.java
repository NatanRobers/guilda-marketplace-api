package com.guilda.registro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class GuildaApplication {
    public static void main(String[] args) {
        SpringApplication.run(GuildaApplication.class, args);
    }
}