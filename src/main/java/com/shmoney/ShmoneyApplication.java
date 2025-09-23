package com.shmoney;

import com.shmoney.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
@EnableCaching
public class ShmoneyApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(ShmoneyApplication.class, args);
    }
}
