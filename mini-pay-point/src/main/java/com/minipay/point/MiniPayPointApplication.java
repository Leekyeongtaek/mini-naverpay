package com.minipay.point;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.minipay")
@EntityScan(basePackages = "com.minipay")
@EnableJpaRepositories(basePackages = "com.minipay")
public class MiniPayPointApplication {

    public static void main(String[] args) {
        SpringApplication.run(MiniPayPointApplication.class, args);
    }

}
