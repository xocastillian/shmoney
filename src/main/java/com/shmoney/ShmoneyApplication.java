package com.shmoney;

import com.shmoney.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class ShmoneyApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(ShmoneyApplication.class, args);
    }
}
