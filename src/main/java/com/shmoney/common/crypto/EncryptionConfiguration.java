package com.shmoney.common.crypto;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AppEncryptionProperties.class)
public class EncryptionConfiguration {

    private static final Logger log = LoggerFactory.getLogger(EncryptionConfiguration.class);

    private final AppEncryptionProperties properties;

    public EncryptionConfiguration(AppEncryptionProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    void initialize() {
        if (properties.key() != null && !properties.key().isBlank()) {
            EncryptionContext.initialize(properties.key());
            log.info("Application encryption context initialized using application properties");
        }
    }
}
