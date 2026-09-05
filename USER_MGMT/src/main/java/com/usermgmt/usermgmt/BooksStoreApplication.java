package com.usermgmt.usermgmt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BooksStoreApplication {
    public static void main(String[] args) {
        SpringApplication.run(BooksStoreApplication.class, args);
    }
}
