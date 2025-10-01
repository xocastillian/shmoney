package com.shmoney;

import com.shmoney.config.JwtProperties;
import com.shmoney.config.TelegramProperties;
import com.shmoney.config.CookieProperties;
import com.shmoney.config.CorsProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableConfigurationProperties({JwtProperties.class, TelegramProperties.class, CookieProperties.class, CorsProperties.class})
@EnableCaching
public class ShmoneyApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(ShmoneyApplication.class, args);
    }
}
