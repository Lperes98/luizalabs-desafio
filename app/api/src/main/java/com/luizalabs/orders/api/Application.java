package com.luizalabs.orders.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(
        basePackages = {
            "com.luizalabs.orders.api",
            "com.luizalabs.orders.usecase",
            "com.luizalabs.orders.dataprovider"
        })
@EnableJpaRepositories(basePackages = "com.luizalabs.orders.dataprovider.repository")
@EntityScan(basePackages = "com.luizalabs.orders.dataprovider.table")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
